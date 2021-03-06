package com.readyposition.reactor;

import java.nio.channels.SelectableChannel;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkReactor implements Reactor, Runnable
{
    /** Class wide logger. */
    private final static Logger s_logger =
        LoggerFactory.getLogger(WorkReactor.class);

    /** Default singleton instance returned by getDefaultInstance(). */
    private static WorkReactor s_defaultInstance;

    /** Count of WorkReactor thread created so far. */
    private static AtomicLong s_workReactorCount = new AtomicLong();

    /** A linked list of pending Work objects. */
    protected Work m_queuedWorks = new SimpleWork();

    /** A linked list of active work objects. */
    protected Work m_activeWorks = new SimpleWork();

    /** A linked list of work from other threads pending to be submitted */
    protected Work m_pendingWorks = new SimpleWork();

    /** When true, the loop will terminate. */
    protected boolean m_done;

    /** The thread that will run the loop. */
    protected Thread m_thread;

    /** Set to true when there is work to do. */
    protected boolean m_isWorkToDo;

    /**
     * The number of passes through the reactor loop. Allowed to
     * wrap. Useful for debugging.
     */
    protected long m_cycleCount;

    /**
     * Returns a default WorkReactor instance. Every call to this method
     * returns the same WorkReactor instance.
     *
     * @return the default WorkReactor instance.
     */
    public static synchronized WorkReactor getDefaultWorkReactor() {
        if (s_defaultInstance == null) {
            s_defaultInstance = new WorkReactor("DefaultWorkReactor");
            s_defaultInstance.m_thread.start();
        }
        return s_defaultInstance;
    }

    /**
     * Returns a new WorkReactor instance.
     *
     * @return a WorkReactor instance.
     */
    public static WorkReactor getWorkReactor() {
        WorkReactor reactor = new WorkReactor();
        reactor.m_thread.start();
        return reactor;
    }

    /**
     * Returns a new named WorkReactor instance.
     *
     * @return a WorkReactor instance.
     */
    public static WorkReactor getWorkReactor(String name) {
        WorkReactor reactor = new WorkReactor(name);
        reactor.m_thread.start();
        return reactor;
    }

    /**
     * Constructs a WorkReactor instance. The constructor does not start the
     * thread. It is necessary to call start() after the WorkReactor has been
     * constructed.
     *
     * @param name the name to assign to the WorkReactor thread.
     */
    protected WorkReactor(String name) {
        m_thread = new Thread(this,name);
        s_workReactorCount.incrementAndGet();
    }

    /**
     * Constructs a WorkReactor instance. The constructor does not start the
     * thread. It is necessary to call start() after the WorkReactor has been
     * constructed. This constructor will create a Thread with the name
     * "WorkReactor_N" where N is the number of WorkReactors created so far.
     */
    protected WorkReactor() {
        this("WorkReactor_" + s_workReactorCount.incrementAndGet());
    }

    /**
     * Gets the Thread for this reactor.
     *
     * @return the Thread for this reactor.
     */
    public Thread getThread() {
        return m_thread;
    }

    /**
     * Creates a Work that will be invoked the next time through the
     * reactor loop.
     *
     * @param handler the WorkHandler that will be invoked the next
     *                time through the loop.
     * @return the Work that was created.
     */
    public Work workCreate(WorkHandler handler) {
        Work work = new SimpleWork(handler);
        workSubmit(work);
        return work;
    }

    /**
     * Place an existing Work on the work queue.
     *
     * @param work A Work created previously by workCreate or a work
     *             subclass created elsewhere
     */
    public void workSubmit(Work work) {
        if (!work.isPending()) {
            throw new IllegalStateException(
                "Attempt to submit a Work that is already busy.");
        }
        if (m_thread != Thread.currentThread()) {
            // Another thread is sending us work.  Put it on the
            // pending queue.  The loop in doWork() will consume these
            // at the appropriate time.
            synchronized (this) {
                m_pendingWorks.insertLeft(work);
                wakeup();
            }
        } else {
            // Since this is the WorkReactor's thread, we don't need
            // to synchronize here.  We can add work directly to the
            // end of the queue.
            m_queuedWorks.insertLeft(work);
        }
    }

    /** The thread's run method. Handles events until shutdown() is called. */
    public void run() {
        while (!m_done) {
            waitForWork();
            doWork();
            m_cycleCount++;
            if (m_cycleCount < 0L) {
                m_cycleCount = 0L;
            }
        }
    }

    /** Retrieves the number of cycles the reactor loop has executed. */
    public long getCycleCount() {
        return m_cycleCount;
    }

    /** XXX - javadoc */
    protected synchronized void waitForWork() {
        // While there is nothing in the work queue
        while (!isWorkPending()) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Determines if there is work pending.  We check to see if there is
     * any work on the queue at all.
     *
     * @return true if ther is work pending.
     */
    protected boolean isWorkPending() {
        // See if there is any pending work submitted by other threads
        synchronized (this) {
            if (m_pendingWorks != m_pendingWorks.getRight()) {
                // Transfer any pending work submitted by other threads
                // to the active queue
                m_queuedWorks.insertLeft(m_pendingWorks);
                m_pendingWorks.remove();
            }
        }
        m_isWorkToDo = (m_queuedWorks != m_queuedWorks.getRight());
        return m_isWorkToDo;
    }

    /** Perform any work that the reactor needs to do. */
    protected void doWork() {
        if (!m_isWorkToDo) {
            // Shortcut if there is trivially nothing to do.
            return;
        }

        // Transfer the works to another queue that can't be
        // touched by callbacks.
        if (m_queuedWorks != m_queuedWorks.getRight()) {
            m_activeWorks.insertLeft(m_queuedWorks);
            m_queuedWorks.remove();
        }

        // We can execute the work items on the temp queue without
        // worrying about synchronization issues.
        for (Work work = m_activeWorks.getRight();
             work != m_activeWorks;
             work = m_activeWorks.getRight())
        {
            // Take the work off the queue now since we may return early
            work.remove();

            if (work.isCanceled() || !work.activate()) {
                // If the work was canceled then we're almost done
                // with it.  If it isn't canceled, then the only way
                // we won't be able to activate the work is if it
                // was canceled between the two calls in the
                // condition.  Don't do the work but note that we're
                // done with this
                work.setToPending();
                continue;
            }

            // If we're here then the work is active
            try {
                if (work.workFire()) {
                    // The work wants to be resubmitted.
                    if (work.complete()) {
                        workSubmit(work);
                    } else {
                        // We were not able to successfully transition
                        // back to the pending state.  This means that
                        // the Work was cancelled after a successful
                        // run so we won't resubmit.
                        work.setToPending();
                    }
                } else {
                    // This work is done and won't be resumitted
                    work.setToPending();
                }
            } catch (Throwable t) {
                // XXX - we need some sort of callback so that we can deal
                // with a failed Work, no?
                s_logger.warn(t.toString(), t);
            }
        }
    }

    /** Ensures that the reactor notices newly registered work. */
    protected synchronized void wakeup() {
        notify();
    }

    /** Instructs the WorkReactor to stop running in the near future. */
    public void shutdown() {
        // Register a Work that will tell the reactor loop to stop running.
        workSubmit(new Work() {
                public boolean workFire() {
                    m_done = true;
                    return false;
                }
            });
    }

    public Timer timerCreateRel(long delta, TimerHandler handler) {
        throw new UnsupportedOperationException("WorkReactor doesn't do Timer operations");
    }

    /**
     * Creates a timer that will fire at a specific time.
     *
     * @param time the time at which the timer should fire (a la
     *             System.currentTimeMillis()). If the time is in the
     *             past then the timer will fire the next time through
     *             the main reactor.
     * @param handler the TimerHandler that should be invoked when the
     *                timer is ripe.
     * @return the Timer that was created.
     */
    public Timer timerCreateAbs(long time, TimerHandler handler) {
        throw new UnsupportedOperationException("WorkReactor doesn't do Timer operations");
    }

    /**
     * XXX - javadoc
     */
    public void timerSubmitRel(long delta, Timer timer) {
        throw new UnsupportedOperationException("WorkReactor doesn't do Timer operations");
    }

    /**
     * XXX - javadoc
     */
    public void timerSubmitAbs(final long time, final Timer timer) {
        throw new UnsupportedOperationException("WorkReactor doesn't do Timer operations");
    }

    /**
     * Creates a registered Valve for a SelectableChannel.  No
     * operations will be selected for this Valve yet.  The Valve's
     * enable and disable methods can be used to express interest in
     * I/O operations.
     *
     * @param channel the SelectableChannel for which a Valve is needed.
     * @param handler the SelectableChannel for which a Valve is needed.
     * @return the Valve for this channel
     */
    public Valve valveCreate(SelectableChannel channel, ValveHandler handler) {
        throw new UnsupportedOperationException("WorkReactor doesn't do I/O operations");
    }

    /**
     * Registers a Valve for a SelectableChannel.  No operations will be
     * selected for this Valve yet.  The Valve's enable and disable methods can
     * be used to express interest in I/O operations.  The Valve can not be
     * currently registered at the time this method is invoked.
     *
     * @param channel the SelectableChannel for which a Valve is needed.
     * @param valve the Valve that is being registered.
     */
    public void valveRegisgter(SelectableChannel channel, Valve valve) {
        throw new UnsupportedOperationException("WorkReactor doesn't do I/O operations");
    }

    /**
     * Sets the Valve for this channel replacing any previously registered
     * valve and disabling all registered I/O operations previously set.

     * @param channel the SelectableChannel for which a Valve is needed.
     * @param valve the Valve to be associated with this channel.
     * @return the old set of I/O operations enabled on this channel
     *         before this call.
     */
    public int valveRegister(SelectableChannel channel, Valve valve) {
        throw new UnsupportedOperationException("WorkReactor doesn't do I/O operations");
    }
}

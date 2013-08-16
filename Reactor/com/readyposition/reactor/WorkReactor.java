package com.readyposition.reactor;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkReactor implements Runnable
{
    /** Class wide logger. */
    private final static Logger s_logger = LoggerFactory.getLogger(WorkReactor.class);

    /** Default singleton instance returned by getDefaultInstance(). */
    private static WorkReactor s_defaultInstance;

    /** Count of WorkReactor thread created so far. */
    private static AtomicLong s_workReactorCount = new AtomicLong();

    /** A linked list of pending Work objects. */
    protected Work m_queuedWorks = new SimpleWork();

    /** A linked list of active work objects. */
    protected Work m_activeWorks = new SimpleWork();

    /** When true, the loop will terminate. */
    protected boolean m_done;

    /** The thread that will run the loop. */
    protected Thread m_thread;

    /** Set to true when there is work to do. */
    protected boolean m_isWorkToDo;

    /**
     * The number of passes through the reactor loop. Allowed to wrap. Useful for
     * debugging.
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
            synchronized (this) {
                m_queuedWorks.insertLeft(work);
            }
            wakeup();
        } else {
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
    protected synchronized boolean isWorkPending() {
        m_isWorkToDo = (m_queuedWorks != m_queuedWorks.getRight());
        return m_isWorkToDo;
    }

    /** Perform any work that the reactor needs to do. */
    protected void doWork() {
        if (!m_isWorkToDo) {
            // Shortcut if there is trivially nothing to do.
            return;
        }

        synchronized (this) {
            // Transfer the works to another queue that can't be
            // touched by callbacks.
            if (m_queuedWorks != m_queuedWorks.getRight()) {
                m_activeWorks.insertLeft(m_queuedWorks);
                m_queuedWorks.remove();
            }
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
                            // We were not able to successfully transition back to
                            // the pending state.  This means that the Work
                            // was cancelled after a successful run so we won't
                            // resubmit.
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
}

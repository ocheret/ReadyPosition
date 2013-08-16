package com.readyposition.reactor;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.readyposition.collections.heap.HeapLongG;

public class TimerReactor extends WorkReactor
{
    /** Class wide logger. */
    private final static Logger s_logger = LoggerFactory.getLogger(TimerReactor.class);

    /** Default singleton instance returned by getDefaultInstance(). */
    private static TimerReactor s_defaultInstance;

    /** Count of TimerReactor thread created so far. */
    private static AtomicLong s_timerReactorCount = new AtomicLong();

    /** A heap of the registered timers ordered by firing time. */
    protected HeapLongG<Timer> m_timerHeap = new HeapLongG<Timer>();

    /** A linked list of active timers. */
    protected Timer m_activeTimers = new SimpleTimer();

    /** The amount of time to sleep until the first timer needs to fire. */
    protected long m_sleepTime;

    /** The TimerReactor's notion of the "current time". */
    protected long m_now;

    /**
     * Returns a default TimerReactor instance. Every call to this
     * method returns the same TimerReactor instance.
     *
     * @return the default TimerReactor instance.
     */
    public static synchronized TimerReactor getDefaultTimerReactor() {
        if (s_defaultInstance == null) {
            s_defaultInstance = new TimerReactor("DefaultTimerReactor");
        }
        return s_defaultInstance;
    }

    /**
     * Returns a TimerReactor instance.
     *
     * @return a TimerReactor instance.
     */
    public static TimerReactor getTimerReactor() {
        TimerReactor reactor = new TimerReactor();
        reactor.getThread().start();
        return reactor;
    }

    /**
     * Returns a named TimerReactor instance.
     *
     * @return a TimerReactor instance.
     */
    public static TimerReactor getTimerReactor(String name) {
        TimerReactor reactor = new TimerReactor(name);
        reactor.getThread().start();
        return reactor;
    }

    /**
     * Constructs a TimerReactor instance. The constructor does not
     * start the thread. It is necessary to call start() after the
     * TimerReactor has been constructed.
     *
     * @param name the name to assign to the TimerReactor thread.
     */
    protected TimerReactor(String name) {
        super(name);
        s_timerReactorCount.incrementAndGet();
    }

    /**
     * Constructs a TimerReactor instance. The constructor does not
     * start the thread. It is necessary to call start() after the
     * TimerReactor has been constructed. This constructor will create
     * a Thread with the name "TimerReactor_N" where N is the number
     * of TimerReactors created so far.
     */
    protected TimerReactor() {
        this("TimerReactor_" + s_timerReactorCount.incrementAndGet());
    }

    /**
     * Creates a timer that will fire in a specified number of milliseconds.
     *
     * @param delta the number of milliseconds from the current time
     *              in which the timer should fire. If delta is
     *              negative the timer will fire the next time through
     *              the main reactor.
     * @param handler the TimerHandler that should be invoked when the
     *                timer is ripe.
     * @return the Timer that was created.
     */
    public Timer timerCreateRel(long delta, TimerHandler handler) {
        return timerCreateAbs(System.currentTimeMillis() + delta, handler);
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
        Timer timer = new SimpleTimer(handler);
        timerSubmitAbs(time, timer);
        return timer;
    }

    /**
     * XXX - javadoc
     */
    public void timerSubmitRel(long delta, Timer timer) {
        timerSubmitAbs(System.currentTimeMillis() + delta, timer);
    }

    /**
     * XXX - javadoc
     */
    public void timerSubmitAbs(final long time, final Timer timer) {
        if (!timer.isPending()) {
            throw new IllegalStateException(
                                            "Attempt to submit a Timer that is already busy.");
        }
        timer.setTime(time);
        timer.setTimerReactor(this);
        if (getThread() == Thread.currentThread()) {
            // We're in the same thread as the reactor so it is safe to
            // do whatever we want.
            timer.setEntry(m_timerHeap.insert(time, timer));
        } else {
            // We're in a different thread from the reactor so we'll
            // queue work to set up the timer.
            workSubmit(new Work() {
                    public boolean workFire() {
                        timer.setEntry(m_timerHeap.insert(time, timer));
                        return false;
                    }
                });
        }
    }

    /**
     * Wait until there is something to do.  If there are ripe timers or there
     * is any work registered, then this will return right away.  Otherwise,
     * we will wait until either a timer becomes ripe or until we are woken
     * up (e.g. when work is submitted or a new timer is registered.
     */
    protected void waitForWork() {
        while (!isWorkPending()) {
            synchronized (this) {
                try {
                    if (m_sleepTime < 0) {
                        // There are no timers registered and there is
                        // no work to do so we will sleep until
                        // something wakes us up.
                        wait();
                    } else {
                        // There is no work to do yet but there is at least
                        // one timer registered.  We will wait until the timer
                        // should be fired or until something wakes us up
                        // first.
                        wait(m_sleepTime);
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Determines if there is work pending. We check to see if there as it least
     * one ripe timer. If not we check if the superclass has work to do. We
     * don't need to synchronize this method because the only access to the
     * relevant data structures is through this thread. Timer submissions and
     * modifications from other threads get submitted as Work to be handled back
     * in this thread. This avoids lots of unnecessary synchronization.
     *
     * @return true if there is work pending.
     */
    protected boolean isWorkPending() {
        int numTimers = m_timerHeap.getSize();
        if (numTimers > 0) {
            // There are timers. Let's see if any are ripe.
            int firstEntry = m_timerHeap.peek();
            Timer timer = m_timerHeap.getEntryValue(firstEntry);

            // Make a note of the time. If there are ripe timers
            // we'll end up passing this time to them.
            m_now = System.currentTimeMillis();

            // Also note how much time to sleep in case there are no
            // ripe timers.
            m_sleepTime = timer.getTime() - m_now;

            if (m_sleepTime <= 0) {
                // There is at least 1 ripe timer
                m_sleepTime = 0;
                return true;
            }
        } else {
            // Indicates that there are no timers so we can sleep
            // indefinitely.
            m_sleepTime = -1;
        }
        return super.isWorkPending();
    }

    /**
     * Gets the amount of time to sleep until the first timer will
     * become ripe.
     */
    protected long getSleepTime() {
        return m_sleepTime;
    }

    /** TODO - javadoc */
    protected void doWork() {
        if (m_sleepTime >= 0) {
            // Compile a queue of timers that are ripe.
            while (m_timerHeap.getSize() > 0) {
                // Check to see if the first timer left on the heap is ripe.
                int entry = m_timerHeap.peek();
                long fireTime = m_timerHeap.getEntryKey(entry);
                if (fireTime > m_now) {
                    // We have consumed any ripe timers on the heap.
                    break;
                }

                // Remove the timer from the heap
                Timer timer = m_timerHeap.getEntryValue(entry);
                m_timerHeap.remove(entry);
                timer.setEntry(-1);

                // Place the timer on the active queue
                m_activeTimers.insertLeft(timer);
            }

            // Now execute the queued timers
            for (Timer timer = m_activeTimers.getRight();
                 timer != m_activeTimers;
                 timer = m_activeTimers.getRight())
                {
                    // Take the timer off of the queue now
                    timer.remove();

                    if (timer.isCanceled() || !timer.activate()) {
                        // If the timer was canceled then we're almost done
                        // with it.  If it isn't canceled, then the only way
                        // we won't be able to activate the timer is if it
                        // was canceled between the two calls in the
                        // condition.  Don't do the work but note that we're
                        // done with this
                        timer.setToPending();
                        continue;
                    }

                    // If we're here, then the timer is active
                    try {
                        long nextTime =
                            timer.timerFire(timer.getTime(), m_now);

                        if (nextTime != 0L) {
                            // The timer wants to be rescheduled
                            if (timer.complete()) {
                                timerSubmitAbs(nextTime, timer);
                            } else {
                                // We were not able to successfully transition
                                // back to the pending state.  This means that the
                                // timer was canceled after a successful run so
                                // we won't reschedule.
                                timer.setToPending();
                            }
                        } else {
                            // This timer is done and won't be rescheduled.
                            timer.setToPending();
                        }
                    } catch (Throwable t) {
                        s_logger.warn(t.toString(), t);
                    }
                }
        }

        // We're done with any ripe timers.  Now handle the work queue.
        super.doWork();
    }
}

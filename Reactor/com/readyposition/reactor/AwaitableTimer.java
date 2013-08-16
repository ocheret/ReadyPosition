package com.readyposition.reactor;

/**
 * Abstract base class for a timer that another thread needs to ensure
 * has completed before continuing.  If the same AwaitableTimer object
 * is to be submitted again, then the reset() method needs to be
 * called before submission.
 */
public abstract class AwaitableTimer extends Timer {
    /** This is true when the timer is all done. */
    protected volatile boolean m_isCompleted;

    /** Resets to an uncompleted state. */
    public void reset() {
        m_isCompleted = false;
    }

    /** Blocks until the timer has completed. */
    public synchronized void await() {
        while (!m_isCompleted) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }
    
    /**
     * Returns true if the timer has completed.
     *
     * @return true if the timer has completed.
     */
    public boolean isCompleted() {
        return m_isCompleted;
    }

    /**
     * The method that will invoke an overridden doTimer method. While
     * doTimer returns a resubmission time then the timer will be
     * resubmitted and the timer is not considered to have been
     * completed yet.
     */
    public long timerFire(long scheduledTime, long actualTime) {
        long nextTime = doTimer(scheduledTime, actualTime);
        if (nextTime == 0L) {
            m_isCompleted = true;
            synchronized (this) {
                notify();
            }
        }
        return nextTime;
    }

    /** Override this to provide work logic. */
    public abstract long doTimer(long scheduledTime, long actualTime);
}

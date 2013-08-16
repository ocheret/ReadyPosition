package com.readyposition.reactor;

/**
 * Abstract base class for work that another thread needs to ensure
 * has completed before continuing.  If the same AwaitableWork object
 * is to be submitted again, then the reset() method needs to be
 * called before submission.
 */
public abstract class AwaitableWork extends Work {
    /** This is true when the work has completed. */
    protected volatile boolean m_isCompleted;

    /** Resets to an uncompleted state. */
    public void reset() {
        m_isCompleted = false;
    }

    /** Blocks until the work has completed. */
    public synchronized void await() {
        while (!m_isCompleted) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }
    
    /**
     * Returns true if the work has completed.
     * @return true if the work has completed.
     */
    public boolean isCompleted() {
        return m_isCompleted;
    }

    /**
     * The method that will invoke an overridden doWork method. While doWork
     * returns true then the work will be resubmitted and the work is not
     * considered to have been completed yet.
     */
    public boolean workFire() {
        boolean continueWorking = doWork();
        if (continueWorking) {
            return true;
        }
        m_isCompleted = true;
        synchronized (this) {
            notify();
        }
        return false;
    }

    /** Override this to provide work logic. */
    public abstract boolean doWork();
}

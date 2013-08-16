package com.readyposition.reactor;

/**
 * Work class that, when firing, waits until it is externally signaled
 * to proceed, and also allows other threads to know when the workFire
 * method has started. This will cause the reactor that is firing it to
 * block until the signal is received. Useful for guaranteeing
 * checkpoints and rendezvous during tests and perhaps dealing with
 * some odd race conditions.
 */
public class BlockingWork extends Work {
    /** While this is false, the workFire() method will block. */
    protected volatile boolean m_shouldProceed;
    
    /** Will become true once workFire has started. */
    protected volatile boolean m_fireStarted;

    /** Allows for reuse of this BlockingWork. */
    public void reset() {
        m_shouldProceed = false;
        m_fireStarted = false;
    }

    /** Used to let the workFire method that it can resume. */
    public void proceed() {
        m_shouldProceed = true;
        synchronized (this) {
            notify();
        }
    }

    /** Waits until the workFire method has started executing. */
    public synchronized void waitForFireStart() {
        while (!m_fireStarted) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }
    
    /**
     * Returns true if the workFire method has started executing.
     *
     * @return true if the workFire method has started executing.
     */
    public boolean isFireStarted() {
        return m_fireStarted;
    }

    /** The unit of work */
    public synchronized boolean workFire() {
        m_fireStarted = true;
        notify();
        while (!m_shouldProceed) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        return false;
    }
}

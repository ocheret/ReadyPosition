package com.readyposition.reactor;

public class SimpleWork extends Work {
    /**
     * This is a stub method that should be overridden in a subclass,
     * or the Work class can be provided a WorkHandler as a target
     * (much like Thread can either be subclassed with run()
     * overridden or it can be provided a Runnable).
     *
     * @return true if the work should be resubmitted after this
     *         invocation, false otherwise.
     */
    public boolean workFire() {
        // If there is a handler we'll invoke it.  Otherwise, we'll do
        // nothing and return false so that we're not resubmitted.
        return (m_handler != null) ? m_handler.workFire() : false;
    }

    /** Constructor for a Work that doesn't need a WorkHandler. */
    protected SimpleWork() {}

    /**
     * Constructrs a work that will run the next time through the main loop.
     *
     * @param handler the WorkHandler that will be invoked the
     *        next time through the main loop.
     */
    protected SimpleWork(WorkHandler handler) {
        m_handler = handler;
    }
}

package com.readyposition.reactor;

/**
 * A Work represents a unit of work to be done during a pass through a
 * Reactor duty cycle.
 */
public abstract class Work
    extends Task<Work>
    implements WorkHandler
{
    /**
     * The WorkHandler that will be invoked the next time through the
     * main loop.
     */
    protected WorkHandler m_handler;

    /**
     * This method is invoked when the system is ready to have this
     * work done.
     *
     * @return true if the work should be resubmitted after this
     *         invocation, false otherwise.
     */
    public abstract boolean workFire();

    /** Constructor for a Work that doesn't need a WorkHandler. */
    protected Work() {}

    /**
     * Constructrs a work that will run the next time through the main loop.
     *
     * @param handler the WorkHandler that will be invoked the next
     *	      time through the main loop.
     */
    protected Work(WorkHandler handler) {
        m_handler = handler;
    }

    /**
     * Sets the WorkHandler that will be invoked via this Work.
     *
     * @param handler the WorkHandler that will be invoked via this Work.
     */
    public void setHandler(WorkHandler handler) {
        m_handler = handler;
    }

    /**
     * Gets the WorkHandler that will be invoked via this Work.
     *
     * @return the WorkHandler that will be invoked via this Work.
     */
    public WorkHandler getHandler() {
        return m_handler;
    }
}

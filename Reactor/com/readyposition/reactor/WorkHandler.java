package com.readyposition.reactor;

/**
 * The WorkHandler interface is used to invoke a unit of work.
 */
public interface WorkHandler
{
    /**
     * This method is invoked when the system is ready to have this work done.
     *
     * @return true if the work should be resubmitted after this
     *         invocation, false otherwise.
     */
    boolean workFire();
}

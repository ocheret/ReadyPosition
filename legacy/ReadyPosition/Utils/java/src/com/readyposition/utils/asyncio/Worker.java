/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

/**
 * A Worker represents a chunk of work to be done during a pass through
 * a MainLoop's run cycle.
 */
public class Worker extends Node {
    /** Constructor for head of linked list of workers. */
    protected Worker() {}

    /**
     * Constructrs a worker that will run the next time through the main loop.
     *
     * @param workLoop the WorkLoop that will manage this worker.
     * @param handler the WorkerHandler that will be invoked the next time
     *	through the main loop.
     */
    protected Worker(WorkLoop workLoop, WorkerHandler handler) {
	m_workLoop = workLoop;
	m_handler = handler;
    }

    /** Cancels this worker. */
    public void cancel() {
	m_workLoop.workerCancel(this);
    }

    /** The MainLopp that manages this worker. */
    protected WorkLoop m_workLoop;

    /** Gets the MainLopp that manages this worker. */
    public WorkLoop getWorkLoop() { return m_workLoop; }

    /** Determines if this worker has been cancelled. */
    protected boolean m_canceled;

    /** Determines if this worker has been cancelled. */
    public boolean isCanceled() { return m_canceled; }

    /**
     * Sets whether or not this worker has been cancelled.
     *
     * @param x true if the worker is canceled, false if not.
     */
    protected void setCanceled(boolean x) { m_canceled = x; }

    /**
     * The WorkerHandler that will be invoked the next time through
     * the main loop.
     */
    protected WorkerHandler m_handler;

    /**
     * Gets the WorkerHandler that will be invoked the next time through
     * the main loop.
     */
    public WorkerHandler getHandler() { return m_handler; }
}

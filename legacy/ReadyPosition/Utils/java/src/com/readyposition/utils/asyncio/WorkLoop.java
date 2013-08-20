/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A "work loop" thread that provides a simple way to do asynchronous
 * work procedures.
 */
public class WorkLoop extends Thread {
    /** Logger for this class. */
    private final static Logger s_logger =
	Logger.getLogger(WorkLoop.class.getName());

    /** Default singleton instance returned by getDefaultInstance(). */
    private static WorkLoop s_defaultInstance;

    /**
     * Returns a default WorkLoop instance.  Every call to this method
     * returns the same WorkLoop instance.
     *
     * @return the default WorkLoop instance.
     */
    public static synchronized WorkLoop getDefaultWorkLoop() {
	if (s_defaultInstance == null) {
	    s_defaultInstance = new WorkLoop("DefaultWorkLoop");
	}
	return s_defaultInstance;
    }

    /**
     * Constructs a WorkLoop instance.  The constructor does not start
     * the thread.  It is necessary to call start() after the WorkLoop
     * has been constructed.
     *
     * @param name the name to assign to the WorkLoop thread.
     */
    public WorkLoop(String name) {
	super(name);
    }

    /**
     * Constructs a WorkLoop instance.  The constructor does not start
     * the thread.  It is necessary to call start() after the WorkLoop
     * has been constructed.  This constructor will create a Thread with
     * the name "WorkLoop".
     */
    public WorkLoop() {
	this("WorkLoop");
    }

    /** A linked list of workers. */
    protected Worker m_workers = new Worker();

    /** When true, the loop will terminate. */
    protected boolean m_done;

    /** Causes this thread to begin execution if it isn't already running. */
    public void start() {
	if (!isAlive()) {
	    super.start();
	}
    }

    /** The thread's run method.  Handles events until shutdown() is called. */
    public void run() {
	Worker headWorker = new Worker();

	// Loop until shutdown is called.
	while (!m_done) {
	    // Transfer the workers to another queue that can't be
	    // touched by callbacks.
	    synchronized (m_workers) {
		while (m_workers == (Worker)m_workers.getNext()) {
		    // List is empty
		    try {
			m_workers.wait();
		    } catch (InterruptedException e) {
		    }
		}
		headWorker.insert(m_workers);
		m_workers.remove();
	    }

	    doWorkers(headWorker);
	}
    }

    /** Ensures that the loop notices newly registered work. */
    protected void wakeup() {
	synchronized (m_workers) {
	    m_workers.notify();
	}
    }

    /**
     * Iterates through a linked list of Workers and for triggers their
     * workFire() methods if not cancelled.
     *
     * @param headWorker the sentinel node of a linked list of Workers.
     */
    protected void doWorkers(Worker headWorker) {
	// We can execute the work items on the temp queue without
	// worrying about synchronization issues.
	for (Worker worker = (Worker)headWorker.getNext();
	     worker != headWorker;
	     worker = (Worker)headWorker.getNext())
	{
	    worker.remove();
	    if (worker.isCanceled()) {
		continue;
	    }
	    try {
		worker.getHandler().workerFire();
	    } catch (Throwable t) {
		s_logger.log(Level.WARNING, t.toString(), t);
	    }
	}
    }

    // TODO - add a synchronized worker type that provides a wait() method.

    /**
     * Creates a worker that will be invoked the next time through the main
     * loop.
     *
     * @param handler the WorkerHandler that will be invoked the next time
     *	through the loop.
     * @return the Worker that was created.
     */
    public Worker workerCreate(WorkerHandler handler) {
	Worker w = new Worker(this, handler);
	synchronized (m_workers) {
	    m_workers.insert(w);
	}
	if (this != Thread.currentThread()) {
	    wakeup();
	}
	return w;
    }

    /**
     * Cancels a worker.
     *
     * @param worker the worker to be cancelled.
     */
    protected void workerCancel(Worker worker) {
	synchronized (m_workers) {
	    // Just mark the worker as cancelled.  It will be reaped the
	    // next time through the main loop.
	    worker.setCanceled(true);
	}
    }

    /** Instructs the WorkLoop to stop running in the near future. */
    public void shutdown() {
	// Register a worker that will tell the main loop to stop running.
	workerCreate(new WorkerHandler() {
		public void workerFire() {
		    m_done = true;
		}
	    });
    }
}

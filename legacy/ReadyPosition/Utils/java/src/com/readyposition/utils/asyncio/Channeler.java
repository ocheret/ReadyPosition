/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.logging.Logger;

/**
 * A Channeler is used by MainLoop to multiplex I/O activities of a
 * SelectableChannel.  A SelectableChannel can be associated with any
 * number of Channelers.  Each Channeler is capable of regulating a set
 * of I/O operations: ACCEPT, CONNECT, READ, and WRITE.  For any given
 * operation, only one Channeler can be enabled at any given time.
 */
public class Channeler
{
    /** Logger for this class. */
    private final static Logger s_logger =
	Logger.getLogger(Channeler.class.getName());

    /**
     * Constructs a Channeler for a SelectableChannel.  Initially, the
     * Channeler is not enabled for any I/O operations.  The enable()
     * and disable() methods are used to regulate this.
     *
     * @param mainLoop the MainLoop that will monitor I/O readiness for this
     *	Channeler.
     * @param channel the SelectableChannel monitored by this Channeler.
     * @param handler the ChannelerHandler that will be invoked when I/O
     *	operations are ready to be performed.
     */
    protected Channeler(MainLoop mainLoop, SelectableChannel channel,
      ChannelerHandler handler)
    {
	m_mainLoop = mainLoop;
	m_channel = channel;
	m_handler = handler;
    }

    /** The MainLoop that manages this channeler. */
    protected MainLoop m_mainLoop;

    /** Gets the MainLoop that manages this channeler. */
    public MainLoop getMainLoop() { return m_mainLoop; }

    /** The SelectableChannel monitored by this Channeler. */
    protected SelectableChannel m_channel;

    /** Gets the SelectableChannel monitored by this Channeler. */
    public SelectableChannel getChannel() { return m_channel; }

    /**
     * The ChannelerHandler that will be invoked when I/O operations
     * are ready to be performed.
     */
    protected ChannelerHandler m_handler;

    /**
     * Gets the ChannelerHandler that will be invoked when I/O operations
     * are ready to be performed.
     */
    public ChannelerHandler getHandler() { return m_handler; }

    /** The current set of operations this Channeler is enabled for. */
    protected int m_ops;

    /** Gets the current set of operations this Channeler is enabled for. */
    public int getOps() { return m_ops; }

    /** Used by Anchor to turn off ops when overridden by another channeler. */
    void turnOffOps(int op) {
	m_ops &= ~op;
    }

    /** Enables OP_ACCEPT. */
    public void enableAccept() {
	enable(SelectionKey.OP_ACCEPT);
    }

    /** Enables OP_CONNECT. */
    public void enableConnect() {
	enable(SelectionKey.OP_CONNECT);
    }

    /** Enables OP_READ. */
    public void enableRead() {
	enable(SelectionKey.OP_READ);
    }

    /** Enables OP_WRITE. */
    public void enableWrite() {
	enable(SelectionKey.OP_WRITE);
    }

    /**
     * Enables a set of operations.  May be called from a thread other than
     * the main loop's thread.
     *
     * @param ops A mask of operations to enable (a la SelectionKey).
     */
    public void enable(int ops) {
	if (m_mainLoop != Thread.currentThread()) {
	    EnableWorker ew = new EnableWorker(ops);
	    m_mainLoop.workerCreate(ew);
	    ew.await();
	} else {
	    doEnable(ops);
	}
    }

    /**
     * An internally used class that allows other threads to enable
     * Channelers without permanently blocking during SelectionKey
     * processing.  This class implements the WorkerHandler interface
     * so that the work of enabling the Channeler can be submitted to
     * the work queue and processed from within the main loop's
     * thread.
     */
    private class EnableWorker implements WorkerHandler {
	/** Creates a worker that will enable ops on the channel. */
	public EnableWorker(int ops) {
	    m_ops = ops;
	}

	/** The operations to enable. */
	public int m_ops;

	/** Variable used to signal that work is complete. */
	public boolean m_done;

	/** Fires when the main loop is ready to do work. */
	public void workerFire() {
	    doEnable(m_ops);
	    synchronized (this) {
		m_done = true;
		notify();
	    }
	}

	/** Waits until the main loop is done with the enable operation. */
	public void await() {
	    synchronized (this) {
		while (!m_done) {
		    try {
			wait();
		    } catch (InterruptedException e) {
		    }
		}
	    }
	}
    }

    /**
     * Enables a set of operations.  Used internally and must be called
     * from the main loop's thread.
     *
     * @param ops A mask of operations to enable (a la SelectionKey).
     */
    private void doEnable(int ops) {
	if ((m_ops & ops) != ops) {
	    m_ops |= m_mainLoop.enable(this, ops);
	}
    }

    /** Disables OP_ACCEPT. */
    public void disableAccept() {
	disable(SelectionKey.OP_ACCEPT);
    }

    /** Disables OP_CONNECT. */
    public void disableConnect() {
	disable(SelectionKey.OP_CONNECT);
    }

    /** Disables OP_READ. */
    public void disableRead() {
	disable(SelectionKey.OP_READ);
    }

    /** Disables OP_WRITE. */
    public void disableWrite() {
	disable(SelectionKey.OP_WRITE);
    }

    /**
     * Disables a set of operations.
     *
     * @param ops A mask of operations to disable (a la SelectionKey).
     */
    public void disable(int ops) {
	if (m_mainLoop != Thread.currentThread()) {
	    DisableWorker dw = new DisableWorker(ops);
	    m_mainLoop.workerCreate(dw);
	    dw.await();
	} else {
	    doDisable(ops);
	}

    }

    /** Disables all operations currently set on this Channeler. */
    public void disable() {
	disable(m_ops);
    }

    /**
     * An internally used class that allows other threads to disable
     * Channelers without permanently blocking during SelectionKey
     * processing.  This class implements the WorkerHandler interface
     * so that the work of disabling the Channeler can be submitted to
     * the work queue and processed from within the main loop's
     * thread.
     */
    private class DisableWorker implements WorkerHandler {
	/** Creates a worker that will disable ops on the channel. */
	public DisableWorker(int ops) {
	    m_ops = ops;
	}

	/** The operations to disable. */
	public int m_ops;

	/** Variable used to signal that work is complete. */
	public boolean m_done;

	/** Fires when the main loop is ready to do work. */
	public void workerFire() {
	    doDisable(m_ops);
	    synchronized (this) {
		m_done = true;
		notify();
	    }
	}

	/** Waits until the main loop is done with the disable operation. */
	public void await() {
	    synchronized (this) {
		while (!m_done) {
		    try {
			wait();
		    } catch (InterruptedException e) {
		    }
		}
	    }
	}
    }

    /**
     * Disables a set of operations.  Used internally and must be called
     * from the main loop's thread.
     *
     * @param ops A mask of operations to disable (a la SelectionKey).
     */
    private void doDisable(int ops) {
	if ((m_ops & ops) != 0) {
	    m_ops &= ~m_mainLoop.disable(this, ops);
	}
    }

    /** Disables a set of operations. */
    public void close() {
	if (m_mainLoop != Thread.currentThread()) {
	    CloseWorker cw = new CloseWorker();
	    m_mainLoop.workerCreate(cw);
	    cw.await();
	} else {
	    doClose();
	}

    }

    /**
     * An internally used class that allows other threads to close
     * Channelers without permanently blocking during SelectionKey
     * processing.  This class implements the WorkerHandler interface
     * so that the work of closing the Channeler can be submitted to
     * the work queue and processed from within the main loop's
     * thread.
     */
    private class CloseWorker implements WorkerHandler {
	/** Variable used to signal that work is complete. */
	public boolean m_done;

	/** Fires when the main loop is ready to do work. */
	public void workerFire() {
	    doClose();
	    synchronized (this) {
		m_done = true;
		notify();
	    }
	}

	/** Waits until the main loop is done with the close operation. */
	public void await() {
	    synchronized (this) {
		while (!m_done) {
		    try {
			wait();
		    } catch (InterruptedException e) {
		    }
		}
	    }
	}
    }

    /** Disables all operations and closes the channel. */
    public void doClose() {
	m_mainLoop.close(this);
	m_ops = 0;
    }

    /** Returns true if OP_ACCEPT is enabled. */
    public boolean isAcceptEnabled() {
	return (m_ops & SelectionKey.OP_ACCEPT) != 0;
    }

    /** Returns true if OP_CONNECT is enabled. */
    public boolean isConnectEnabled() {
	return (m_ops & SelectionKey.OP_CONNECT) != 0;
    }

    /** Returns true if OP_READ is enabled. */
    public boolean isReadEnabled() {
	return (m_ops & SelectionKey.OP_READ) != 0;
    }

    /** Returns true if OP_WRITE is enabled. */
    public boolean isWriteEnabled() {
	return (m_ops & SelectionKey.OP_WRITE) != 0;
    }
}

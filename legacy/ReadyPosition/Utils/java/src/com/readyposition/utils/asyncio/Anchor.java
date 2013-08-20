/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import java.nio.channels.SelectionKey;
import java.nio.channels.SelectableChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation class representing a collection of Channelers for each
 * type of I/O operation.
 */
class Anchor
{
    /** Logger for this class. */
    private final static Logger s_logger =
	Logger.getLogger(Anchor.class.getName());

    /**
     * Constructs an Anchor that represents all of the enabled Channelers
     * for a SelectableChannel.
     *
     * @param selectableChannel the selectableChannel this Anchor serves.
     */
    Anchor(SelectableChannel selectableChannel) {
	m_selectableChannel = selectableChannel;
    }

    /**
     * Registers a channeler for specific I/O operations.
     *
     * @param chan the Channeler to register.
     * @param ops the operations to register for.
     */
    int register(Channeler chan, int ops) {
	if ((ops & SelectionKey.OP_ACCEPT) != 0) {
	    if (m_accept != null) {
		m_accept.turnOffOps(SelectionKey.OP_ACCEPT);
	    }
	    m_accept = chan;
	}
	if ((ops & SelectionKey.OP_CONNECT) != 0) {
	    if (m_connect != null) {
		m_connect.turnOffOps(SelectionKey.OP_CONNECT);
	    }
	    m_connect = chan;
	}
	if ((ops & SelectionKey.OP_READ) != 0) {
	    if (m_read != null) {
		m_read.turnOffOps(SelectionKey.OP_READ);
	    }
	    m_read = chan;
	}
	if ((ops & SelectionKey.OP_WRITE) != 0) {
	    if (m_write != null) {
		m_write.turnOffOps(SelectionKey.OP_WRITE);
	    }
	    m_write = chan;
	}
	return ops;
    }

    /**
     * Unregisters channelers for specific I/O operations.
     *
     * @param ops the operations to register for.
     */
    int unregister(Channeler chan, int ops) {
	int result = 0;
	if ((ops & SelectionKey.OP_ACCEPT) != 0) {
	    if (m_accept != chan) {
		s_logger.warning("Can't disable ACCEPT when not enabled:" +
		  m_selectableChannel);
	    } else {
		m_accept = null;
		result |= SelectionKey.OP_ACCEPT;
	    }
	}
	if ((ops & SelectionKey.OP_CONNECT) != 0) {
	    if (m_connect != chan) {
		s_logger.warning("Can't disable CONNECT when not enabled:" +
		  m_selectableChannel);
	    } else {
		m_connect = null;
		result |= SelectionKey.OP_CONNECT;
	    }
	}
	if ((ops & SelectionKey.OP_READ) != 0) {
	    if (m_read != chan) {
		s_logger.warning("Can't disable READ when not enabled:" +
		  m_selectableChannel);
	    } else {
		m_read = null;
		result |= SelectionKey.OP_READ;
	    }
	}
	if ((ops & SelectionKey.OP_WRITE) != 0) {
	    if (m_write != chan) {
		s_logger.warning("Can't disable WRITE when not enabled:" +
		  m_selectableChannel);
	    } else {
		m_write = null;
		result |= SelectionKey.OP_WRITE;
	    }
	}
	return result;
    }

    /**
     * Invokes the handlers for Channelers that have operations ready.
     *
     * @param ops the mask containing the list of ready operations.
     */
    void fire(int ops) {
	if ((ops & SelectionKey.OP_ACCEPT) != 0) {
	    fire(m_accept, "ACCEPT");
	}
	if ((ops & SelectionKey.OP_CONNECT) != 0) {
	    fire(m_connect, "CONNECT");
	}
	if ((ops & SelectionKey.OP_READ) != 0) {
	    fire(m_read, "READ");
	}
	if ((ops & SelectionKey.OP_WRITE) != 0) {
	    fire(m_write, "WRITE");
	}
    }

    /**
     * Invokes the handler of a Channeler.
     *
     * @param channeler the Channeler ready for work to be done.
     */
    void fire(Channeler channeler, String opName) {
	if (m_closed) {
	    return;
	}

	if (channeler == null) {
	    s_logger.warning(opName +
	      " operation attempted with no registered channel:" +
	      m_selectableChannel);
	    return;
	}

	try {
	    channeler.getHandler().channelerFire(channeler);
	} catch (Throwable t) {
	    s_logger.log(Level.WARNING, "During " + opName +
	      " channel processing:" + m_selectableChannel, t);
	}
    }

    /** Disables all channelers. */
    void close() {
	m_accept = m_connect = m_read = m_write = null;
	m_closed = true;
    }

    /** The SelectableChannel this Anchor is for. */
    protected SelectableChannel m_selectableChannel;

    /** Channeler registered for socket-accept operations. */
    protected Channeler m_accept;

    /** Channeler registerd for socket-connect operations. */
    protected Channeler m_connect;

    /** Channeler registerd for read operations. */
    protected Channeler m_read;

    /** Channeler registerd for write operations. */
    protected Channeler m_write;

    /** If true then the channel for this anchor has been closed. */
    protected boolean m_closed;
}

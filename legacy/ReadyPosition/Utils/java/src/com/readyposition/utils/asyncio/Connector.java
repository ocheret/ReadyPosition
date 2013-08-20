/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

/**
 * The Connector class is used to initiate socket connections.
 */
public class Connector implements ChannelerHandler, TimerHandler {
    /** Logger for this class. */
    private final static Logger s_logger =
	Logger.getLogger(Connector.class.getName());

    /**
     * Constructs a Connector that will attempt to connect and will notify
     * a ConnectorListener of its progress.
     *
     * @param mainLoop the asynchronous I/O loop used to manage activities.
     * @param listener the object to be informed of connections and other
     * 	progress.
     */
    public Connector(MainLoop mainLoop, ConnectorListener listener) {
	m_mainLoop = mainLoop;
	m_listener = listener;
    }

    /** The multiplexed asynchronous I/O main loop. */
    protected MainLoop m_mainLoop;

    /** The object informed of connection events and other connector status. */
    protected ConnectorListener m_listener;

    /** The socket channel that will be connected. */
    protected SocketChannel m_socketChannel;

    /** The Channeler that manages CONNECT events. */
    protected Channeler m_channeler;

    /** A timer used to schedule retry attempts after failures. */
    protected Timer m_timer;

    /** Attempts to establish a connected socket. */
    public void connect() {
	SocketAddress sa = m_listener.getSocketAddress();
	try {
	    m_socketChannel = SocketChannel.open();
	    m_socketChannel.configureBlocking(false);
	    m_listener.setup(m_socketChannel);
	    m_socketChannel.connect(sa);
	    if (m_socketChannel.isConnected()) {
		// Sucessful connection.
		connected();
	    } else {
		// Our connection process is in progress.
		m_listener.connecting();
		m_channeler = m_mainLoop.channelerCreate(m_socketChannel,
		  this);
		m_channeler.enable(SelectionKey.OP_CONNECT);
	    }
	} catch (Throwable t) {
	    // We failed to connect.
	    failure(t);
	}
    }

    /** This is invoked when a successful connection is established. */
    protected void connected() {
	m_listener.connected(m_mainLoop, m_socketChannel);
    }

    /**
     * This is called when we fail to establish a connection.  This
     * cleans up, notifies the listener, and schedules a retry.
     *
     * @param t the Throwable that indicates the reason for failure.
     */
    protected void failure(Throwable t) {
	if (m_channeler != null) {
	    m_channeler.close();
	}
	m_channeler = null;
	m_socketChannel = null;

	long nextDelay = m_listener.connectionFailed(t);

	// If the returned value is Long.MIN_VALUE then we won't retry.
	if (nextDelay != Long.MIN_VALUE) {
	    m_timer = m_mainLoop.timerCreate(nextDelay, this);
	}
    }

    /**
     * Invoked when there is an I/O operation ready to be performed.  This
     * handles the completion of pending connections.
     *
     * @param channeler the Channeler registered for this I/O operations.
     */
    public void channelerFire(Channeler channeler) {
	try {
	    if (!m_socketChannel.finishConnect()) {
		// Still trying to connect.
		m_listener.connecting();
	    } else {
		// We're all connected!
		m_channeler.disable(SelectionKey.OP_CONNECT);
		m_channeler = null;
		connected();
	    }
	} catch (Throwable t) {
	    // We failed to connect.
	    failure(t);
	    return;
	}
    }

    /**
     * Performs work that was previously scheduled to be invoked by a timer.
     * This is used to reattempt connection after a failure.
     *
     * @param time the time at which the Timer actually fired.
     */
    public void timerFire(long time) {
	m_timer = null;

	// Initiate a new connection attempt.
	connect();
    }
}

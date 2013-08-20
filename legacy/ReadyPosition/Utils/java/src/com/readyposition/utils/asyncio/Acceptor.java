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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * The Acceptor class is used to initiate socket listeners.
 */
public class Acceptor implements ChannelerHandler, TimerHandler
{
    /**
     * Constructs an Acceptor that will listen for connections and will notify
     * an AcceptorListener of its progress.
     *
     * @param mainLoop the asynchronous I/O loop used to manage activities.
     * @param listener the object to be informed of connections and other
     * 	progress.
     */
    public Acceptor(MainLoop mainLoop, AcceptorListener listener)
    {
	m_mainLoop = mainLoop;
	m_listener = listener;
    }

    /** The multiplexed asynchronous I/O main loop. */
    protected MainLoop m_mainLoop;

    /** The object informed of connection events and other acceptor status. */
    protected AcceptorListener m_listener;

    /** The Channeler that manages ACCEPT events. */
    protected Channeler m_channeler;

    /** The server channel that will listen for connections. */
    protected ServerSocketChannel m_serverChannel;

    /** A timer used to schedule retry attempts after failures. */
    protected Timer m_timer;

    /** Attempts to establish a listening socket. */
    public void listen()
    {
	SocketAddress sa = m_listener.getSocketAddress();
	try {
	    m_serverChannel = ServerSocketChannel.open();
	    m_serverChannel.configureBlocking(false);
	    m_listener.setup(m_serverChannel);
	    m_serverChannel.socket().bind(sa, 1000);
	    m_listener.listening();
	    m_channeler = m_mainLoop.channelerCreate(m_serverChannel, this);
	    m_channeler.enable(SelectionKey.OP_ACCEPT);
	} catch (Throwable t) {
	    failure(t);
	}
    }

    /** Closes the listening socket.  It is possible to call listen() again. */
    public void shutdown()
    {
	if (m_timer != null) {
	    m_timer.cancel();
	    m_timer = null;
	}
	if (m_channeler != null) {
	    m_channeler.disable(SelectionKey.OP_ACCEPT);
	    m_channeler.close();
	    m_channeler = null;
	    m_serverChannel = null;
	}
    }

    /**
     * This is called when we fail to establish a listening socket.  This
     * cleans up, notifies the listener, and schedules a retry.
     *
     * @param t the Throwable that indicates the reason for failure.
     */
    protected void failure(Throwable t)
    {
	if (m_channeler != null) {
	    m_channeler.close();
	}
	m_channeler = null;
	m_serverChannel = null;

	long nextDelay = m_listener.listenFailed(t);

	// If the returned value is Long.MIN_VALUE then we won't retry.
	if (nextDelay != Long.MIN_VALUE) {
	    m_timer = m_mainLoop.timerCreate(nextDelay, this);
	}
    }

    /**
     * Invoked when there is an I/O operation ready to be performed.  This
     * handles the acceptance of new connections.
     *
     * @param channeler the Channeler registered for this I/O operations.
     */
    public void channelerFire(Channeler channeler)
    {
	try {
	    SocketChannel socketChannel = m_serverChannel.accept();
	    socketChannel.configureBlocking(false);
	    m_listener.accepted(m_mainLoop, socketChannel);
	} catch (Throwable t) {
	    m_listener.acceptFailed(t);
	}
    }

    /**
     * Performs work that was previously scheduled to be invoked by a timer.
     * This is used to reattempt listening after a failure.
     *
     * @param time the time at which the Timer actually fired.
     */
    public void timerFire(long time)
    {
	if (m_timer == null) {
	    // Be extra careful not to fire the timer after a shutdown.
	    return;
	}

	m_timer = null;

	// Initiate a new connection attempt.
	listen();
    }
}

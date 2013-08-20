/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convenient abstract implementation of AcceptorListener that
 * provides useful behavior.
 */
public abstract class AcceptorAdapter implements AcceptorListener {
    /** Logger for this class. */
    private final static Logger s_logger =
	Logger.getLogger(AcceptorAdapter.class.getName());

    /**
     * Creates an AcceptorAdapter where the IP address is the wildcard address
     * and the port number a specified value.
     *
     * @param name the name to be associated with the acceptor.
     * @param port the port number
     */
    public AcceptorAdapter(String name, int port) {
	this(name, null, port);
    }

    /**
     * Creates an AcceptorAdapter from a hostname and a port number.
     *
     * @param name the name to be associated with the acceptor.
     * @param host the host name.
     * @param port the port number.
     */
    public AcceptorAdapter(String name, String host, int port) {
	m_name = name;
	m_host = host;
	m_port = port;
    }

    /** The name of this adapter. */
    protected String m_name;

    /** Gets the name of this adapter. */
    public String getName() { return m_name; }

    /** The host name or null for a wildcard address. */
    protected String m_host;

    /** Gets the host name. */
    public String getHost() { return m_host; }

    /** The listening port. */
    protected int m_port;

    /** Gets the listening port. */
    public int getPort() { return m_port; }

    /** Delay for first connection retry. Defaults to 1 millisecond. */
    protected long m_minRetryInterval = 1L;

    /** Delay for last connection retry. Defaults to 30 seconds. */
    protected long m_maxRetryInterval = 30000L;

    /** Used for exponential backoff on listening attempts. */
    protected BackOff m_backOff;

    /**
     * Sets the min and max retry intervals for exponential backoff on
     * listening attempts.
     *
     * @param min the minimum retry interval.
     * @param max the maximum retry interval.
     */
    public void setRetryParams(long min, long max) {
	// Argument checking
	if (min <= 0) {
	    throw new IllegalArgumentException("min == " + min +
	      " needs to be > 0");
	}
	if (max < min) {
	    throw new IllegalArgumentException("max < min, max == " +
	      max + ", min == " + min);
	}
	m_minRetryInterval = min;
	m_maxRetryInterval = max;
    }

    // AcceptorListener methods start here

    // Javadoc from interface
    public SocketAddress getSocketAddress() {
	return (m_host == null) ?
	    new InetSocketAddress(m_port) :
	    new InetSocketAddress(m_host, m_port);
    }

    // Javadoc from interface
    public void setup(ServerSocketChannel serverChannel) {
	ServerSocket socket = serverChannel.socket();
	try {
	    socket.setReuseAddress(true);
	} catch (Throwable t) {
	    s_logger.log(Level.WARNING, "Couldn't enable SO_REUSEADDR", t);
	}
    }

    // Javadoc from interface
    public void listening() {
	s_logger.info("Acceptor " + m_name + " listening.");
	m_backOff = null;
    }

    // Javadoc from interface
    public long listenFailed(Throwable t) {
	if (m_backOff == null) {
	    m_backOff = new BackOff(m_minRetryInterval, m_maxRetryInterval);
	}
	long nextDelay = m_backOff.getNext();
	s_logger.log(Level.INFO, "Acceptor " + m_name +
	  " listen failed - trying again in " + nextDelay + " ms.", t);
	return nextDelay;
    }

    // Javadoc from interface
    public void accepted(MainLoop mainLoop, SocketChannel socketChannel) {
	// TODO - print more detail about the connection? - CAO 12/03/2003
	s_logger.info("Acceptor " + m_name + " accepted: " + socketChannel);
	peerCreate(mainLoop, socketChannel);
    }

    // Javadoc from interface
    public void acceptFailed(Throwable t) {
	s_logger.log(Level.INFO, "Acceptor " + m_name + " accept failed.", t);
    }

    // AcceptorListener methods end here

    /**
     * Invoked to allow subclasses to do something useful with the
     * socketChannel after a connection is successfully accepted.
     *
     * @param mainLoop the mainLoop used for event dispatch.
     * @param socketChannel the successfully connected socketChannel.
     */
    protected abstract void peerCreate(MainLoop mainLoop,
      SocketChannel socketChannel);
}

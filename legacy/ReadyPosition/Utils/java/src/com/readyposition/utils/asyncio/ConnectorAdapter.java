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
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

/**
 * Convenient abstract implementation of ConnectorListener that
 * provides useful behavior.
 */
public abstract class ConnectorAdapter implements ConnectorListener {
    /** Logger for this class. */
    private final static Logger s_logger =
	Logger.getLogger(ConnectorAdapter.class.getName());

    /**
     * Constructor for a named instance that will handle the process of
     * connecting to a specific host and port.
     *
     * @param name the name of this instance.
     * @param host the host to connect to.
     * @param port the port to connect to.
     */

    public ConnectorAdapter(String name, String host, int port) {
	m_name = name;
	m_host = host;
	m_port = port;
    }

    /** Random number generator for retrying. */
    protected static Random s_rnd = new Random();

    /** The name of this instance. */
    protected String m_name;

    /** Gets the name of this instance. */
    public String getName() { return m_name; }

    /** The host to connect to. */
    protected String m_host;

    /** Gets the host to connect to. */
    public String getHost() { return m_host; }

    /** The port to connect to. */
    protected int m_port;

    /** Gets the port to connect to. */
    public int getPort() { return m_port; }

    /** The minimum delay for connection retry. Defaults to 1 millisecond. */
    protected long m_minRetryInterval = 1L;

    /** The maximum delay for connection retry. Defaults to 30 seconds. */
    protected long m_maxRetryInterval = 30000L;

    /** Max random amount to add into retry interval. */
    protected int m_randomness = 1000;

    /** Used for exponential backoff on connection attempts. */
    protected BackOff m_backOff;

    /**
     * Sets the min and max retry intervals for exponential backoff on
     * listening attempts.
     *
     * @param min the minimum retry interval.
     * @param max the maximum retry interval.
     * @param randomness the max number of random milliseconds to add to the
     *   retry interval.  The number of random milliseconds will always be
     *   between 0 and randomness (non-inclusive).  A value of 0 will produce
     *   no randomness (more efficiently than a value of 1).
     */
    public void setRetryParams(long min, long max, int randomness) {
	// Argument checking
	if (min <= 0) {
	    throw new IllegalArgumentException("min == " + min +
	      " needs to be > 0");
	}
	if (max < min) {
	    throw new IllegalArgumentException("max < min, max == " +
	      max + ", min == " + min);
	}
	if (randomness < 0) {
	    throw new IllegalArgumentException("randomness < 0, randomness == "
	    + randomness);
	}
	m_minRetryInterval = min;
	m_maxRetryInterval = max;
	m_randomness = randomness;
    }

    // ConnectorListener methods start here

    // Javadoc from interface
    public SocketAddress getSocketAddress() {
	return new InetSocketAddress(m_host, m_port);
    }

    /**
     * This method is invoked right after the socketChannel is created
     * and set to non-blocking and before connection is initiated to
     * allow customization of socket options.  This implementation turns
     * on SO_KEEPALIVE.
     *
     * @param socketChannel the SocketChannel to set up.
     */
    public void setup(SocketChannel socketChannel) {
	Socket socket = socketChannel.socket();
	try {
	    socket.setKeepAlive(true);
	} catch (Throwable t) {
	    s_logger.log(Level.WARNING, "Couldn't enable SO_KEEPALIVE", t);
	}
    }

    // Javadoc from interface.
    public void connecting() {
	s_logger.info("Connector " + m_name + " connecting.");
    }

    // Javadoc from interface.
    public long connectionFailed(Throwable t) {
	if (m_backOff == null) {
	    m_backOff = new BackOff(m_minRetryInterval, m_maxRetryInterval);
	}
	long nextDelay = m_backOff.getNext();
	if (m_randomness != 0) {
	    nextDelay += s_rnd.nextInt(m_randomness);
	}
	s_logger.log(Level.INFO, "Connector " + m_name +
	  " connection failed - trying again in " + nextDelay + " ms.", t);
	return nextDelay;
    }

    // Javadoc from interface.
    public void connected(MainLoop mainLoop, SocketChannel socketChannel) {
	m_backOff = null;
	s_logger.info("Connector " + m_name + " connected: " + socketChannel);
	peerCreate(mainLoop, socketChannel);
    }

    // ConnectorListener methods end here

    /**
     * Invoked to allow subclasses to do something useful with the
     * socketChannel after a successful connection.
     *
     * @param mainLoop the mainLoop used for event dispatch.
     * @param socketChannel the successfully connected socketChannel.
     */
    protected abstract void peerCreate(MainLoop mainLoop,
      SocketChannel socketChannel);
}

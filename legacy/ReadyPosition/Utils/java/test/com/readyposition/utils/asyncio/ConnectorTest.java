/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import junit.framework.TestCase;

import java.nio.channels.SocketChannel;

public class ConnectorTest extends TestCase
{
    /** Asynchronous multiplexed I/O loop. */
    private MainLoop m_mainLoop;

    /** Set up tests. */
    public void setUp() {
	m_mainLoop = new MainLoop();
	m_mainLoop.start();
	// We should be able to call start() mutliple times
	m_mainLoop.start();
	m_connectDone = m_acceptDone = m_timedOut = false;
    }

    /** Tear down tests. */
    public void tearDown() {
	m_mainLoop.shutdown();
    }

    /** Used for synchronization. */
    protected Object m_lock = new Object();

    /** The Connector succeeded. */
    protected boolean m_connectDone;

    /** The Acceptor succeeded. */
    protected boolean m_acceptDone;

    /** The timer went off. */
    protected boolean m_timedOut;

    /** Used to time out operations. */
    protected Timer m_timer;

    /** Simple ConnectorListener that notes if connection is successful. */
    protected class TestConnectorAdapter extends ConnectorAdapter {
	TestConnectorAdapter(String name, String host, int port) {
	    super(name, host, port);
	}

	public void peerCreate(MainLoop mainLoop,
	  SocketChannel socketChannel)
	{
	    m_timer.cancel();
	    try {
		socketChannel.close();
		synchronized (m_lock) {
		    m_connectDone = true;
		    m_lock.notify();
		}
	    } catch (Throwable t) {
	    }
	}
    }

    /** Simple AcceptorListener that notes if connection is successful. */
    protected class TestAcceptorAdapter extends AcceptorAdapter {
	TestAcceptorAdapter(String name, String host, int port) {
	    super(name, host, port);
	}

	public void peerCreate(MainLoop mainLoop,
	  SocketChannel socketChannel)
	{
	    m_timer.cancel();
	    try {
		socketChannel.close();
		synchronized (m_lock) {
		    m_acceptDone = true;
		    m_lock.notify();
		}
	    } catch (Throwable t) {
	    }
	}
    }

    /**
     * Spawn a Connector and Listener and time out if they don't
     * connect soon.
     */
    public void testConnector() {
	ConnectorListener cl = new TestConnectorAdapter("testConnector",
	  "localhost", 9876);
	Connector conn = new Connector(m_mainLoop, cl);
	conn.connect();

	AcceptorListener al = new TestAcceptorAdapter("testAcceptor",
	  "localhost", 9876);
	Acceptor acc = new Acceptor(m_mainLoop, al);
	acc.listen();

	m_timer = m_mainLoop.timerCreate(1000L, new TimerHandler() {
		public void timerFire(long time) {
		    synchronized (m_lock) {
			m_timedOut = true;
			m_lock.notify();
		    }
		}
	    });

	// XXX
	synchronized (m_lock) {
	    while ((!m_acceptDone || !m_connectDone) && !m_timedOut) {
		try {
		    m_lock.wait();
		} catch (InterruptedException t) {
		}
	    }
	}

	assertTrue("Connection not established.", !m_timedOut);
    }

    /**
     * Spawn a connector and listener that are guaranteed not to connect
     * and ensure that the timout occurs.
     */
    public void testBadConnector() {
	ConnectorListener cl = new TestConnectorAdapter("testConnector",
	  "localhost", 9877);
	Connector conn = new Connector(m_mainLoop, cl);
	conn.connect();

	AcceptorListener al = new TestAcceptorAdapter("testAcceptor",
	  "localhost", 9876);
	Acceptor acc = new Acceptor(m_mainLoop, al);
	acc.listen();

	m_timer = m_mainLoop.timerCreate(1000L, new TimerHandler() {
		public void timerFire(long time) {
		    synchronized (m_lock) {
			m_timedOut = true;
			m_lock.notify();
		    }
		}
	    });

	synchronized (m_lock) {
	    while ((!m_acceptDone || !m_connectDone) && !m_timedOut) {
		try {
		    m_lock.wait();
		} catch (InterruptedException t) {
		}
	    }
	}

	assertTrue("Connection not established.", m_timedOut);
    }
}

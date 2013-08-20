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

public class TimerTest extends TestCase {
    /** Asynchronous multiplexed I/O loop. */
    private MainLoop m_mainLoop;

    /** Set up tests. */
    public void setUp() {
	m_mainLoop = new MainLoop();
	m_mainLoop.start();
    }

    /** Tear down tests. */
    public void tearDown() {
	m_mainLoop.shutdown();
    }

    /** Used for synchronization. */
    protected Object m_lock = new Object();

    /** Counter. */
    protected int m_count;

    /** Used to time out operations. */
    protected Timer m_timer;

    /** XXX - javadoc */
    private static final int ITERATIONS = 10;

    /** XXX - javadoc */
    private static final long DELAY = 1L;

    /** XXX - javadoc */
    class TestTimerHandler implements TimerHandler {
	public void timerFire(long time) {
	    System.out.println("Inside timer: " + time);
	    synchronized (m_lock) {
		m_count++;
		if (m_count == ITERATIONS) {
		    m_lock.notify();
		    return;
		}
	    }
	    m_timer = m_mainLoop.timerCreate(DELAY, this);
	}
    }

    /** XXX - javadoc */
    public void testTimer() {
	m_timer = m_mainLoop.timerCreate(DELAY, new TestTimerHandler());
	synchronized (m_lock) {
	    while (m_count < ITERATIONS) {
		try {
		    m_lock.wait();
		} catch (InterruptedException ie) {
		}
	    }
	}
	assertEquals("Counter wrong", ITERATIONS, m_count);
    }
}

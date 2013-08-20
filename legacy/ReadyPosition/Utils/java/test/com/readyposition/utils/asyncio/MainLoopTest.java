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

public class MainLoopTest extends TestCase
{
    /** Asynchronous multiplexed I/O loop. */
    private MainLoop m_mainLoop;

    /** Set up tests. */
    public void setUp() {
	m_mainLoop = MainLoop.getDefaultInstance();
	m_mainLoop.start();
	// We should be able to call start() mutliple times
	m_mainLoop.start();
    }

    /** Tear down tests. */
    public void tearDown() {
	m_mainLoop.shutdown();
    }

    /** Test a couple of things about mainloops. */
    public void testMainLoop() {
	MainLoop ml = MainLoop.getDefaultInstance();
	assertSame("Default instance doesn't match", ml, m_mainLoop);

	// Make sure we can invoke start() multiple times now.
	ml.start();
	ml.start();
	ml.start();
    }
}

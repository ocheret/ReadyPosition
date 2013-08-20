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

public class WorkerTest extends TestCase {
    /** Asynchronous multiplexed I/O loop. */
    private MainLoop m_mainLoop;

    /** Set up tests. */
    public void setUp() {
	m_mainLoop = new MainLoop();
	m_mainLoop.start();
    }

    /** Tear down tests. */
    public void tearDown() {
    }

    /** XXX - javadoc */
    private static final int ITERATIONS = 1000000;

    /** XXX - javadoc */
    protected int m_count = 0;

    /** XXX - javadoc */
    public void testWorker() {
	long time = -System.currentTimeMillis();
	m_mainLoop.workerCreate(new WorkerHandler() {
		public void workerFire() {
		    for (int i = 0; i < ITERATIONS; i++) {
			m_mainLoop.workerCreate(new WorkerHandler() {
				public void workerFire() {
				    m_count++;
				}
			    });
		    }
		    m_mainLoop.shutdown();
		}
	    });

	time += System.currentTimeMillis();
	System.out.println("Cycles = " + ITERATIONS);
	System.out.println("Total setup time = " + time);
	System.out.println("Time time/setup = " +
	  (double)time/(double)ITERATIONS);
	
	time = -System.currentTimeMillis();
	try {
	    m_mainLoop.join();
	} catch (InterruptedException e) {
	}
	time += System.currentTimeMillis();

	System.out.println("Total time = " + time);
	System.out.println("Time/cycle = " + (double)time/(double)ITERATIONS);
	assertEquals("Counter wrong", ITERATIONS, m_count);
    }
}

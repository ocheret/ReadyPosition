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

public class WorkLoopTest extends TestCase
{
    /** Asynchronous multiplexed I/O loop. */
    private WorkLoop m_workLoop;

    /** Set up tests. */
    public void setUp() {
	m_workLoop = new WorkLoop();
	m_workLoop.start();
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
	m_workLoop.workerCreate(new WorkerHandler() {
		public void workerFire() {
		    for (int i = 0; i < ITERATIONS; i++) {
			m_workLoop.workerCreate(new WorkerHandler() {
				public void workerFire() {
				    m_count++;
				}
			    });
		    }
		    m_workLoop.shutdown();
		}
	    });

	time += System.currentTimeMillis();
	System.out.println("Cycles = " + ITERATIONS);
	System.out.println("Total setup time = " + time);
	System.out.println("Time time/setup = " +
	  (double)time/(double)ITERATIONS);
	
	time = -System.currentTimeMillis();
	try {
	    m_workLoop.join();
	} catch (InterruptedException e) {
	}
	time += System.currentTimeMillis();

	System.out.println("Total time = " + time);
	System.out.println("Time/cycle = " + (double)time/(double)ITERATIONS);
	assertEquals("Counter wrong", ITERATIONS, m_count);
    }

    private static class SumWorker implements WorkerHandler {
	public SumWorker(int a, int b) {
	    m_a = a;
	    m_b = b;
	}

	private int m_a;
	private int m_b;
	private int m_result;
	public int getResult() { return m_result; }

	private boolean m_done;

	public void workerFire() {
	    // Do the math
	    m_result = m_a + m_b;
	    try {
		Thread.sleep(500L);
	    } catch (InterruptedException e) {
	    }
	    synchronized (this) {
		m_done = true;
		notify();
	    }
	}

	public synchronized void await() {
	    while (!m_done) {
		try {
		    wait();
		} catch (InterruptedException e) {
		}
	    }
	}
    }

    private int computeSum(int a, int b) {
	SumWorker sw = new SumWorker(a, b);
	m_workLoop.workerCreate(sw);
	sw.await();
	return sw.getResult();
    }

    public void testSyncWorker() {
	int result = computeSum(1200, 34);
	assertEquals("Work didn't complete properly", 1234, result);
    }
}

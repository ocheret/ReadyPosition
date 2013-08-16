package com.readyposition.reactor.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.readyposition.reactor.IOReactor;

public class IOReactorTest extends TimerReactorTest {
    /** The number of TimerReactors created during testing. */
    protected int m_reactorCount;

    /** Holds a TimerReactor for testing. */
    protected IOReactor m_ioReactor;

    @BeforeClass
    public static void setUpBeforeClass() {}

    @AfterClass
    public static void tearDownAfterClass() {
    }

    @Before
    public void setUp() {
        m_ioReactor = IOReactor.getIOReactor("TestIOReactor_" + ++m_reactorCount);
        m_workReactor = m_timerReactor = m_ioReactor;
    }

    @After
    public void tearDown() {
        m_ioReactor.shutdown();
    }

    @Test
    public void testNothing() {
        // Stub
    }
}

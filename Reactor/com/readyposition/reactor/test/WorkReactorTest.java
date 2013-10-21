package com.readyposition.reactor.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.readyposition.reactor.AwaitableWork;
import com.readyposition.reactor.BlockingWork;
import com.readyposition.reactor.Work;
import com.readyposition.reactor.WorkReactor;

public class WorkReactorTest
{
    /** The number of WorkReactors created during testing. */
    protected int m_loopCount;

    /** Holds a WorkReactor for testing. */
    protected WorkReactor m_workReactor;

    /** Number of times CountDownWorker should resubmit itself. */
    protected final static int ITERATIONS = 10;

    @BeforeClass
    public static void setUpBeforeClass() {}

    @AfterClass
    public static void tearDownAfterClass() {}

    @Before
    public void setUp() {
        m_workReactor = WorkReactor.getWorkReactor("TestWorkReactor_" +
                                                   ++m_loopCount);
    }

    @After
    public void tearDown() {
        m_workReactor.shutdown();
    }

    /**
     * Work class that resubmits itself a specified number of times.
     * It also allows other threads to wait until the work completes.
     */
    protected static class CountdownWork extends AwaitableWork {
        protected int m_count;

        public CountdownWork() {
            setCount(1);
        }

        public CountdownWork(int count) {
            setCount(count);
        }

        public void setCount(int count) {
            m_count = (count < 1) ? 1 : count;
        }
        public boolean doWork() {
            return (--m_count != 0);
        }
    }

    /**
     * Work class that waits until it is externally signaled to proceed.
     * Useful for guaranteeing checkpoints during tests.
     */
    protected static class CounterWork extends Work {
        int m_count;

        public int getCount() {
            return m_count;
        }

        public boolean workFire() {
            m_count++;
            return false;
        }
    }

    @Test
    public void testWorkSimple() {
        CountdownWork work = new CountdownWork(ITERATIONS);

        // Treat work as a WorkHandler.  This should return a different
        // Work object.
        Work tmp = m_workReactor.workCreate(work);
        assertTrue("We didn't get a new Work object?", tmp != work);
        work.await();

        // Treat work as a Work (new new Work object is created).
        work.setCount(ITERATIONS);
        m_workReactor.workSubmit(work);
        work.await();
    }

    @Test
    public void testWorkMultiple() {
        // Make sure nothing happens until we're ready
        BlockingWork bw = new BlockingWork();
        m_workReactor.workSubmit(bw);

        // Submit a bunch of work
        CounterWork counterWork = new CounterWork();
        for (int i = 0; i < ITERATIONS; i++) {
            // We submit counterWork as a WorkHandler.
            // A new Work will be created internally
            // for each call.
            m_workReactor.workCreate(counterWork);
        }
        CountdownWork work = new CountdownWork();
        m_workReactor.workSubmit(work);

        // Allow work to proceed
        bw.proceed();

        // Make sure all work we care about is done
        work.await();

        // Ensure that all of the counterWork submissions
        // were done.
        assertEquals("The CounterWork value is wrong",
                     ITERATIONS, counterWork.getCount());
    }

    @Test
    public void testWorkCancel() {
        // Make sure nothing happens until we're ready
        BlockingWork bw = new BlockingWork();
        m_workReactor.workSubmit(bw);

        // Wait until we're sure that the blocking work
        // is has fired. It will block the queue
        // block until we tell it to proceed.
        bw.waitForFireStart();

        // Canceling work that is active isn't allowed
        assertFalse("Canceled already active work!", bw.cancel());

        // Submit a bunch of work
        CounterWork counterWork = new CounterWork();
        Work toCancel = null;
        for (int i = 0; i < ITERATIONS; i++) {
            // We submit counterWork as a WorkHandler.
            // A new Work will be created internally
            // for each call.
            toCancel = m_workReactor.workCreate(counterWork);
        }

        // Cancel that last piece of work
        assertTrue("Couldn't cancel inactive work",
                   toCancel.cancel());

        // Submit some final work that we can wait for
        CountdownWork work = new CountdownWork();
        m_workReactor.workSubmit(work);

        // Allow work to proceed
        bw.proceed();

        // Make sure all work we care about is done
        work.await();

        // Ensure that all of the counterWork submissions
        // were done except for the canceled one.
        assertEquals("The CounterWork value is wrong",
                     (ITERATIONS - 1), counterWork.getCount());
    }
}

package com.readyposition.reactor.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.readyposition.reactor.Timer;
import com.readyposition.reactor.AwaitableTimer;
import com.readyposition.reactor.BlockingTimer;
import com.readyposition.reactor.TimerReactor;

public class TimerReactorTest extends WorkReactorTest {
    /** The number of TimerReactors created during testing. */
    protected int m_reactorCount;

    /** Holds a TimerReactor for testing. */
    protected TimerReactor m_timerReactor;

    /** Number of times CountDownWorker should resubmit itself. */
    protected final static long INTERVAL = 10L;

    @BeforeClass
    public static void setUpBeforeClass() {}

    @AfterClass
    public static void tearDownAfterClass() {}

    @Before
    public void setUp() {
        m_timerReactor = TimerReactor.getTimerReactor("TestTimerReactor_" + ++m_reactorCount);
        m_workReactor = m_timerReactor;
    }

    @After
    public void tearDown() {
        m_timerReactor.shutdown();
    }

    /**
     * Timer class that resubmits itself a specified number of times. It also
     * allows other threads to wait until completion.
     */
    protected static class CountdownTimer extends AwaitableTimer {
        protected int m_count;
        protected long m_interval;

        public CountdownTimer() {
            this(0L, 1);
        }

        public CountdownTimer(long interval, int count) {
            m_interval = interval;
            m_count = count;
        }

        public void setCount(int count) {
            m_count = count;
        }

        public long doTimer(long scheduledTime, long actualTime) {
            return (--m_count == 0) ? 0L : actualTime + m_interval;
        }
    }

    /**
     * Work class that waits until it is externally signaled to proceed. Useful
     * for guaranteeing checkpoints during tests.
     */
    protected static class CounterTimer extends Timer {
        int m_count;

        public int getCount() {
            return m_count;
        }

        public long timerFire(long schedule, long actual) {
            m_count++;
            return 0L;
        }
    }

    @Test
    public void testTimerSimple() {
        CountdownTimer timer = new CountdownTimer(INTERVAL, ITERATIONS);

        // Treat a timer as a TimerHandler. This should return a different
        // Timer object.
        Timer tmp = m_timerReactor.timerCreateRel(INTERVAL, timer);
        assertTrue("We didn't get a new Work object?", tmp != timer);
        timer.await();

        // Treat work as a Work (new new Work object is created).
        timer.setCount(ITERATIONS);
        m_timerReactor.timerSubmitRel(INTERVAL, timer);
        timer.await();
    }

    @Test
    public void testTimerMultiple() {
        // Make sure nothing happens until we're ready
        BlockingTimer bt = new BlockingTimer();
        m_timerReactor.timerSubmitRel(0L, bt);

        // Submit a bunch of work
        CounterWork counterWork = new CounterWork();
        for (int i = 0; i < ITERATIONS; i++) {
            // We submit counterWork as a WorkHandler.
            // A new Work will be created internally
            // for each call.
            m_timerReactor.workCreate(counterWork);
        }
        CountdownTimer timer = new CountdownTimer();
        m_timerReactor.timerSubmitRel(INTERVAL, timer);

        // Allow work to proceed
        bt.proceed();

        // Make sure all work we care about is done
        timer.await();

        // Ensure that all of the counterWork submissions
        // were done.
        assertEquals("The CounterWork value is wrong", ITERATIONS, counterWork
                     .getCount());
    }

    @Test
    public void testCancel() {
        // Make sure nothing happens until we're ready
        BlockingTimer bt = new BlockingTimer();
        m_timerReactor.timerSubmitRel(0L, bt);

        // Wait until we're sure that the blocking timer
        // is has fired. It will block the queue
        // until we tell it to proceed.
        bt.waitForFireStart();

        // Canceling work that is active isn't allowed
        assertFalse("Canceled already active timer!", bt.cancel());

        // Submit a bunch of timers.  Guarantee order of execution by
        // incrementing deadlines by a millisecond each time.
        long deadline = System.currentTimeMillis() + INTERVAL;
        CounterTimer counterTimer = new CounterTimer();
        Timer toCancel = null;
        for (int i = 0; i < ITERATIONS; i++) {
            // We submit counterTimer as a TimerHandler.
            // A new Timer will be created internally
            // for each call.
            toCancel = m_timerReactor.timerCreateAbs(deadline++, counterTimer);
        }

        // Cancel that last timer
        assertTrue("Couldn't cancel inactive timer", toCancel.cancel());

        // Submit a final timer that we can wait for
        CountdownTimer timer = new CountdownTimer();
        m_timerReactor.timerSubmitAbs(deadline++, timer);

        // Allow work to proceed
        bt.proceed();

        // Make sure all work we care about is done
        timer.await();

        // Ensure that all of the counterWork submissions
        // were done except for the canceled one.
        assertEquals("The CounterTimer value is wrong", (ITERATIONS - 1),
                     counterTimer.getCount());
    }
}

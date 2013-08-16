package com.readyposition.reactor.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.readyposition.reactor.Work;
import com.readyposition.reactor.WorkHandler;
import com.readyposition.reactor.WorkReactor;

/** Simple example of WorkReactor. */
public class WorkReactorSimpleExample {
    /** Class wide logger. */
    private final static Logger s_logger =
        LoggerFactory.getLogger(WorkReactorSimpleExample.class);

    /** The WorkReactor we're playing with. */
    protected WorkReactor m_workReactor;

    /** The number of counter iterations. */
    protected final static int COUNT = 3;

    /** The counter updated by the workers. */
    protected int m_counter;

    /**
     * A Work object that will keep resubmitting itself until a
     * counter reaches a threshold, at which point it resets the
     * counter and submits a WorkHandler.
     */
    protected Work m_countWorker = new Work() {
            public boolean workFire() {
                s_logger.info("Work: counter = " + m_counter);
                if (++m_counter == COUNT) {
                    s_logger.info("Work: Handing off to WorkHandler");
                    m_counter = 0;
                    submitWorkHandler();
                    return false;
                }
                return true;
            }
        };

    /**
     * A WorkHandler that will keep resubmitting itself until a
     * counter reaches a threshold, at which point it shuts down the
     * reactor.
     */
    protected WorkHandler m_countWorkerHandler = new WorkHandler() {
            public boolean workFire() {
                s_logger.info("WorkHandler: counter = " + m_counter);
                if (++m_counter == COUNT) {
                    s_logger.info("WorkHandler: Shutting down the reactor");
                    m_workReactor.shutdown();
                    return false;
                }
                return true;
            }
        };

    /** Constructor for the example. */
    protected WorkReactorSimpleExample() {
        m_workReactor = WorkReactor.getDefaultWorkReactor();
    }

    /** Gets the work reactor. */
    protected WorkReactor getWorkReactor() {
        return m_workReactor;
    }

    /** Demonstrate submission of Work. */
    protected void submitWork() {
        m_workReactor.workSubmit(m_countWorker);
    }

    /** Demonstrate creation of Work from a handler. */
    protected void submitWorkHandler() {
        m_workReactor.workCreate(m_countWorkerHandler);
    }

    /** The main event. */
    public static void main(String[] args) {
        WorkReactorSimpleExample reactor = new WorkReactorSimpleExample();
        reactor.submitWork();
        for (;;) {
            try {
                reactor.getWorkReactor().getThread().join();
                break;
            } catch (InterruptedException e) {
            }
        }
        s_logger.info("WorkReactor has terminated: cycle count = " +
                      reactor.getWorkReactor().getCycleCount());
        System.exit(0);
    }
}

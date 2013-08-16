package com.readyposition.reactor;

/** Simple Timer that holds a TimerHandler. */
public class SimpleTimer extends Timer {
    /**
     * Constructor for an empty Timer.
     */
    public SimpleTimer() {}

    /**
     * Constructor for a Timer with a handler.
     *
     * @param handler the TimerHandler that will be invoked when the
     *                timer is ripe.
     */
    public SimpleTimer(TimerHandler handler) {
        super(handler);
    }

    /**
     * This is a stub method that should be overridden in a subclass,
     * or the Timer class can be provided a TimerHandler as a target
     * (much like Thread can be either be subclassed with run()
     * overridden or it can be provided a Runnable).
     *
     * @param scheduledTime at which the timer was scheduled.
     * @param actualTime the time at which the timer actually fired.
     * @return the absolute time at which the timer should fire again.
     *         Return 0 if the timer should not fire again.
     */
    public long timerFire(long scheduledTime, long actualTime) {
        // If there is a handler we'll invoke it.  Otherwise, we'll do
        // nothing and return 0 so that we're not resubmitted.
        return (m_handler != null) ?
            m_handler.timerFire(scheduledTime, actualTime) : 0L;
    }
}

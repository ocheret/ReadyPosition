package com.readyposition.reactor;

/**
 * A Timer represents a unit of work to be done during a pass through
 * a TimerReactor or IOReactor duty cycle.
 */
public abstract class Timer
    extends Task<Timer>
    implements TimerHandler
{
    /** The reactor that manages this timer. */
    protected TimerReactor m_timerReactor;

    /** The absolute time in milliseconds when this timer should fire. */
    protected long m_time;

    /**
     * The entry number of this Timer in the heap of timers owned by
     * the TimerReactor.
     */
    protected int m_entry;

    /**
     * The TimerHandler that will be invoked the next time through
     * the main reactor.
     */
    protected TimerHandler m_handler;

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
    public abstract long timerFire(long scheduledTime, long actualTime);

    /**
     * Constructor for an empty Timer.
     */
    public Timer() {}

    /**
     * Constructor for a Timer with a handler.
     *
     * @param handler the TimerHandler that will be invoked when the timer is
     *	ripe.
     */
    public Timer(TimerHandler handler) {
        m_handler = handler;
    }

    /**
     * Sets the TimerHandler that will be invoked via this Timer.
     *
     * @param handler the TimerHandler that will be invoked via this Timer.
     */
    public void setHandler(TimerHandler handler) {
        m_handler = handler;
    }

    /**
     * Gets the TimerHandler that will be invoked via this Timer.
     *
     * @return the TimerHandler that will be invoked via this Timer.
     */
    public TimerHandler getHandler() {
        return m_handler;
    }

    /**
     * Gets the reactor that is controlling this timer.
     *
     * @return the reactor that is controlling this timer.
     */
    public TimerReactor getTimerReactor() {
        return m_timerReactor;
    }

    /**
     * Sets the reactor that is controlling this timer.  This is used internally
     * in case somebody resubmits a timer to a different reactor.
     */
    protected void setTimerReactor(TimerReactor timerReactor) {
        m_timerReactor = timerReactor;
    }

    /**
     * Gets the time at which this Timer is scheduled to fire.
     *
     * @return the time at which this Timer is scheduled to fire.
     */
    public long getTime() {
        return m_time;
    }

    /**
     * Sets time at which the Timer is scheduled to fire.  This is used
     * internally when the timer is submitted.
     */
    protected void setTime(long time) {
        m_time = time;
    }

    /** XXX - javadoc */
    public void setEntry(int entry) {
        m_entry = entry;
    }

    /** XXX - javadoc */
    public int getEntry() {
        return m_entry;
    }
}

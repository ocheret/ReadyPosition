/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

/**
 * A Timer schedules work to be done at a specified time.  Timers are created
 * and managed by a MainLoop object.
 */
public class Timer extends Node
{
    /** Constructor for head of linked list of timers. */
    Timer() {}

    /**
     * Constructs a timer that will fire at a specific time.
     *
     * @param mainLoop the MainLoop that will manage this timer.
     * @param time the absolute time at which the timer should fire.
     * @param handler the TimerHandler that will be invoked when the timer is
     * 	fired.
     */
    Timer(MainLoop mainLoop, long time, TimerHandler handler) {
	if (handler == null)
	    throw new IllegalArgumentException("Null TimerHandler");
	m_mainLoop = mainLoop;
	m_time = time;
	m_handler = handler;
    }

    /** Cancels this timer. */
    public void cancel() {
	m_mainLoop.timerCancel(this);
    }

    /** The MainLopp that manages this timer. */
    protected MainLoop m_mainLoop;

    /**
     * Gets the mainLoop associated with this timer.
     *
     * @return the MainLoop object.
     */
    public MainLoop getMainLoop() { return m_mainLoop; }

    /** The absolute time at which this timer should fire. */
    protected long m_time;

    /** Gets the time at which this timer should fire.
     *
     *  @return the time at which this timer should fire.
     */
    public long getTime() { return m_time; }

    /**
     * The entry number of this timer in the heap of timers maintained by
     * the main loop.
     */
    protected int m_entry = -1;

    /**
     * Gets the entry number of this timer in the heap of timers maintained
     * by the main loop.
     *
     * @return the entry number.
     */
    protected int getEntry() { return m_entry; }

    /** Sets the entry number of this timer in the heap of timers maintained
     * by the main loop.
     *
     * @param x the entry number.
     */
    protected void setEntry(int x) { m_entry = x; }

    /** The TimerHandler that will be invoked when this timer fires. */
    protected TimerHandler m_handler;

    /**
     * Gets the TimerHandler that will be invoked when this timer fires.
     *
     * @return the TimerHandler for this timer.
     */
    public TimerHandler getHandler() { return m_handler; }
}

package com.readyposition.reactor;

/**
 * The TimerHandler interface is used to invoke work when a timer
 * expires.
 */
public interface TimerHandler
{
    /**
     * This method is invoked when a timer has expired.
     *
     * @param scheduledTime at which the timer was scheduled.
     * @param actualTime the time at which the timer actually fired.
     * @return the absolute time at which the timer should fire again.
     *         Return 0 if the timer should not fire again.
     */
    long timerFire(long scheduledTime, long actualTime);
}

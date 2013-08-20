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
 * The TimerHandler interface represents work that can be performed by a Timer.
 */
public interface TimerHandler {
    /**
     * Performs work that was previously scheduled to be invoked by a timer.
     *
     * @param time the time at which the Timer actually fired.
     */
    public void timerFire(long time);
}

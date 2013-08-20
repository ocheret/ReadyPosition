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
 * The WorkerHandler interface represents work that can be performed when the
 * main loop is done handling pending I/O and timers.
 */
public interface WorkerHandler {
    /** Performs work when main loop is don handling pending I/O and timers. */
    void workerFire();
}

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
 * Performs work when I/O operations are ready.
 */
public interface ChannelerHandler {
    /**
     * Invoked when there is an I/O operation ready to be performed.
     *
     * @param channeler the Channeler registered for this I/O operations.
     */
    public void channelerFire(Channeler channeler);
}

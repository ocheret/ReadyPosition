/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.itemizer;

/**
 * Interface for iterating over data structures with integer
 * entry numbers.
 */
public interface EntryItemizer {
    /** Returns true if the iteration has more elements. */
    public boolean hasNext();

    /** Advances to the next entry in the iteration. */
    public void next();

    /** Returns the current entry in the iteration. */
    public int entry();

    /** Removes the current entry from the underlying collection. */
    public void remove();
}

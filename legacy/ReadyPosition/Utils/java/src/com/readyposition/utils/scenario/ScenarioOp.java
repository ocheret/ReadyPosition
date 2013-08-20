/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.scenario;

/**
 * This interface is used by reversible operations residing in a scenario tree.
 * Operation implementation classes are expected to maintain whatever state is
 * necessary to do and undo operations quickly.
 */
public interface ScenarioOp
{
    /**
     * Applies a forward operation to a scenario.
     *
     * @param scenario the scenario to operate on.
     */
    public void doIt(Scenario scenario);

    /**
     * Applies a reverse operation to a scenario.
     *
     * @param scenario the scenario to operate on.
     */
    public void undoIt(Scenario scenario);
}

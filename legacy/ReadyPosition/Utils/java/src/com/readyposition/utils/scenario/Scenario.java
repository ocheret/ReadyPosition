/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.scenario;

import com.readyposition.utils.hash.ObjectObjectHash;

import java.util.ArrayList;

public class Scenario
{
    // XXX - maybe replace constructors with static methods

    /**
     * Constructor for an unnamed root scenario
     *
     * @param gestalt the state of the world we're starting with
     */
    public Scenario(Object gestalt)
    {
	m_depth = 0;
	m_setting = new Setting(this, gestalt);
	m_parent = null;
    }

    /**
     * Constructor for a named root scenario
     *
     * @param gestalt the state of the world we're starting with
     * @param name the name to associate with the scenario
     */
    public Scenario(Object gestalt, String name)
    {
	this(gestalt);
	m_name = name;
	m_setting.register(this, name);
    }

    /**
     * Constructor for an unnamed scenario in the tree
     *
     * @param parent the direct ancestor of this scenario
     */
    public Scenario(Scenario parent)
    {
	m_setting = parent.m_setting;
	m_depth = parent.m_depth + 1;
	m_parent = parent;
	m_parent.m_kids.add(this);
    }

    /**
     * Constructor for a named scenario in the tree
     *
     * @param parent the direct ancestor of this scenario
     * @param name the name to associate with the scenario
     */
    public Scenario(Scenario parent, String name)
    {
	this(parent);
	m_name = name;
	m_setting.register(this, name);
    }

    /** The parent of this scenario in the tree. */
    protected Scenario m_parent;

    /** Gets the parent of this scenario in the tree. */
    public Scenario getParent() { return m_parent; }

    /** The depth of this scenario in the tree. */
    protected int m_depth;

    /** Gets the depth of this scenario in the tree. */
    public int getDepth() { return m_depth; }

    /** XXX - javadoc */
    protected Setting m_setting;

    /** XXX - javadoc */
    public Setting getSetting() { return m_setting; }

    /** XXX - javadoc */
    protected ArrayList m_ops = new ArrayList();

    /** XXX - javadoc */
    public ArrayList getOps() { return m_ops; }

    /** XXX - javadoc */
    protected ArrayList m_kids = new ArrayList();

    /** XXX - javadoc */
    public ArrayList getKids() { return m_kids; }

    /** XXX - javadoc */
    public int getKidCount() { return m_kids.size(); }

    /** XXX - javadoc */
    protected String m_name;

    /** XXX - javadoc */
    public String getName() { return m_name; }

    /** XXX - javadoc */
    public void setName(String name) {
	if (null != m_name)
	    m_setting.unregister(m_name);
	m_name = name;
	m_setting.register(this, name);
    }

    /** XXX - javadoc */
    public void delete() {
	int i = m_parent.m_kids.indexOf(this);
	if (-1 != i)
	    m_parent.m_kids.remove(i);
	if (null != m_name)
	    m_setting.unregister(m_name);
    }

    /** XXX - javadoc */
    public Object getGestalt() { return m_setting.getGestalt(); }

    /** XXX - javadoc */
    public Scenario getCurrent() {
	return m_setting.getCurrent();
    }

    /** XXX - javadoc */
    protected void redo() {
	int len = m_ops.size();
	for (int i = 0; i < len; i++) {
	    ScenarioOp op = (ScenarioOp)m_ops.get(i);
	    op.doIt(this);
	}
    }

    /** XXX - javadoc */
    protected void undo() {
	int len = m_ops.size();
	for (int i = len - 1; i >= 0; i--) {
	    ScenarioOp op = (ScenarioOp)m_ops.get(i);
	    op.undoIt(this);
	}
    }

    /** XXX - javadoc */
    public Scenario lookup(String name) {
	return m_setting.lookup(name);
    }

    /** XXX - javadoc */
    public void reAssert() {
	// We will go up the tree form the current Scenario to the common
	// ancestor and then down the tree to this Scenario
	Scenario up = m_setting.getCurrent();
	Scenario down = this;

	if (up == down) {
	    // This Scenario is already asserted
	    return;
	}

	// If the current Scenario is deeper than the target Scenario then
	// we can safely undo operations from the current Scenario until
	// we reach the depth of the target Scenario
	while (up.m_depth > down.m_depth) {
	    up.undo();
	    up = up.m_parent;
	}

	// If the current Scenario is not as deep as the target Scenario
	// then we can record the Scenarios up to the current Scenario's depth
	ArrayList downpath = new ArrayList();
	while (up.m_depth < down.m_depth) {
	    downpath.add(down);
	    down = down.m_parent;
	}

	// March up the tree one level at a time until we converge on the
	// common ancestor
	while (up != down) {
	    up.undo();
	    up = up.m_parent;
	    downpath.add(down);
	    down = down.m_parent;
	}

	// Redo operations from the common ancestor to get the gestalt
	// in synch with the target Scenario (this)
	int len = downpath.size();
	for (int i = 0; i < len; i++) {
	    down = (Scenario)downpath.get(i);
	    down.redo();
	}

	m_setting.setCurrent(this);
    }

    /** XXX - javadoc */
    public void operate(ScenarioOp op) {
	reAssert();
	op.doIt(this);

	// Don't bother recording operations on the root Scenario since we
	// can't undo them anyway
	if (!isRoot())
	    m_ops.add(op);
    }

    /** XXX - javadoc */
    public void removeOp(ScenarioOp op) {
	int index = m_ops.indexOf(op);
	if (-1 == index)
	    // No such operation
	    return;

	// Remember where we were
	Scenario old = m_setting.getCurrent();

	// Go to this scenario and remove the operation
	reAssert();
	undo();
	m_ops.remove(index);
	redo();

	// Return to the old scenario
	old.reAssert();
    }

    /** XXX - javadoc */
    public boolean isRoot() {
	return this == m_setting.getRoot();
    }

    /**
     * A single setting instance is created for each independent
     * scenario tree.  It keeps track of the actual state being
     * maintained as we traverse the tree and also keeps track of what
     * scenario is currently being asserted.
     */
    static class Setting {
	/** XXX - javadoc */
	protected Object m_gestalt;

	/** XXX - javadoc */
	Object getGestalt() { return m_gestalt; }

	/** XXX - javadoc */
	protected Scenario m_root;

	/** XXX - javadoc */
	Scenario getRoot() { return m_root; }

	/** XXX - javadoc */
	protected Scenario m_current;

	/** XXX - javadoc */
	Scenario getCurrent() { return m_current; }

	/** XXX - javadoc */
	void setCurrent(Scenario current) { m_current = current; }

	/** XXX - javadoc */
	ObjectObjectHash m_hash = new ObjectObjectHash();

	/** XXX - javadoc */
	Setting(Scenario root, Object gestalt) {
	    m_root = m_current = root;
	    m_gestalt = gestalt;
	}

	/** XXX - javadoc */
	void register(Scenario scenario, String name) {
	    // XXX - deal with duplicates?
	    m_hash.put(name, scenario);
	}

	/** XXX - javadoc */
	void unregister(String name) {
	    m_hash.remove(name);
	}

	/** XXX - javadoc */
	Scenario lookup(String name) {
	    return (Scenario)m_hash.get(name);
	}
    }
}

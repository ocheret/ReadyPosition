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
 * The Node class implements a doubly linked list.  Insert operations
 * for the list have interesting symmetry properties (i.e.
 * node1.insert(node2) results in the same configuration as
 * node2.insert(node1)). TODO - write more detail here (refer to libdap).
 */
public class Node {
    /**
     * Constructor for a node.  After construction a node's next and
     * previous pointers both refer to the node itself.
     */
    public Node() { m_next = m_prev = this; }

    /**
     * Inserts nodes into each other's lists.  This can be used to splice
     * two lists together.  The operation is also reversible.  Weird, huh?
     *
     * @param j the node to insert in our list.
     */
    public void insert(Node j) {
	if (j == null)
	    return;
	Node t = m_prev;
	m_prev.m_next = j;
	m_prev = j.m_prev;
	j.m_prev.m_next = this;
	j.m_prev = t;
    }

    /**
     * Appends nodes onto each other's lists.  This can be used to
     * splice two lists together.  The operation is also reversible.
     * Weird, huh?
     *
     * @param j the node to append to our list.
     */
    public void append(Node j) {
	if (j == null)
	    return;
	Node t = m_next;
	m_next.m_prev = j;
	m_next = j.m_next;
	j.m_next.m_prev = this;
	j.m_next = t;
    }

    /** Removes a node from a list so it forms its own single element list. */
    public void remove() {
	m_prev.m_next = m_next;
	m_next.m_prev = m_prev;
	m_next = m_prev = this;
    }

    /** The next node in the list. */
    protected Node m_next;

    /** Gets the next node in the list. */
    public Node getNext() { return m_next; }

    /** The previous node in the list. */
    protected Node m_prev;

    /** Gets the previous node in the list. */
    public Node getPrev() { return m_prev; }
}

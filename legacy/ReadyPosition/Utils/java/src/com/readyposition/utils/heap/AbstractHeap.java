/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.heap;

import com.readyposition.utils.comparator.IntCmp;

import java.util.NoSuchElementException;

/**
 * AbstractHeap is intended to be subclasses to build efficient
 * array oriented heaps with arbitrarily typed keys and values (including
 * primitive types).
 */
public abstract class AbstractHeap
{
    /**
     * Constructor for invocation by subclass constructors.
     *
     * @param initialCapacity the initial number of elements that can be
     *        held in the heap.
     * @param growthFactor factor used to grow internal arrays when the
     *        heap needs to grow (e.g. 2.0f means to double array sizes).
     */
    protected AbstractHeap(int initialCapacity, float growthFactor)
    {
	if (initialCapacity < 0)
	    throw new IllegalArgumentException("Illegal capacity: " +
	      initialCapacity);
	if (growthFactor <= 1.0 || java.lang.Float.isNaN(growthFactor))
	    throw new IllegalArgumentException("Illegal growth factor: " +
	      growthFactor);

	m_initialCapacity = (initialCapacity == 0) ? 1 : initialCapacity;
	m_growthFactor = growthFactor;
	clear();
    }

    /**
     * Constructor for invocation by subclass constructors.  A default
     * growth factor is used.
     *
     * @param initialCapacity the initial number of elements that can be
     *        held in the heap.
     */
    protected AbstractHeap(int initialCapacity)
    {
	this(initialCapacity, DEFAULT_GROWTH_FACTOR);
    }

    /**
     * Constructor for invocation by subclass constructors.  A default
     * initial capacity and a default growth factor are used.
     */
    protected AbstractHeap()
    {
	this(DEFAULT_INITIAL_CAPACITY, DEFAULT_GROWTH_FACTOR);
    }

    /**
     * The default initial capacity.
     */
    public final static int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The default growth factor.
     */
    public final static float DEFAULT_GROWTH_FACTOR = 2.0f;

    /**
     * The initial capacity for a new or cleared heap.
     */
    protected int m_initialCapacity;

    /**
     * Gets the initial capacity for this heap.
     *
     * @return the initial capacity specified to the constructor or the
     *         default initial capacity if none was provided.
     */
    public int getInitialCapacity()
    {
	return m_initialCapacity;
    }

    /**
     * The grow factor for this heap.
     */
    protected float m_growthFactor;

    /**
     * Gets the growth factor for this heap.
     *
     * @return the growth factor specified to the constructor or the
     *         default growth factor if none was provided.
     */
    public float getGrowthFactor()
    {
	return m_growthFactor;
    }

    /**
     * A comparitor to prioritize entry numbers base on their
     * associated keys.  This is null until needed for the first time
     * by a subclass.
     */
    protected IntCmp m_sortCmp;

    /**
     * An array representing the heap.  The elements of the array are indices
     * into the m_key array and any value arrays implemented by subclasses.
     * The m_tree array represents a binary tree where for element n in the
     * array...
     *
     * <blockquote>
     * <pre>
     *	left(n) = (n * 2) + 1
     *	right(n) = (n + 1) * 2
     *	parent(n) = (n - 1) / 2
     * </pre>
     * </blockquote>
     *
     * The trailing entries in the array (from m_tree[m_size] thru
     * m_tree[m_size + m_freeCount - 1]) contain the indices of free
     * elements of the m_key and parallel arrays.
     */
    protected int[] m_tree;

    /**
     * An inverse permutation of the m_tree array.  This allows
     * mapping from an entry (index into the m_key and parallel
     * arrays) back to an index into the m_tree array.
     */
    protected int[] m_inverse;

    /**
     * The number of free elements in the m_key array.
     */
    protected int m_freeCount;

    /**
     * The number of elements in the heap.
     */
    protected int m_size;

    /**
     * Gets the number of elements in the heap.
     *
     * @return the number of elements in the heap.
     */
    public int getSize()
    {
	return m_size;
    }

    /**
     * Gets the length of the arrays allocated for the heap.  This may be
     * greater than or equal to the number of elements in the heap.
     */
    public int getAllocated()
    {
	return m_tree.length;
    }

    /**
     * This is incremented whenever the heap is modified.  This can be used
     * by iterators to detect that they may have become invalid.
     * <p>TODO - implement iterators!
     */
    protected transient int m_modCount = 0;

    /**
     * Get the entry number of the first entry in the heap.  The entry is
     * not removed from the heap.
     *
     * @return the number of the first entry in the heap according to the
     *         heaps priority order.
     */
    public int peek()
    {
	if (m_size == 0)
	    throw new NoSuchElementException("Invoked peek() on empty heap");
	return m_tree[0];
    }

    /**
     * Gets all of the entries in the heap.
     *
     * @return an array of all of the entry numbers in the heap.  This array
     *         parallels the arrays returned by getKeys() and any getValues()
     *         methods implemented by subclasses.
     */
    public int[] getEntries()
    {
	int[] r = new int[m_size];
	for (int i = 0; i < m_size; i++)
	    r[i] = m_tree[i];
	return r;
    }

    /**
     * Resets the heap to initial empty conditions.
     */
    public abstract void clear();

    /**
     * Invoked when subclasses need to resize any data structures used to
     * hold values associated with keys.
     *
     * @param size the new number of entries the value data structures need
     *        to be able to hold.
     */
    protected abstract void resizeValues(int size);

    /**
     * Invoked when an entry is removed so that subclasses can remove
     * references to values in support of garbage collection.
     *
     * @param entry the number of the entry that has been removed.
     */
    protected abstract void removeValue(int entry);
}

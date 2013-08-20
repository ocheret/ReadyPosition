/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.hash;

import com.readyposition.utils.itemizer.EntryItemizer;

import java.io.Serializable;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

/**
 * AbstractHash is intended to be subclassed to build efficient
 * array oriented hash tables and hash sets with arbitrarily typed
 * keys and values (including primitive types).
 * <p>
 * Keys, values, and other internal information are maintained in
 * parallel arrays.  It is up to subclasses to manage storage and
 * access to the key and value arrays by implementing the growKeys,
 * growValues, and removeValue abstract methods.
 */
public abstract class AbstractHash implements Serializable
{
    /**
     * Constructs an empty AbstractHash with the specified inital
     * capacity and load factor.
     *
     * @param initialCapacity the initial capacity.
     * @param loadFactor the load factor.
     */
    protected AbstractHash(int initialCapacity, float loadFactor)
    {
	if (initialCapacity < 0)
	    throw new IllegalArgumentException("Illegal Capacity: " +
	      initialCapacity);
        if (loadFactor <= 0 || java.lang.Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);

	if (0 == initialCapacity)
	    initialCapacity = 1;

	m_loadFactor = loadFactor;
	m_threshold = 1 + (int)(initialCapacity * loadFactor);
	m_buckets = new int[initialCapacity];
	Arrays.fill(m_buckets, -1);
	m_next = new int[m_threshold];
	growKeys(m_threshold);
	growValues(m_threshold);
    }

    /**
     * Constructs an empty AbstractHash with the specified inital
     * capacity and the default load factor (0.75)
     *
     * @param initialCapacity the initial capacity.
     */
    protected AbstractHash(int initialCapacity)
    {
	this(initialCapacity, 0.75f);
    }

    /**
     * Constructs an empty AbstractHash with default capacity (11)
     * and the default load factor (0.75)
     */
    protected AbstractHash()
    {
	this(11, 0.75f);
    }

    /**
     * The number of entries in this table.
     */
    protected int m_size;

    /**
     * Gets the number of entries in this table.
     */
    public int getSize()
    {
	return m_size;
    }

    /**
     * The first free element in the parallel arrays.
     */
    protected int m_firstFree = -1;

    /**
     * Each bucket can reference the head of a linked list of entries.
     */
    protected int[] m_buckets;

    /**
     * Parallels the key/value arrays and represents entry linked lists.
     */
    protected int[] m_next;

    /**
     * Used by iterators to ensure consistency.
     */
    protected int m_modCount = 0;

    /**
     * Determines when we need to grow the table.
     */
    protected int m_threshold;

    /**
     * Used to compute the threshold for growing the table.
     */
    protected float m_loadFactor;

    /**
     * The next available entry.
     */
    protected int m_nextUnused = 0;

    /**
     * Retrieves all of the entries in the table.  Subclasses should
     * provide appropriately typed getKeys and getValues methods that
     * will return arrays parallel to the one returned here.
     *
     * @return an array of integer entries.
     */
    public int[] getEntries()
    {
	int[] entries = new int[m_size];
	int count = 0;
	for (int bucket = 0; bucket < m_buckets.length; bucket++)
	    for (int i = m_buckets[bucket]; i != -1; i = m_next[i])
		entries[count++] = i;
	return entries;
    }

    /**
     * Removes all entries (mappings) from the table.
     */
    public void clear()
    {
	m_modCount++;
	Arrays.fill(m_buckets, -1);
	clearKeys();
	clearValues();
	m_size = 0;
	m_firstFree = -1;
	m_nextUnused = 0;
    }

    /**
     * Grow the keys array.  All keys are copied to the expanded array.
     *
     * @param size the new size of the keys array.
     */
    protected abstract void growKeys(int size);

    /**
     * Clears the keys array.
     */
    protected abstract void clearKeys();

    /**
     * Grow the values array.  All values are copied to the expanded array.
     *
     * @param size the new size of the values array.
     */
    protected abstract void growValues(int size);

    /** Clears the values array. */
    protected abstract void clearValues();

    /**
     * Clean up data for the value at a specified entry.
     *
     * @param entry the entry number of the value to clean up.
     */
    protected abstract void removeValue(int entry);

    /**
     * Class allowing iteration through a hash table's entries.
     */
    protected class Itemizer implements Serializable, EntryItemizer {
	/**
	 * Constructor
	 */
	Itemizer()
	{
	    m_bucket = 0;
	    m_prev = -1;
	    m_index = m_buckets[0];
	    while (-1 == m_index && ++m_bucket < m_buckets.length)
		m_index = m_buckets[m_bucket];
	}

	/**
	 * The bucket for the current entry.
	 */
	protected int m_bucket;

	/**
	 * The index of the current entry.
	 */
	protected int m_index;

	/**
	 * Used to manage removal.
	 */
	protected int m_prev;

	/**
	 * Bucket of last returned entry.
	 */
	protected int m_returnedBucket = -1;

	/**
	 * Prev index of last returned entry.
	 */
	protected int m_returnedPrev = -1;

	/**
	 * Index of last returned entry.
	 */
	protected int m_returnedIndex = -1;

	/**
	 * Makes sure hash table hasn't been messed with during traversal.
	 */
	protected int m_expectedModCount = m_modCount;

	/**
	 * Returns true if there are more entries.
	 */
	public boolean hasNext()
	{
	    return -1 != m_index;
	}

	/**
	 * Gets the entry number of the next entry.
	 */
	public void next()
	{
	    if (m_modCount != m_expectedModCount)
		throw new ConcurrentModificationException();

	    if (-1 == m_index)
		throw new NoSuchElementException();

	    // Remember where we were
	    m_returnedBucket = m_bucket;
	    m_returnedPrev = m_prev;
	    m_returnedIndex = m_index;

	    // Advance to the next entry
	    m_prev = m_index;
	    m_index = m_next[m_index];
	    if (-1 == m_index)
		m_prev = -1;
	    while (-1 == m_index && ++m_bucket < m_buckets.length)
		m_index = m_buckets[m_bucket];
	}

	/**
	 * Gets the entry number of the current entry (without advancing).
	 */
	public int entry()
	{
	    if (m_modCount != m_expectedModCount)
		throw new ConcurrentModificationException();

	    if (-1 == m_index)
		throw new NoSuchElementException();

	    // This is what we'll return
	    int index = m_index;

	    // Remember where we are
	    m_returnedBucket = m_bucket;
	    m_returnedPrev = m_prev;
	    m_returnedIndex = m_index;
	    return index;
	}

	/**
	 * Removes the current entry.
	 */
	public void remove()
	{
	    if (-1 == m_returnedBucket)
		throw new IllegalStateException();
	    if (m_modCount != m_expectedModCount)
		throw new ConcurrentModificationException();

	    // Remove the entry from the bucket's list
	    if (-1 == m_returnedPrev)
		m_buckets[m_returnedBucket] = m_next[m_returnedIndex];
	    else
		m_next[m_returnedPrev] = m_next[m_returnedIndex];

	    // Add the entry to free list
	    m_next[m_returnedIndex] = m_firstFree;
	    m_firstFree = m_returnedIndex;

	    // Clean up value
	    removeValue(m_returnedIndex);

	    // Keep stats up to date
	    m_size--;
	    m_modCount++;
	    m_expectedModCount++;
	}
    }
}

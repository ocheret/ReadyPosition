/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

// XXX - This should be machine generated!  I'm tired of editing this stuff
// by hand

package com.readyposition.utils.hash;

import com.readyposition.utils.comparator.Comparators;
import com.readyposition.utils.comparator.ObjectEq;
import com.readyposition.utils.itemizer.ObjectKeyItemizer;
import com.readyposition.utils.Hash;

import java.lang.reflect.Array;

/**
 * ObjectAbstractHash is intended to be subclassed to build efficient
 * array oriented hash tables and hash sets with Object keys
 * and arbitrarily typed values (including primitive types).
 * <p>
 * Keys, values, and other internal information are maintained in
 * parallel arrays.  It is up to subclasses to manage storage and
 * access to the value array by implementing the growValues and
 * removeValue abstract methods.
 */
public abstract class ObjectAbstractHash extends AbstractHash
{
    /**
     * Constructs an empty ObjectAbstractHash with the specified inital
     * capacity and load factor.
     *
     * @param initialCapacity the initial capacity.
     * @param loadFactor the load factor.
     */
    protected ObjectAbstractHash(int initialCapacity, float loadFactor) {
	super(initialCapacity, loadFactor);
    }

    /**
     * Constructs an empty ObjectAbstractHash with the specified inital
     * capacity and the default load factor (0.75)
     *
     * @param initialCapacity the initial capacity.
     */
    protected ObjectAbstractHash(int initialCapacity) {
	super(initialCapacity);
    }

    /**
     * Constructs an empty ObjectAbstractHash with default capacity (11)
     * and the default load factor (0.75)
     */
    protected ObjectAbstractHash() {
	super();
    }

    /** Value representing null keys inside tables. */
    protected static final Object NULL_KEY = new Object();

    /** Returns internal representation for key. Use NULL_KEY if null key. */
    protected static Object maskNull(Object key) {
        return (key == null ? NULL_KEY : key);
    }

    /** Returns key represented by specified internal representation. */
    protected static Object unmaskNull(Object key) {
        return (key == NULL_KEY ? null : key);
    }

    /**
     * Grow the keys array.  All keys are copied to the expanded array.
     *
     * @param size the new size of the keys array.
     */
    protected void growKeys(int size) {
	Object[] newKeys = new Object[size];
	if (m_keys != null)
	    System.arraycopy(m_keys, 0, newKeys, 0, m_keys.length);
	m_keys = newKeys;
    }

    /** Clears the keys array. */
    protected void clearKeys() {
	// Simply replace the array with a new array of the same length.
	m_keys = new Object[m_keys.length];
    }

    /** Array holding the keys to the kingdom. */
    protected Object[] m_keys;

    /** Comparator for keys. */
    protected ObjectEq m_eq = Comparators.OBJECT_EQ;

    /** Sets the comparator for keys. */
    public void setEq(ObjectEq eq) {
	if (eq == null)
	    throw new IllegalArgumentException("Null ObjectEq");
	m_eq = eq;
    }

    /** Gets the comparator for keys. */
    public ObjectEq getEq() { return m_eq; }

    /**
     * Retrieves all of the keys in the table.  This array is parallel
     * to the one returned by getEntries in the superclass.
     * Subclasses should provide appropriately typed getValues methods
     * that will return arrays parallel to the one returned here.
     *
     * @return an array of integer entries.
     */
    public Object[] getKeys() {
	return getKeys(null);
    }

    /**
     * Retrieves all of the keys in the table.  This array is parallel
     * to the one returned by getEntries in the superclass.
     * Subclasses should provide appropriately typed getValues methods
     * that will return arrays parallel to the one returned here.
     *
     * @param dst the array into which the keys are to be stored, if
     *	it is big enough; otherwise, a new array of the same runtime
     *	type is allocated for this purpose.  If the array is longer than needed
     *  then the extra elements at the end of the array will be initialized
     *  to null.  If dst is null, then an Object[] of the appropriate size
     *  is returned.
     * @return an array of integer entries.
     */
    public Object[] getKeys(Object[] dst) {
	if (dst == null) {
	    dst = new Object[m_size];
	} else if (dst.length < m_size) {
	    dst = (Object[])Array.newInstance(
		dst.getClass().getComponentType(), m_size);
	}
	int count = 0;
	for (int bucket = 0; bucket < m_buckets.length; bucket++)
	    for (int i = m_buckets[bucket]; i != -1; i = m_next[i])
		dst[count++] = unmaskNull(m_keys[i]);
	for (int i = dst.length - 1; i >= m_size; i--) {
	    dst[i] = null;
	}
	return dst;
    }

    /**
     * Interns a key in the table.  If the key is already interned
     * in the table, then the existing entry is retrieved.
     *
     * @param key the key to intern in the table.
     * @return the entry in which the key is interned.
     */
    protected int intern(Object key) {
	m_modCount++;
	key = maskNull(key);
	int bucket = computeBucket(key);
	for (int i = m_buckets[bucket]; -1 != i; i = m_next[i]) {
	    if (m_eq.equals(key, m_keys[i]))
		// This entry already exists
		return i;
	}

	// If we're here we are adding a new entry

	if (m_size >= m_threshold) {
	    // Time to increase the number of buckets
	    rehash();
	    bucket = computeBucket(key);
	}

	int entry;
	if (-1 != m_firstFree) {
	    // There are entries on the free list to use
	    entry = m_firstFree;
	    m_firstFree = m_next[entry];
	} else
	    // No free entries - append new element
	    entry = m_nextUnused++;
	m_keys[entry] = key;
	m_next[entry] = m_buckets[bucket];

	// Complete the insertion of new entry into the bucket's list
	m_buckets[bucket] = entry;
	m_size++;

	return entry;
    }

    /**
     * Searches for a key's entry in the table.
     *
     * @param key the key to look for in the table.
     * @return the entry number of the key or -1 if the key is not in
     *	the table.
     */
    public int getEntry(Object key) {
	key = maskNull(key);
	int bucket = computeBucket(key);
	for (int i = m_buckets[bucket]; -1 != i; i = m_next[i])
	    if (m_eq.equals(key, m_keys[i]))
		return i;
	return -1;
    }

    /**
     * Retrieves an entry's key.
     *
     * @param entry the entry whose key we want to retrieve.
     * @return the key associated with the entry.
     */
    public Object getKey(int entry) {
	return unmaskNull(m_keys[entry]);
    }

    /**
     * Returns true if this table contains a mapping for the specified key.
     *
     * @param key the key whose presence in this table is to be tested.
     * @return true if this table contains the specified key, false otherwise.
     */
    public boolean containsKey(Object key) {
	return -1 != getEntry(key);
    }

    /**
     * Removes any mapping for the specified key.
     *
     * @param key the key whose mapping is to be removed from the table.
     */
    public void remove(Object key) {
	m_modCount++;
	key = maskNull(key);
	int bucket = computeBucket(key);
	int prev = -1;
	for (int i = m_buckets[bucket]; -1 != i; prev = i, i = m_next[i]) {
	    if (!m_eq.equals(key, m_keys[i]))
		continue;

	    m_keys[i] = null;
	    int n = m_next[i];
	    if (-1 == prev)
		// Head of list
		m_buckets[bucket] = n;
	    else
		// Not the head of the list
		m_next[prev] = n;

	    m_next[i] = m_firstFree;
	    m_firstFree = i;
	    m_size--;
	    removeValue(i);
	    return;
	}
    }

    /**
     * Removes an entry from the table.
     *
     * @param entry the entry to remove from the table.
     */
    public void removeEntry(int entry) {
	remove(unmaskNull(m_keys[entry]));
    }

    /**
     * Computes the hash bucket for a key.
     *
     * @param key the key to map to a bucket.
     * @return the bucket into which the key hashes.
     */
    private int computeBucket(Object key) {
	int code = Hash.supp(Hash.code(key)) & Integer.MAX_VALUE;
	int bucket = code % m_buckets.length;
	return bucket;
    }

    /** Grows the hash table and reassigns all entries to new buckets. */
    private void rehash() {
	int oldCapacity = m_buckets.length;
	int newCapacity = oldCapacity * 2 + 1;
	m_threshold = 1 + (int)(newCapacity * m_loadFactor);

	int[] oldBuckets = m_buckets;
	Object[] oldKeys = m_keys;
	int[] oldIndex = m_next;
	m_buckets = new int[newCapacity];
	m_keys = new Object[m_threshold];
	m_next = new int[m_threshold];
	System.arraycopy(oldKeys, 0, m_keys, 0, oldKeys.length);
	System.arraycopy(oldIndex, 0, m_next, 0, oldIndex.length);
	growValues(m_threshold);

	for (int i = 0; i < newCapacity; i++)
	    m_buckets[i] = -1;

	for (int i = 0; i < oldCapacity; i++) {
	    int index = oldBuckets[i];
	    while (-1 != index) {
		int bucket = computeBucket(oldKeys[index]);
		int n = oldIndex[index];
		m_next[index] = m_buckets[bucket];
		m_buckets[bucket] = index;
		index = n;
	    }
	}
    }

    /** Class for iterating through a hash table. */
    protected class Itemizer
	extends AbstractHash.Itemizer
	implements ObjectKeyItemizer
    {
	/** Gets the current key. */
	public Object key() {
	    return unmaskNull(m_keys[entry()]);
	}
    }
}

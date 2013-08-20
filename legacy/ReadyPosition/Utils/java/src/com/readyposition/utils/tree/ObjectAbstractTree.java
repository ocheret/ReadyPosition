/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.tree;

import com.readyposition.utils.comparator.Comparators;
import com.readyposition.utils.comparator.ObjectCmp;
import com.readyposition.utils.itemizer.ObjectKeyItemizer;
import java.lang.reflect.Array;

/**
 * ObjectAbstractTree is intended to be subclassed to build efficient
 * array oriented tree maps and tree sets with Object keys
 * and arbitrarily typed values (including primitive types).
 * <p>
 * Keys, values, and other internal information are maintained in
 * parallel arrays.  It is up to subclasses to manage storage and
 * access to the value array by implementing abstract methods.
 */
public abstract class ObjectAbstractTree extends AbstractTree {
    /**
     * Constructs an empty ObjectAbstractTree with the specified initial
     * capacity and growth factor.
     *
     * @param initialCapacity the number of entries the tree is
     *  initially allocated to hold.
     * @param growthFactor the factor by which to increase the tree size
     *  when it needs to expand.
     */
    protected ObjectAbstractTree(int initialCapacity, float growthFactor) {
	super(initialCapacity, growthFactor);
    }

    /**
     * Constructs an empty ObjectAbstractTree with the specified initial
     * capacity and a default growth factor of 2.
     *
     * @param initialCapacity the number of entries the tree is
     *  initially allocated to hold.
     */
    protected ObjectAbstractTree(int initialCapacity) {
	super(initialCapacity);
    }

    /**
     * Constructs an empty ObjectAbstractTree with a default initial
     * capacity of 16 and a default growth factor of 2.
     */
    protected ObjectAbstractTree() {
	super();
    }

    /**
     * Value representing null keys inside tables.
     */
    protected static final Object NULL_KEY = new Comparable() {
	    public int compareTo(Object o) {
		if (o == NULL_KEY) {
		    return 0;
		}
		return 1;
	    }
	};

    /**
     * Returns internal representation for key. Use NULL_KEY if null key.
     */
    protected static Object maskNull(Object key) {
        return (key == null ? NULL_KEY : key);
    }

    /**
     * Returns key represented by specified internal representation.
     */
    protected static Object unmaskNull(Object key) {
        return (key == NULL_KEY ? null : key);
    }

    /**
     * The comparator used to compare entries in the tree.
     */
    protected ObjectCmp m_cmp = Comparators.OBJECT_ASC;

    /**
     * Gets the comparator used to compare entries in the tree.
     */
    public ObjectCmp getCmp() { return m_cmp; }

    /**
     * Sets the comparator used to compare entries in the tree.
     */
    public void setCmp(ObjectCmp x) {
	if (x == null)
	    throw new IllegalArgumentException("Null comparitor");
	if (m_cmp == x)
	    return;
	m_cmp = x;

	int ic = m_initialCapacity;
	m_initialCapacity = m_key.length;
	reinsert();
	m_initialCapacity = ic;
    }

    /**
     * The array that holds the keys.
     */
    protected Object[] m_key;


    /**
     * Clears the array of keys to an initial state.
     */
    public void clearKeys() {
	m_key = new Object[m_initialCapacity];
    }

    /**
     * Grows the keys array.  All keys are copied to the expanded array.
     *
     * @param size the new size of the keys array.
     */
    public void growKeys(int size) {
	Object[] newKeys = new Object[size];
	if (m_key != null) {
	    System.arraycopy(m_key, 0, newKeys, 0, m_key.length);
	}
	m_key = newKeys;
    }

    /**
     * Interns a key in the tree.  If the key is already interned
     * in the tree, then the existing entry is retrieved.
     *
     * @param key the key to intern in the tree.
     * @return the entry in which the key is interned.
     */
    public int intern(Object key) {
	int t = m_root;
	key = maskNull(key);

	if (t == -1) {
	    m_root = getNewEntry(-1);
	    m_key[m_root] = key;
	    return m_root;
	}

	while (true) {
	    int comparison = m_cmp.compare(key, m_key[t]);
	    if (comparison == 0){
		// We found a match
		return t;
	    }

	    if (comparison < 0) {
		if (m_left[t] != -1) {
		    // Descend left
		    t = m_left[t];
		} else {
		    // No left child - insert here
		    int r = getNewEntry(t);
		    m_key[r] = key;
		    m_left[t] = r;
		    fixAfterInsertion(r);
		    return r;
		}
	    } else {
		if (m_right[t] != -1) {
		    // Descent right
		    t = m_right[t];
		} else {
		    int r = getNewEntry(t);
		    m_key[r] = key;
		    m_right[t] = r;
		    fixAfterInsertion(r);
		    return r;
		}
	    }
	}
    }

    /**
     * Gets the key associated with an entry number.
     */
    public Object getKey(int entry) {
	return unmaskNull(m_key[entry]);
    }

    /**
     * Determines whether or not the tree contains a mapping for a key.
     *
     * @param key the key whose presence is being queried.
     * @return true if the key has a mapping in the tree, false otherwise.
     */
    public boolean containsKey(Object key) {
	return -1 != getEntry(key);
    }

    /**
     * Looks up the entry number for the node containing a specified key.
     *
     * @param key the key to look up.
     * @return the entry number of the node containing the key or -1
     *  if not found.
     */
    public int getEntry(Object key) {
	key = maskNull(key);
	int p = m_root;
	while (p != -1) {
	    int comparison = m_cmp.compare(key, m_key[p]);
	    if (comparison == 0)
		return p;
	    if (comparison < 0)
		p = m_left[p];
	    else
		p = m_right[p];
	}
	return -1;
    }

    /**
     * Retrieves an array of all of the keys in the tree 'in order'.
     *
     * @return an array of keys 'in order'.
     */
    public Object[] getKeys() {
	Object[] keys = new Object[m_size];
	int count = 0;
	count = getTreeKeys(keys, count, m_root);
	return keys;
    }

    /**
     * Retrieves an array of all of the keys in the tree 'in order'.
     *
     * @param dst the array into which the keys are to be stored, if
     *	it is big enough; otherwise, a new array of the same runtime
     *	type is allocated for this purpose.
     * @return an array of keys 'in order'.
     */
    public Object[] getKeys(Object[] dst) {
	if (dst.length < m_size) {
	    dst = (Object[])Array.newInstance(
		dst.getClass().getComponentType(), m_size);
	}
	int count = 0;
	count = getTreeKeys(dst, count, m_root);
	return dst;
    }

    /**
     * Fills in an array of keys 'in order' for a subtree of the tree.
     *
     * @param keys an array of keys to be filled in.
     * @param count the index in the keys array at which to start filling.
     * @param entry the root of the subtree whose keys are to be retrieved.
     * @return the number of keys currently filled in in the keys array.
     */
    protected int getTreeKeys(Object[] keys, int count, int entry) {
	if (entry == -1)
	    return count;
	count = getTreeKeys(keys, count, m_left[entry]);
	keys[count++] = unmaskNull(m_key[entry]);
	return getTreeKeys(keys, count, m_right[entry]);
    }

    /**
     * Returns a String representation of the tree useful for diagnostics.
     */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	int indent = 0;
	getTreeString(sb, indent, m_root);

	sb.append("entry\tkey\tparent\tleft\tright\tcolor\n");
	for (int i = 0; i < m_key.length; i++) {
	    if (i != 0)
		sb.append('\n');
	    sb.append(i).append('\t');
	    sb.append(m_key[i]).append('\t');
	    sb.append(m_parent[i]).append('\t');
	    sb.append(m_left[i]).append('\t');
	    sb.append(m_right[i]).append('\t');
	    sb.append(colorString(m_color[i]));
	}

	return sb.toString();
    }

    /**
     * Fills in a StringBuffer with a hierarchical view of the tree.
     */
    protected void getTreeString(StringBuffer sb, int indent, int entry) {
	if (entry == -1)
	    return;
	getTreeString(sb, indent + 1, m_left[entry]);
	for (int i = 0; i < indent; i++)
	    sb.append("  ");
	sb.append(entry).append('/').append(m_key[entry])
	    .append('/').append(colorString(m_color[entry])).append('\n');
	getTreeString(sb, indent + 1, m_right[entry]);
    }

    /**
     * Removes any mapping for the specified key.
     *
     * @param key the key whose mapping is to be removed from the tree.
     */
    public void remove(Object key) {
	int i = getEntry(key);
	if (i == -1)
	    throw new IndexOutOfBoundsException("No such key " + key);
	removeEntry(i);
    }

    /**
     * Clean up data for the key at a specified entry.
     *
     * @param entry the entry number of the value to clean up.
     */
    protected void removeKey(int entry) {
	m_key[entry] = null;
    }

    /**
     * Class for iterating through a tree.
     */
    protected class Itemizer
	extends AbstractTree.Itemizer
	implements ObjectKeyItemizer
    {
	/** Gets the current key. */
	public Object key() {
	    return unmaskNull(m_key[entry()]);
	}
    }
}

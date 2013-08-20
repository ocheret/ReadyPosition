/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.tree;

import com.readyposition.utils.itemizer.EntryItemizer;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

/**
 * AbstractTree is intended to be subclassed to build efficient
 * array oriented tree maps and tree sets with arbitrarily typed
 * keys and values (including primitive types).
 * <p>
 * Keys, values, and other internal information are maintained in
 * parallel arrays.  It is up to subclasses to manage storage and
 * access to the key and value arrays by implementing abstract
 * methods.
 * <p>
 * The tree is implemented as a typical red/black tree in an array
 * oriented fashion.
 */
public abstract class AbstractTree implements Serializable
{
    /**
     * Constructs an empty AbstractTree with the specified initial
     * capacity and growth factor.
     *
     * @param initialCapacity the number of entries the tree is
     *  initially allocated to hold.
     * @param growthFactor the factor by which to increase the tree size
     *  when it needs to expand.
     */
    protected AbstractTree(int initialCapacity, float growthFactor) {
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
     * Constructs an empty AbstractTree with the specified initial
     * capacity and a default growth factor of 2.
     *
     * @param initialCapacity the number of entries the tree is
     *  initially allocated to hold.
     */
    protected AbstractTree(int initialCapacity) {
	this(initialCapacity, DEFAULT_GrowthFactor);
    }

    /**
     * Constructs an empty AbstractTree with a default initial
     * capacity of 16 and a default growth factor of 2.
     */
    protected AbstractTree() {
	this(DEFAULT_InitialCapacity, DEFAULT_GrowthFactor);
    }

    /** The default initial capacity of the tree. */
    protected final static int DEFAULT_InitialCapacity = 16;

    /** The default growth factor of the tree. */
    protected final static float DEFAULT_GrowthFactor = 2.0f;

    /** The initial capacity of the tree. */
    protected int m_initialCapacity;

    /** Gets the initial capacity of the tree. */
    public int getInitialCapacity() { return m_initialCapacity; }

    /** The growth factor of the tree. */
    protected float m_growthFactor;

    /** Gets the growth factor of the tree. */
    public float getGrowthFactor() { return m_growthFactor; }

    /** Indicates the RED color in a RED/BLACK tree. */
    protected static final byte RED = -1;

    /** Indicates an unused node in a RED/BLACK tree. */
    protected static final byte EMPTY = 0;

    /** Indicates the BLACK color in a RED/BLACK tree. */
    protected static final byte BLACK = 1;

    /**
     * Returns a String representation of a color code.
     *
     * @param color the color code.
     * @return a color String of either "RED", "BLACK", "EMPTY", or
     *  "???" if the color code is not recognized.
     */
    protected static String colorString(byte color) {
	switch (color) {
	case BLACK:
	    return "BLACK";
	case EMPTY:
	    return "EMPTY";
	case RED:
	    return "RED";
	}
	return "???";
    }

    /**
     * Array containing the indices of the left children in the tree.
     * Also used to chain free list
     */
    protected int[] m_left;

    /** Array containing the indices of the right children in the tree. */
    protected int[] m_right;

    /** Array containing the indices of the parents in the tree. */
    protected int[] m_parent;

    /** Array containing the colors of the nodes in the tree. */
    protected byte[] m_color;	// RED, BLACK, EMPTY

    /** Index of first free entry in the tree. */
    protected int m_firstFree;

    /** Index of first unused entry in the tree. */
    protected int m_nextUnused;

    /** Index of the root node in the tree. */
    protected int m_root;

    /** The number of entries in the tree. */
    protected int m_size;

    /** Gets the number of entries populated in the tree. */
    public int getSize() { return m_size; }

    /** Gets the number of entries allocated in the tree. */
    public int getAllocated() { return m_color.length; }

    /** Every time something changes in the tree modCount is incremented. */
    protected transient int m_modCount = 0;

    /** Clears the tree and resizes it to its initialCapacity. */
    public void clear() {
	clearKeys();
	clearValues();
	m_left = new int[m_initialCapacity];
	m_right = new int[m_initialCapacity];
	m_parent = new int[m_initialCapacity];
	m_color = new byte[m_initialCapacity];
	m_firstFree = -1;
	m_nextUnused = 0;
	m_root = -1;
	m_size = 0;
	m_modCount++;
    }

    /**
     * Gets an unused node in the tree to be a child of a specified node.
     *
     * @param parent the index of the parent node to get a new child.
     * @return the index of the fresh node.
     */
    protected int getNewEntry(int parent) {
	int t;
	if (m_firstFree != -1) {
	    // There are entries on the free list to use
	    t = m_firstFree;
	    m_firstFree = m_left[t];
	} else {
	    // There are no entries on the free list
	    int oldlen = m_color.length;
	    if (m_nextUnused >= oldlen) {
		// There is no room for a new entry so we must expand arrays
		int newlen = (int)(oldlen * m_growthFactor);
		if (newlen <= oldlen)
		    newlen = oldlen + 1;
		int[] oldleft = m_left;
		int[] oldright = m_right;
		int[] oldparent = m_parent;
		byte[] oldcolor = m_color;

		m_left = new int[newlen];
		m_right = new int[newlen];
		m_parent = new int[newlen];
		m_color = new byte[newlen];

		System.arraycopy(oldleft, 0, m_left, 0, oldlen);
		System.arraycopy(oldright, 0, m_right, 0, oldlen);
		System.arraycopy(oldparent, 0, m_parent, 0, oldlen);
		System.arraycopy(oldcolor, 0, m_color, 0, oldlen);

		growKeys(newlen);
		growValues(newlen);
	    }
	    t = m_nextUnused++;
	}
	m_left[t] = m_right[t] = -1;
	m_parent[t] = parent;
	m_color[t] = BLACK;

	m_modCount++;
	m_size++;

	return t;
    }

    /**
     * Retrieves an array of all of the entries in the tree 'in order'.
     *
     * @return an array of entries 'in order'.
     */
    public int[] getEntries() {
	int[] entries = new int[m_size];
	int count = 0;
	count = getTreeEntries(entries, count, m_root);
	return entries;
    }

    /**
     * Fills in an array of entries 'in order' for a subtree of the tree.
     *
     * @param entries an array of entries to be filled in.
     * @param count the index in the entries array at which to start filling.
     * @param entry the root of the subtree whose entries are to be retrieved.
     * @return the number of entries currently filled in in the entries array.
     */
    protected int getTreeEntries(int[] entries, int count, int entry) {
	if (entry == -1)
	    return count;
	count = getTreeEntries(entries, count, m_left[entry]);
	entries[count++] = entry;
	return getTreeEntries(entries, count, m_right[entry]);
    }

    /**
     * Determines the depth of the tree.
     *
     * @return the depth of the tree.  If the tree is empty then 0 is
     *  returned.  If there is only a root node then 1 is returned.
     */
    public int getDepth() {
	return doGetDepth(m_root);
    }

    /**
     * Recursively determines the depth of a subtree.
     *
     * @param entry the root of the subtree whose depth we want.
     * @return the depth of the tree.  If the subtree is empty then 0 is
     *  returned.  If there is only a root node then 1 is returned.
     */
    protected int doGetDepth(int entry) {
	if (entry == -1)
	    return 0;
	int leftDepth = doGetDepth(m_left[entry]);
	int rightDepth = doGetDepth(m_right[entry]);
	if (leftDepth > rightDepth) {
	    return leftDepth + 1;
	}
	return rightDepth + 1;
    }

    /**
     * Finds the successor to a node in the tree.
     *
     * @param t the node whose successor will be returned.
     * @return the node number of the successor or -1 if there is none.
     */
    public int successor(int t) {
	if (t == -1)
	    return -1;
	else if (m_right[t] != -1) {
	    int p = m_right[t];
	    while (m_left[p] != -1)
		p = m_left[p];
	    return p;
	} else {
	    int p = m_parent[t];
	    int ch = t;
	    while (p != -1 && ch == m_right[p]) {
		ch = p;
		p = m_parent[p];
	    }
	    return p;
	}
    }

    /**
     * Deletes an entry from the tree.
     *
     * @param p the entry to remove from the tree.
     */
    public void removeEntry(int p) {
	// If strictly internal, first swap position with successor.
	if (m_left[p] != -1 && m_right[p] != -1) {
	    int s = successor(p);
	    swapPosition(s, p);
	}

	// Start fixup at replacement node, if it exists.
	int replacement = (m_left[p] != -1 ? m_left[p] : m_right[p]);

	if (replacement != -1) {
	    // Link replacement to parent
	    m_parent[replacement] = m_parent[p];
	    if (m_parent[p] == -1)
		m_root = replacement;
	    else if (p == m_left[m_parent[p]])
		m_left[m_parent[p]]  = replacement;
	    else
		m_right[m_parent[p]] = replacement;

	    // Null out links so they are OK to use by fixAfterDeletion.
	    m_left[p] = m_right[p] = m_parent[p] = -1;

	    // Fix replacement
	    if (m_color[p] == BLACK)
		fixAfterDeletion(replacement);
	} else if (m_parent[p] == -1) { // return if we are the only node.
	    m_root = -1;
	} else { //  No children. Use self as phantom replacement and unlink.
	    if (m_color[p] == BLACK)
		fixAfterDeletion(p);

	    if (m_parent[p] != -1) {
		if (p == m_left[m_parent[p]])
		    m_left[m_parent[p]] = -1;
		else if (p == m_right[m_parent[p]])
		    m_right[m_parent[p]] = -1;
		m_parent[p] = -1;
	    }
	}
	removeKey(p);
	removeValue(p);
	m_color[p] = EMPTY;

	// Put the deleted entry on the free list
	m_left[p] = m_firstFree;
	m_firstFree = p;

	m_modCount++;
	m_size--;
    }

    /**
     * Readjusts a red/black tree after a deletion.
     *
     * @param x the entry of the node to readjust.
     */
    protected void fixAfterDeletion(int x) {
	while (x != m_root && colorOf(x) == BLACK) {
	    if (x == leftOf(parentOf(x))) {
		int sib = rightOf(parentOf(x));

		if (colorOf(sib) == RED) {
		    setColor(sib, BLACK);
		    setColor(parentOf(x), RED);
		    rotateLeft(parentOf(x));
		    sib = rightOf(parentOf(x));
		}

		if (colorOf(leftOf(sib))  == BLACK &&
		    colorOf(rightOf(sib)) == BLACK) {
		    setColor(sib,  RED);
		    x = parentOf(x);
		} else {
		    if (colorOf(rightOf(sib)) == BLACK) {
			setColor(leftOf(sib), BLACK);
			setColor(sib, RED);
			rotateRight(sib);
			sib = rightOf(parentOf(x));
		    }
		    setColor(sib, colorOf(parentOf(x)));
		    setColor(parentOf(x), BLACK);
		    setColor(rightOf(sib), BLACK);
		    rotateLeft(parentOf(x));
		    x = m_root;
		}
	    } else { // symmetric
		int sib = leftOf(parentOf(x));

		if (colorOf(sib) == RED) {
		    setColor(sib, BLACK);
		    setColor(parentOf(x), RED);
		    rotateRight(parentOf(x));
		    sib = leftOf(parentOf(x));
		}

		if (colorOf(rightOf(sib)) == BLACK &&
		    colorOf(leftOf(sib)) == BLACK) {
		    setColor(sib,  RED);
		    x = parentOf(x);
		} else {
		    if (colorOf(leftOf(sib)) == BLACK) {
			setColor(rightOf(sib), BLACK);
			setColor(sib, RED);
			rotateLeft(sib);
			sib = leftOf(parentOf(x));
		    }
		    setColor(sib, colorOf(parentOf(x)));
		    setColor(parentOf(x), BLACK);
		    setColor(leftOf(sib), BLACK);
		    rotateRight(parentOf(x));
		    x = m_root;
		}
	    }
	}
	setColor(x, BLACK);
    }

    /**
     * Swaps two entries in the tree.
     *
     * @param x first entry to swap.
     * @param y second entry to swap.
     */
    protected void swapPosition(int x, int y) {
	// Save initial values.
	int px = m_parent[x], lx = m_left[x], rx = m_right[x];
	int py = m_parent[y], ly = m_left[y], ry = m_right[y];
	boolean xWasLeftChild = px != -1 && x == m_left[px];
	boolean yWasLeftChild = py != -1 && y == m_left[py];

	// Swap, handling special cases of one being the other's parent.
	if (x == py) {  // x was y's parent
	    m_parent[x] = y;
	    if (yWasLeftChild) {
		m_left[y] = x;
		m_right[y] = rx;
	    } else {
		m_right[y] = x;
		m_left[y] = lx;
	    }
	} else {
	    m_parent[x] = py;
	    if (py != -1) {
		if (yWasLeftChild)
		    m_left[py] = x;
		else
		    m_right[py] = x;
	    }
	    m_left[y] = lx;
	    m_right[y] = rx;
	}

	if (y == px) { // y was x's parent
	    m_parent[y] = x;
	    if (xWasLeftChild) {
		m_left[x] = y;
		m_right[x] = ry;
	    } else {
		m_right[x] = y;
		m_left[x] = ly;
	    }
	} else {
	    m_parent[y] = px;
	    if (px != -1) {
		if (xWasLeftChild)
		    m_left[px] = y;
		else
		    m_right[px] = y;
	    }
	    m_left[x] = ly;
	    m_right[x] = ry;
	}

	// Fix children's parent pointers
	if (m_left[x] != -1)
	    m_parent[m_left[x]] = x;
	if (m_right[x] != -1)
	    m_parent[m_right[x]] = x;
	if (m_left[y] != -1)
	    m_parent[m_left[y]] = y;
	if (m_right[y] != -1)
	    m_parent[m_right[y]] = y;

	// Swap colors
	byte c = m_color[x];
	m_color[x] = m_color[y];
	m_color[y] = c;

	// Check if root changed
	if (m_root == x)
	    m_root = y;
	else if (m_root == y)
	    m_root = x;
    }

    /**
     * Returns the color of a node in the tree.
     *
     * @param x the entry number of the node.
     * @return the color of the entry or BLACK if the entry is invalid.
     */
    protected byte colorOf(int x) {
	return (x == -1 ? BLACK : m_color[x]);
    }

    /**
     * Sets the color of a node in the tree.  If the node isn't valid
     * no color is set.
     *
     * @param x the entry number of the node whose color is to be set.
     * @param c the color to set the node to.
     */
    protected void setColor(int x, byte c) {
	if (x != -1)
	    m_color[x] = c;
    }

    /**
     * Gets the parent of a node.
     *
     * @param x the entry number of the node.
     * @return the entry number of the parent or -1 if none.
     */
    protected int parentOf(int x) {
	return (x == -1 ? -1 : m_parent[x]);
    }

    /**
     * Gets the left child of a node.
     *
     * @param x the entry number of the parent node.
     * @return the entry number of the left child node or -1 if none.
     */
    protected int leftOf(int x) {
	return (x == -1 ? -1 : m_left[x]);
    }

    /**
     * Gets the right child of a node.
     *
     * @param x the entry number of the parent node.
     * @return the entry number of the right child node or -1 if none.
     */
    protected int rightOf(int x) {
	return (x == -1 ? -1 : m_right[x]);
    }

    /**
     * Performs a left roration operation around a specified node.
     *
     * @param x the entry number of the node to rotate about.
     */
    protected void rotateLeft(int x) {
	int r = m_right[x];
	m_right[x] = m_left[r];
	if (m_left[r] != -1)
	    m_parent[m_left[r]] = x;
	m_parent[r] = m_parent[x];
	if (m_parent[x] == -1)
	    m_root = r;
	else if (m_left[m_parent[x]] == x)
	    m_left[m_parent[x]] = r;
	else
	    m_right[m_parent[x]] = r;
	m_left[r] = x;
	m_parent[x] = r;
    }

    /**
     * Performs a right roration operation around a specified node.
     *
     * @param x the entry number of the node to rotate about.
     */
    protected void rotateRight(int x) {
	int l = m_left[x];
	m_left[x] = m_right[l];
	if (m_right[l] != -1)
	    m_parent[m_right[l]] = x;
	m_parent[l] = m_parent[x];
	if (m_parent[x] == -1)
	    m_root = l;
	else if (m_right[m_parent[x]] == x)
	    m_right[m_parent[x]] = l;
	else
	    m_left[m_parent[x]] = l;
	m_right[l] = x;
	m_parent[x] = l;
    }

    /**
     * Readjusts a red/black tree after an insertion.
     *
     * @param x the entry of the node to readjust.
     */
    protected void fixAfterInsertion(int x) {
	m_color[x] = RED;
	while (x != -1 && x != m_root && m_color[m_parent[x]] == RED) {
	    if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
		int y = rightOf(parentOf(parentOf(x)));
		if (colorOf(y) == RED) {
		    setColor(parentOf(x), BLACK);
		    setColor(y, BLACK);
		    setColor(parentOf(parentOf(x)), RED);
		    x = parentOf(parentOf(x));
		} else {
		    if (x == rightOf(parentOf(x))) {
			x = parentOf(x);
			rotateLeft(x);
		    }
		    setColor(parentOf(x), BLACK);
		    setColor(parentOf(parentOf(x)), RED);
		    if (parentOf(parentOf(x)) != -1)
			rotateRight(parentOf(parentOf(x)));
		}
	    } else {
		int y = leftOf(parentOf(parentOf(x)));
		if (colorOf(y) == RED) {
		    setColor(parentOf(x), BLACK);
		    setColor(y, BLACK);
		    setColor(parentOf(parentOf(x)), RED);
		    x = parentOf(parentOf(x));
		} else {
		    if (x == leftOf(parentOf(x))) {
			x = parentOf(x);
			rotateRight(x);
		    }
		    setColor(parentOf(x),  BLACK);
		    setColor(parentOf(parentOf(x)), RED);
		    if (parentOf(parentOf(x)) != -1)
			rotateLeft(parentOf(parentOf(x)));
		}
	    }
	}
	m_color[m_root] = BLACK;
    }

    /** Clears the array of keys to an initial state. */
    protected abstract void clearKeys();

    /**
     * Grows the keys array.  All keys are copied to the expanded array.
     *
     * @param size the new size of the keys array.
     */
    protected abstract void growKeys(int size);

    /**
     * Clean up data for the key at a specified entry.
     *
     * @param entry the entry number of the value to clean up.
     */
    protected abstract void removeKey(int entry);

    /** Clears the array of values to an initial state. */
    protected abstract void clearValues();

    /**
     * Grow the values array.  All values are copied to the expanded array.
     *
     * @param size the new size of the values array.
     */
    protected abstract void growValues(int size);

    /**
     * Clean up data for the value at a specified entry.
     *
     * @param entry the entry number of the value to clean up.
     */
    protected abstract void removeValue(int entry);

    /**
     * Invoked to reinsert entries into the tree after the comparator
     * has been changed.
     */
    protected abstract void reinsert();

    /** Class allowing iteration through a tree's entries. */
    protected class Itemizer implements Serializable, EntryItemizer {
	/** Constructor */
	Itemizer() {
	    m_entry = m_root;
	    if (m_entry != -1) {
		while (m_left[m_entry] != -1) {
		    m_entry = m_left[m_entry];
		}
	    }
	}

	/** The current entry. */
	protected int m_entry = -1;

	/** The successor of an entry that has been removed. */
	protected int m_successor = -1;

	/** Makes sure tree hasn't been messed with during traversal. */
	protected int m_expectedModCount = m_modCount;

	/** Returns true if there are more entries. */
	public boolean hasNext() {
	    return -1 != m_entry;
	}

	/** Gets the entry number of the next entry. */
	public void next() {
	    if (m_modCount != m_expectedModCount)
		throw new ConcurrentModificationException();

	    if (-1 == m_entry)
		throw new NoSuchElementException();

	    if (m_successor == -1) {
		m_entry = successor(m_entry);
	    } else {
		m_entry = m_successor;
		m_successor = -1;
	    }
	}

	/** Gets the entry number of the current entry (without advancing). */
	public int entry() {
	    if (m_modCount != m_expectedModCount)
		throw new ConcurrentModificationException();

	    if (-1 == m_entry)
		throw new NoSuchElementException();

	    return m_entry;
	}

	/** Removes the current entry. */
	public void remove() {
	    if (m_modCount != m_expectedModCount)
		throw new ConcurrentModificationException();

	    if (-1 == m_entry)
		throw new NoSuchElementException();

	    // Clean up value
	    m_successor = successor(m_entry);
	    removeEntry(m_entry);
	    m_entry = -1;

	    // Keep stats up to date
	    m_expectedModCount++;
	}
    }
}

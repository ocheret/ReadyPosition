// _Warning_

package com.readyposition.collections.heap;

import com.readyposition.collections.comparator.*;
import com.readyposition.collections.common.*;

/**
 * This is an abstract base class for array oriented heap data
 * structures (sets, maps, and tables) with _KeyType_ keys. This
 * provides the common machinery used by subclasses to implement heap
 * data structures with _KeyType_ keys and values of arbitrary types
 * and consisting of an arbitrary number of fields.  Values are
 * maintained by subclasses in arrays parallel to the keys array to
 * reduce the number of Object references. This abstract class
 * provides a classic heap implementation and provides a key array, a
 * tree array with a permutation representing the heap order of
 * entries in the tree, and an inverse permutation.
 */
public abstract class AbstractHeap_KeyName_
    extends AbstractHeap
{
    /**
     * Constructor for invocation by subclass constructors. A default
     * initial capacity and a default growth factor are used.
     */
    protected AbstractHeap_KeyName_() {
    }

    /**
     * Constructor for invocation by subclass constructors. A default
     * growth factor is used.
     * 
     * @param initialCapacity the initial number of elements that can be held in the heap.
     */
    protected AbstractHeap_KeyName_(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructor for invocation by subclass constructors.
     * 
     * @param initialCapacity the initial number of elements that can
     *                        be held in the heap.
     * @param growthFactor factor used to grow internal arrays when
     *                     the heap needs to grow (e.g. 2.0f means to
     *                     double array sizes).
     */
    protected AbstractHeap_KeyName_(int initialCapacity, float growthFactor) {
        super(initialCapacity, growthFactor);
    }

    /** The current comparator used to prioritize the heap. */
    protected Cmp_KeyName_ m_cmp = Comparators._KeyName_Asc;

    /**
     * Gets the current comparator used to prioritize the heap.
     * 
     * @return the current comparator for this heap.
     */
    public Cmp_KeyName_ getCmp() {
        return m_cmp;
    }

    /**
     * Sets the comparator used to prioritize this heap. If the heap
     * already contains elements, the heap is rebuilt to reflect the
     * new priority. All future changes to the heap will reflect the
     * new priority.
     * 
     * @param x the comparator to use to reprioritize the heap.
     */
    public void setCmp(Cmp_KeyName_ x) {
        if (x == null) {
            throw new IllegalArgumentException("Null comparitor");
        }

        if (m_cmp == x) {
            return;
        }

        m_cmp = x;

        for (int i = 0; i < m_size; i++) {
            int entry = m_tree[i];
            filterUp(i, m_key[entry], entry);
        }
    }

    /** Resets the heap to initial empty conditions. */
    public void clear() {
        m_tree = new int[m_initialCapacity];
        m_inverse = new int[m_initialCapacity];
        m_key = new _KeyType_[m_initialCapacity];
        resizeValues(0);
        resizeValues(m_initialCapacity);
        m_freeCount = 0;
        m_size = 0;
        m_modCount++;
    }

    /**
     * Inserts a new entry into the heap.
     * 
     * @param key the key of the new element to insert.
     * @return the entry number of the new entry. Subclasses can use
     *         this number as an index into an array to associate a
     *         value with the key.
     */
    public int insert(_KeyType_ key) {
        // Insert the new node as the bottom-rightmost leaf in the tree
        int child = m_size;

        // Find space for the new entry
        int entry = getNewEntry(key);

        // Move the new node up the tree until it is in the right place
        filterUp(child, key, entry);

        return entry;
    }

    /**
     * Removes an entry from the heap.
     * 
     * @param entry the number of the entry that is to be removed.
     */
    public void remove(int entry) {
        // Get the node of the tree that refers to this entry.
        int node = m_inverse[entry];

        // Allow old values to be garbage collected
        removeKey(entry);
        removeValue(entry);
        m_size--;

        // Get information about the last leaf in the tree
        int lastEntry = m_tree[m_size];
        _KeyType_ key = m_key[lastEntry];

        // Record the removed entry on the free list
        m_freeCount++;
        m_tree[m_size] = entry;

        // If we just removed the last node in the tree we're done.
        if (node == m_size) {
            return;
        }

        filterDown(filterUp(node, key, lastEntry), key, lastEntry);
    }
    
    /**
     * Invoked when an entry is removed so that subclasses can remove
     * references to keys in support of garbage collection.
     * 
     * @param entry the number of the entry that has been removed.
     */
    public void removeKey(int entry) {
    	m_key[entry] = Types.get_KeyName_For(0);
    }

    /**
     * Change the key for an entry in the heap. The entry is moved to
     * a new location in the heap that satisfies the heap ordering.
     * 
     * @param entry the number of the entry whose key is to be changed.
     * @param newKey the new key value for the entry.
     */
    public void resetKey(int entry, _KeyType_ newKey) {
        m_key[entry] = newKey;
        filterDown(filterUp(m_inverse[entry], newKey, entry), newKey, entry);
    }

    /**
     * Gets the key for an entry.
     * 
     * @param entry the number of the entry whose key is to be retrieved.
     * @return the key of the specified entry.
     */
    public _KeyType_ getEntryKey(int entry) {
        return m_key[entry];
    }

    /**
     * Gets all of the keys in the heap.
     * 
     * @return an array of all of the keys in the heap. This array
     *         parallels the arrays returned by getEntries() and any
     *         getValues() methods implemented by subclasses.
     */
    public _KeyType_[] getKeys() {
        _KeyType_[] r = new _KeyType_[m_size];
        for (int i = 0; i < m_size; i++) {
            r[i] = m_key[m_tree[i]];
        }
        return r;
    }

    /**
     * Determines if a particular key is in the heap.
     * 
     * @param key the key to search for.
     * @return true if at least one entry in the heap has the
     *         specified key, otherwise false.
     */
    public boolean containsKey(_KeyType_ key) {
        for (int i = 0; i < m_size; i++) {
            int entry = m_tree[i];
            if (m_cmp.compare(key, m_key[entry]) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * The array of heap key values. Any values associated with the
     * keys should be kept in arrays parallel to this one.
     */
    protected _KeyType_[] m_key;

    /**
     * Gets a new entry in the m_key and parallel arrays. If there are
     * any free entries, then the first of those is
     * returned. Otherwise, a new entry at the end of the arrays is
     * returned.
     * 
     * @param key the key of the new entry.
     * @return the index of the new entry in the parallel arrays.
     */
    protected int getNewEntry(_KeyType_ key) {
        int t;
        if (m_freeCount > 0) {
            // There are entries on the free list to use
            t = m_tree[m_size];
            m_freeCount--;
        } else {
            // There are no free entries
            int oldlen = m_key.length;
            if (m_size >= oldlen) {
                // There is no room for a new entry so we must expand arrays
                int newlen = (int) (oldlen * m_growthFactor);
                if (newlen <= oldlen) {
                    newlen = oldlen + 1;
                }
                int[] oldTree = m_tree;
                int[] oldInverse = m_inverse;
                _KeyType_[] oldKey = m_key;

                m_tree = new int[newlen];
                m_inverse = new int[newlen];
                m_key = new _KeyType_[newlen];

                System.arraycopy(oldTree, 0, m_tree, 0, oldlen);
                System.arraycopy(oldInverse, 0, m_inverse, 0, oldlen);
                System.arraycopy(oldKey, 0, m_key, 0, oldlen);

                resizeValues(newlen);
            }
            t = m_size;
        }
        m_key[t] = key;

        m_modCount++;
        m_size++;

        return t;
    }

    /**
     * Moves a node up the tree until it is in the right place.
     * 
     * @param child the node in the tree where an entry has been placed.
     * @param key the key of the entry.
     * @param entry the number of the entry being placed in the tree.
     * @return the node where the entry has finally been placed.
     */
    protected int filterUp(int child, _KeyType_ key, int entry) {
        while (child != 0) {
            // Compare the child to its parent to see if it needs to move up
            int parent = (child - 1) >> 1;
            _KeyType_ parentKey = m_key[m_tree[parent]];
            if (m_cmp.compare(key, parentKey) >= 0) {
                // The new node is fine where it is
                break;
            }

            // We need to swap the parent with the new node
            m_tree[child] = m_tree[parent];
            m_inverse[m_tree[child]] = child;
            child = parent;
        }

        m_tree[child] = entry;
        m_inverse[entry] = child;

        return child;
    }

    /**
     * Moves a node down the tree until it is in the right place.
     * 
     * @param parent the node in the tree where an entry has been placed.
     * @param key the key of the entry.
     * @param entry the number of the entry being placed in the tree.
     */
    protected void filterDown(int parent, _KeyType_ key, int entry) {
        // While parent is not a leaf node...
        int child;
        while ((child = (parent << 1) + 1) < m_size) {
            _KeyType_ childKey = m_key[m_tree[child]];

            // If we have a right child, compare it to the left
            if (child + 1 < m_size) {
                _KeyType_ rightKey = m_key[m_tree[child + 1]];
                if (m_cmp.compare(childKey, rightKey) > 0) {
                    // The right key comes before the left key
                    child++;
                    childKey = rightKey;
                }
            }

            // Compare our key to the earliest child key. We use <
            // to preserve insert order.
            if (m_cmp.compare(key, childKey) < 0) {
                // The node is fine where it is.
                break;
            }

            // We need to swap the parent with the child.
            m_tree[parent] = m_tree[child];
            m_inverse[m_tree[parent]] = parent;
            parent = child;
        }

        m_tree[parent] = entry;
        m_inverse[entry] = parent;
    }
}

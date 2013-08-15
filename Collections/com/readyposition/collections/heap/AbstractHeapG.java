package com.readyposition.collections.heap;

import java.lang.reflect.Array;

import com.readyposition.collections.comparator.*;

/**
 * This is an abstract base class for array oriented heap data
 * structures (sets, maps, and tables) with Generic keys. This
 * provides the common machinery used by subclasses to implement heap
 * data structures with Generic keys and values of arbitrary types and
 * consisting of an arbitrary number of fields.  Values are maintained
 * by subclasses in arrays parallel to the keys array to reduce the
 * number of Object references. This abstract class provides a classic
 * heap implementation and provides a key array, a tree array with a
 * permutation representing the heap order of entries in the tree, and
 * an inverse permutation.
 */
public abstract class AbstractHeapG<K>
    extends AbstractHeap
{
    /**
     * Constructor for invocation by subclass constructors. A default
     * initial capacity and a default growth factor are used.
     */
    protected AbstractHeapG() {}

    /**
     * Constructor for invocation by subclass constructors. A default
     * growth factor is used.
     *
     * @param initialCapacity the initial number of elements that can be held in the heap.
     */
    protected AbstractHeapG(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructor for invocation by subclass constructors.
     *
     * @param initialCapacity the initial number of elements that can be held in the heap.
     * @param growthFactor factor used to grow internal arrays when
     *                     the heap needs to grow (e.g. 2.0f means to
     *                     double array sizes).
     */
    protected AbstractHeapG(int initialCapacity, float growthFactor) {
        super(initialCapacity, growthFactor);
    }

    /** The current comparator used to prioritize the heap. */
    protected CmpG<Object> m_cmp = Comparators.GenericObjectAsc;

    /**
     * Gets the current comparator used to prioritize the heap.
     *
     * @return the current comparator for this heap.
     */
    public CmpG<Object> getCmp() {
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
    public void setCmp(CmpG<Object> x) {
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
        m_key = new Object[m_initialCapacity];
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
    public int insert(K key) {
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
        Object key = m_key[lastEntry];

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
     * references to values in support of garbage collection.
     * 
     * @param entry the number of the entry that has been removed.
     */
    protected void removeKey(int entry) {
    	m_key[entry] = null;    	
    }

    /**
     * Change the key for an entry in the heap. The entry is moved to
     * a new location in the heap that satisfies the heap ordering.
     *
     * @param entry the number of the entry whose key is to be changed.
     * @param newKey the new key value for the entry.
     */
    public void resetKey(int entry, K newKey) {
        m_key[entry] = newKey;
        filterDown(filterUp(m_inverse[entry], newKey, entry), newKey, entry);
    }

    /**
     * Gets the key for an entry.
     *
     * @param entry the number of the entry whose key is to be retrieved.
     * @return the key of the specified entry.
     */
    @SuppressWarnings("unchecked")
    public K getEntryKey(int entry) {
        return (K)m_key[entry];
    }

    /**
     * Gets all of the keys in the heap.
     *
     * @return an array of all of the keys in the heap. This array
     *         parallels the arrays returned by getEntries() and any
     *         getValues() methods implemented by subclasses.
     */
    public Object[] getKeys() {
    	Object[] dst = new Object[m_size];
    	for (int i = 0; i < m_size; i++) {
    		dst[i] = m_key[m_tree[i]];
    	}
    	return dst;
   }

    /**
     * Retrieves all of the keys in the heap.
     *
     * @param dst the array into which the keys are to be stored, if
     *	          it is big enough; otherwise, a new array of the same
     *	          runtime type is allocated for this purpose.  If the
     *	          array is longer than needed then the extra elements
     *	          are not touched.
     * @return an array of keys.
     */
    @SuppressWarnings("unchecked")
    public K[] getKeys(K[] dst) {
        if (dst.length < m_size) {
            dst = (K[])Array.newInstance(dst.getClass()
            		.getComponentType(), m_size);
        }
    	for (int i = 0; i < m_size; i++) {
    		dst[i] = (K)m_key[m_tree[i]];
    	}
        return dst;
    }

    /**
     * Determines if a particular key is in the heap.
     *
     * @param key the key to search for.
     * @return true if at least one entry in the heap has the
     *         specified key, otherwise false.
     */
    public boolean containsKey(K key) {
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
    protected Object[] m_key;

    /**
     * Gets a new entry in the m_key and parallel arrays. If there are
     * any free entries, then the first of those is
     * returned. Otherwise, a new entry at the end of the arrays is
     * returned.
     *
     * @param key the key of the new entry.
     * @return the index of the new entry in the parallel arrays.
     */
    protected int getNewEntry(K key) {
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
                Object[] oldKey = m_key;

                m_tree = new int[newlen];
                m_inverse = new int[newlen];
                m_key = new Object[newlen];

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
    protected int filterUp(int child, Object key, int entry) {
        while (child != 0) {
            // Compare the child to its parent to see if it needs to move up
            int parent = (child - 1) >> 1;
            Object parentKey = m_key[m_tree[parent]];
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
    protected void filterDown(int parent, Object key, int entry) {
        // While parent is not a leaf node...
        int child;
        while ((child = (parent << 1) + 1) < m_size) {
            Object childKey = m_key[m_tree[child]];

            // If we have a right child, compare it to the left
            if (child + 1 < m_size) {
                Object rightKey = m_key[m_tree[child + 1]];
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

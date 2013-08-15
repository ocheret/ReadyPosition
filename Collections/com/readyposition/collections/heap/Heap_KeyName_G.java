// _Warning_

package com.readyposition.collections.heap;

import com.readyposition.collections.common.*;

/**
 * Heap_KeyName_G provides an array oriented heap with _KeyType_ keys
 * and Generic values. Keys and values are maintained in parallel
 * arrays to reduce the number of Object references.
 */
public class Heap_KeyName_G<V> extends AbstractHeap_KeyName_ {
    /** The array of values that parallels the m_keys array of keys. */
    protected Object[] m_values;

    /** Constructs a heap with _KeyType_ keys and Generic values. */
    public Heap_KeyName_G() {}

    /**
     * Constructs a heap with _KeyType_ keys and Generic values.
     * 
     * @param initialCapacity the initial number of elements that can be held in the heap.
     */
    public Heap_KeyName_G(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs a heap with _KeyType_ keys and Generic values.
     * 
     * @param initialCapacity the initial number of elements that can be held in the heap.
     * @param growthFactor factor used to grow internal arrays when
     *                     the heap needs to grow (e.g. 2.0f means to
     *                     double array sizes).
     */
    public Heap_KeyName_G(int initialCapacity, float growthFactor) {
        super(initialCapacity, growthFactor);
    }

    /**
     * Inserts a new entry into the heap.
     * 
     * @param key the key of the new element to insert.
     * @param value the value of the new element to insert.
     * @return the entry number of the new entry. This number should
     *         be used to refer to the key and value for this entry.
     */
    public int insert(_KeyType_ key, V value) {
        int i = insert(key);
        m_values[i] = value;
        return i;
    }

    /**
     * Gets the value associated with an entry.
     * 
     * @param entry the number of the entry whose value is to be retrieved.
     * @return the value for the specified entry.
     */
    @SuppressWarnings("unchecked")
    public V getEntryValue(int entry) {
        return (V)m_values[entry];
    }

    /**
     * Gets all of the values in the heap.
     * 
     * @return an array of the values in the heap. This array
     *         parallels the arrays returned by getKeys() and
     *         getEntries().
     */
    public Object[] getValues() {
        Object[] r = new Object[m_size];
        for (int i = 0; i < m_size; i++) {
            r[i] = m_values[m_tree[i]];
        }
        return r;
    }

    /**
     * Resizes the values array.
     * 
     * @param size the new size for the values array.
     */
    protected void resizeValues(int size) {
        if (size == 0) {
            m_values = null;
            return;
        }
        Object[] newValues = new Object[size];
        if (m_values != null) {
            System.arraycopy(m_values, 0, newValues, 0, m_values.length);
        }
        m_values = newValues;
    }

    /**
     * Does nothing in this class.
     * 
     * @param entry ignored.
     */
    protected void removeValue(int entry) {
        m_values[entry] = null;
    }
}

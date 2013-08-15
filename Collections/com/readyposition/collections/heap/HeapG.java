package com.readyposition.collections.heap;

import java.lang.reflect.Array;

/**
 * HeapG provides an array oriented heap with Generic keys and
 * values. Keys and values are maintained in parallel arrays to reduce
 * the number of Object references.
 */
public class HeapG<K, V> extends AbstractHeapG<K> {
    /** The array of values that parallels the m_keys array of keys. */
    protected Object[] m_values;

    /** Constructs a heap with K keys and V values. */
    public HeapG() {}

    /**
     * Constructs a heap with K keys and V values.
     * 
     * @param initialCapacity the initial number of elements that can be held in the heap.
     */
    public HeapG(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs a heap with K keys and V values.
     * 
     * @param initialCapacity the initial number of elements that can be held in the heap.
     * @param growthFactor factor used to grow internal arrays when
     *                     the heap needs to grow (e.g. 2.0f means to
     *                     double array sizes).
     */
    public HeapG(int initialCapacity, float growthFactor) {
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
    public int insert(K key, V value) {
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
     * Retrieves all of the values in the heap.  This array is
     * parallel to the one returned by getEntries and getKeys in the
     * superclass.
     *
     * @return an array of values.
     */
    public Object[] getValues() {
    	Object[] dst = new Object[m_size];
    	for (int i = 0; i < m_size; i++) {
    		dst[i] = m_values[m_tree[i]];
    	}
    	return dst;
    }

    /**
     * Retrieves all of the values in the heap.  This array is
     * parallel to the one returned by getEntries and getKeys in the
     * superclass.
     *
     * @param dst the array into which the values are to be stored, if
     *	          it is big enough; otherwise, a new array of the same
     *	          runtime type is allocated for this purpose.  If the
     *	          array is longer than needed then the extra elements
     *	          are not touched.
     * @return an array of values.
     */
    @SuppressWarnings("unchecked")
    public V[] getValues(V[] dst) {
        if (dst.length < m_size) {
            dst = (V[])Array.newInstance(dst.getClass()
            		.getComponentType(), m_size);
        }
    	for (int i = 0; i < m_size; i++) {
    		dst[i] = (V)m_values[m_tree[i]];
    	}
        return dst;
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

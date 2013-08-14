// _Warning_

package com.readyposition.collections.hash;

import java.util.Arrays;

import com.readyposition.collections.common.*;

/**
 * HashMapG is an efficient array oriented hash map
 * with Generic keys and values.
 */
public class HashMapG<K, V>
    extends AbstractHashG<K>
    implements MapG<K, V>
{
    /** Array holding the values to be associated with keys. */
    protected Object[] m_values;

    /**
     * Constructs an empty HashMapG with default
     * capacity and the default load factor
     */
    public HashMapG() {}

    /**
     * Constructs an empty HashMapG with the specified initial
     * capacity and the default load factor.
     *
     * @param initialCapacity the initial capacity.
     */
    public HashMapG(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs an empty HashMapG with the specified initial
     * capacity and load factor.
     *
     * @param initialCapacity the initial capacity.
     * @param loadFactor the load factor.
     */
    public HashMapG(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Associates a key with a value in the table.  If the table previously
     * contained a mapping for this key, the old value is replaced.
     *
     * @param key key with which the value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the entry number for the key/value pair.
     */
    public int put(K key, V value) {
        int i = intern(key);
        m_values[i] = value;
        return i;
    }

    /**
     * Replaces the value for a specified entry.
     *
     * @param entry the entry to receive a new value.
     * @param value the new value for the entry.
     */
    public void putEntryValue(int entry, V value) {
        m_values[entry] = value;
    }

    /**
     * Retrieves the value associated with a specified key.
     *
     * @param key the key whose associated value we want to retrieve.
     * @return the value associated with the key.
     * @exception IndexOutOfBoundsException if the key is not found in the
     *  table.  To avoid the use of exceptions, call
     *  int getEntry(K key), check for a -1 result, and optionally
     *  call V getEntryValue(int entry).
     */
    @SuppressWarnings("unchecked")
    public V get(K key) {
        int i = getEntry(key);
        if (-1 == i) {
            throw new IndexOutOfBoundsException("No such key " + key);
        }
        return (V)m_values[i];
    }

    /**
     * Returns the value to which the specified key is mapped, or if the key
     * is not present in the table, a specified missing value is returned.
     * 
     * @param key the key whose associated value is to be returned.
     * @param missingValue the value to return if the key is not in the map.
     * @return the value to which the specified key is mapped
     */
    @SuppressWarnings("unchecked")
    public V get(K key, V missingValue) {
        int i = getEntry(key);
        return (i == -1) ? missingValue : (V)m_values[i];
    }

    /**
     * Retrieves the value for a specified entry.
     *
     * @param entry the entry whose value we want to retrieve.
     * @return the value of the specified entry.
     */
    @SuppressWarnings("unchecked")
    public V getEntryValue(int entry) {
        return (V)m_values[entry];
    }

    /**
     * Retrieves all of the values in the table.  This array is parallel
     * to the one returned by getEntries and getKeys in the superclass.
     *
     * @return an array of values.
     */
    public Object[] getValues() {
        return (Object[])Arrays.copyOf(m_values, m_size);
    }

    /**
     * Retrieves all of the values in the table.  This array is parallel
     * to the one returned by getEntries and getKeys in the superclass.
     *
     * @param dst the array into which the values are to be stored, if
     *	it is big enough; otherwise, a new array of the same runtime
     *	type is allocated for this purpose.  If the array is longer
     *	than needed then the extra elements are not touched.
     * @return an array of values.
     */
    @SuppressWarnings("unchecked")
    public V[] getValues(V[] dst) {
        if (dst.length < m_size) {
        	return (V[])Arrays.copyOf(m_values, m_size,
        			dst.getClass());
        }
        System.arraycopy(m_values, 0, dst, 0, m_size);
        return dst;
    }

    @SuppressWarnings("unchecked")
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");

        for (int i = 0; i < m_values.length; i++) {
            V value = (V)m_values[i];
            K key = (K)m_keys[i];
            buf.append(key);
            buf.append("=");
            buf.append(value);
            if ( i < m_values.length-1) {
                buf.append(", ");
            }
        }

        buf.append("}");
        return buf.toString();
    }

    /**
     * Grow the values array.  All values are copied to the expanded array.
     *
     * @param size the new size of the values array.
     */
    @SuppressWarnings("unchecked")
    protected void growValues(int size) {
        V[] newValues = (V[])new Object[size];
        if (null != m_values) {
            System.arraycopy(m_values, 0, newValues, 0, m_values.length);
        }
        m_values = newValues;
    }

    /** Clears the values array. */
    protected void clearValues() {
        Arrays.fill(m_values, null);
    }

    /**
     * Clean up data for the value at a specified entry.
     *
     * @param entry the entry number of the value to clean up.
     */
    protected void removeValue(int entry) {
        m_values[entry] = null;
    }
}

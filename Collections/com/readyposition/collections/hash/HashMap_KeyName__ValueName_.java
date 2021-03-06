// _Warning_

package com.readyposition.collections.hash;

import java.lang.reflect.Array;
import java.util.Arrays;

import com.readyposition.collections.common.*;

/**
 * HashMap_KeyName__ValueName_ is an efficient array oriented hash map
 * with _KeyType_ keys and _ValueType_ values.
 */
public class HashMap_KeyName__ValueName_
    extends AbstractHash_KeyName_
    implements Map_KeyName__ValueName_
{
    /** Array holding the values to be associated with keys. */
    protected _ValueType_[] m_values;

    /**
     * Constructs an empty HashMap_KeyName__ValueName_ with default
     * capacity and the default load factor
     */
    public HashMap_KeyName__ValueName_() {}

    /**
     * Constructs an empty HashMap_KeyName__ValueName_ with the
     * specified initial capacity and the default load factor.
     *
     * @param initialCapacity the initial capacity.
     */
    public HashMap_KeyName__ValueName_(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs an empty HashMap_KeyName__ValueName_ with the
     * specified initial capacity and load factor.
     *
     * @param initialCapacity the initial capacity.
     * @param loadFactor the load factor.
     */
    public HashMap_KeyName__ValueName_(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Associates a key with a value in the table.  If the table
     * previously contained a mapping for this key, the old value is
     * replaced.
     *
     * @param key key with which the value is to be associated.
     * @param value the value to be associated with the specified key.
     * @return the entry number for the key/value pair.
     */
    public int put(_KeyType_ key, _ValueType_ value) {
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
    public void putEntryValue(int entry, _ValueType_ value) {
        m_values[entry] = value;
    }

    /**
     * Retrieves the value associated with a specified key.
     *
     * @param key the key whose associated value we want to retrieve.
     * @return the value associated with the key.
     * @exception IndexOutOfBoundsException if the key is not found in
     *  the table.  To avoid the use of exceptions, call int
     *  getEntry(_KeyType_ key), check for a -1 result, and optionally
     *  call _ValueType_ getEntryValue(int entry).
     */
    public _ValueType_ get(_KeyType_ key) {
        int i = getEntry(key);
        if (-1 == i) {
            throw new IndexOutOfBoundsException("No such key " + key);
        }
        return m_values[i];
    }

    /**
     * Returns the value to which the specified key is mapped, or if
     * the key is not present in the table, a specified missing value
     * is returned.
     * 
     * @param key the key whose associated value is to be returned.
     * @param missingValue the value to return if the key is not in the map.
     * @return the value to which the specified key is mapped
     */
    public _ValueType_ get(_KeyType_ key, _ValueType_ missingValue) {
        int i = getEntry(key);
        return (i == -1) ? missingValue : m_values[i];
    }

    /**
     * Retrieves the value for a specified entry.
     *
     * @param entry the entry whose value we want to retrieve.
     * @return the value of the specified entry.
     */
    public _ValueType_ getEntryValue(int entry) {
        return m_values[entry];
    }

    /**
     * Retrieves all of the values in the table.  This array is
     * parallel to the one returned by getEntries and getKeys in the
     * superclass.
     *
     * @return an array of values.
     */
    public _ValueType_[] getValues() {
        return getValues(null);
    }

    /**
     * Retrieves all of the values in the table.  This array is
     * parallel to the one returned by getEntries and getKeys in the
     * superclass.
     *
     * @param dst the array into which the values are to be stored, if
     *	it is big enough; otherwise, a new array of the same runtime
     *	type is allocated for this purpose.  If the array is longer
     *	than needed then the extra elements are not touched.
     * @return an array of values.
     */
    public _ValueType_[] getValues(_ValueType_[] dst) {
        if (dst == null) {
            dst = new _ValueType_[m_size];
        } else if (dst.length < m_size) {
            dst = (_ValueType_[])Array.newInstance(
                dst.getClass().getComponentType(), m_size);
        }
        int count = 0;
        for (int bucket = 0; bucket < m_buckets.length; bucket++) {
            for (int i = m_buckets[bucket]; i != -1; i = m_next[i]) {
                dst[count++] = m_values[i];
            }
        }
        return dst;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");

        for (int i = 0; i < m_values.length; i++) {
            _ValueType_ value = m_values[i];
            _KeyType_ key = m_keys[i];
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
     * Grow the values array.  All values are copied to the expanded
     * array.
     *
     * @param size the new size of the values array.
     */
    protected void growValues(int size) {
        _ValueType_[] newValues = new _ValueType_[size];
        if (null != m_values) {
            System.arraycopy(m_values, 0, newValues, 0, m_values.length);
        }
        m_values = newValues;
    }

    /** Clears the values array. */
    protected void clearValues() {
        Arrays.fill(m_values, Types.get_ValueName_For(0));
    }

    /**
     * Clean up data for the value at a specified entry.
     *
     * @param entry the entry number of the value to clean up.
     */
    protected void removeValue(int entry) {
        m_values[entry] = Types.get_ValueName_For(0);
    }
}

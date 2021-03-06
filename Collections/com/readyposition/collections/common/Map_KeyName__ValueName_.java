// _Warning_ //

package com.readyposition.collections.common;

import com.readyposition.collections.itemizer.Itemizable;
import com.readyposition.collections.itemizer.Itemizer;

/** Interface for maps with _KeyType_ keys and _ValueType_ values */
public interface Map_KeyName__ValueName_ extends Itemizable {
    /** Removes all of the mappings from this map. */
    void clear();

    /**
     * Returns true if this map contains a mapping for the specified key.
     * 
     * @param key the key whose presence in this map is to be tested.
     * @return true if this map contains the specified key, false otherwise.
     */
    boolean containsKey(_KeyType_ key);

    /**
     * Gets the number of mappings in this map.
     * 
     * @return the number of mappings in this map.
     */
    int getSize();

    /**
     * Returns the value to which the specified key is mapped. An exception is
     * thrown if no mapping is present.
     * 
     * @param key the key whose associated value is to be returned.
     * @return the value to which the specified key is mapped
     * @exception IndexOutOfBoundsException if there is no mapping for
     *             the specified key in the map.  Call int
     *             getEntry(_KeyType_ key) and check for a result of
     *             -1 in order to avoid checking for this
     *             exception. If the result is not -1, then
     *             _ValueType_ getEntryValue(int entry) can be called
     *             with the result of int getEntry(_KeyType_ key) to
     *             efficiently retrieve the value.
     */
    _ValueType_ get(_KeyType_ key);

    /**
     * Returns the value to which the specified key is mapped, or if the key
     * is not present in the table, a specified missing value is returned.
     * 
     * @param key the key whose associated value is to be returned.
     * @param missingValue the value to return if the key is not in the map.
     * @return the value to which the specified key is mapped
     */
    _ValueType_ get(_KeyType_ key, _ValueType_ missingValue);

    /**
     * Retrieves the value for a specified entry.
     * 
     * @param entry the entry whose associated value is to be returned.
     * @return the value associated with the key for the specified entry.
     */
    _ValueType_ getEntryValue(int entry);

    /** Gets the key associated with an entry number. */
    _KeyType_ getEntryKey(int entry);

    /**
     * Retrieves all of the entries in the map.
     * 
     * @return an array of integer entries.
     */
    int[] getEntries();

    /**
     * Retrieves all of the entries in the map.
     * 
     * @param dst the arry into which the entries are to be stored, if
     *            it is big enough; otherwise, a new array will be
     *            allocated for this purpose. If the array is longer
     *            than needed, then the extra elements are not
     *            touched.
     * @return an array of integer entries.
     */
    int[] getEntries(int[] dst);

    /**
     * Retrieves all of the keys in the map.
     * 
     * @return an array of integer entries.
     */
    _KeyType_[] getKeys();

    /**
     * Retrieves all of the keys in the map.
     * 
     * @param dst the array into which the keys are to be stored, if
     *            it is big enough; otherwise, a new array of the same
     *            runtime type is allocated for this purpose. If the
     *            array is longer than needed then the extra elements
     *            are not touched.
     * @return an array of integer entries.
     */
    _KeyType_[] getKeys(_KeyType_[] dst);

    /**
     * Retrieves all of the values in the map.
     * 
     * @return an array of integer entries.
     */
    _ValueType_[] getValues();

    /**
     * Retrieves all of the values in the map.
     * 
     * @param dst the array into which the values are to be stored, if
     *            it is big enough; otherwise, a new array of the same
     *            runtime type is allocated for this purpose. If the
     *            array is longer than needed then the extra elements
     *            are not touched.
     * @return an array of integer entries.
     */
    _ValueType_[] getValues(_ValueType_[] dst);

    /**
     * Removes the mapping for a key from this map if it is present.
     * 
     * @param key the key of the mapping to be removed from this map
     *            if is is present.
     */
    void remove(_KeyType_ key);

    /**
     * Returns true if this map contains no key-value mappings.
     * 
     * @return true if this map contains no key-valuye mappings.
     */
    boolean isEmpty();

    /**
     * Associates the specified value with the specified key in this
     * map. If the map previously contained a mapping for the key, the
     * old value is replaced by the specified value.
     * 
     * @param key key with which the value is to be associated.
     * @param value value to be associated with the specified key.
     * @return the entry number for the mapping between the key and the value.
     */
    int put(_KeyType_ key, _ValueType_ value);

    /**
     * Returns the entry associated with a specified key.
     * 
     * @return the entry associated with a specified key.
     */
    int getEntry(_KeyType_ key);

    /** Returns an itemizer for traversing the entries in the map. */
    Itemizer itemizer();
}

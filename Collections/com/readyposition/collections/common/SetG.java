// _Warning_ //

package com.readyposition.collections.common;

import com.readyposition.collections.itemizer.Itemizable;
import com.readyposition.collections.itemizer.Itemizer;

/** Interface for a set of _KeyType_ keys. */
public interface SetG<K> extends Itemizable {
    /**
     * Adds the specified item to this set if it is not already present.
     * 
     * @param key key to be added to the set.
     * @return the entry number for the key in the set, whether or not the key
     *         was already in the set.
     */
    int add(K key);

    /**
     * Returns the entry associated with a specified key.
     * 
     * @return the entry associated with a specified key.
     */
    int getEntry(K key);

    /**
     * Retrieves an entry's key.
     * 
     * @param entry the entry whose key we want to retrieve.
     * @return the key associated with the entry.
     */
    K getEntryKey(int entry);

    /** Removes all of the elements from this set. */
    void clear();

    /**
     * Removes the specified element from this set if it is present.
     * 
     * @param key the key of the item to be removed from the set.
     */
    void remove(K key);

    /**
     * Returns true if this set contains the specified key.
     * 
     * @param key the key whose presence in this set is to be tested.
     * @return true if this set contains the specified key.
     */
    boolean containsKey(K key);

    /**
     * Gets the number of entries in this set.
     * 
     * @return the number of entries in this set.
     */
    int getSize();

    /**
     * Returns true if this set contains no entries.
     * 
     * @return true if this set contains no entries.
     */
    boolean isEmpty();

    /**
     * Retrieves all of the entries in the set.
     * 
     * @return an array of integer entries.
     */
    int[] getEntries();

    /**
     * Retrieves all of the entries in the set.
     * 
     * @param dst the array into which the entries are to be stored, if
     *            it is big enough; otherwise, a new array will be
     *            allocated for this purpose. If the array is longer
     *            than needed, then the extra elements at the end of
     *            the array will be initialized to -1;
     * @return an array of integer entries.
     */
    int[] getEntries(int[] dst);

    /**
     * Retrieves all of the keys in the set.
     * 
     * @return an array of integer entries.
     */
    Object[] getKeys();

    /**
     * Retrieves all of the keys in the set.
     * 
     * @param dst the array into which the keys are to be stored, if
     *            it is big enough; otherwise, a new array of the same
     *            runtime type is allocated for this purpose. If the
     *            array is longer than needed then the extra elements
     *            at the end of the array will be initialized to
     *            null. If dst is null, then an array of the
     *            appropriate size is returned.
     * @return an array of integer entries.
     */
    K[] getKeys(K[] dst);

    /** Returns an itemizer for traversing the entries in the map. */
    Itemizer itemizer();
}

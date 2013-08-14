// _Warning_

package com.readyposition.collections.hash;

import java.lang.reflect.Array;
import java.util.Arrays;

import com.readyposition.collections.comparator.*;
import com.readyposition.collections.common.*;

/**
 * AbstractHash_KeyName_ is an abstract superclass intended to
 * facilitate construction of efficient array oriented hash tables and
 * hash sets with _KeyType_ keys and arbitrarily typed values,
 * including primitive types. This class maintains keys, values, and
 * other internal information in parallel arrays. Subclasses are
 * responsible for maintaining storage and managing access to the
 * value array by implementing the growValues and removeValue abstract
 * methods.
 */
public abstract class AbstractHashG<K> extends AbstractHash {
    /** Array holding the keys. */
    protected Object[] m_keys;

    /** Comparator for keys. */
    protected EqG<K> m_eq = (EqG<K>)Comparators.GenericObjectAsc;

    /** Hasher for the keys */
    protected HashG<K> m_hash = (HashG<K>)Hashers.GenericObjectHash;

    /**
     * Constructs an empty AbstractHash_KeyName_ with default capacity and the
     * default load factor
     */
    protected AbstractHashG() {}
    
    /**
     * Constructs an empty AbstractHash_KeyName_ with the specified
     * inital capacity and the default load factor
     * 
     * @param initialCapacity the initial capacity.
     */
    protected AbstractHashG(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs an empty AbstractHash_KeyName_ with the specified
     * inital capacity and load factor.
     * 
     * @param initialCapacity the initial capacity.
     * @param loadFactor the load factor.
     */
    protected AbstractHashG(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /** Sets the comparator for keys. */
    public void setEq(EqG<K> eq) {
        if (eq == null) {
            throw new IllegalArgumentException("Null EqObject");
        }
        m_eq = eq;
    }

    /** Gets the comparator for keys. */
    public EqG<K> getEq() {
        return m_eq;
    }

    /** Gets the hasher for keys. */
    public HashG<K> getHash() {
        return m_hash;
    }

    /** Sets the hashing function for keys. */
    public void setHash(HashG<K> h) {
        if (h == null) {
            throw new IllegalArgumentException("Null HashObject");
        }
        m_hash = h;
    }

    /**
     * Retrieves all of the keys in the table. This array is parallel
     * to the one returned by getEntries in the superclass. Subclasses
     * should provide appropriately typed getValues methods that will
     * return arrays parallel to the one returned here.
     * 
     * @return an array of keys.
     */
    public Object[] getKeys() {
    	Object[] dst = new Object[m_size];
        int count = 0;
        for (int bucket = 0; bucket < m_buckets.length; bucket++)
            for (int i = m_buckets[bucket]; i != -1; i = m_next[i])
                dst[count++] = Types.unmaskNull(m_keys[i]);
        return dst;
    }

    /**
     * Retrieves all of the keys in the table. This array is parallel
     * to the one returned by getEntries in the superclass. Subclasses
     * should provide appropriately typed getValues methods that will
     * return arrays parallel to the one returned here.
     * 
     * @param dst the array into which the keys are to be stored, if
     *            it is big enough; otherwise, a new array of the same
     *            runtime type is allocated for this purpose. If the
     *            array is longer than needed then the extra elements
     *            are not touched.
     * @return an array of keys.
     */
    @SuppressWarnings("unchecked")
    public K[] getKeys(K[] dst) {
        if (dst.length < m_size) {
            dst = (K[])Array.newInstance(dst.getClass()
            		.getComponentType(), m_size);
        }
        int count = 0;
        for (int bucket = 0; bucket < m_buckets.length; bucket++)
            for (int i = m_buckets[bucket]; i != -1; i = m_next[i])
                dst[count++] = (K)Types.unmaskNull(m_keys[i]);
        return dst;
    }

    /**
     * Searches for a key's entry in the table.
     * 
     * @param key the key to look for in the table.
     * @return the entry number of the key or -1 if the key is not in
     *         the table.
     */
    @SuppressWarnings("unchecked")
    public int getEntry(K key) {
        key = (K)Types.maskNull(key);
        int bucket = computeBucket(key);
        for (int i = m_buckets[bucket]; -1 != i; i = m_next[i]) {
            if (m_eq.equals(key, (K)m_keys[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Retrieves an entry's key.
     * 
     * @param entry the entry whose key we want to retrieve.
     * @return the key associated with the entry.
     */
    @SuppressWarnings("unchecked")
    public K getEntryKey(int entry) {
        return (K)Types.unmaskNull(m_keys[entry]);
    }

    /**
     * Returns true if this table contains a mapping for the specified key.
     * 
     * @param key the key whose presence in this table is to be tested.
     * @return true if this table contains the specified key, false otherwise.
     */
    public boolean containsKey(K key) {
        return -1 != getEntry(key);
    }

    /**
     * Removes any mapping for the specified key.
     * 
     * @param key the key whose mapping is to be removed from the table.
     */
    @SuppressWarnings("unchecked")
    public void remove(K key) {
        m_modCount++;
        key = (K)Types.maskNull(key);
        int bucket = computeBucket(key);
        int prev = -1;
        for (int i = m_buckets[bucket]; -1 != i; prev = i, i = m_next[i]) {
            if (!m_eq.equals(key, (K)m_keys[i])) {
                continue;
            }

            int n = m_next[i];
            if (-1 == prev) {
                // Head of list
                m_buckets[bucket] = n;
            } else {
                // Not the head of the list
                m_next[prev] = n;
            }

            m_next[i] = m_firstFree;
            m_firstFree = i;
            m_size--;
            removeValue(i);
            return;
        }
    }

    /**
     * Removes an entry from the table.
     * 
     * @param entry the entry to remove from the table.
     */
    @SuppressWarnings("unchecked")
    public void removeEntry(int entry) {
        remove((K)Types.unmaskNull(m_keys[entry]));
    }
    
    /**
     * Clean up data for the value at a specified entry.
     *
     * @param entry the entry number of the value to clean up.
     */
    protected void removeKey(int entry) {
        m_keys[entry] = null;
    }

    /**
     * Grow the keys array. All keys are copied to the expanded array.
     * 
     * @param size the new size of the keys array.
     */
    protected void growKeys(int size) {
        Object[] newKeys = new Object[size];
        if (m_keys != null) {
            System.arraycopy(m_keys, 0, newKeys, 0, m_keys.length);
        }
        m_keys = newKeys;
    }

    /** Clears the keys array. */
    protected void clearKeys() {
        Arrays.fill(m_keys, null);
    }

    /**
     * Interns a key in the table. If the key is already interned in the table,
     * then the existing entry is retrieved.
     * 
     * @param key the key to intern in the table.
     * @return the entry in which the key is interned.
     */
    @SuppressWarnings("unchecked")
    protected int intern(K key) {
        m_modCount++;
        key = (K)Types.maskNull(key);
        int bucket = computeBucket(key);
        for (int i = m_buckets[bucket]; -1 != i; i = m_next[i]) {
            if (m_eq.equals(key, (K)m_keys[i])) {
                // This entry already exists
                return i;
            }
        }

        // If we're here we are adding a new entry

        if (m_size >= m_threshold) {
            // Time to increase the number of buckets
            rehash();
            bucket = computeBucket(key);
        }

        int entry;
        if (-1 != m_firstFree) {
            // There are entries on the free list to use
            entry = m_firstFree;
            m_firstFree = m_next[entry];
        } else
            // No free entries - append new element
            entry = m_nextUnused++;
        m_keys[entry] = key;
        m_next[entry] = m_buckets[bucket];

        // Complete the insertion of new entry into the bucket's list
        m_buckets[bucket] = entry;
        m_size++;

        return entry;
    }

    /**
     * Computes the hash bucket for a key.
     * 
     * @param key the key to map to a bucket.
     * @return the bucket into which the key hashes.
     */
    private int computeBucket(K key) {
        int code = m_hash.code(key) & Integer.MAX_VALUE;
        int bucket = code % m_buckets.length;
        return bucket;
    }

    /**
     * Grows the hash table and reassigns all of the entries to new
     * buckets.
     */
    @SuppressWarnings("unchecked")
    private void rehash() {
        int oldCapacity = m_buckets.length;
        if (oldCapacity == MAX_CAPACITY) {
            m_threshold = Integer.MAX_VALUE;
            // We won't rehash. We'll just have to live with longer queues.
            return;
        }

        int newCapacity = oldCapacity * 2;

        m_threshold = (int) (newCapacity * m_loadFactor);

        int[] oldBuckets = m_buckets;
        Object[] oldKeys = m_keys;
        int[] oldIndex = m_next;
        m_buckets = new int[newCapacity];
        m_keys = new Object[m_threshold];
        m_next = new int[m_threshold];
        System.arraycopy(oldKeys, 0, m_keys, 0, oldKeys.length);
        System.arraycopy(oldIndex, 0, m_next, 0, oldIndex.length);
        growValues(m_threshold);

        for (int i = 0; i < newCapacity; i++) {
            m_buckets[i] = -1;
        }

        for (int i = 0; i < oldCapacity; i++) {
            int index = oldBuckets[i];
            while (-1 != index) {
                int bucket = computeBucket((K)oldKeys[index]);
                int n = oldIndex[index];
                m_next[index] = m_buckets[bucket];
                m_buckets[bucket] = index;
                index = n;
            }
        }
    }
}

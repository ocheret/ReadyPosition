package com.readyposition.collections.hash;

import com.readyposition.collections.common.*;

/**
 * HashSet_KeyName_ is an efficient array oriented hash set with
 * _KeyType_ keys
 */
public class HashSet_KeyName_
    extends AbstractHash_KeyName_
    implements Set_KeyName_
{
    /**
     * Constructs an empty _KeyName_HashSet with default capacity and
     * the default load factor
     */
    public HashSet_KeyName_() {}

    /**
     * Constructs an empty HashSet_KeyName_ with the specified inital
     * capacity and the default load factor
     * 
     * @param initialCapacity the initial capacity.
     */
    public HashSet_KeyName_(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs an empty HashSet_KeyName_ with the specified inital
     * capacity and load factor.
     * 
     * @param initialCapacity the initial capacity.
     * @param loadFactor the load factor.
     */
    public HashSet_KeyName_(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Adds a key to the set. If the table previously contained a
     * mapping for this key, no new entry is added.
     * 
     * @param key key to be added to the set.
     * @return the entry number for the key/value pair.
     */
    public int add(_KeyType_ key) {
        return intern(key);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");

        for (int i = 0; i < m_keys.length; i++) {
            _KeyType_ key = m_keys[i];
            buf.append(key);
            if (i < m_keys.length - 1) {
                buf.append(", ");
            }
        }

        buf.append("]");
        return buf.toString();
    }

    // Required by superclass but doesn't need to do anything for a set
    protected void growValues(int size) {}

    // Required by superclass but doesn't need to do anything for a set
    protected void clearValues() {};

    // Required by superclass but doesn't need to do anything for a set
    protected void removeValue(int entry) {}
}

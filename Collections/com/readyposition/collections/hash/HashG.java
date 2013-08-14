package com.readyposition.collections.hash;

/** Interface that allows hashing of a Generic value. */
public interface HashG<K> {
    /**
     * Returns a hash code for an object of Generic type
     * 
     * @param v the value for which a hash is desired
     * @return a hash code for the given value (see Object.hashCode
     *         for a definition of hash codes)
     */
    int code(K v);
}

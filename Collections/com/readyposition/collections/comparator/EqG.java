package com.readyposition.collections.comparator;

/** Interface that tests for equivalence of two values of Generic type. */
public interface EqG<T> {
    /**
     * Tests two values of Generic type  for equivalence. This is an
     * equivalence test and not an equality test.
     * 
     * @param a the first value to be compared.
     * @param b the second value to be compared.
     * @return true if the first value is equivalent to the second,
     *         false otherwise
     */
    boolean equals(T a, T b);
}

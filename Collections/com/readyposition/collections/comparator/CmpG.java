package com.readyposition.collections.comparator;

/** Interface that compares two values of Generic type. */
public interface CmpG<T> {
    /**
     * Compares two values of Generic type.
     * 
     * @param a the first value to be compared.
     * @param b the second value to be compared.
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than
     *         the second, respectively.
     */
    int compare(T a, T b);
}

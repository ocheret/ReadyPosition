package com.readyposition.collections.sort;

/** Utilities to aid with permutations, primarily for sorting activities. */
public class Permutations {
    /**
     * Generate an ascending sequence of integers.
     * 
     * @param count the number of elements in the sequence.
     * @return an array containing the sequence of integers from 0 to count-1
     */
    public static int[] sequence(int count) {
        int[] seq = new int[count];
        for (int i = 1; i < count; i++) {
            seq[i] = i;
        }
        return seq;
    }

    /**
     * Generate an ascending sequence of integers.
     * 
     * @param start the first number in the sequence.
     * @param end one past the last number in the sequence.
     * @return an array containing the sequence of integers from start to end-1
     */
    public static int[] sequence(int start, int end) {
        int len = end - start;
        int[] seq = new int[len];
        for (int i = 0; i < len; i++) {
            seq[i] = start + i;
        }
        return seq;
    }

    /**
     * Returns an inverse permutation.
     * 
     * @param permutation the permutation to be inverted.
     * @return an inverse permutation of the input permutation.
     */
    public static int[] inverse(int[] permutation) {
        int len = permutation.length;
        int[] rev = new int[len];
        for (int i = 0; i < len; i++) {
            rev[permutation[i]] = i;
        }
        return rev;
    }
}

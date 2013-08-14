// _Warning_

package com.readyposition.collections.sort;

import com.readyposition.collections.comparator.*;
import com.readyposition.collections.common.*;

/** Utilities to aid with permutations, primarily for sorting activities. */
public class PermutationSort_ValueName_ extends Permutations
{
    /**
     * Produces a permutation array that indexes a supplied array of
     * values in order according to a supplied comparator.
     * 
     * @param values an array of values in arbitrary order.
     * @param cmp a comparator that will be used to compare the
     *            values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final _ValueType_[] values,
                              final Cmp_ValueName_ cmp)
    {
        int[] permutation = sequence(values.length);
        SortInt.quick(permutation, new CmpInt() {
                public int compare(int a, int b) {
                    return cmp.compare(values[a], values[b]);
                }
            });
        return permutation;
    }

    /**
     * Produces a permutation array that indexes a subrange of an
     * array of values in order according to a supplied comparator.
     * 
     * @param values an array of values in arbitrary order.
     * @param start the first element of the array to sort.
     * @param end one past the last element of the array to sort.
     * @param cmp a comparator that will be used to compare the
     *            values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final _ValueType_[] values, int start, int end,
            final Cmp_ValueName_ cmp) {
        int[] permutation = sequence(start, end);
        SortInt.quick(permutation, new CmpInt() {
                public int compare(int a, int b) {
                    return cmp.compare(values[a], values[b]);
                }
            });
        return permutation;
    }
}

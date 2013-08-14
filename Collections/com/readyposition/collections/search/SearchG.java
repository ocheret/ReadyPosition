package com.readyposition.collections.search;

import com.readyposition.collections.comparator.CmpG;
import com.readyposition.collections.comparator.Comparators;

/** Class for performing binary searches on Generic arrays. */
public class SearchG
{
    /**
     * Performs a binary search of an array for a specified value
     * using a default comparator.
     * 
     * @param values the array of values to search
     * @param value the value to search for
     * @return the offset of the value in the vector or, if the value is not,
     *         found, the negative of the index of the insertion point (suitable
     *         for passing to the insert methods).
     */
    public static <T> int binary(T[] values, T value) {
        return binary(values, 0, values.length, value, Comparators.GenericObjectAsc);
    }

    /**
     * Performs a binary search of an array for a specified value
     * using a specified comparator.
     * 
     * @param values the array of values to search.
     * @param value the value to search for.
     * @param cmp the comparator to use when searching.
     * @return the offset of the value in the vector or, if the value
     *         is not, found, the negative of the index of the
     *         insertion point (suitable for passing to the insert
     *         methods).
     */
    public static <T> int binary(T[] values, T value, CmpG<T> cmp) {
        return binary(values, 0, values.length, value, cmp);
    }

    /**
     * Performs a binary search of a span of an array for a specified
     * value using a default comparator.
     * 
     * @param values the array of values to search
     * @param offset the start of the span in which to search
     * @param length the length of the span to search
     * @param value the value to search for
     * @return the offset of the value in the vector or, if the value
     *         is not, found, the negative of the index of the
     *         insertion point (suitable for passing to the insert
     *         methods).
     */
    public static <T> int binary(T[] values, int offset, int length, T value) {
        return binary(values, offset, length, value, Comparators.GenericObjectAsc);
    }

    /**
     * Performs a binary search of a span of an array for a specified value
     * using a specified comparator.
     * 
     * @param values the array of values to search.
     * @param offset the start of the span in which to search.
     * @param length the length of the span to search.
     * @param value the value to search for.
     * @param cmp the comparator to use when searching.
     * @return the offset of the value in the vector or, if the value
     *         is not, found, the negative of the index of the
     *         insertion point (suitable for passing to the insert
     *         methods).
     */
    public static <T> int binary(T[] values, int offset, int length, T value,
      CmpG<T> cmp)
    {
        int low = offset;
        int high = offset + length - 1;
        int mid = 0;
        while (low <= high) {
            mid = (low + high) >>> 1;
            int comparison = cmp.compare(value, values[mid]);
            if (comparison > 0) {
                // Value is higher than the middle value
                low = mid + 1;
            } else if (comparison < 0) {
                // Value is lower than the middle value
                high = mid - 1;
            } else {
                // Value is the same as the middle value
                return mid;
            }
        }
        return -(low + 1);
    }
}

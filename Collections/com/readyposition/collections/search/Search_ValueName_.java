// _Warning_

package com.readyposition.collections.search;

import com.readyposition.collections.comparator.Comparators;
import com.readyposition.collections.comparator.Cmp_ValueName_;
import com.readyposition.collections.common.*;

/** Class for performing binary searches on _ValueType_ arrays. */
public class Search_ValueName_
{
    /**
     * Performs a binary search of an array for a specified value
     * using a default comparator.
     * 
     * @param values the array of values to search.
     * @param value the value to search for.
     * @return the offset of the value in the vector or, if the value
     *         is not, found, the negative of the index of the
     *         insertion point (suitable for passing to the insert
     *         methods).
     */
    public static int binary(_ValueType_[] values, _ValueType_ value) {
        return binary(values, 0, values.length, value,
          Comparators._ValueName_Asc);
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
    public static int binary(_ValueType_[] values, _ValueType_ value,
      Cmp_ValueName_ cmp)
    {
        return binary(values, 0, values.length, value, cmp);
    }

    /**
     * Performs a binary search of a span of an array for a specified value
     * using a default comparator.
     * 
     * @param values the array of values to search.
     * @param offset the start of the span in which to search.
     * @param length the length of the span to search.
     * @param value the value to search for.
     * @return the offset of the value in the vector or, if the value
     *         is not, found, the negative of the index of the
     *         insertion point (suitable for passing to the insert
     *         methods).
     */
    public static int binary(_ValueType_[] values, int offset, int length,
      _ValueType_ value)
    {
        return binary(values, offset, length, value,
                      Comparators._ValueName_Asc);
    }

    /**
     * Performs a binary search of a span of an array for a specified
     * value using a specified comparator.
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
    public static int binary(_ValueType_[] values, int offset, int length,
                             _ValueType_ value, Cmp_ValueName_ cmp)
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

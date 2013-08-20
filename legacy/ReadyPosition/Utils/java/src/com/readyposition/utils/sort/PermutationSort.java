/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.sort;

import com.readyposition.utils.comparator.BooleanCmp;
import com.readyposition.utils.comparator.CharCmp;
import com.readyposition.utils.comparator.ByteCmp;
import com.readyposition.utils.comparator.ShortCmp;
import com.readyposition.utils.comparator.IntCmp;
import com.readyposition.utils.comparator.LongCmp;
import com.readyposition.utils.comparator.FloatCmp;
import com.readyposition.utils.comparator.DoubleCmp;
import com.readyposition.utils.comparator.ObjectCmp;
import java.util.List;

/**
 * Sort module that produces sorted permutation arrays that represent the
 * sorted order of arrays of primitive types and objects.  For example,
 * an unsorted array of Strings
 *   String[] s = new String[] {"A", "C", "E", "D", "B"};
 * can be passed to PermutationSort.quick(s, Comparators.OBJECT_ASC)
 * to produce an int permutation array containing { 0, 4, 1, 3, 2 }.
 */
public class PermutationSort {
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
     * @param permutation the permutation, probably returned by one of the
     *  quick() methods in this class.
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

    /**
     * Produces a permutation array that indexes a supplied array of values
     * in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final boolean[] values, final BooleanCmp cmp) {
	int[] permutation = sequence(values.length);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a subrange of an array of
     * values in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param start the first element of the array to sort.
     * @param end one past the last element of the array to sort.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final boolean[] values, int start, int end,
      final BooleanCmp cmp)
    {
	int[] permutation = sequence(start, end);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a supplied array of values
     * in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final char[] values, final CharCmp cmp) {
	int[] permutation = sequence(values.length);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a subrange of an array of
     * values in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param start the first element of the array to sort.
     * @param end one past the last element of the array to sort.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final char[] values, int start, int end,
      final CharCmp cmp)
    {
	int[] permutation = sequence(start, end);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a supplied array of values
     * in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final byte[] values, final ByteCmp cmp) {
	int[] permutation = sequence(values.length);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a subrange of an array of
     * values in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param start the first element of the array to sort.
     * @param end one past the last element of the array to sort.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final byte[] values, int start, int end,
      final ByteCmp cmp)
    {
	int[] permutation = sequence(start, end);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a supplied array of values
     * in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final short[] values, final ShortCmp cmp) {
	int[] permutation = sequence(values.length);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a subrange of an array of
     * values in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param start the first element of the array to sort.
     * @param end one past the last element of the array to sort.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final short[] values, int start, int end,
      final ShortCmp cmp)
    {
	int[] permutation = sequence(start, end);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a supplied array of values
     * in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final int[] values, final IntCmp cmp) {
	int[] permutation = sequence(values.length);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a subrange of an array of
     * values in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param start the first element of the array to sort.
     * @param end one past the last element of the array to sort.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final int[] values, int start, int end,
      final IntCmp cmp)
    {
	int[] permutation = sequence(start, end);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a supplied array of values
     * in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final long[] values, final LongCmp cmp) {
	int[] permutation = sequence(values.length);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a subrange of an array of
     * values in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param start the first element of the array to sort.
     * @param end one past the last element of the array to sort.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final long[] values, int start, int end,
      final LongCmp cmp)
    {
	int[] permutation = sequence(start, end);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a supplied array of values
     * in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final float[] values, final FloatCmp cmp) {
	int[] permutation = sequence(values.length);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a subrange of an array of
     * values in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param start the first element of the array to sort.
     * @param end one past the last element of the array to sort.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final float[] values, int start, int end,
      final FloatCmp cmp)
    {
	int[] permutation = sequence(start, end);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a supplied array of values
     * in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final double[] values, final DoubleCmp cmp) {
	int[] permutation = sequence(values.length);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a subrange of an array of
     * values in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param start the first element of the array to sort.
     * @param end one past the last element of the array to sort.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final double[] values, int start, int end,
      final DoubleCmp cmp)
    {
	int[] permutation = sequence(start, end);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a supplied array of values
     * in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final Object[] values, final ObjectCmp cmp) {
	int[] permutation = sequence(values.length);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a subrange of an array of
     * values in order according to a supplied comparator.
     *
     * @param values an array of values in arbitrary order.
     * @param start the first element of the array to sort.
     * @param end one past the last element of the array to sort.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final Object[] values, int start, int end,
      final ObjectCmp cmp)
    {
	int[] permutation = sequence(start, end);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values[a], values[b]);
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a supplied List of values
     * in order according to a supplied comparator.
     *
     * @param values a List of values in arbitrary order.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final List values, final ObjectCmp cmp) {
	int[] permutation = sequence(values.size());
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values.get(a), values.get(b));
		}
	    });
	return permutation;
    }

    /**
     * Produces a permutation array that indexes a subrange of an array of
     * values in order according to a supplied comparator.
     *
     * @param values a List of values in arbitrary order.
     * @param start the first element of the List to sort.
     * @param end one past the last element of the List to sort.
     * @param cmp a comparator that will be used to compare the values.
     * @return a permutation array that orders the values.
     */
    public static int[] quick(final List values, int start, int end,
      final ObjectCmp cmp)
    {
	int[] permutation = sequence(start, end);
	IntSort.quick(permutation, new IntCmp() {
		public int compare(int a, int b) {
		    return cmp.compare(values.get(a), values.get(b));
		}
	    });
	return permutation;
    }
}

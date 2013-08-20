/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.sort;

import java.util.List;

import com.readyposition.utils.comparator.ObjectCmp;

/**
 * Sort module for java.util.List implementations (e.g. ArrayList)
 * allowing insertion sorts and quicksorts using custom comparators.
 */
public class ListSort {
    /**
     * Performs a quicksort on a List using a comparator.
     *
     * @param a   the List to sort.
     * @param cmp the comparator to use.
     */
    public static void quick(List a, ObjectCmp cmp) {
        quick(a, 0, a.size(), cmp);
    }

    /**
     * Performs a quicksort on a subrange of a List using a comparator.
     *
     * @param a     the List to sort.
     * @param start the first element of the List to sort.
     * @param end   one past the last element of the List to sort.
     * @param cmp   the comparator to use.
     */
    public static void quick(List a, int start, int end, ObjectCmp cmp) {
        Object[] oa = a.toArray();
        ObjectSort.quick(oa, start, end, cmp);

        for (int i = start; i < end; i++)
            a.set(i, oa[i]);
    }

    /**
     * Performs an insertion sort on a List using a comparator.
     *
     * @param a   the List to sort.
     * @param cmp the comparator to use.
     */
    public static void insertion(List a, ObjectCmp cmp) {
        insertion(a, 0, a.size(), cmp);
    }

    /**
     * Performs an insertion sort on a subrange of a List using a comparator.
     *
     * @param a     the List to sort.
     * @param start the first element of the List to sort.
     * @param end   one past the last element of the List to sort.
     * @param cmp   the comparator to use.
     */
    public static void insertion(List a, int start, int end, ObjectCmp cmp) {
        Object[] oa = a.toArray();
        ObjectSort.insertion(oa, start, end, cmp);

        for (int i = start; i < end; i++)
            a.set(i, oa[i]);
    }
}

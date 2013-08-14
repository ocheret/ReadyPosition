// _Warning_

package com.readyposition.collections.sort;

import com.readyposition.collections.comparator.CmpG;

/**
 * Module that implements the classic quick sort algorithm for Generic
 * arrays and array slices using a custom comparator. The algorithm is
 * a direct translation of the one in the original paper as seen in
 * many implementations.
 */
public class SortG {
    /**
     * Performs a quicksort on an array using a comparator.
     * 
     * @param a the array to sort.
     * @param cmp the comparator to use.
     */
    public static <V> void quick(V[] a, CmpG<V> cmp) {
        quick(a, 0, a.length, cmp);
    }

    /**
     * Performs a quicksort on a subrange of an array using a comparator.
     * 
     * @param a the array to sort.
     * @param start the first element of the array to sort.
     * @param end one past the last element of the array to sort.
     * @param cmp the comparator to use.
     */
    public static <V> void quick(V[] a, int start, int end, CmpG<V> cmp) {
        for (;;) {
            int pa, pb, pc, pd, pl, pm, pn, r;
            boolean wereSwaps = false; // no swaps yet
            int length = end - start;

            if (length < 7) {
                insertion(a, start, end, cmp);
                return;
            }

            pa = pb = start + 1;
            pc = pd = end - 1;

            pm = start + length / 2;

            if (length > 7) {
                pl = start;
                pn = pc;
                if (length > 40) {
                    int d = length / 8;
                    pl = medianOf3(a, pl, pl + d, pl + 2 * d, cmp);
                    pm = medianOf3(a, pm - d, pm, pm + d, cmp);
                    pn = medianOf3(a, pn - 2 * d, pn - d, pn, cmp);
                }
                pm = medianOf3(a, pl, pm, pn, cmp);
            }

            V tmp = a[start];
            a[start] = a[pm];
            a[pm] = tmp; // swap

            for (;;) {
                while (pb <= pc && (r = cmp.compare(a[pb], a[start])) <= 0) {
                    if (r == 0) {
                        wereSwaps = true;
                        tmp = a[pa];
                        a[pa] = a[pb];
                        a[pb] = tmp;
                        pa++;
                    }
                    pb++;
                }

                while (pb <= pc && (r = cmp.compare(a[pc], a[start])) >= 0) {
                    if (r == 0) {
                        wereSwaps = true;
                        tmp = a[pc];
                        a[pc] = a[pd];
                        a[pd] = tmp;
                        pd--;
                    }
                    pc--;
                }

                if (pb > pc)
                    break;

                wereSwaps = true;
                tmp = a[pb];
                a[pb] = a[pc];
                a[pc] = tmp;
                pb++;
                pc--;
            }

            if (!wereSwaps) {
                insertion(a, start, end, cmp);
                return;
            }

            int i = pa - start;
            int j = pb - pa;
            r = (i < j) ? i : j; // min
            swapN(a, start, pb - r, r);
            pn = end;
            i = pd - pc;
            j = pn - pd - 1;
            r = (i < j) ? i : j; // min
            swapN(a, pb, pn - r, r);

            if ((r = pb - pa) > 1)
                quick(a, start, start + r, cmp);

            if ((r = pd - pc) <= 1)
                break;

            start = pn - r;
            end = pn;
        }
    }

    /**
     * Performs an insertion sort on an array using a comparator.
     * 
     * @param a the array to sort.
     * @param cmp the comparator to use.
     */
    public static <V> void insertion(V[] a, CmpG<V> cmp) {
        insertion(a, 0, a.length, cmp);
    }

    /**
     * Performs an insertion sort on a subrange of an array using a comparator.
     * 
     * @param a the array to sort.
     * @param start the first element of the array to sort.
     * @param end one past the last element of the array to sort.
     * @param cmp the comparator to use.
     */
    public static <V> void insertion(V[] a, int start, int end, CmpG<V> cmp) {
        for (int p = start + 1; p < end; p++) {
            int q = p - 1;
            while (q >= start && cmp.compare(a[q], a[p]) > 0)
                q--;
            q++;
            if (q < p) {
                V tmp = a[p];
                System.arraycopy(a, q, a, q + 1, p - q);
                a[q] = tmp;
            }
        }
    }

    /**
     * Determines the median of 3 elements of an array using a comparator.
     * 
     * @param a the array containing the values.
     * @param i the index of the first element to compare.
     * @param j the index of the second element to compare.
     * @param k the index of the third element to compare.
     * @param cmp the comparator to use.
     */
    private static <V> int medianOf3(V[] a, int i, int j, int k, CmpG<V> cmp) {
        if (cmp.compare(a[i], a[j]) < 0) {
            if (cmp.compare(a[j], a[k]) < 0)
                return j;
            if (cmp.compare(a[i], a[k]) < 0)
                return k;
            return i;
        }
        if (cmp.compare(a[j], a[k]) > 0)
            return j;
        if (cmp.compare(a[i], a[k]) < 0)
            return i;
        return k;
    }

    /**
     * Swaps the contents of two subranges of an array.
     * 
     * @param a the array containing the values.
     * @param p the starting index of the first range of values.
     * @param q the starting index of the second range of values.
     * @param count the number of elements from each range to swap.
     */
    private static <V> void swapN(V[] a, int p, int q, int count) {
        while (--count >= 0) {
            V tmp = a[p];
            a[p++] = a[q];
            a[q++] = tmp;
        }
    }
}

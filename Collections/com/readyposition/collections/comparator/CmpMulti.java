package com.readyposition.collections.comparator;

/**
 * This comparator provides a convenient mechanism to compose multiple
 * CmpObject objects to, for instance, sort an array of objects based
 * on multiple member comparisons. This is conceptually similar to
 * what can be done with an order by statement in SQL (e.g. order by
 * price asc, size desc).
 */
public class CmpMulti implements CmpG<Object> {
    /**
     * Constructor.
     * 
     * @param cmps an array of CmpG<Object> objects in the order that
     *            they should be applied to compare Objects.
     */
    public CmpMulti(CmpG<Object>[] cmps) {
        if (cmps == null) {
            throw new IllegalArgumentException("Comparator arrays is null");
        }
        m_cmps = cmps;
    }

    /**
     * Compares two Objects using elements of the comparators array in
     * order.  Comparisons are shortcutted as soon as a difference is
     * found.
     * 
     * @param a the first Object to be compared.
     * @param b the second Object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than
     *         the second.
     */
    public int compare(Object a, Object b) {
        int n = m_cmps.length;
        for (int i = 0; i < n; i++) {
            int s = m_cmps[i].compare(a, b);
            if (0 != s) {
                return s;
            }
        }
        return 0;
    }

    /** The array of comparators to apply in order when comparing objects. */
    protected CmpG<Object>[] m_cmps;
}

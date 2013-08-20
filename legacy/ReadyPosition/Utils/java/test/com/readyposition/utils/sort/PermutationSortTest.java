/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.sort;

import com.readyposition.utils.comparator.Comparators;
import com.readyposition.utils.comparator.ObjectCmp;
import com.readyposition.utils.comparator.ObjectMultiCmp;
import com.readyposition.utils.sort.ObjectSort;

import java.util.Random;

import junit.framework.TestCase;

public class PermutationSortTest extends TestCase {
    static class Values {
	public Values(int ival, double dval, String sval) {
	    m_ival = ival;
	    m_dval = dval;
	    m_sval = sval;
	}

	private int m_ival;
	private static ObjectCmp s_ivalCmp = new ObjectCmp() {
		public int compare(Object a, Object b) {
		    return Comparators.compare(
			((Values)a).m_ival, ((Values)b).m_ival);
		}
	    };
	private double m_dval;
	private static ObjectCmp s_dvalCmp = new ObjectCmp() {
		public int compare(Object a, Object b) {
		    return Comparators.compare(
			((Values)a).m_dval, ((Values)b).m_dval);
		}
	    };
	private String m_sval;
	private static ObjectCmp s_svalCmp = new ObjectCmp() {
		public int compare(Object a, Object b) {
		    return Comparators.compare(
			((Values)a).m_sval, ((Values)b).m_sval);
		}
	    };
	public String toString() {
	    return "(" + m_ival + ',' + m_dval + ',' + m_sval + ')';
	}
    }

    private Values[] m_values;

    public void setUp() {
	Random rnd = new Random();
	m_values = new Values[30];
	for (int i = 0; i < m_values.length; i++) {
	    m_values[i] = new Values(rnd.nextInt(10), (double)rnd.nextInt(10),
	      "X" + rnd.nextInt(10));
	}
    }

    public void tearDown() {
	m_values = null;
    }

    public void testEverything() {
	ObjectMultiCmp cmp = new ObjectMultiCmp(new ObjectCmp[] {
	    Values.s_ivalCmp, Values.s_dvalCmp, Values.s_svalCmp
	});
	int[] permutation = PermutationSort.quick(m_values, cmp);
	for (int i = 1; i < m_values.length; i++) {
	    Values a = m_values[permutation[i - 1]];
	    Values b = m_values[permutation[i]];
	    if (a.m_ival != b.m_ival) {
		assertTrue("Int order is wrong", a.m_ival < b.m_ival);
	    } else if (a.m_dval != b.m_dval) {
		assertTrue("Double order is wrong", a.m_dval < b.m_dval);
	    } else {
		assertTrue("String order is wrong",
		  a.m_sval.compareTo(b.m_sval) <= 0);
	    }
	}

	String[] s = new String[] { "A", "C", "E", "D", "B" };
	int[] expectedPermutation = new int[] { 0, 4, 1, 3, 2 };
	int[] expectedInverse = new int[] { 0, 2, 4, 3, 1 };
	permutation = PermutationSort.quick(s, Comparators.OBJECT_ASC);
	int[] inverse = PermutationSort.inverse(permutation);
	for (int i = 0; i < s.length; i++) {
	    assertEquals("Permutation is wrong.", expectedPermutation[i],
	      permutation[i]);
	    assertEquals("Inverse is wrong.", expectedInverse[i],
	      inverse[i]);
	}
    }
}

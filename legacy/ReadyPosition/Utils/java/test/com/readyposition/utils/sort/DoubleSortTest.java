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
import com.readyposition.utils.comparator.DoubleCmp;

import junit.framework.TestCase;

public class DoubleSortTest extends TestCase {
    private double[] m_subject;

    public void setUp() {
	// A list of numbers to sort where some numbers are close but
	// not equal to each other.
	m_subject = new double[]{
	    1.23,
	    4.57,
	    4.56,
	    4.55,
	    4.58
	  - 1.35,
	    Double.MAX_VALUE,
	    Double.MIN_VALUE
	};
    }

    public void tearDown() {
	m_subject = null;
    }

    public void testQuickAsc() {
	DoubleSort.quick(m_subject, Comparators.DOUBLE_ASC);
	for (int i = 1; i < m_subject.length; i++) {
	    assertTrue("Result not in ascending order",
	      m_subject[i - 1] <= m_subject[i]);
	}
    }

    public void testQuickDesc() {
	DoubleSort.quick(m_subject, Comparators.DOUBLE_DESC);
	for (int i = 1; i < m_subject.length; i++) {
	    assertTrue("Result not in descending order",
	      m_subject[i - 1] >= m_subject[i]);
	}
    }

    public void testCustom() {
	DoubleSort.quick(m_subject, new DoubleCmp() {
	    // Only compare to 1 decimal place.
	    public int compare(double a, double b) {
		int ia = (int)(a * 10.0);
		int ib = (int)(b * 10.0);
		return (ia == ib) ? 0 : ((ia > ib) ? 1 : -1);
	    }
	});
	for (int i = 1; i < m_subject.length; i++) {
	    if (m_subject[i - 1] > m_subject[i]) {
		int ia = (int)(m_subject[i - 1] * 10.0);
		int ib = (int)(m_subject[i] * 10.0);
		assertTrue("Result not in ascending order", ia == ib);
	    }
	}
    }
}

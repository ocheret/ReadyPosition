/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils;

import junit.framework.TestCase;

public class Many2ManyTest extends TestCase
{
    private Many2Many m_m2m;

    public void setUp() {
	m_m2m = new Many2Many();
    }

    public void tearDown() {
	m_m2m = null;
    }

    public void testEverything() {
	// Make sure the initial size is 0
	assertEquals("Initial size", 0, m_m2m.getSize());

	// Establish working parameters
	int leftChunk = 20;
	int rightChunk = 30;
	int iterations = 1000;

	// Allocate arrays of working data
	Integer[] left = new Integer[leftChunk * iterations];
	for (int i = 0; i < left.length; i++) {
	    left[i] = new Integer(i);
	}
	Integer[] right = new Integer[rightChunk * iterations];
	for (int i = 0; i < right.length; i++) {
	    right[i] = new Integer(i);
	}

	// Make an initial set of associations
	for (int i = 0; i < iterations; i++) {
	    for (int l = 0; l < leftChunk; l++) {
		int ll = leftChunk * i + l;
		for (int r = 0; r < rightChunk; r++) {
		    int rr = rightChunk * i + r;
		    if (l == 0 && r == 0) {
			// Let's store some userData in this case
			m_m2m.associate(left[ll], right[rr],
			  left[ll] + "," + right[rr]);
		    } else {
			m_m2m.associate(left[ll], right[rr]);
		    }
		}
	    }
	}

	// Make sure the correct numbers of lefts and rights are present.
	assertEquals("Unexpected left count",
	  (iterations * leftChunk), m_m2m.getLeftCount());
	assertEquals("Unexpected right count",
	  (iterations * rightChunk), m_m2m.getRightCount());

	// Make sure we can extract lefts using a precisely sized
	// destination array.
	Integer[] ia = new Integer[m_m2m.getLeftCount()];
	Integer[] tmp = (Integer[])m_m2m.getAllLefts(ia);
	assertSame("A new array was made instead of using one supplied",
	  ia, tmp);

	// Make sure we can extract lefts using an oversized
	// destination array and that the extra elements are cleared.
	ia = new Integer[m_m2m.getLeftCount() + 2];
	ia[m_m2m.getLeftCount() + 1] = ia[m_m2m.getLeftCount()] =
	    new Integer(-1);
	tmp = (Integer[])m_m2m.getAllLefts(ia);
	assertSame("A new array was made instead of using oversized one",
	  ia, tmp);
	assertNull("First extra left element not cleared",
	  ia[m_m2m.getLeftCount()]);
	assertNull("Last extra left element not cleared",
	  ia[m_m2m.getLeftCount() + 1]);

	// Make sure we can extract lefts using an zero length
	// destination array prototype.
	ia = new Integer[0];
	tmp = (Integer[])m_m2m.getAllLefts(ia);
	assertNotSame("A new array wasn't made", ia, tmp);
	assertEquals("Returned length is wrong",
	  (iterations * leftChunk), tmp.length);

	// Make sure we can extract lefts using a null destination
	// array.
	Object[] oa = m_m2m.getAllLefts(null);
	assertEquals("Returned length from null argument is wrong",
	  (iterations * leftChunk), oa.length);

	// Make sure we can extract lefts using no destination array.
	oa = m_m2m.getAllLefts();
	assertEquals("Returned length with no arguments is wrong",
	  (iterations * leftChunk), oa.length);

	// Make sure we can extract rights using a precisely sized
	// destination array.
	ia = new Integer[m_m2m.getRightCount()];
	tmp = (Integer[])m_m2m.getAllRights(ia);
	assertSame("A new array was made instead of using one supplied",
	  ia, tmp);

	// Make sure we can extract rights using an oversized
	// destination array and that the extra elements are cleared.
	ia = new Integer[m_m2m.getRightCount() + 2];
	ia[m_m2m.getRightCount() + 1] = ia[m_m2m.getRightCount()] =
	    new Integer(-1);
	tmp = (Integer[])m_m2m.getAllRights(ia);
	assertSame("A new array was made instead of using oversized one",
	  ia, tmp);
	assertNull("First extra right element not cleared",
	  ia[m_m2m.getRightCount()]);
	assertNull("Last extra right element not cleared",
	  ia[m_m2m.getRightCount() + 1]);

	// Make sure we can extract rights using an zero length
	// destination array prototype.
	ia = new Integer[0];
	tmp = (Integer[])m_m2m.getAllRights(ia);
	assertNotSame("A new array wasn't made", ia, tmp);
	assertEquals("Returned length is wrong",
	  (iterations * rightChunk), tmp.length);

	// Make sure we can extract rights using a null destination
	// array.
	oa = m_m2m.getAllRights(null);
	assertEquals("Returned length from null argument is wrong",
	  (iterations * rightChunk), oa.length);

	// Make sure we can extract rights using no destination array.
	oa = m_m2m.getAllRights();
	assertEquals("Returned length with no arguments is wrong",
	  (iterations * rightChunk), oa.length);

	for (int i = 0; i < iterations; i++) {
	    for (int l = 0; l < leftChunk; l++) {
		int ll = leftChunk * i + l;

		// Make sure each left has the correct number of rights
		assertEquals("Wrong right count for left",
		  rightChunk, m_m2m.getRightCount(left[ll]));

		// Make sure we can get right entries with a precisely sized
		// destination array.
		int[] inta = new int[rightChunk];
		int[] intb = m_m2m.getRightEntries(left[ll], inta);
		assertSame("A new int array was made", inta, intb);
		// XXX - make this test into common code
		for (int k = 0; k < rightChunk; k++) {
		    Integer t = (Integer)m_m2m.getRight(intb[k]);
		    int v = t.intValue();
		    assertTrue("Unexpected right",
		      (v >= (i * rightChunk) && v < ((i + 1) * rightChunk)));
		}

		// Make sure we can get right entries with an oversized
		// destination array and that the extra elements are cleared.
		inta = new int[rightChunk + 2];
		inta[rightChunk] = inta[rightChunk + 1] = 0;
		intb = m_m2m.getRightEntries(left[ll], inta);
		assertSame("A new int[] was made despite oversized right",
		  inta, intb);
		assertEquals("First extra right entry not cleared",
		  -1, intb[rightChunk]);
		assertEquals("Last extra right entry not cleared",
		  -1, intb[rightChunk + 1]);
		for (int k = 0; k < rightChunk; k++) {
		    Integer t = (Integer)m_m2m.getRight(intb[k]);
		    int v = t.intValue();
		    assertTrue("Unexpected right",
		      (v >= (i * rightChunk) && v < ((i + 1) * rightChunk)));
		}

		// Make sure we can get right entries with a zero
		// sized destination array.
		inta = new int[0];
		intb = m_m2m.getRightEntries(left[ll], inta);
		assertNotSame("A new int array wasn't made", inta, intb);
		assertEquals("Returned int[] length is wrong",
		  rightChunk, intb.length);
		for (int k = 0; k < rightChunk; k++) {
		    Integer t = (Integer)m_m2m.getRight(intb[k]);
		    int v = t.intValue();
		    assertTrue("Unexpected right",
		      (v >= (i * rightChunk) && v < ((i + 1) * rightChunk)));
		}

		// Make sure we can get right entries with a null
		// destination array.
		intb = m_m2m.getRightEntries(left[ll], null);
		assertEquals("Returned int[] length from null arg is wrong",
		  rightChunk, intb.length);
		for (int k = 0; k < rightChunk; k++) {
		    Integer t = (Integer)m_m2m.getRight(intb[k]);
		    int v = t.intValue();
		    assertTrue("Unexpected right",
		      (v >= (i * rightChunk) && v < ((i + 1) * rightChunk)));
		}

		// Make sure we can get right entries with no
		// destination array.
		intb = m_m2m.getRightEntries(left[ll]);
		assertEquals("Returned int[] length with no args is wrong",
		  rightChunk, intb.length);
		for (int k = 0; k < rightChunk; k++) {
		    Integer t = (Integer)m_m2m.getRight(intb[k]);
		    int v = t.intValue();
		    assertTrue("Unexpected right",
		      (v >= (i * rightChunk) && v < ((i + 1) * rightChunk)));
		}

		// Make sure we can get rights with a precisely sized
		// destination array.
		ia = new Integer[rightChunk];
		tmp = (Integer[])m_m2m.getRights(left[ll], ia);
		assertSame("A new right array was made", ia, tmp);
		for (int k = 0; k < rightChunk; k++) {
		    int v = tmp[k].intValue();
		    assertTrue("Unexpected right",
		      (v >= (i * rightChunk) && v < ((i + 1) * rightChunk)));
		}

		// Make sure we can get rights with an oversized
		// destination array and that the extra elements are cleared.
		ia = new Integer[rightChunk + 2];
		ia[rightChunk] = ia[rightChunk + 1] = new Integer(-1);
		tmp = (Integer[])m_m2m.getRights(left[ll], ia);
		assertSame("A new array was made despite oversized right",
		  ia, tmp);
		assertNull("First extra right object not cleared",
		  tmp[rightChunk]);
		assertNull("Last extra right object not cleared",
		  tmp[rightChunk + 1]);
		for (int k = 0; k < rightChunk; k++) {
		    int v = tmp[k].intValue();
		    assertTrue("Unexpected right",
		      (v >= (i * rightChunk) && v < ((i + 1) * rightChunk)));
		}

		// Make sure we can get rights with a zero sized
		// destination array.
		ia = new Integer[0];
		tmp = (Integer[])m_m2m.getRights(left[ll], ia);
		assertNotSame("A new right array wasn't made", ia, tmp);
		assertEquals("Returned right length is wrong",
		  rightChunk, tmp.length);
		for (int k = 0; k < rightChunk; k++) {
		    int v = tmp[k].intValue();
		    assertTrue("Unexpected right",
		      (v >= (i * rightChunk) && v < ((i + 1) * rightChunk)));
		}

		// Make sure we can get rights with a null destination
		// array.
		oa = m_m2m.getRights(left[ll], null);
		assertEquals("Returned right length from null arg is wrong",
		  rightChunk, oa.length);
		for (int k = 0; k < rightChunk; k++) {
		    int v = ((Integer)oa[k]).intValue();
		    assertTrue("Unexpected right",
		      (v >= (i * rightChunk) && v < ((i + 1) * rightChunk)));
		}

		// Make sure we can get rights with no destination
		// array.
		oa = m_m2m.getRights(left[ll]);
		assertEquals("Returned right length with no args is wrong",
		  rightChunk, oa.length);
		for (int k = 0; k < rightChunk; k++) {
		    int v = ((Integer)oa[k]).intValue();
		    assertTrue("Unexpected right",
		      (v >= (i * rightChunk) && v < ((i + 1) * rightChunk)));
		}
	    }

	    for (int r = 0; r < rightChunk; r++) {
		int rr = rightChunk * i + r;

		// Make sure each left has the correct number of rights
		assertEquals("Wrong left count for right",
		  leftChunk, m_m2m.getLeftCount(right[rr]));

		// Make sure we can get left entries with a precisely sized
		// destination array.
		int[] inta = new int[leftChunk];
		int[] intb = m_m2m.getLeftEntries(right[rr], inta);
		assertSame("A new int array was made", inta, intb);
		for (int k = 0; k < leftChunk; k++) {
		    Integer t = (Integer)m_m2m.getLeft(intb[k]);
		    int v = t.intValue();
		    assertTrue("Unexpected left",
		      (v >= (i * leftChunk) && v < ((i + 1) * leftChunk)));
		}

		// Make sure we can get left entries with an oversized
		// destination array and that the extra elements are cleared.
		inta = new int[leftChunk + 2];
		inta[leftChunk] = inta[leftChunk + 1] = 0;
		intb = m_m2m.getLeftEntries(right[rr], inta);
		assertSame("A new int[] was made despite oversized left",
		  inta, intb);
		assertEquals("First extra left entry not cleared",
		  -1, intb[leftChunk]);
		assertEquals("Last extra left entry not cleared",
		  -1, intb[leftChunk + 1]);
		for (int k = 0; k < leftChunk; k++) {
		    Integer t = (Integer)m_m2m.getLeft(intb[k]);
		    int v = t.intValue();
		    assertTrue("Unexpected left",
		      (v >= (i * leftChunk) && v < ((i + 1) * leftChunk)));
		}

		// Make sure we can get left entries with a zero
		// sized destination array.
		inta = new int[0];
		intb = m_m2m.getLeftEntries(right[rr], inta);
		assertNotSame("A new int array wasn't made", inta, intb);
		assertEquals("Returned int[] length is wrong",
		  leftChunk, intb.length);
		for (int k = 0; k < leftChunk; k++) {
		    Integer t = (Integer)m_m2m.getLeft(intb[k]);
		    int v = t.intValue();
		    assertTrue("Unexpected left",
		      (v >= (i * leftChunk) && v < ((i + 1) * leftChunk)));
		}

		// Make sure we can get left entries with a null
		// destination array.
		intb = m_m2m.getLeftEntries(right[rr], null);
		assertEquals("Returned int[] length from null arg is wrong",
		  leftChunk, intb.length);
		for (int k = 0; k < leftChunk; k++) {
		    Integer t = (Integer)m_m2m.getLeft(intb[k]);
		    int v = t.intValue();
		    assertTrue("Unexpected left",
		      (v >= (i * leftChunk) && v < ((i + 1) * leftChunk)));
		}

		// Make sure we can get left entries with no
		// destination array.
		intb = m_m2m.getLeftEntries(right[rr]);
		assertEquals("Returned int[] length with no args is wrong",
		  leftChunk, intb.length);
		for (int k = 0; k < leftChunk; k++) {
		    Integer t = (Integer)m_m2m.getLeft(intb[k]);
		    int v = t.intValue();
		    assertTrue("Unexpected left",
		      (v >= (i * leftChunk) && v < ((i + 1) * leftChunk)));
		}

		// Make sure we can get lefts with a precisely sized
		// destination array.
		ia = new Integer[leftChunk];
		tmp = (Integer[])m_m2m.getLefts(right[rr], ia);
		assertSame("A new left array was made", ia, tmp);
		for (int k = 0; k < leftChunk; k++) {
		    int v = tmp[k].intValue();
		    assertTrue("Unexpected left " + v,
		      (v >= (i * leftChunk) && v < ((i + 1) * leftChunk)));
		}

		// Make sure we can get lefts with an oversized
		// destination array and that the extra elements are cleared.
		ia = new Integer[leftChunk + 2];
		ia[leftChunk] = ia[leftChunk + 1] = new Integer(-1);
		tmp = (Integer[])m_m2m.getLefts(right[rr], ia);
		assertSame("A new array was made despite oversized left",
		  ia, tmp);
		assertNull("First extra left object not cleared",
		  tmp[leftChunk]);
		assertNull("Last extra left object not cleared",
		  tmp[leftChunk + 1]);
		for (int k = 0; k < leftChunk; k++) {
		    int v = tmp[k].intValue();
		    assertTrue("Unexpected left",
		      (v >= (i * leftChunk) && v < ((i + 1) * leftChunk)));
		}

		// Make sure we can get lefts with a zero sized
		// destination array.
		ia = new Integer[0];
		tmp = (Integer[])m_m2m.getLefts(right[rr], ia);
		assertNotSame("A new left array wasn't made", ia, tmp);
		assertEquals("Returned left length is wrong",
		  leftChunk, tmp.length);
		for (int k = 0; k < leftChunk; k++) {
		    int v = tmp[k].intValue();
		    assertTrue("Unexpected left",
		      (v >= (i * leftChunk) && v < ((i + 1) * leftChunk)));
		}

		// Make sure we can get lefts with a null destination
		// array.
		oa = m_m2m.getLefts(right[rr], null);
		assertEquals("Returned left length from null arg is wrong",
		  leftChunk, oa.length);
		for (int k = 0; k < leftChunk; k++) {
		    int v = ((Integer)oa[k]).intValue();
		    assertTrue("Unexpected left",
		      (v >= (i * leftChunk) && v < ((i + 1) * leftChunk)));
		}

		// Make sure we can get lefts with no destination
		// array.
		oa = m_m2m.getLefts(right[rr]);
		assertEquals("Returned left length with no args is wrong",
		  leftChunk, oa.length);
		for (int k = 0; k < leftChunk; k++) {
		    int v = ((Integer)oa[k]).intValue();
		    assertTrue("Unexpected left",
		      (v >= (i * leftChunk) && v < ((i + 1) * leftChunk)));
		}
	    }

	    // Make sure each association we know about is there.
	    for (int l = 0; l < leftChunk; l++) {
		int ll = i * leftChunk + l;
		for (int r = 0; r < rightChunk; r++) {
		    int rr = i * rightChunk + r;
		    assertTrue("Missing association",
		      m_m2m.isAssociated(left[ll], right[rr]));
		    String userData;
		    if ((i & 1) == 0) {
			int entry = m_m2m.getEntry(left[ll], right[rr]);
			userData = (String)m_m2m.getUserData(entry);
		    } else {
			userData =
			    (String)m_m2m.getUserData(left[ll], right[rr]);
		    }
		    if (l == 0 && r == 0) {
			assertEquals("String userData mismatch",
			  (left[ll] + "," + right[rr]), userData);
		    } else {
			assertNull("Non-null userData found", userData);
		    }
		}
	    }
	}

	// Make sure new size is correct.
	assertEquals("New size",
	  leftChunk * rightChunk * iterations, m_m2m.getSize());

	// Test dissociation
	for (int i = 0; i < iterations; i++) {
	    for (int l = 0; l < leftChunk; l++) {
		int ll = i * leftChunk + l;
		for (int r = 0; r < rightChunk; r++) {
		    int rr = i * rightChunk + r;
		    if (l == 0 && r == 0) {
			if ((i & 1) == 0) {
			    m_m2m.dissociate(left[ll], right[rr]);
			} else {
			    m_m2m.dissociate(
				m_m2m.getEntry(left[ll], right[rr]));
			}
			assertFalse("Failed to dissociate",
			  m_m2m.isAssociated(left[ll], right[rr]));
		    } else {
			assertTrue("Bad side effect from dissociate",
			  m_m2m.isAssociated(left[ll], right[rr]));
		    }
		}
	    }

	    for (int l = 0; l < leftChunk; l++) {
		// Make sure each left has the correct number of rights
		int ll = i * leftChunk + l;
		if (l == 0) {
		    assertEquals("Wrong right count for dissociated left",
		      (rightChunk - 1), m_m2m.getRightCount(left[ll]));
		} else {
		    assertEquals("Wrong right count for left",
		      rightChunk, m_m2m.getRightCount(left[ll]));
		}
	    }
	    for (int r = 0; r < rightChunk; r++) {
		// Make sure each left has the correct number of rights
		int rr = i * rightChunk + r;
		if (r == 0) {
		    assertEquals("Wrong left count for dissociated right",
		      (leftChunk - 1), m_m2m.getLeftCount(right[rr]));
		} else {
		    assertEquals("Wrong left count for right",
		      leftChunk, m_m2m.getLeftCount(right[rr]));
		}
	    }
	}

	// Make sure new size is correct.
	assertEquals("New size after dissociations",
	  (leftChunk * rightChunk - 1) * iterations, m_m2m.getSize());

	// Now let's dissociate everything including things already
	// dissociated.
	for (int i = 0; i < iterations; i++) {
	    for (int l = 0; l < leftChunk; l++) {
		int ll = i * leftChunk + l;
		for (int r = 0; r < rightChunk; r++) {
		    int rr = i * rightChunk + r;
		    m_m2m.dissociate(left[ll], right[rr]);
		}
	    }
	}

	// Make sure new size is correct.
	assertEquals("New size after all dissociations", 0, m_m2m.getSize());
    }
}

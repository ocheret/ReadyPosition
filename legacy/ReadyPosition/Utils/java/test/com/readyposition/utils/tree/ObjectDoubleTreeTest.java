/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.tree;

import com.readyposition.utils.itemizer.ObjectDoubleItemizer;
import com.readyposition.utils.comparator.ObjectCmp;
import com.readyposition.utils.comparator.Comparators;

import junit.framework.TestCase;

public class ObjectDoubleTreeTest extends TestCase
{
    private ObjectDoubleTree m_tree;
    private int m_iterations = 10000;

    public void setUp() {
	// Create and populate a tree
	m_tree = new ObjectDoubleTree();
	setupValues();
    }

    public void tearDown() {
	m_tree = null;
    }

    private void setupValues() {
	for (int i = 0; i < m_iterations; i++) {
	    m_tree.put(new Integer(i), (double)i);
	}
    }

    public void testClear() {
	m_tree.clear();

	// Make sure the tree is empty.
	assertEquals("Cleared tree has non-zero size", m_tree.getSize(), 0);

	// Make sure array methods return empty arrays.
	Object[] keys = m_tree.getKeys();
	assertEquals("Cleared key array has non-zero length", keys.length, 0);
	double[] values = m_tree.getValues();
	assertEquals("Cleared value array has non-zero length",
	  values.length, 0);
	int[] entries = m_tree.getEntries();
	assertEquals("Cleared entries array has non-zero length",
	  entries.length, 0);

	// Make sure the other stuff still works.
	setupValues();
	testKeyPutAndGet();
	m_tree.clear();
	setupValues();
	testItemizerAndEntries();
	m_tree.clear();
	setupValues();
	testArrayMethods();
    }

    public void testDepth() {
	// Make sure the depth of the tree is no worse than the theoretical
	// tree depth of 2 log(n + 1).
	int maxDepth = (int)((2.0 *
	    (Math.log((double)m_tree.getSize() + 1.0) / Math.log(2.0))));
	assertTrue("Max depth exceeded.", m_tree.getDepth() <= maxDepth);
	m_tree.clear();
	assertEquals("Empty tree has depth.", 0, m_tree.getDepth());
    }

    public void testKeyPutAndGet() {
	// Make sure the size of the tree is correct
	assertEquals("Size problem", m_tree.getSize(), m_iterations);

	// Make sure the same values can be retrieved.
	for (int i = 0; i < m_iterations; i++) {
	    assertTrue("Bad value in tree",
	      ((double)i == m_tree.get(new Integer(i))));
	}
	// Replace even values with new values and remove odd values.
	for (int i = 0; i < m_iterations; i++) {
	    Integer key = new Integer(i);
	    if ((i & 1) == 0) {
		m_tree.put(key, ((double)(i * 2)));
	    } else {
		m_tree.remove(key);
	    }
	}

	// Make sure the size of the tree is correct
	assertEquals("Reduced size problem", m_tree.getSize(), m_iterations/2);

	// Make sure the new values can be retrieved and removed
	// values are gone.
	for (int i = 0; i < m_iterations; i++) {
	    Integer key = new Integer(i);
	    if ((i & 1) == 0) {
		// Test containment using containsKey.
		assertTrue("Positive containment failed",
		  m_tree.containsKey(key));

		// Test containment and content using get.
		assertTrue("Bad new value in tree",
		  (((double)(i * 2)) == m_tree.get(key)));
	    } else {
		// Test containment using containsKey.
		assertFalse("Negative containment failed",
		  m_tree.containsKey(key));
		try {
		    // Test containment and content using get.
		    m_tree.get(key);
		    fail("Entry wasn't removed as expected");
		} catch (IndexOutOfBoundsException e) {
		    // We hope to get here.
		}
	    }
	}
    }

    public void testItemizerAndEntries() {
	// Iterate over the tree and make sure the itemizer returns
	// the right values.
	ObjectDoubleItemizer it = m_tree.itemizer();
	while (it.hasNext()) {
	    Object key = it.key();
	    double value = it.value();
	    int entry = it.entry();
	    assertTrue("itemizer key not right",
	      (m_tree.getKey(entry) == key));
	    assertTrue("itemizer value not right",
	      (m_tree.getEntryValue(entry) == value));
	    it.next();
	}
    }

    public void testOrder() {
	Object[] keys = m_tree.getKeys();
	ObjectCmp cmp = m_tree.getCmp();
	for (int i = 1; i < keys.length; i++) {
	    assertTrue("Keys are not in order",
	      cmp.compare(keys[i-1], keys[i]) < 0);
	}
	ObjectDoubleItemizer it = m_tree.itemizer();
	for (int i = 0; it.hasNext(); i++) {
	    assertTrue("Keys aren't in the same order",
	      cmp.compare(keys[i], it.key()) == 0);
	    it.next();
	}
    }

    public void testArrayMethods() {
	// Get arrays of keys, values, and entries from the tree.
	Object[] keys = m_tree.getKeys();
	double[] values = m_tree.getValues();
	int[] entries = m_tree.getEntries();

	// Make sure the data is consistent
	for (int i = 0; i < m_iterations; i++) {
	    assertTrue("Key inconsistency",
	      (m_tree.get(keys[i]) == values[i]));
	    assertTrue("Value inconsistency",
	      (m_tree.getEntryValue(entries[i]) == values[i]));
	    assertTrue("Entry inconsistency",
	      (m_tree.getEntry(keys[i]) == entries[i]));
	}

	// Test the other getKeys() method that takes a destination
	Integer[] ikeys = new Integer[0];
	Integer[] akeys = (Integer[])m_tree.getKeys(ikeys);
	assertNotSame("getKeys didn't allocate array", akeys, ikeys);
	ikeys = new Integer[m_tree.getSize()];
	Integer[] bkeys = (Integer[])m_tree.getKeys(ikeys);
	assertSame("getKeys allocated an array", bkeys, ikeys);
	for (int i = 0; i < ikeys.length; i++) {
	    assertEquals("Keys don't match", ikeys[i], keys[i]);
	    assertEquals("Keys don't match", akeys[i], bkeys[i]);
	}
    }

    public void testNullKey() {
	// Make sure we can't find a null key when we haven't mapaped one.
	assertFalse("Non-existent null key found", m_tree.containsKey(null));

	// Put a null key in the table.
	m_tree.put(null, -1.0);

	// Make sure we think the null key is there.
	assertTrue("Null key not found", m_tree.containsKey(null));

	// Make sure we can retrieve the value for the null key.
	assertEquals("Null key's value not found", m_tree.get(null),
	  -1.0, 0.0);

	// Make sure we can remove the null key.
	m_tree.remove(null);

	// Make sure we can't find a null key when we haven't mapaped one.
	assertFalse("Non-existent null key found", m_tree.containsKey(null));

	// Make sure that getKeys() returns null for the null key
	m_tree.clear();
	m_tree.put(null, -1.0);
	Object[] keys = m_tree.getKeys();
	assertNull("Null key was lost", keys[0]);
	keys = m_tree.getKeys(new Object[m_tree.getSize()]);
	assertNull("Null key was lost", keys[0]);

	// Make sure the key is returned as null
	assertNull("Key is not null", m_tree.getKey(m_tree.getEntry(null)));

	// Make sure removing the null 'entry' works
	m_tree.removeEntry(m_tree.getEntry(null));
	assertEquals("Didn't remove null entry", m_tree.getSize(), 0);
    }

    public void testEq() {
	assertSame("Unexpected comparator",
	  m_tree.getCmp(), Comparators.OBJECT_ASC);

	m_tree.setCmp(Comparators.OBJECT_DESC);
	assertSame("Comparator not set",
	  m_tree.getCmp(), Comparators.OBJECT_DESC);

	Integer key = new Integer(-1);
	assertFalse("Comparator not working", m_tree.containsKey(key));

	m_tree.put(key, 123.456);
	assertTrue("Id put failed", m_tree.containsKey(key));

	assertEquals("It get failed", m_tree.get(key), 123.456, 0.0);
    }
}

/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.hash;

import com.readyposition.utils.comparator.Comparators;
import com.readyposition.utils.itemizer.ObjectDoubleItemizer;
import junit.framework.TestCase;

public class ObjectDoubleHashTest extends TestCase
{
    private ObjectDoubleHash m_hash;
    private int m_iterations = 10000;

    public void setUp() {
	// Create and populate a hash table.
	m_hash = new ObjectDoubleHash();
	setupValues();
    }

    public void tearDown() {
	m_hash = null;
    }

    private void setupValues() {
	for (int i = 0; i < m_iterations; i++) {
	    m_hash.put(new Integer(i), (double) i);
	}
    }

    public void testClear() {
	m_hash.clear();

	// Make sure the table is empty.
	assertEquals("Cleared table has non-zero size", m_hash.getSize(), 0);

	// Make sure array methods return empty arrays.
	Object[] keys = m_hash.getKeys();
	assertEquals("Cleared key array has non-zero length", keys.length, 0);
	double[] values = m_hash.getValues();
	assertEquals("Cleared value array has non-zero length",
	  values.length, 0);
	int[] entries = m_hash.getEntries();
	assertEquals("Cleared entries array has non-zero length",
	  entries.length, 0);

	// Make sure the other stuff still works.
	setupValues();
	testKeyPutAndGet();
	m_hash.clear();
	setupValues();
	testItemizerAndEntries();
	m_hash.clear();
	setupValues();
	testArrayMethods();
    }

    public void testKeyPutAndGet() {
	// Make sure the size of the table is correct
	assertEquals("Size problem", m_hash.getSize(), m_iterations);

	// Make sure the same values can be retrieved.
	for (int i = 0; i < m_iterations; i++) {
	    assertTrue("Bad value in hash table",
	      ((double) i == m_hash.get(new Integer(i))));
	}
	// Replace even values with new values and remove odd values.
	for (int i = 0; i < m_iterations; i++) {
	    if ((i & 1) == 0) {
		m_hash.put(new Integer(i), ((double) (i * 2)));
	    } else {
		m_hash.remove(new Integer(i));
	    }
	}

	// Make sure the size of the table is correct
	assertEquals("Reduced size problem",
	  m_hash.getSize(), m_iterations / 2);

	// Make sure the new values can be retrieved and removed
	// values are gone.
	for (int i = 0; i < m_iterations; i++) {
	    Integer key = new Integer(i);
	    if ((i & 1) == 0) {
		// Test containment using containsKey.
		assertTrue("Positive containment failed",
		  m_hash.containsKey(key));

		// Test containment and content using get.
		assertTrue("Bad new value in hash table",
		  (((double) (i * 2)) == m_hash.get(key)));
	    } else {
		// Test containment using containsKey.
		assertFalse("Negative containment failed",
		  m_hash.containsKey(key));
		try {
		    // Test containment and content using get.
		    m_hash.get(key);
		    fail("Entry wasn't removed as expected");
		} catch (IndexOutOfBoundsException e) {
		    // We hope to get here.
		}
	    }
	}
    }

    public void testItemizerAndEntries() {
	// Iterate over the table and make sure the itemizer returns
	// the right values.
	ObjectDoubleItemizer it = m_hash.itemizer();
	while (it.hasNext()) {
	    Object key = it.key();
	    double value = it.value();
	    int entry = it.entry();
	    assertTrue("itemizer key not right",
	      (m_hash.getKey(entry) == key));
	    assertTrue("itemizer value not right",
	      (m_hash.getEntryValue(entry) == value));
	    it.next();
	}
    }

    public void testArrayMethods() {
	// Get arrays of keys, values, and entries from the table.
	Object[] keys = m_hash.getKeys();
	double[] values = m_hash.getValues();
	int[] entries = m_hash.getEntries();

	// Make sure the data is consistent
	for (int i = 0; i < m_iterations; i++) {
	    assertTrue("Key inconsistency",
	      (m_hash.get(keys[i]) == values[i]));
	    assertTrue("Value inconsistency",
	      (m_hash.getEntryValue(entries[i]) == values[i]));
	    assertTrue("Entry inconsistency",
	      (m_hash.getEntry(keys[i]) == entries[i]));
	}

	// Test the other getKeys() method that takes a destination
	Integer[] ikeys = new Integer[0];
	Integer[] akeys = (Integer[])m_hash.getKeys(ikeys);
	assertNotSame("getKeys didn't allocate array", akeys, ikeys);
	ikeys = new Integer[m_hash.getSize()];
	Integer[] bkeys = (Integer[])m_hash.getKeys(ikeys);
	assertSame("getKeys allocated an array", bkeys, ikeys);
	for (int i = 0; i < ikeys.length; i++) {
	    assertEquals("Keys don't match", ikeys[i], keys[i]);
	    assertEquals("Keys don't match", akeys[i], bkeys[i]);
	}
    }

    public void testNullKey() {
	// Make sure we can't find a null key when we haven't mapaped one.
	assertFalse("Non-existent null key found", m_hash.containsKey(null));

	// Put a null key in the table.
	m_hash.put(null, -1.0);

	// Make sure we think the null key is there.
	assertTrue("Null key not found", m_hash.containsKey(null));

	// Make sure we can retrieve the value for the null key.
	assertEquals("Null key's value not found", m_hash.get(null),
	  -1.0, 0.0);

	// Make sure we can remove the null key.
	m_hash.remove(null);

	// Make sure we can't find a null key when we haven't mapaped one.
	assertFalse("Non-existent null key found", m_hash.containsKey(null));

	// Make sure that getKeys() returns null for the null key
	m_hash.clear();
	m_hash.put(null, -1.0);
	Object[] keys = m_hash.getKeys();
	assertNull("Null key was lost", keys[0]);
	keys = m_hash.getKeys(new Object[m_hash.getSize()]);
	assertNull("Null key was lost", keys[0]);

	// Make sure the key is returned as null
	assertNull("Key is not null", m_hash.getKey(m_hash.getEntry(null)));

	// Make sure removing the null 'entry' works
	m_hash.removeEntry(m_hash.getEntry(null));
	assertEquals("Didn't remove null entry", m_hash.getSize(), 0);
    }

    public void testEq() {
	assertSame("Unexpected comparator",
	  m_hash.getEq(), Comparators.OBJECT_EQ);

	m_hash.setEq(Comparators.OBJECT_ID_EQ);
	assertSame("Comparator not set",
	  m_hash.getEq(), Comparators.OBJECT_ID_EQ);

	Integer key = new Integer(0);
	assertFalse("Comparator not working", m_hash.containsKey(key));

	m_hash.put(key, 123.456);
	assertTrue("Id put failed", m_hash.containsKey(key));

	assertEquals("It get failed", m_hash.get(key), 123.456, 0.0);
    }
}

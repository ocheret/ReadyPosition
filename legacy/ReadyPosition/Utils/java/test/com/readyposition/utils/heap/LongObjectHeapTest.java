/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.heap;

import java.util.Random;
import java.util.Arrays;

import junit.framework.TestCase;

public class LongObjectHeapTest extends TestCase
{
    private LongObjectHeap m_heap;
    final private int m_iterations = 10000;
    private int testSize;
    // Used to keep track of MIN/MAX in the heap
    private long randomSeed = 0xcafe;
    private long testValues[];

    class MyLong {
	public long theLong;
	public MyLong(long x) {
	    theLong = x;
	}
	public long longValue() {
	    return theLong;
	}
	public void setValue(long x) {
	    theLong = x;
	}
    }

    public void setUp() {
	m_heap = new LongObjectHeap();
	setupValues();
    }

    public void tearDown() {
	m_heap = null;
    }

    private void setupValues() {
	// Use a fixed random seed so we get the same
	// sequence every test
	Random rand = new Random(randomSeed);
	testValues = new long[m_iterations];
	testSize = m_iterations;
	for (int i = 0; i < m_iterations; i++)
	    {
		long value = rand.nextLong();
		m_heap.insert(value, new MyLong(value));
		testValues[i] = value;
	    }
    }
    
    public void testClear() {
	m_heap.clear();

	// Make sure it's empty
	assertEquals("Cleared heap has non-zero size", m_heap.getSize(), 0);

	long[] keys = m_heap.getKeys();
	assertEquals("Cleared key array has non-zero length",
		     keys.length, 0);
	Object[] values = m_heap.getValues();
	assertEquals("Cleared value array has non-zero length",
	  values.length, 0);
	int[] entries = m_heap.getEntries();
	assertEquals("Cleared entries array has non-zero length",
		     entries.length, 0);

    }

    public void testKeyValueOrder() {
	int entry = -1;
	long key = -1L;
	MyLong value;
	// Make sure the size of the heap is correct
	assertEquals("Size problem", m_heap.getSize(), testSize);

	// Make sure the correct sequence is retrieved
	// Sort the testValues first
	Arrays.sort(testValues);
	for (int i = 0; i < testSize; i++) {
	    entry = m_heap.peek();
	    key = m_heap.getKey(entry);
	    
	    assertTrue("Bad sequence in heap, iter " + i +
		       " testValue " + testValues[i] + " key " + key,
		       testValues[i] == key);
	    value = (MyLong) m_heap.getValue(entry);
	    assertTrue("Bad key-value association",
		       key == value.longValue());
	    m_heap.remove(entry);
	    assertTrue("Bad heap size after remove",
		       i + m_heap.getSize() + 1 == testSize);
	}
    }
    // No need to test insert since it's taken care of by
    // setupValues() + testKeyValueOrder()
    public void testKeyValueRemove() {
	long key;
	// Make sure the size of the heap is correct
	assertEquals("Size problem", m_heap.getSize(), testSize);

	// Test removal at testSize/2, testSize/3,
	// testSize/5, testSize/7 testSize*2/3 positions
	key = m_heap.getKey(testSize / 2);
	m_heap.remove(testSize / 2);
	testValues = m_heap.getKeys();
	// Make sure the old key isn't in there
	for (int i = 0; i < testValues.length; i++)
	    {
		assertTrue("Old key still in key set after remove",
			   testValues[i] != key);
	    }
	testSize = testSize - 1;
	// Use testKeyValueOrder() to test the validity after remove
	testKeyValueOrder();

	// Reset and test
	setupValues();
	key = m_heap.getKey(testSize / 3);
	m_heap.remove(testSize / 3);
	testValues = m_heap.getKeys();
	// Make sure the old key isn't in there
	for (int i = 0; i < testValues.length; i++)
	    {
		assertTrue("Old key still in key set after remove",
			   testValues[i] != key);
	    }
	testSize = testSize - 1;
	// Use testKeyValueOrder() to test the validity after remove
	testKeyValueOrder();

	// Reset and test
	setupValues();
	key = m_heap.getKey(testSize / 5);
	m_heap.remove(testSize / 5);
	testValues = m_heap.getKeys();
	// Make sure the old key isn't in there
	for (int i = 0; i < testValues.length; i++)
	    {
		assertTrue("Old key still in key set after remove",
			   testValues[i] != key);
	    }
	testSize = testSize - 1;
	// Use testKeyValueOrder() to test the validity after remove
	testKeyValueOrder();

	// Reset and test
	setupValues();
	key = m_heap.getKey(testSize / 7);
	m_heap.remove(testSize / 7);
	testValues = m_heap.getKeys();
	// Make sure the old key isn't in there
	for (int i = 0; i < testValues.length; i++)
	    {
		assertTrue("Old key still in key set after remove",
			   testValues[i] != key);
	    }
	testSize = testSize - 1;
	// Use testKeyValueOrder() to test the validity after remove
	testKeyValueOrder();

	// Reset and test
	setupValues();
	key = m_heap.getKey((testSize / 3) * 2);
	m_heap.remove((testSize / 3) * 2);
	testValues = m_heap.getKeys();
	// Make sure the old key isn't in there
	for (int i = 0; i < testValues.length; i++)
	    {
		assertTrue("Old key still in key set after remove",
			   testValues[i] != key);
	    }
	testSize = testSize - 1;
	// Use testKeyValueOrder() to test the validity after remove
	testKeyValueOrder();
    }
    // No need to test insert since it's taken care of by
    // setupValues() + testKeyValueOrder()
    public void testKeyValueChange() {
	long key;
	MyLong value;
	// Make sure the size of the heap is correct
	assertEquals("Size problem", m_heap.getSize(), testSize);

	// Test removal at testSize/2, testSize/3,
	// testSize/5, testSize/7 testSize/1.5 positions
	key = m_heap.getKey(testSize / 2);
	value = (MyLong) m_heap.getValue(testSize / 2);
	value.setValue(60231023L);
	m_heap.resetKey(testSize / 2, 60231023L);
	for (int i = 0; i < testValues.length; i++)
	    {
		if (testValues[i] == key)
		    {
			testValues[i] = 60231023L;
		    }
	    }
	testKeyValueOrder();

	setupValues();
	key = m_heap.getKey(testSize / 3);
	value = (MyLong) m_heap.getValue(testSize / 3);
	value.setValue(60231023L);
	m_heap.resetKey(testSize / 3, 60231023L);
	for (int i = 0; i < testValues.length; i++)
	    {
		if (testValues[i] == key)
		    {
			testValues[i] = 60231023L;
		    }
	    }
	testKeyValueOrder();

	setupValues();
	key = m_heap.getKey(testSize / 5);
	value = (MyLong) m_heap.getValue(testSize / 5);
	value.setValue(60231023L);
	m_heap.resetKey(testSize / 5, 60231023L);
	for (int i = 0; i < testValues.length; i++)
	    {
		if (testValues[i] == key)
		    {
			testValues[i] = 60231023L;
		    }
	    }
	testKeyValueOrder();

	setupValues();
	key = m_heap.getKey(testSize / 7);
	value = (MyLong) m_heap.getValue(testSize / 7);
	value.setValue(60231023L);
	m_heap.resetKey(testSize / 7, 60231023L);
	for (int i = 0; i < testValues.length; i++)
	    {
		if (testValues[i] == key)
		    {
			testValues[i] = 60231023L;
		    }
	    }
	testKeyValueOrder();

	setupValues();
	key = m_heap.getKey((testSize / 3) * 2);
	value = (MyLong) m_heap.getValue((testSize / 3) * 2);
	value.setValue(60231023L);
	m_heap.resetKey((testSize / 3) * 2, 60231023L);
	for (int i = 0; i < testValues.length; i++)
	    {
		if (testValues[i] == key)
		    {
			testValues[i] = 60231023L;
		    }
	    }
	testKeyValueOrder();
    }
}

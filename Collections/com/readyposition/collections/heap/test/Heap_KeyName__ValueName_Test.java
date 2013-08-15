// _Warning_

package com.readyposition.collections.heap.test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.readyposition.collections.common.*;
import com.readyposition.collections.comparator.Comparators;
import com.readyposition.collections.heap.*;

/** Unit tests for heaps. */
public class Heap_KeyName__ValueName_Test
{
    /** This holds heaps we will be testing. */
    private Heap_KeyName__ValueName_ m_heap;
    
    /** Used to play with test size. */
    private int m_testSize;

    /** Array of keys to compare against what's in the heap. */
    private _KeyType_ m_testKeys[];
    
    /** Determines the size of the tests. */
    private final static int TEST_SIZE = Types.testSize_KeyName_();

    /** Used to keep track of MIN/MAX in the heap */
    private final static long RANDOM_SEED = 0xcafe;

    @Before
    public void setUp() {
        m_heap = new Heap_KeyName__ValueName_();
        setupValues();
    }

    @After
    public void tearDown() {
        m_heap = null;
    }

    private void setupValues() {
        // Use a fixed random seed so we get the same
        // sequence every test
        Random rand = new Random(RANDOM_SEED);
        m_testKeys = new _KeyType_[TEST_SIZE];
        m_testSize = TEST_SIZE;
        for (int i = 0; i < TEST_SIZE; i++) {
            _KeyType_ key = Types.get_KeyName_For(rand.nextInt());
            m_heap.insert(key, Types.get_ValueName_For(i));
            m_testKeys[i] = key;
        }
    }

    @Test
    public void testClear() {
        m_heap.clear();

        // Make sure it's empty
        assertEquals("Cleared heap has non-zero size", m_heap.getSize(), 0);

        _KeyType_[] keys = m_heap.getKeys();
        assertEquals("Cleared key array has non-zero length", keys.length, 0);
        _ValueType_[] values = m_heap.getValues();
        assertEquals("Cleared value array has non-zero length", values.length,
                     0);
        int[] entries = m_heap.getEntries();
        assertEquals("Cleared entries array has non-zero length",
                     entries.length, 0);
    }

    @Test
    public void testKeyValueOrder() {
        int entry = -1;
        _KeyType_ key = Types.get_KeyName_For(-1);
        // Make sure the size of the heap is correct
        assertEquals("Size problem", m_heap.getSize(), m_testSize);

        // Make sure the correct sequence is retrieved
        // Sort the testKeys first
        Arrays.sort(m_testKeys);
        for (int i = 0; i < m_testSize; i++) {
            entry = m_heap.peek();
            key = m_heap.getEntryKey(entry);

            assertTrue("Bad sequence in heap, iter " + i + " testValue "
                       + m_testKeys[i] + " key " + key,
                       Comparators._KeyName_Asc.equals(m_testKeys[i], key));
            m_heap.remove(entry);
            assertTrue("Bad heap size after remove",
                       i + m_heap.getSize() + 1 == m_testSize);
        }
    }

    // No need to test insert since it's taken care of by
    // setupValues() + testKeyValueOrder()
    @Test
    public void testKeyValueRemove() {
        _KeyType_ key;
        // Make sure the size of the heap is correct
        assertEquals("Size problem", m_heap.getSize(), m_testSize);

        // Test removal at m_testSize/2, m_testSize/3,
        // m_testSize/5, m_testSize/7 m_testSize*2/3 positions
        key = m_heap.getEntryKey(m_testSize / 2);
        m_heap.remove(m_testSize / 2);
        m_testKeys = m_heap.getKeys();
        // Make sure the old key isn't in there
        for (int i = 0; i < m_testKeys.length; i++) {
            assertTrue("Old key still in key set after remove",
                       m_testKeys[i] != key);
        }
        m_testSize = m_testSize - 1;
        // Use testKeyValueOrder() to test the validity after remove
        testKeyValueOrder();

        // Reset and test
        setupValues();
        key = m_heap.getEntryKey(m_testSize / 3);
        m_heap.remove(m_testSize / 3);
        m_testKeys = m_heap.getKeys();
        // Make sure the old key isn't in there
        for (int i = 0; i < m_testKeys.length; i++) {
            assertTrue("Old key still in key set after remove",
                       m_testKeys[i] != key);
        }
        m_testSize = m_testSize - 1;
        // Use testKeyValueOrder() to test the validity after remove
        testKeyValueOrder();

        // Reset and test
        setupValues();
        key = m_heap.getEntryKey(m_testSize / 5);
        m_heap.remove(m_testSize / 5);
        m_testKeys = m_heap.getKeys();
        // Make sure the old key isn't in there
        for (int i = 0; i < m_testKeys.length; i++) {
            assertTrue("Old key still in key set after remove",
                       m_testKeys[i] != key);
        }
        m_testSize = m_testSize - 1;
        // Use testKeyValueOrder() to test the validity after remove
        testKeyValueOrder();

        // Reset and test
        setupValues();
        key = m_heap.getEntryKey(m_testSize / 7);
        m_heap.remove(m_testSize / 7);
        m_testKeys = m_heap.getKeys();
        // Make sure the old key isn't in there
        for (int i = 0; i < m_testKeys.length; i++) {
            assertTrue("Old key still in key set after remove",
                       m_testKeys[i] != key);
        }
        m_testSize = m_testSize - 1;
        // Use testKeyValueOrder() to test the validity after remove
        testKeyValueOrder();

        // Reset and test
        setupValues();
        key = m_heap.getEntryKey((m_testSize / 3) * 2);
        m_heap.remove((m_testSize / 3) * 2);
        m_testKeys = m_heap.getKeys();
        // Make sure the old key isn't in there
        for (int i = 0; i < m_testKeys.length; i++) {
            assertTrue("Old key still in key set after remove",
                       m_testKeys[i] != key);
        }
        m_testSize = m_testSize - 1;
        // Use testKeyValueOrder() to test the validity after remove
        testKeyValueOrder();
    }

    // No need to test insert since it's taken care of by
    // setupValues() + testKeyValueOrder()
    @Test
    public void testKeyValueChange() {
        _KeyType_ key;
        _ValueType_[] values;
        // Make sure the size of the heap is correct
        assertEquals("Size problem", m_heap.getSize(), m_testSize);

        // Test removal at m_testSize/2, m_testSize/3,
        // m_testSize/5, m_testSize/7 m_testSize/1.5 positions
        key = m_heap.getEntryKey(m_testSize / 2);
        values = m_heap.getValues();
        values[m_testSize / 2] = Types.get_ValueName_For(60231);
        m_heap.resetKey(m_testSize / 2, Types.get_KeyName_For(60231));
        for (int i = 0; i < m_testKeys.length; i++) {
            if (Comparators._KeyName_Asc.equals(m_testKeys[i], key)) {
                m_testKeys[i] = Types.get_KeyName_For(60231);
            }
        }
        testKeyValueOrder();

        setupValues();
        key = m_heap.getEntryKey(m_testSize / 3);
        values = m_heap.getValues();
        values[m_testSize / 3] = Types.get_ValueName_For(60231);
        m_heap.resetKey(m_testSize / 3, Types.get_KeyName_For(60231));
        for (int i = 0; i < m_testKeys.length; i++) {
            if (Comparators._KeyName_Asc.equals(m_testKeys[i], key)) {
                m_testKeys[i] = Types.get_KeyName_For(60231);
            }
        }
        testKeyValueOrder();

        setupValues();
        key = m_heap.getEntryKey(m_testSize / 5);
        values = m_heap.getValues();
        values[m_testSize / 5] = Types.get_ValueName_For(60231);
        m_heap.resetKey(m_testSize / 5, Types.get_KeyName_For(60231));
        for (int i = 0; i < m_testKeys.length; i++) {
            if (Comparators._KeyName_Asc.equals(m_testKeys[i], key)) {
                m_testKeys[i] = Types.get_KeyName_For(60231);
            }
        }
        testKeyValueOrder();

        setupValues();
        key = m_heap.getEntryKey(m_testSize / 7);
        values = m_heap.getValues();
        values[m_testSize / 7] = Types.get_ValueName_For(60231);
        m_heap.resetKey(m_testSize / 7, Types.get_KeyName_For(60231));
        for (int i = 0; i < m_testKeys.length; i++) {
            if (Comparators._KeyName_Asc.equals(m_testKeys[i], key)) {
                m_testKeys[i] = Types.get_KeyName_For(60231);
            }
        }
        testKeyValueOrder();

        setupValues();
        key = m_heap.getEntryKey((m_testSize / 3) * 2);
        values = m_heap.getValues();
        values[(m_testSize / 3) * 2] = Types.get_ValueName_For(60231);
        m_heap.resetKey((m_testSize / 3) * 2, Types.get_KeyName_For(60231));
        for (int i = 0; i < m_testKeys.length; i++) {
            if (Comparators._KeyName_Asc.equals(m_testKeys[i], key)) {
                m_testKeys[i] = Types.get_KeyName_For(60231);
            }
        }
        testKeyValueOrder();
    }

    //     @Test
    //     public void testToString() {
    //         System.out.println("toString: "+ m_heap.toString());
    //     }
}

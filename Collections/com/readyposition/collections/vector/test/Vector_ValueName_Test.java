// _Warning_

package com.readyposition.collections.vector;

import java.util.Random;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.readyposition.collections.comparator.*;
import com.readyposition.collections.common.*;

/** Unit tests for heaps. */
public class Vector_ValueName_Test
{
    /** This holds vectors we will be testing. */
    private Vector_ValueName_ m_vector;

    /** Array of values to compare against what's in the vector. */
    private _ValueType_[] m_testValues;

    /** Determines the size of the tests. */
    private final static int TEST_SIZE = Types.testSize_ValueName_();

    /** Used to keep track of MIN/MAX in the heap */
    private final static long RANDOM_SEED = 0xcafe;

    /** Random number generator. */
    Random m_rand;

    @Before
    public void setUp() {
        m_vector = new Vector_ValueName_();
        setupValues();
    }

    @After
    public void tearDown() {
        m_vector = null;
    }

    private void setupValues() {
        // Use a fixed random seed so we get the same
        // sequence every test
        m_rand = new Random(RANDOM_SEED);
        m_testValues = new _ValueType_[TEST_SIZE];
        for (int i = 0; i < TEST_SIZE; i++) {
            _ValueType_ value = Types.get_ValueName_For(m_rand.nextInt());
            m_vector.add(value);
            m_testValues[i] = value;
        }
    }

    @Test
    public void testResetA() {
        // Test clearing with initial capacity
        m_vector.reset();

        // Make sure it's empty
        assertEquals("Cleared vector has non-zero size",
          0, m_vector.getSize());
    }
    
    @Test
    public void testResetB() {
        // Test clearing with double capacity
        int newCapacity = 2 * m_vector.getCapacity();
        m_vector.reset(newCapacity);
        assertEquals("Re-cleared vector has non-zero size",
                0, m_vector.getSize());
        assertEquals("Doubling capacity didn't work",
                (newCapacity), m_vector.getCapacity());
    }

    protected void checkValues(String msg, int vectorStart, int testStart,
            int length)
    {
        for (int i = 0; i < length; i++) {
            assertTrue(msg,
                    Comparators._ValueName_Asc.equals(
                            m_vector.get(i + vectorStart),
                            m_testValues[i + testStart]));
        }
    }

    @Test
    public void testAddA() {
        // Check that add in setupValues did the right thing
        assertEquals("Setup resulted in wrong length",
          TEST_SIZE, m_vector.getSize());
        checkValues("Individual adds result in wrong values",
                0, 0, TEST_SIZE);
    }
    
    @Test
    public void testAddB() {
        // Add an entire vector
        m_vector.add(m_testValues);
        assertEquals("Add of vector resulted in wrong length",
          TEST_SIZE * 2, m_vector.getSize());
        checkValues("Vector add results in wrong values",
                TEST_SIZE, 0, TEST_SIZE);
    }

    @Test
    public void testAddC() {
        // Add a vector subset
        m_vector.add(m_testValues, 1, TEST_SIZE - 2);
        assertEquals("Add of sub-vector resulted in wrong length",
          (TEST_SIZE * 2) - 2, m_vector.getSize());
        checkValues("Subvector add results in wrong values",
                TEST_SIZE, 1, TEST_SIZE - 2);
    }

    @Test
    public void testFillA() {
        // Test filling the entire vector
        _ValueType_ value = Types.get_ValueName_For(m_rand.nextInt());
        m_vector.fill(value);
        for (int i = m_vector.getSize() - 1; i >= 0; i--) {
            assertTrue("Full fill value wrong",
                    Comparators._ValueName_Asc.equals(m_vector.get(i), value));
        }
    }

    @Test
    public void testFillB() {
        // Test filling a slice of the vector
        _ValueType_ value = Types.get_ValueName_For(m_rand.nextInt());
        int start = TEST_SIZE / 4;
        int end = (TEST_SIZE * 3) / 4;
        m_vector.fill(start, end, value);
        checkValues("Fill overwrote start of vector", 0, 0, start);
        for (int i = start; i < end; i++) {
            assertTrue("Partial fill value wrong",
                    Comparators._ValueName_Asc.equals(m_vector.get(i), value));
        }
        checkValues("Fill overwrote end of vector", end, end, TEST_SIZE - end);
    }
    
    @Test
    public void testInsertA() {
        // Insert a single value
        _ValueType_ value = Types.get_ValueName_For(m_rand.nextInt());
        int position = TEST_SIZE / 2;
        m_vector.insert(position, value);
        assertEquals("Length wrong after single value insert",
                TEST_SIZE + 1, m_vector.getSize());
    }
    
    @Test
    public void testInsertB() {
        int position = TEST_SIZE / 2;
        m_vector.insert(position, m_testValues);
        assertEquals("Length wrong after full vector insert",
                TEST_SIZE * 2, m_vector.getSize());
        checkValues("Full insert overwrote start of vector", 0, 0, position);
        checkValues("Full insert values wrong", position, 0, TEST_SIZE);
        checkValues("Full insert overwrote end of vector",
                position + TEST_SIZE, position, TEST_SIZE - position);
    }
    
    @Test
    public void testInsertC() {
        int position = TEST_SIZE / 2;
        int start = TEST_SIZE / 4;
        int end = (TEST_SIZE * 3) / 4;
        m_vector.insert(position, m_testValues, start, end - start);
        assertEquals("Length wrong after partial vector insert",
                TEST_SIZE + end - start, m_vector.getSize());
        checkValues("Partial insert overwrote start of vector",
                0, 0, position);
        checkValues("Partial insert values wrong",
                position, start, end - start);
        checkValues("Partial insert overwrote end of vector",
                position + end - start, position, TEST_SIZE - position);
    }
    
    @Test
    public void testRemoveA() {
        int position = TEST_SIZE / 2;
        m_vector.remove(position);
        assertEquals("Length wrong after single remove",
                TEST_SIZE - 1, m_vector.getSize());
        checkValues("Single remove tainted start of vector",
                0, 0, position - 1);
        checkValues("Single remove tainted end of vector",
                position, position + 1, TEST_SIZE - position - 1);
    }
    
    @Test
    public void testRemoveB() {
        int start = TEST_SIZE / 4;
        int end = (TEST_SIZE * 3) / 4;
        m_vector.remove(start, end - start);
        assertEquals("Length wrong after remove",
                TEST_SIZE - (end - start), m_vector.getSize());
        checkValues("Remove tainted start of vector",
                0, 0, start - 1);
        checkValues("Remove tainted end of vector",
                start, end, TEST_SIZE - end);
    }
    
    protected void checkOrder(int start, int end, Cmp_ValueName_ cmp) {
        for (int i = start + 1; i < end; i++) {
            _ValueType_ a = m_vector.get(i - 1);
            _ValueType_ b = m_vector.get(i);
            assertTrue("Order is wrong", cmp.compare(a, b) <= 0);
        }
    }
    
    @Test
    public void testSortA() {
        m_vector.sort();
        checkOrder(0, m_vector.getSize(), Comparators._ValueName_Asc);
    }

    @Test
    public void testSortB() {
        m_vector.sort(Comparators._ValueName_Desc);
        checkOrder(0, m_vector.getSize(), Comparators._ValueName_Desc);
    }
    
    @Test
    public void testSortC() {
        int start = TEST_SIZE / 4;
        int end = (TEST_SIZE * 3) / 4;
        m_vector.sort(start, end);
        checkOrder(start, end, Comparators._ValueName_Asc);
        checkValues("Partial sort overwrote start of vector",
                    0, 0, start);
        checkValues("Partial sort overwrote end of vector",
                    end, end, TEST_SIZE - end);
    }
    
    @Test
    public void testSortD() {
        int start = TEST_SIZE / 4;
        int end = (TEST_SIZE * 3) / 4;
        m_vector.sort(start, end, Comparators._ValueName_Desc);
        checkOrder(start, end, Comparators._ValueName_Desc);
        checkValues("Partial sort overwrote start of vector",
                    0, 0, start);
        checkValues("Partial sort overwrote end of vector",
                    end, end, TEST_SIZE - end);
    }
}

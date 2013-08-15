// _Warning_

package com.readyposition.collections.hash.test;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.readyposition.collections.common.*;
import com.readyposition.collections.comparator.Comparators;
import com.readyposition.collections.hash.*;
import com.readyposition.collections.itemizer.Itemizer;

/** Unit tests for hash maps. */
public class HashMap_KeyName__ValueName_Test {
    /** The hash map we will be testing. */
    protected Map_KeyName__ValueName_ m_map;

    protected final static int TEST_SIZE = Types.testSize_KeyName_();

    @Before
    public void setUp() {
        m_map = new HashMap_KeyName__ValueName_();
    }

    @After
    public void tearDown() {
        m_map = null;
    }

    /** Convenience method to assert value equality. */
    protected void assertValueEquals(String message, _ValueType_ expected,
                                     _ValueType_ actual) {
        if (!Comparators._ValueName_Asc.equals(expected, actual)) {
            fail(message + " expected:<" + expected + "> but was:<" + actual
                 + ">");
        }
    }

    /** Convenience method to assert key equality. */
    protected void assertKeyEquals(String message, _KeyType_ expected,
                                   _KeyType_ actual) {
        if (!Comparators._KeyName_Asc.equals(expected, actual)) {
            fail(message + " expected:<" + expected + "> but was:<" + actual
                 + ">");
        }
    }

    @Test
    public void testInitialCondition() {
        assertEquals("Map should initially be empty", 0, m_map.getSize());
        assertEquals("Map values should initially be empty", 0,
                     m_map.getValues().length);
        assertEquals("Map keys should initially be empty", 0,
                     m_map.getKeys().length);
        Itemizer itm = m_map.itemizer();
        assertFalse("Itemizer for empty map should not show more elements",
                    itm.hasMore());
    }

    @Test
    public void testSimplePut() {
        m_map.put(Types.get_KeyName_For(100), Types.get_ValueName_For(200));
        assertTrue("Map should contain new key",
                   m_map.containsKey(Types.get_KeyName_For(100)));
        assertKeyEquals("Map keys should contain new value", 
                        Types.get_KeyName_For(100), m_map.getKeys()[0]);
        assertValueEquals("Map values should contain new value",
                          Types.get_ValueName_For(200), m_map.getValues()[0]);
    }

    @Test
    public void testRemove() {
        m_map.put(Types.get_KeyName_For(1), Types.get_ValueName_For(2));
        m_map.put(Types.get_KeyName_For(2), Types.get_ValueName_For(3));
        Itemizer itm = m_map.itemizer();
        // This is ok
        itm.remove();
        // This is not
        try {
            itm.remove();
            fail("Double remove should throw exception");
        } catch (NoSuchElementException e) {
        }
        try {
            itm.entry();
            fail("Shouldn't be able to retrieve removed entry");
        } catch (NoSuchElementException e) {
        }
    }

    @Test
    public void testItemizer() {
        doTestItemizer(1);
        doTestItemizer(TEST_SIZE);
    }

    private void doTestItemizer(int count) {
        HashMap<Object, Object> expected = new HashMap<Object, Object>();
        for (int i = 1; i <= count; i++) {
            m_map.put(Types.get_KeyName_For(i), Types.get_ValueName_For(i * 2));
            expected.put(Types.get_KeyName_For(i), Types
                         .get_ValueName_For(i * 2));
        }

        Itemizer itm = m_map.itemizer();
        HashMap<Object, Object> actual = new HashMap<Object, Object>();
        for (int i = 1; i <= count; i++) {
            assertTrue(itm.hasMore());
            int entry = itm.entry();
            actual.put(m_map.getEntryKey(entry), m_map.getEntryValue(entry));
            itm.advance();
        }
        assertFalse(itm.hasMore());
        assertTrue(expected.equals(actual));

        // Now remove
        actual.clear();
        itm = m_map.itemizer();
        for (int i = 1; i <= count; i++) {
            assertTrue(itm.hasMore());
            int entry = itm.entry();
            actual.put(m_map.getEntryKey(entry), m_map.getEntryValue(entry));
            itm.remove();
            itm.advance();
        }
        assertFalse(itm.hasMore());
        assertEquals(m_map.getSize(), 0);
        assertEquals(expected, actual);
    }

    @Test
    public void testFastFailItemizer() {
        Itemizer itm = m_map.itemizer();
        m_map.put(Types.get_KeyName_For(100), Types.get_ValueName_For(200));
        try {
            itm.hasMore();
            fail("Table modified but Itemizer entry() didn't fail.");
        } catch (ConcurrentModificationException e){
        }
        try {
            itm.advance();
            fail("Table modified but Itemizer entry() didn't fail.");
        } catch (ConcurrentModificationException e){
        }
        try {
            itm.entry();
            fail("Table modified but Itemizer entry() didn't fail.");
        } catch (ConcurrentModificationException e){
        }
        try {
            itm.remove();
            fail("Table modified but Itemizer entry() didn't fail.");
        } catch (ConcurrentModificationException e){
        }
    }
    
    @Test
    public void testItemizerReuse() {
        //        TreeMap_KeyName__ValueName_ treeMap =
        //            (TreeMap_KeyName__ValueName_)m_map;

        int half = TEST_SIZE / 2;
        for (int i = 1; i < half; i++) {
            m_map.put(Types.get_KeyName_For(i),
                      Types.get_ValueName_For(i * 2));
        }

        Itemizer itemizer = m_map.itemizer(null);
        doTestIteratorContents(itemizer, half);

        Itemizer itemizer2 = m_map.itemizer(itemizer);
        assertEquals(itemizer, itemizer2);
        doTestIteratorContents(itemizer2, half);

        // Add more entries
        for (int i = half; i < TEST_SIZE; i++) {
            m_map.put(Types.get_KeyName_For(i),
                      Types.get_ValueName_For(i * 2));
        }

        itemizer2 = m_map.itemizer(itemizer);
        doTestIteratorContents(itemizer2, TEST_SIZE);
    }

    protected void doTestIteratorContents(Itemizer itm, int count) {
        for (int i = 1; i < count; i++) {
            assertTrue(itm.hasMore());
            assertTrue(m_map.containsKey(m_map.getEntryKey(itm.entry())));
            assertTrue(Comparators._ValueName_Asc.equals(
                                                         m_map.get(m_map.getEntryKey(itm.entry())),
                                                         m_map.getEntryValue(itm.entry())));
            itm.advance();
        }
        assertFalse(itm.hasMore());
    }

    // @Test
    // public void testToString() {
    // map.put(Types.get_KeyName_For(100), Types.get_ValueName_For(200));
    // map.put(Types.get_KeyName_For(200), Types.get_ValueName_For(200));
    // System.out.println("toString: "+ map.toString());
    // }
}

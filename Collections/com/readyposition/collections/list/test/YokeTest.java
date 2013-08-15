package com.readyposition.collections.list.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.readyposition.collections.list.Yoke;

/** Unit tests for Yoke. */
public class YokeTest
{
    /** This will exercise the Generic subclassing of Yoke. */
    protected static class IntYoke extends Yoke<IntYoke> {
        /** The value for this yoke. */
        protected int m_value;

        /** Constructor. */
        public IntYoke() { super(); }

        /**
         * Constructor.
         *
         * @param value the value to place in this Yoke.
         */
        public IntYoke(int value) {
            super();
            m_value = value;
        }

        /**
         * Gets the value in this Yoke.
         *
         * @return the value in this Yoke.
         */
        public int getValue() {
            return m_value;
        }
        
        public String toString() {
            return String.valueOf(m_value);
        }
    }
    
    protected interface Orientation {
        String direction();
        void insertBefore(IntYoke a, IntYoke b);
        void insertAfter(IntYoke a, IntYoke b);
        IntYoke getBefore(IntYoke j);
        IntYoke getAfter(IntYoke j);
    }
    
    protected static Orientation s_leftOrientation = new Orientation() {
            public String direction() { return "Left"; }
            public void insertBefore(IntYoke a, IntYoke b) { a.insertLeft(b); }
            public void insertAfter(IntYoke a, IntYoke b) { a.insertRight(b); }
            public IntYoke getBefore(IntYoke j) { return j.getLeft(); }
            public IntYoke getAfter(IntYoke j) { return j.getRight(); }
        };
    
    protected static Orientation s_rightOrientation = new Orientation() {
            public String direction() { return "Right"; }
            public void insertBefore(IntYoke a, IntYoke b) { a.insertRight(b); }
            public void insertAfter(IntYoke a, IntYoke b) { a.insertLeft(b); }
            public IntYoke getBefore(IntYoke j) { return j.getRight(); }
            public IntYoke getAfter(IntYoke j) { return j.getLeft(); }
        };

    /** Head of linked list A. */
    protected IntYoke m_headA = new IntYoke();

    /** Head of linked list B. */
    protected IntYoke m_headB = new IntYoke();

    /** Arbitrary size of list A to use. */
    protected final static int SIZE_A = 16;

    /** Arbitrary size of list B to use. */
    protected final static int SIZE_B = 8;
    
    @Before
    public void setUp()  {}

    @After
    public void tearDown()  {
        // Remove all elements from both linked lists.
        m_headA.remove();
        m_headB.remove();
    }

    @Test
    public void testInitialConditions() {
        assertEquals("Head A doesn't point right to itself",
                     m_headA, m_headA.getRight());
        assertEquals("Head A doesn't point left to itself",
                     m_headA, m_headA.getLeft());

        assertEquals("Head B doesn't point right to itself",
                     m_headB, m_headB.getRight());
        assertEquals("Head B doesn't point left to itself",
                     m_headB, m_headB.getLeft());
    }

    @Test
    public void testLeftInserts() {
        doInserts(s_leftOrientation);
    }
    
    @Test
    public void testRightInserts() {
        doInserts(s_rightOrientation);
    }

    protected void doInserts(Orientation o) {
        // First join the 2 single element lists
        o.insertBefore(m_headA, m_headB);
        int aAfterLength = getAfterLength(o, m_headA);
        int aBeforeLength = getBeforeLength(o, m_headA);
        int bAfterLength = getAfterLength(o, m_headB);
        int bBeforeLength = getBeforeLength(o, m_headB);
        assertEquals("Lists have wrong length", 2, aAfterLength);
        assertTrue("Before and after lengths don't match",
                   aAfterLength == aBeforeLength &&
                   aAfterLength == bAfterLength &&
                   aAfterLength == bBeforeLength);

        // Inserting again should return to starting conditions
        o.insertBefore(m_headA, m_headB);
        testInitialConditions();
    }
    
    @Test
    public void testLeftListInserts() {
        doListInserts(s_leftOrientation);
    }
    
    @Test
    public void testRightListInserts() {
        doListInserts(s_rightOrientation);
    }
    
    public void doListInserts(Orientation o) {
        // Form a list on Head A
        IntYoke seqStartA = makeList(o, 1, SIZE_A);
        IntYoke seqEndA = o.getBefore(seqStartA);
        o.insertBefore(m_headA, seqStartA);
        
        // Verify that the head was inserted in the right place
        assertEquals(o.direction() + ": Head A is not before start",
                     m_headA, o.getBefore(seqStartA));
        assertEquals(o.direction() + ": Head A is not after end",
                     m_headA, o.getAfter(seqEndA));
        assertEquals(o.direction() + ": Start A is not after head",
                     seqStartA, o.getAfter(m_headA));
        assertEquals(o.direction() + ": End A is not before head",
                     seqEndA, o.getBefore(m_headA));

        verifyLengthAndSequence(o, m_headA, SIZE_A + 1,
                                1, SIZE_A, SIZE_A, SIZE_A);

        // Verify that the insert is reversible
        o.insertBefore(m_headA, seqStartA);
        testInitialConditions();
        assertEquals(o.direction() + ": Sequence A end is not before start",
                     seqEndA, o.getBefore(seqStartA));
        assertEquals(o.direction() + ": Sequence A start is not after end",
                     seqStartA, o.getAfter(seqEndA));

        // Form the list on Head A again.  We know this works now.
        o.insertBefore(m_headA, seqStartA);

        // Form a second list on Head B
        // We don't need to do all of the checks as above.
        // We believe that that all works now.
        IntYoke seqStartB = makeList(o, SIZE_A + 1, SIZE_B);
        IntYoke seqEndB = o.getBefore(seqStartB);
        o.insertBefore(m_headB, seqStartB);
        verifyLengthAndSequence(o, m_headB, SIZE_B + 1,
                                SIZE_A + 1, SIZE_B, SIZE_A + SIZE_B, SIZE_B);

        // Let's splice the two lists together
        o.insertBefore(m_headA, m_headB);
        
        // Verify that head A was inserted in the right place
        assertEquals(o.direction() + ": Start A is not after head A",
                     seqStartA, o.getAfter(m_headA));
        assertEquals(o.direction() + ": End B is not before head A",
                     seqEndB, o.getBefore(m_headA));
        assertEquals(o.direction() + ": Head A is not after end B",
                     m_headA, o.getAfter(seqEndB));
        assertEquals(o.direction() + ": Head A is not before start A",
                     m_headA, o.getBefore(seqStartA));
        
        // Verify that head B was inserted in the right place
        assertEquals(o.direction() + ": Start B is not after head B",
                     seqStartB, o.getAfter(m_headB));
        assertEquals(o.direction() + ": End A is not before head B",
                     seqEndA, o.getBefore(m_headB));
        assertEquals(o.direction() + ": Head B is not after end A",
                     m_headB, o.getAfter(seqEndA));
        assertEquals(o.direction() + ": Head B is not before start B",
                     m_headB, o.getBefore(seqStartB));

        // Verify the combined lists
        verifyLengthAndSequence(o, m_headA, SIZE_A + SIZE_B + 2,
                                1, SIZE_A, SIZE_A + SIZE_B, SIZE_B);
        verifyLengthAndSequence(o, m_headB, SIZE_A + SIZE_B + 2,
                                SIZE_A + 1, SIZE_B, SIZE_A, SIZE_A);
        
        // Let's split the two lists as before
        o.insertBefore(m_headA, m_headB);

        // Verify that both lists are back to their original state.
        verifyLengthAndSequence(o, m_headA, SIZE_A + 1,
                                1, SIZE_A, SIZE_A, SIZE_A);
        verifyLengthAndSequence(o, m_headB, SIZE_B + 1,
                                SIZE_A + 1, SIZE_B, SIZE_A + SIZE_B, SIZE_B);
    }

    protected void verifyLengthAndSequence(Orientation o, IntYoke head,
                                           int length, int startAfter, int countAfter,
                                           int endBefore, int countBefore)
    {
        // Verify lengths
        assertEquals(o.direction() + ": List has wrong length after",
                     length, getAfterLength(o, head));
        assertEquals(o.direction() + ": List has wrong length before",
                     length, getBeforeLength(o, head));

        // Verify the contents of the list to verify that the head is in
        // the right place
        assertTrue(o.direction() + ": Head A list is out of sequence after",
                   isAfterInOrder(o, o.getAfter(head), startAfter, countAfter));
        assertTrue(o.direction() + ": Head A list is out of sequence before",
                   isBeforeInOrder(o, o.getBefore(head), endBefore, countBefore));
    }

    protected boolean isAfterInOrder(Orientation o, IntYoke j,
                                     int start, int count)
    {
        for (int i = 0; i < count; i++) {
            if (j.getValue() != start++) {
                return false;
            }
            j = o.getAfter(j);
        }
        return true;
    }

    protected boolean isBeforeInOrder(Orientation o, IntYoke j,
                                      int end, int count)
    {
        for (int i = 0; i < count; i++) {
            if (j.getValue() != end--) {
                return false;
            }
            j = o.getBefore(j);
        }
        return true;
    }

    protected IntYoke makeList(Orientation o, int start, int count) {
        if (count == 0) {
            return null;
        }
        IntYoke j = new IntYoke(start++);
        for (int i = 1; i < count; i++) {
            o.insertBefore(j, new IntYoke(start++));
        }
        return j;
    }

    protected int getAfterLength(Orientation o, IntYoke yoke) {
        int count = 1;
        for (IntYoke j = o.getAfter(yoke); j != yoke; j = o.getAfter(j)) {
            count++;
        }
        return count;
    }

    protected int getBeforeLength(Orientation o, IntYoke yoke) {
        int count = 1;
        for (IntYoke j = o.getBefore(yoke); j != yoke; j = o.getBefore(j)) {
            count++;
        }
        return count;
    }
}

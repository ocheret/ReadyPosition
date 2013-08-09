package com.readyposition.collections.common.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.readyposition.collections.common.BitRotate;

public class BitRotateTest
{
    @Before
    public void setUp()  {
    }

    @After
    public void tearDown()  {
    }

    @Test
    public void testRotations() {
        byte byteA = (byte)0xad;
        byte byteB = (byte)0xda;
        int bits = 4;
        assertEquals("byte rotate right",
                     BitRotate.right(byteA, bits), byteB);
        assertEquals("byte rotate left",
                     BitRotate.left(byteA, bits), byteB);

        short shortA = (short)0xabcd;
        short shortB = (short)0xcdab;
        bits = 8;
        assertEquals("short rotate right",
                     BitRotate.right(shortA, bits), shortB);
        assertEquals("short rotate left",
                     BitRotate.left(shortA, bits), shortB);

        int intA = (int)0xdeadbeef;
        int intB = (int)0xbeefdead;
        bits = 16;
        assertEquals("int rotate right",
                     BitRotate.right(intA, bits), intB);
        assertEquals("int rotate left",
                     BitRotate.left(intA, bits), intB);

        long longA = 0xdeadbeef87654321L;
        long longB = 0x87654321deadbeefL;
        bits = 32;
        assertEquals("long rotate right",
                     BitRotate.right(longA, bits), longB);
        assertEquals("long rotate left",
                     BitRotate.left(longA, bits), longB);
    }
}

/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import com.readyposition.utils.Types;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;
import java.util.Arrays;

import junit.framework.TestCase;

public class ByteBulbTest extends TestCase
{
    public static void testConstructors()
    {
	ByteBulb bb = new ByteBulb(true, 10);
	assertTrue("True didn't take", bb.isDirect());
	assertTrue("Buffer isn't direct", bb.getInBuffer().isDirect());
	assertEquals("Initial capacity didn't take", 10,
	  bb.getInitialCapacity());
	assertEquals("Buffer has wrong capacity", 10, bb.getCapacity());

	bb = new ByteBulb(false, 10);
	assertTrue("False didn't take", !bb.isDirect());
	assertTrue("Buffer is direct", !bb.getInBuffer().isDirect());
	assertEquals("Initial capacity didn't take", 10,
	  bb.getInitialCapacity());
	assertEquals("Buffer has wrong capacity", 10, bb.getCapacity());

	bb = new ByteBulb(10);
	assertTrue("Unexpected direct", !bb.isDirect());
	assertTrue("Buffer is direct", !bb.getInBuffer().isDirect());
	assertEquals("Initial capacity didn't take", 10,
	  bb.getInitialCapacity());
	assertEquals("Buffer has wrong capacity", 10, bb.getCapacity());

	bb = new ByteBulb(true);
	assertTrue("True didn't take", bb.isDirect());
	assertTrue("Buffer isn't direct", bb.getInBuffer().isDirect());
	assertEquals("Unexpected Initial capacity", 128,
	  bb.getInitialCapacity());
	assertEquals("Buffer has wrong capacity", 128, bb.getCapacity());

	bb = new ByteBulb(false);
	assertTrue("False didn't take", !bb.isDirect());
	assertTrue("Buffer is direct", !bb.getInBuffer().isDirect());
	assertEquals("Unexpected Initial capacity", 128,
	  bb.getInitialCapacity());
	assertEquals("Buffer has wrong capacity", 128, bb.getCapacity());

	bb = new ByteBulb();
	assertTrue("Unexpected direct", !bb.isDirect());
	assertTrue("Buffer is direct", !bb.getInBuffer().isDirect());
	assertEquals("Unexpected Initial capacity", 128,
	  bb.getInitialCapacity());
	assertEquals("Buffer has wrong capacity", 128, bb.getCapacity());
    }

    public static void testByteOrder() {
	// Test for direct and non-direct buffers.
	boolean direct = false;
	do {
	    // Make sure we can get a default ByteOrder even though none
	    // has been set.
	    ByteBulb bb = new ByteBulb(direct);
	    assertNotNull("No default ByteOrder", bb.getByteOrder());

	    // Make sure that setting a ByteOrder works on the
	    // ByteBulb and on the underlying ByteBuffer.
	    bb = new ByteBulb(direct);
	    bb.setByteOrder(ByteOrder.BIG_ENDIAN);
	    assertSame("BIG_ENDIAN didn't take",
	      ByteOrder.BIG_ENDIAN, bb.getByteOrder());
	    assertSame("BIG_ENDIAN didn't take",
	      ByteOrder.BIG_ENDIAN, bb.getInBuffer().order());

	    bb.setByteOrder(ByteOrder.LITTLE_ENDIAN);
	    assertSame("LITTLE_ENDIAN didn't take",
	      ByteOrder.LITTLE_ENDIAN, bb.getByteOrder());
	    assertSame("LITTLE_ENDIAN didn't take",
	      ByteOrder.LITTLE_ENDIAN, bb.getInBuffer().order());

	    direct = !direct;
	} while (direct);
    }

//     public static void testGetBuffers() {
// 	// Test for direct and non-direct buffers.
// 	boolean direct = false;
// 	do {
// 	    ByteBulb bb = new ByteBulb(direct);
// 	    bb.putDouble(1.0);

// 	    assertEquals("Used after putting a double is " + bb.getUsed(),
// 	      Types.DOUBLE.size(), bb.getUsed());

// 	    ByteBuffer byb = bb.getInBuffer();
// 	    assertEquals("position=" + byb.position() +
// 	      " and in=" + bb.getIn() + " but should match",
// 	      bb.getIn(), byb.position());
// 	    assertEquals("limit=" + byb.limit() +
// 	      " and capacity=" + bb.getCapacity() + " but should match",
// 	      bb.getCapacity(), byb.limit());

// 	    byb = bb.getOutBuffer();
// 	    assertEquals("position=" + byb.position() +
// 	      " and out=" + bb.getOut() + " but should match",
// 	      bb.getOut(), byb.position());
// 	    assertEquals("limit=" + byb.limit() +
// 	      " and in=" + bb.getIn() + " but should match",
// 	      bb.getIn(), byb.limit());
// 	} while (direct);
//     }

    public static void testMarking() {
	// Test for direct and non-direct buffers.
	boolean direct = false;
	do {
	    ByteBulb bb = new ByteBulb(direct);
	    bb.putDouble(1.0);
	    ByteBuffer byb;
	    try {
		byb = bb.getOutMarkedBuffer();
		fail("No out mark set but getOutMarkedBuffer() succeeded");
	    } catch (InvalidMarkException e) {
	    }

	    try {
		byb = bb.getInMarkedBuffer();
		fail("No in mark set but getInMarkedBuffer() succeeded");
	    } catch (InvalidMarkException e) {
	    }

	    // TODO - add more detailed tests here - reset methods, ensure
	    // that marks get cleared, etc...

	    bb.markIn();
	    assertEquals("In mark wasn't set properly", bb.getIn(),
	      bb.getInMark());
	    bb.putDouble(100.0);
	    byb = bb.getInMarkedBuffer();
	    assertEquals("position=" + byb.position() +
	      " and in mark=" + bb.getInMark() + " but should match",
	      bb.getInMark(), byb.position());
	    assertEquals("limit=" + byb.limit() +
	      " and in=" + bb.getIn() + " but should match",
	      bb.getIn(), byb.limit());

	    bb.markOut();
	    assertEquals("Out mark wasn't set properly", bb.getOut(),
	      bb.getOutMark());
	    bb.getDouble();
	    byb = bb.getOutMarkedBuffer();
	    assertEquals("position=" + byb.position() +
	      " and out mark=" + bb.getOutMark() + " but should match",
	      bb.getOutMark(), byb.position());
	    assertEquals("limit=" + byb.limit() +
	      " and out=" + bb.getOut() + " but should match",
	      bb.getOut(), byb.limit());
	} while (direct);
    }

    public static void testClear() {
	// Test for direct and non-direct buffers.
	boolean direct = false;
	do {
	    ByteBulb bb = new ByteBulb(direct, 4);
	    bb.putDouble(1.0);
	    bb.clear();
	    assertEquals("Didn't reset to initialCapacity",
	      4, bb.getCapacity());
	    assertEquals("Didn't remove data", 0, bb.getUsed());
	    assertEquals("in", 0, bb.getIn());
	    assertEquals("out", 0, bb.getIn());
	} while (direct);
    }

    public static void testEmpty() {
	// Test for direct and non-direct buffers.
	boolean direct = false;
	do {
	    ByteBulb bb = new ByteBulb(direct, 4);
	    bb.putDouble(1.0);
	    int capacity = bb.getCapacity();
	    bb.empty();
	    assertEquals("Capacity was reduced", capacity, bb.getCapacity());
	    assertEquals("Didn't remove data", 0, bb.getUsed());
	    assertEquals("in", 0, bb.getIn());
	    assertEquals("out", 0, bb.getIn());
	} while (direct);
    }

    public static void testMakeRoom() {
	// Test for direct and non-direct buffers.
	boolean direct = false;
	do {
	    ByteBulb bb = new ByteBulb(direct, 4);
	    assertTrue("Not enough initial room made", bb.getRoom() >= 4);

	    bb.makeRoom(32);
	    int room = bb.getRoom();
	    assertTrue("Not enough room made", room >= 32);

	    bb.makeRoom(16);
	    assertEquals("Room changed", room, bb.getRoom());
	} while (direct);
    }

    public static void testGetsPeeksPuts() {
	// Test for direct and non-direct buffers.
	boolean direct = false;
	do {
	    ByteBulb bb = new ByteBulb(direct, 0);

	    // Do lockstep gets and puts

	    bb.put((byte)0xa);
	    byte b = bb.peek();
	    assertEquals("byte put/peek mismatch", (byte)0xa, b);
	    b = bb.get();
	    assertEquals("byte put/get mismatch", (byte)0xa, b);

	    byte[] ba = new byte[10];
	    for (int i = 0; i < ba.length; i++) {
		ba[i] = (byte)i;
	    }
	    bb.put(ba);
	    Arrays.fill(ba, (byte)-1);
	    bb.peek(ba);
	    for (int i = 0; i < ba.length; i++) {
		assertEquals("byte[] put/peek mismatch", (byte)i, ba[i]);
	    }
	    Arrays.fill(ba, (byte)-1);
	    bb.get(ba);
	    for (int i = 0; i < ba.length; i++) {
		assertEquals("byte[] put/get mismatch", (byte)i, ba[i]);
	    }

	    bb.put(ba, 5, 2);
	    Arrays.fill(ba, (byte)-1);
	    bb.peek(ba, 5, 2);
	    for (int i = 5; i < 7; i++) {
		assertEquals("byte[] subset put/peek mismatch",
		  (byte)i, ba[i]);
	    }
	    Arrays.fill(ba, (byte)-1);
	    bb.get(ba, 5, 2);
	    for (int i = 5; i < 7; i++) {
		assertEquals("byte[] subset put/get mismatch",
		  (byte)i, ba[i]);
	    }

	    // TODO - put(ByteBuffer)

	    bb.putBoolean(true);
	    boolean bool = bb.peekBoolean();
	    assertTrue("put true, peeked false", bool);
	    bool = bb.getBoolean();
	    assertTrue("put true, got false", bool);

	    bb.putBoolean(false);
	    bool = bb.peekBoolean();
	    assertTrue("put false, peeked true", !bool);
	    bool = bb.getBoolean();
	    assertTrue("put false, got true", !bool);

	    bb.putChar('\u03a9');
	    char c = bb.peekChar();
	    assertEquals("char put/peek mismatch", '\u03a9', c);
	    c = bb.getChar();
	    assertEquals("char put/get mismatch", '\u03a9', c);

	    bb.putShort((short)0xabc);
	    short s = bb.peekShort();
	    assertEquals("short put/peek mismatch", (short)0xabc, s);
	    s = bb.getShort();
	    assertEquals("short put/get mismatch", (short)0xabc, s);

	    bb.putInt(0xacbdfea);
	    int it = bb.peekInt();
	    assertEquals("int put/peek mismatch", 0xacbdfea, it);
	    it = bb.getInt();
	    assertEquals("int put/get mismatch", 0xacbdfea, it);

	    bb.putLong(0xaf86effcfffbf7fL);
	    long l = bb.peekLong();
	    assertEquals("long put/peek mismatch", 0xaf86effcfffbf7fL, l);
	    l = bb.getLong();
	    assertEquals("long put/get mismatch", 0xaf86effcfffbf7fL, l);

	    bb.putFloat(123.456f);
	    float f = bb.peekFloat();
	    assertEquals("float put/peek mismatch", 123.456f, f, 0.0f);
	    f = bb.getFloat();
	    assertEquals("float put/get mismatch", 123.456f, f, 0.0f);

	    bb.putDouble(456.789);
	    double d = bb.peekDouble();
	    assertEquals("double put/peek mismatch", 456.789, d, 0.0f);
	    d = bb.getDouble();
	    assertEquals("double put/get mismatch", 456.789, d, 0.0f);

	    // Now do all puts first and all gets afterwards

	    bb.put((byte)0xa);
	    for (int i = 0; i < ba.length; i++) {
		ba[i] = (byte)i;
	    }
	    bb.put(ba);
	    bb.put(ba, 5, 2);
	    // TODO - put(ByteBuffer)
	    bb.putBoolean(true);
	    bb.putBoolean(false);
	    bb.putChar('\u03a9');
	    bb.putShort((short)0xabc);
	    bb.putInt(0xacbdfea);
	    bb.putLong(0xaf86effcfffbf7fL);
	    bb.putFloat(123.456f);
	    bb.putDouble(456.789);

	    b = bb.get();
	    assertEquals("byte put/get mismatch", (byte)0xa, b);

	    Arrays.fill(ba, (byte)-1);
	    bb.get(ba);
	    for (int i = 0; i < ba.length; i++) {
		assertEquals("byte[] put/get mismatch", (byte)i, ba[i]);
	    }

	    Arrays.fill(ba, (byte)-1);
	    bb.get(ba, 5, 2);
	    for (int i = 5; i < 7; i++) {
		assertEquals("byte[] subset put/get mismatch", (byte)i, ba[i]);
	    }

	    bool = bb.getBoolean();
	    assertTrue("put true, got false", bool);

	    bool = bb.getBoolean();
	    assertTrue("put false, got true", !bool);

	    c = bb.getChar();
	    assertEquals("char put/get mismatch", '\u03a9', c);

	    s = bb.getShort();
	    assertEquals("short put/get mismatch", (short)0xabc, s);

	    it = bb.getInt();
	    assertEquals("int put/get mismatch", 0xacbdfea, it);

	    l = bb.getLong();
	    assertEquals("long put/get mismatch", 0xaf86effcfffbf7fL, l);

	    f = bb.getFloat();
	    assertEquals("float put/get mismatch", 123.456f, f, 0.0f);

	    d = bb.getDouble();
	    assertEquals("double put/get mismatch", 456.789, d, 0.0f);
	} while (direct);
    }

    // TODO - test for consume()
    // TODO - test for trim()
    // TODO - test for read()
    // TODO - test for write()
    // TODO - test for toStringBuffer()
    // TODO - test for toString()
}

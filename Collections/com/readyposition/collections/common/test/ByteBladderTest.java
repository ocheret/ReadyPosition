package com.readyposition.collections.common.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;
import java.util.Arrays;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.readyposition.collections.common.ByteBladder;
import com.readyposition.collections.common.Types;

public class ByteBladderTest {
    @BeforeClass
    public static void setUpBeforeClass() {}

    @AfterClass
    public static void tearDownAfterClass() {}

    @Before
    public void setUp() {}

    @After
    public void tearDown() {}

    @Test
    public void testConstructors() {
        // Make a direct buffer
        ByteBladder bb = new ByteBladder(10, true);
        assertEquals("Initial capacity not as specified",
                     10, bb.getInitialCapacity());
        assertEquals("Buffer has wrong capacity",
                     10, bb.getCapacity());
        assertTrue("Bladder is not direct as specified",
                   bb.isDirect());
        assertTrue("Buffer is not direct as specified",
                   bb.getInBuffer().isDirect());

        // Make a non-direct buffer
        bb = new ByteBladder(10, false);
        assertEquals("Initial capacity not as specified",
                     10, bb.getInitialCapacity());
        assertEquals("Buffer has wrong capacity",
                     10, bb.getCapacity());
        assertFalse("Bladder is not non-direct as specified",
                    bb.isDirect());
        assertFalse("Buffer is not non-direct as specified",
                    bb.getInBuffer().isDirect());

        // Make a non-direct buffer by default
        bb = new ByteBladder(10);
        assertEquals("Initial capacity is not as specified",
                     10, bb.getInitialCapacity());
        assertEquals("Buffer has wrong capacity",
                     10, bb.getCapacity());
        assertFalse("Bladder did not default to non-direct",
                    bb.isDirect());
        assertFalse("Buffer is not non-direct by default",
                    bb.getInBuffer().isDirect());

        // Make a direct buffer with default size
        bb = new ByteBladder(true);
        assertEquals("Initial capacity not at expected default",
                     128, bb.getInitialCapacity());
        assertEquals("Buffer has unexpected capacity",
                     128, bb.getCapacity());
        assertTrue("Bladder is not direct as specified",
                   bb.isDirect());
        assertTrue("Buffer is not direct as specified",
                   bb.getInBuffer().isDirect());

        // Make a non-direct buffer with default size
        bb = new ByteBladder(false);
        assertEquals("Initial capacity not at expected default",
                     128, bb.getInitialCapacity());
        assertEquals("Buffer has unexpected capacity",
                     128, bb.getCapacity());
        assertFalse("Bladder is not non-direct as specified",
                    bb.isDirect());
        assertFalse("Buffer is not non-direct as specified",
                    bb.getInBuffer().isDirect());

        // Make a non-direct buffer by default with default size
        bb = new ByteBladder();
        assertEquals("Initial capacity not at expected default",
                     128, bb.getInitialCapacity());
        assertEquals("Buffer has unexpected capacity",
                     128, bb.getCapacity());
        assertFalse("Bladder did not default to non-direct",
                    bb.isDirect());
        assertFalse("Buffer is not non-direct by default",
                    bb.getInBuffer().isDirect());
    }

    @Test
    public void testByteOrder() {
        // Test for direct and non-direct buffers.
        boolean direct = false;
        do {
            // Make sure we can get a default ByteOrder even though
            // none has been set.
            ByteBladder bb = new ByteBladder(direct);
            assertNotNull("No default ByteOrder", bb.getByteOrder());
            assertSame("Byte order should default to little endian",
                       ByteOrder.LITTLE_ENDIAN, bb.getByteOrder());
            // Make sure that setting a ByteOrder works on the
            // ByteBladder and on the underlying ByteBuffer.
            bb = new ByteBladder(direct);
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

    @Test
    public void testGetBuffers() {
        // Test for direct and non-direct buffers.
        boolean direct = false;
        do {
            ByteBladder bb = new ByteBladder(direct);
            bb.putDouble(1.0);

            assertEquals("Used after putting a double is " + bb.getUsed(),
                         Types.DOUBLE.getSize(), bb.getUsed());

            ByteBuffer byb = bb.getInBuffer();
            assertEquals("Bladder in and Buffer position should match",
                         bb.getIn(), byb.position());
            assertEquals("Bladder capacity and Buffer limit should match",
                         bb.getCapacity(), byb.limit());

            byb = bb.getOutBuffer();
            assertEquals("Bladder out and Buffer position should match",
                         bb.getOut(), byb.position());
            assertEquals("Bladder in and Buffer limit should match",
                         bb.getIn(), byb.limit());

            direct = !direct;
        } while (direct);
    }

    protected void verifyNoInMark(ByteBladder bb) {
        try {
            bb.getInMarkedBuffer();
            fail("No in mark set but getInMarkedBuffer() succeeded");
        } catch (InvalidMarkException e) {
        }
    }

    protected void verifyNoOutMark(ByteBladder bb) {
        try {
            bb.getOutMarkedBuffer();
            fail("No out mark set but getOutMarkedBuffer() succeeded");
        } catch (InvalidMarkException e) {
        }
    }

    @Test
    public void testMarking() {
        // Test for direct and non-direct buffers.
        boolean direct = false;
        do {
            ByteBladder bb = new ByteBladder(direct);
            bb.putDouble(1.0);

            // Trying to use unset marks should fail.
            verifyNoInMark(bb);
            verifyNoOutMark(bb);

            // Mark the current input location
            bb.markIn();

            // Make sure it is where we expect
            assertEquals("In mark wasn't set properly",
                         bb.getIn(), bb.getInMark());

            // Put a double after the mark
            bb.putDouble(100.0);

            // Get the buffer between the mark and in
            ByteBuffer byb = bb.getInMarkedBuffer();

            assertEquals("Bladder in mark and Buffer position should match",
                         bb.getInMark(), byb.position());
            assertEquals("Bladder in and Buffer limit should match",
                         bb.getIn(), byb.limit());

            // Mark the current output location
            bb.markOut();

            // Make sure it is where we expect
            assertEquals("Out mark wasn't set properly",
                         bb.getOut(), bb.getOutMark());

            // Get a double from the marked position
            bb.getDouble();

            // Get a buffer between the mark and out
            byb = bb.getOutMarkedBuffer();

            assertEquals("Bladder out mark and buffer position should match",
                         bb.getOutMark(), byb.position());
            assertEquals("Bladder out and buffer limit should match",
                         bb.getOut(), byb.limit());

            direct = !direct;
        } while (direct);
    }

    @Test
    public void testReset() {
        // Test for direct and non-direct buffers.
        boolean direct = false;
        do {
            ByteBladder bb = new ByteBladder(4, direct);
            bb.putDouble(1.0);
            bb.reset();
            assertEquals("Didn't reset to initialCapacity",
                         4, bb.getCapacity());
            assertEquals("Didn't remove data", 0, bb.getUsed());
            assertEquals("in", 0, bb.getIn());
            assertEquals("out", 0, bb.getIn());

            direct = !direct;
        } while (direct);
    }

    @Test
    public void testClear() {
        // Test for direct and non-direct buffers.
        boolean direct = false;
        do {
            ByteBladder bb = new ByteBladder(4, direct);
            bb.putDouble(1.0);
            int capacity = bb.getCapacity();
            bb.clear();
            assertEquals("Capacity was reduced", capacity, bb.getCapacity());
            assertEquals("Didn't remove data", 0, bb.getUsed());
            assertEquals("in", 0, bb.getIn());
            assertEquals("out", 0, bb.getIn());

            direct = !direct;
        } while (direct);
    }

    @Test
    public void testMakeRoom() {
        // Test for direct and non-direct buffers.
        boolean direct = false;
        do {
            // Make an instance with room for at least 4 bytes
            ByteBladder bb = new ByteBladder(4, direct);

            // Make sure there are at least 4 bytes of room
            assertTrue("Not enough initial room made", bb.getRoom() >= 4);

            // Make the instance big enough for at least 32 bytes and verify
            bb.makeRoom(32);
            int room = bb.getRoom();
            assertTrue("Not enough room made", room >= 32);

            // Request for 16 bytes of room should do nothing since we
            // know we have at least 32 bytes of room already.
            bb.makeRoom(16);

            // Make sure we haven't done any work to change the room available.
            assertEquals("Room changed", room, bb.getRoom());

            direct = !direct;
        } while (direct);
    }

    @Test
    public void testLockstepGetsAndPuts() {
        // Test for direct and non-direct buffers.
        boolean direct = false;
        do {
            ByteBladder bb = new ByteBladder(0, direct);

            // Do lockstep gets and puts

            bb.putByte((byte)0xa);
            // A get at offset 0 from out is a 'peek' operation
            byte b = bb.getByte(0);
            assertEquals("byte put/peek mismatch", (byte)0xa, b);
            b = bb.getByte();
            assertEquals("byte put/get mismatch", (byte)0xa, b);

            byte[] ba = new byte[10];
            for (int i = 0; i < ba.length; i++) {
                ba[i] = (byte)i;
            }
            bb.putBytes(ba);
            Arrays.fill(ba, (byte)-1);
            bb.getBytes(0, ba);
            for (int i = 0; i < ba.length; i++) {
                assertEquals("byte[] put/peek mismatch", (byte)i, ba[i]);
            }
            Arrays.fill(ba, (byte)-1);
            bb.getBytes(ba);
            for (int i = 0; i < ba.length; i++) {
                assertEquals("byte[] put/get mismatch", (byte)i, ba[i]);
            }

            bb.putBytes(ba, 5, 2);
            Arrays.fill(ba, (byte)-1);
            bb.getBytes(0, ba, 5, 2);
            for (int i = 5; i < 7; i++) {
                assertEquals("byte[] subset put/peek mismatch", (byte)i, ba[i]);
            }
            Arrays.fill(ba, (byte)-1);
            bb.getBytes(ba, 5, 2);
            for (int i = 5; i < 7; i++) {
                assertEquals("byte[] subset put/get mismatch", (byte)i, ba[i]);
            }

            // TODO - put(ByteBuffer)
            // TODO - Array put/get methods

            bb.putChar('\u0bad');
            char c = bb.getChar(0);
            assertEquals("char put/peek mismatch", '\u0bad', c);
            c = bb.getChar();
            assertEquals("char put/get mismatch", '\u0bad', c);

            bb.putShort((short)0xabc);
            short s = bb.getShort(0);
            assertEquals("short put/peek mismatch", (short)0xabc, s);
            s = bb.getShort();
            assertEquals("short put/get mismatch", (short)0xabc, s);

            bb.putInt(0xdeadbeef);
            int it = bb.getInt(0);
            assertEquals("int put/peek mismatch", 0xdeadbeef, it);
            it = bb.getInt();
            assertEquals("int put/get mismatch", 0xdeadbeef, it);

            bb.putLong(0xdeadbeeffedb100dL);
            long l = bb.getLong(0);
            assertEquals("long put/peek mismatch", 0xdeadbeeffedb100dL, l);
            l = bb.getLong();
            assertEquals("long put/get mismatch", 0xdeadbeeffedb100dL, l);

            bb.putFloat(123.456f);
            float f = bb.getFloat(0);
            assertEquals("float put/peek mismatch", 123.456f, f, 0.0f);
            f = bb.getFloat();
            assertEquals("float put/get mismatch", 123.456f, f, 0.0f);

            bb.putDouble(456.789);
            double d = bb.getDouble(0);
            assertEquals("double put/peek mismatch", 456.789, d, 0.0f);
            d = bb.getDouble();
            assertEquals("double put/get mismatch", 456.789, d, 0.0f);

            direct = !direct;
        } while (direct);
    }

    @Test
    public void testSeparatedGetsAndPuts() {
        // Test for direct and non-direct buffers.
        boolean direct = false;
        do {
            ByteBladder bb = new ByteBladder(0, direct);

            // Now do all puts first and all gets afterwards

            bb.putByte((byte)0xa);

            byte[] ba = new byte[10];
            for (int i = 0; i < ba.length; i++) {
                ba[i] = (byte)i;
            }
            bb.putBytes(ba);
            bb.putBytes(ba, 5, 2);

            // TODO - put(ByteBuffer)
            // TODO - Array put/get methods

            bb.putChar('\u0bad');
            bb.putShort((short)0xabc);
            bb.putInt(0xdeadbeef);
            bb.putLong(0xdeadbeeffedb100dL);
            bb.putFloat(123.456f);
            bb.putDouble(456.789);

            byte b = bb.getByte();
            assertEquals("byte put/get mismatch", (byte)0xa, b);

            Arrays.fill(ba, (byte)-1);
            bb.getBytes(ba);
            for (int i = 0; i < ba.length; i++) {
                assertEquals("byte[] put/get mismatch", (byte)i, ba[i]);
            }

            Arrays.fill(ba, (byte)-1);
            bb.getBytes(ba, 5, 2);
            for (int i = 5; i < 7; i++) {
                assertEquals("byte[] subset put/get mismatch", (byte)i, ba[i]);
            }

            char c = bb.getChar();
            assertEquals("char put/get mismatch", '\u0bad', c);

            short s = bb.getShort();
            assertEquals("short put/get mismatch", (short)0xabc, s);

            int it = bb.getInt();
            assertEquals("int put/get mismatch", 0xdeadbeef, it);

            long l = bb.getLong();
            assertEquals("long put/get mismatch", 0xdeadbeeffedb100dL, l);

            float f = bb.getFloat();
            assertEquals("float put/get mismatch", 123.456f, f, 0.0f);

            double d = bb.getDouble();
            assertEquals("double put/get mismatch", 456.789, d, 0.0f);

            direct = !direct;
        } while (direct);
    }

    @Test
    public void testEnsureRoom() {
        ByteBladder bb = new ByteBladder(5);
        bb.putInt(10);
        bb.putInt(10);
        bb.putInt(10);
        assertEquals("First int", 10, bb.getInt());
        assertEquals("Second int", 10, bb.getInt());
        assertEquals("Third int", 10, bb.getInt());
        assertTrue("It did not increase", bb.getCapacity() > 5);

        // Tests that capacity was expanded properly.

        bb = new ByteBladder(5);
        bb.putInt(10);
        assertEquals("An int", 10, bb.getInt());
        bb.putInt(10);
        assertEquals("An int", 10, bb.getInt());
        bb.putInt(10);
        assertEquals("An int", 10, bb.getInt());
        assertEquals("Should not increase. Should reuse. It did increase", 5,
                     bb.getCapacity());
    }
    // TODO - tests for read() and write()

    @Test
    public void testPutGetByteArray() {
        ByteBladder bb = new ByteBladder();
        byte[] expectedArray = { 54, 23, 64, 0, (byte)254 };
        bb.putBytes(expectedArray);
        byte[] outputArray = new byte[5];
        bb.getBytes(outputArray, 0, 5);

        for (int i = 0; i < expectedArray.length; i++) {
            assertEquals("Array positions " + i + " are not equal",
                         expectedArray[i], outputArray[i]);
        }
    }

    // TODO - test for toStringBuilder() and toString()

    @Test
    public void testTrim() {
        ByteBladder bb = new ByteBladder(100);
        assertEquals("Capacity incorrect prior to trim", 100, bb.getCapacity());
        bb.putByte((byte)10);
        bb.trim();
        assertEquals("Capacity incorrect after trim", 1, bb.getCapacity());
    }

    @Test
    public void testProduceAndConsume() {
        ByteBladder bb = new ByteBladder(20);

        // Make sure produce advances in and ensures room
        bb.produce(5);
        assertEquals("In should have advanced 5", 5, bb.getIn());
        bb.produce(20);
        assertEquals("In should have advanced 20", 25, bb.getIn());
        assertTrue("Capacity should have expanded", bb.getCapacity() >= 25);

        // Now consume everything
        bb.consume(25);
        assertEquals("After consume in should match out",
                     bb.getIn(), bb.getOut());
    }
}

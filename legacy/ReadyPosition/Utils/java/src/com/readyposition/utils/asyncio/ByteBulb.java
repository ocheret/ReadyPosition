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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * The ByteBulb class is a convenience wrapper for the java.nio.ByteBuffer
 * class.  The benefits of ByteBulb include:
 * <ul>
 *   <li>ByteBulb's dynamically manages the size of the underlying buffer
 *       so that programmers's don't have to keep track themselves.
 *       With ByteBuffer, programmers need to pay a lot of attention to
 *       buffer size management and overflow issues.
 *   <li>ByteBuffer provides a multiple state model (a ByteBuffer is either
 *       set up for input or output) which is a common source of confusion
 *       and bugs (was flip() called last, or compact(), or what?).  ByteBulb
 *       corrects this by maintaining separate input and output pointers.
 *   <li>TODO - add more stuff...
 * </ul>
 *
 * This is the structure of a ByteBulb.  Empty cells are allocated
 * but have no valid data.  Cells marked with 'X' contain valid data.
 * <br>
 * <pre><code>
 * +-+-+-+-+-   -+-+-+-+-+-+-+-+-   -+-+-+-+-+-+-+-+-+-+-+-   -+-+-+
 * | | | | | ... | |X|X|X|X|X|X|X...X|X|X|X|X|X|X|X| | | | ... | | |
 * +-+-+-+-+-   -+-+-+-+-+-+-+-+-   -+-+-+-+-+-+-+-+-+-+-+-   -+-+-+
 *  ^               ^                               ^               ^
 *  |               |                               |               |
 *  0              out                              in           capacity
 * </code></pre>
 */
public class ByteBulb {
    /**
     * Constructs a new ByteBulb with a specified initial capacity and
     * which uses direct or non-direct ByteBuffers as specified.  The
     * in and out indices will both start out at 0, implying a
     * completely empty ByteBulb.
     *
     * @param direct if true then a direct ByteBuffer will be used otherwise
     *	a non-direct ByteBuffer will be used.
     * @param initialCapacity the initial number of bytes to be allocated.
     */
    public ByteBulb(boolean direct, int initialCapacity) {
	m_direct = direct;
	m_initialCapacity = initialCapacity;
	m_buffer = makeBuffer(m_initialCapacity);
	m_byteOrder = m_buffer.order();
    }

    /**
     * Constructs a new ByteBulb with a specified initial capacity and
     * which uses non-direct ByteBuffers.  The in and out indices will
     * both start out at 0, implying a completely empty ByteBulb.
     *
     * @param initialCapacity the initial number of bytes to be allocated.
     */
    public ByteBulb(int initialCapacity) {
	this(false, initialCapacity);
    }

    /**
     * Constructs a new ByteBulb with an initial capacity of 128 bytes
     * and which uses direct or non-direct ByteBuffers as specified.
     * The in and out indices will both start out at 0, implying a
     * completely empty ByteBulb.
     *
     * @param direct if true then a direct ByteBuffer will be used otherwise
     * a non-direct ByteBuffer will be used.
     */
    public ByteBulb(boolean direct) {
	this(direct, 128);
    }

    /**
     * Constructs a new ByteBulb with an initial capacity of 128 bytes
     * and which will use non-direct ByteBuffers.  The in and out
     * indices will both start out at 0, implying a completely empty
     * ByteBulb.
     */
    public ByteBulb() {
	this(false, 128);
    }

    /**
     * Allocates a ByteBuffer with a specified capacity.
     *
     * @param capacity the capacity of the ByteBuffer.
     */
    private ByteBuffer makeBuffer(int capacity) {
	ByteBuffer buffer = m_direct ?
	  ByteBuffer.allocateDirect(capacity) :
	  ByteBuffer.allocate(capacity);
	if (m_byteOrder != null) {
	    buffer.order(m_byteOrder);
	}
	return buffer;
    }

    /**
     * If true then direct ByteBuffers are used otherwise non-direct
     * ByteBuffers are used.
     */
    private boolean m_direct;

    /** Returns true if direct ByteBuffers are used otherwise false. */
    public boolean isDirect() {
	return m_direct;
    }

    /** The number of bytes to be allocated for new and cleared ByteBulbs. */
    private int m_initialCapacity;

    /** The number of bytes to be allocated for new and cleared ByteBulbs. */
    public int getInitialCapacity() {
	return m_initialCapacity;
    }

    /** The ByteBuffer that holds the byte data for this ByteBulb. */
    private ByteBuffer m_buffer;

    /** The ByteOrder to be used or null to use the default. */
    private ByteOrder m_byteOrder;

    /** Gets the ByteOrder in use. */
    public ByteOrder getByteOrder() {
	return m_byteOrder;
    }

    /** Sets the ByteOrder. */
    public void setByteOrder(ByteOrder byteOrder) {
	m_buffer.order(byteOrder);
	m_byteOrder = byteOrder;
    }

    /**
     * Gets the ByteBuffer managed by this ByteBulb such that it is
     * ready to have bytes read from it (suitable for passing to a
     * write() method).  That is, 'position' is set to 'out' and
     * 'limit' is set to 'in'.
     *
     * @return a ByteBuffer representing the valid data in the
     *  ByteBulb.  Modification of bytes in this ByteBuffer will
     *  affect the data in the ByteBulb.  The application is free to
     *  manipulate the ByteBuffer pointers at will.
     */
    public ByteBuffer getOutBuffer() {
	m_buffer.limit(m_in).position(m_out);
	return m_buffer;
    }

    /**
     * Gets the ByteBuffer managed by this ByteBulb such that it is
     * ready to have bytes written into it (suitable for passing to a
     * read() method).  That is, 'position' is set to 'in' and 'limit'
     * is set to 'capacity'.
     *
     * @return a ByteBuffer representing the unpopulated data at the
     *	end of the ByteBulb.  Modification of bytes in this ByteBuffer
     *	will affect the data in the ByteBulb.  The application is free
     *	to manipulate the ByteBuffer pointers at will.
     */
    public ByteBuffer getInBuffer() {
	m_buffer.limit(m_buffer.capacity()).position(m_in);
	return m_buffer;
    }

    /**
     * Gets the ByteBuffer managed by this ByteBulb such that it is
     * set up to have the data between the out mark and the out pointer
     * read.  This is useful for things like logging a set of bytes
     * that were just written out.
     *
     * @return a ByteBuffer representing the the section of bytes output
     *  since the out mark was set.
     */
    public ByteBuffer getOutMarkedBuffer() {
	if (m_outMark == -1) {
	    throw new InvalidMarkException();
	}
	m_buffer.limit(m_out).position(m_outMark);
	return m_buffer;
    }

    /**
     * Gets the ByteBuffer managed by this ByteBulb such that it is
     * set up to have the data between the in mark and the in pointer
     * read.  This is useful for things like logging a set of bytes
     * that were just read in.
     *
     * @return a ByteBuffer representing the the section of bytes input
     *  since the in mark was set.
     */
    public ByteBuffer getInMarkedBuffer() {
	if (m_inMark == -1) {
	    throw new InvalidMarkException();
	}
	m_buffer.limit(m_in).position(m_inMark);
	return m_buffer;
    }

    /** The index of the next byte to be consumed. */
    private int m_out;

    /** Gets the index of the next byte to be consumed. */
    public int getOut() {
	return m_out;
    }

    /** The value of m_out remembered when markOut() is called. */
    private int m_outMark = -1;

    /** Gets the out mark. */
    public int getOutMark() {
	if (m_outMark == -1) {
	    throw new InvalidMarkException();
	}
	return m_outMark;
    }

    /** Sets the out mark to the current out pointer. Clears the in mark. */
    public ByteBulb markOut() {
	m_outMark = m_out;
	m_inMark = -1;
	return this;
    }

    /**
     * Resets out to the out mark.
     *
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb resetOut() {
	if (m_outMark == -1) {
	    throw new InvalidMarkException();
	}
	m_out = m_outMark;
	return this;
    }

    /** The index of the next byte available to be filled. */
    private int m_in;

    /** Gets the index of the next byte available to be filled. */
    public int getIn() {
	return m_in;
    }

    /** The value of m_in remembered when markIn() is called. */
    private int m_inMark = -1;

    /** Gets the in mark. */
    public int getInMark() {
	if (m_inMark == -1) {
	    throw new InvalidMarkException();
	}
	return m_inMark;
    }

    /** Sets the in mark to the current in pointer.  Clears the out mark. */
    public ByteBulb markIn() {
	m_inMark = m_in;
	m_outMark = -1;
	return this;
    }

    /**
     * Resets in to the in mark.
     *
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb resetIn() {
	if (m_inMark == -1) {
	    throw new InvalidMarkException();
	}
	m_in = m_inMark;
	return this;
    }

    /** Gets the number of bytes that have been stored but not yet consumed. */
    public int getUsed() {
	return m_in - m_out;
    }

    /**
     * Returns the amount of space available for inserting new data at
     * the end of the buffer.
     */
    public int getRoom() {
	return m_buffer.capacity() - m_in;
    }

    /** Returns the amount of space allocated for this ByteBulb. */
    public int getCapacity() {
	return m_buffer.capacity();
    }

    /**
     * Resets the ByteBulb to its originally allocated state,
     * discarding all data.
     */
    public void clear() {
	m_buffer = makeBuffer(m_initialCapacity);
	m_out = m_in = 0;
	m_outMark = m_inMark = -1;
    }

    /**
     * Discards all data in the ByteBulb but does not reduce the size
     * of the allocation.
     */
    public void empty() {
	m_out = m_in = 0;
	m_outMark = m_inMark = -1;
    }

    /**
     * Ensures that at least the specified amount of space exists at
     * the end of the buffer (after the in pointer).  After this call
     * the ByteBuffer is ready to have data appended (as if compact() were
     * just called).  The outMark is always cleared since this method is
     * called in preparation of getting new input.
     *
     * @param room the amount of space required at the end of the ByteBulb.
     * @return the underlying ByteBuffer set up to have new data appended
     *	to the end.
     */
    private ByteBuffer ensureRoom(int room) {
	// Clear the out mark
	m_outMark = -1;

	int capacity = m_buffer.capacity();
	int deficiency = room - (capacity - m_in);
	if (deficiency <= 0) {
	    return getInBuffer();
	}

	int used = m_in - m_out;

	// Adjust the in mark.
	if (m_inMark != -1) {
	    m_inMark -= m_out;
	}

	if (deficiency > m_out) {
	    // Moving the data over will not be sufficient.
	    int required = capacity + deficiency;

	    // Try increasing capacity by 50% but if that's not enough
	    // then grow just enough.
	    capacity += capacity / 2;
	    if (capacity < required) {
		capacity = required;
	    }

	    // Make a new buffer and load it with the bytes of the
	    // current buffer.
	    ByteBuffer newbuff = makeBuffer(capacity);
	    newbuff.put(getOutBuffer());
	    m_buffer = newbuff;
	    m_out = 0;
	    m_in = used;
	} else {
	    // There is enough room in this buffer if we compact.
	    getOutBuffer().compact();
	    m_out = 0;
	    m_in = used;
	}
	return m_buffer;
    }

    /**
     * Ensures that at least the specified amount of space exists at
     * the end of the buffer (after the in pointer).  After this call
     * the ByteBuffer is ready to have data appended (as if compact() were
     * just called).  The outMark is always cleared since this method is
     * called in preparation of getting new input.
     *
     * @param room the amount of space required at the end of the ByteBulb.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb makeRoom(int room) {
	ensureRoom(room);
	return this;
    }

    // TODO - Add array put and get methods (e.g. put(double[] src))
    // TODO - Add absolute get and put methods.

    /**
     * Appends a byte to the end of the buffer (at 'in').  The ByteBulb is
     * grown as needed and the out mark is cleared.
     *
     * @param b the byte to append.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb put(byte b) {
	ensureRoom(1).put(b);
	m_in++;
	return this;
    }

    /**
     * Appends bytes to the end of the buffer (at 'in').  The ByteBulb is
     * grown as needed and the out mark is cleared.
     *
     * @param src the byte array to append.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb put(byte[] src) {
	int length = src.length;
	ensureRoom(length).put(src);
	m_in += length;
	return this;
    }

    /**
     * Appends a portion of a byte array to the end of the buffer (at
     * 'in').  The ByteBulb is grown as needed and the out mark is
     * cleared.
     *
     * @param src the byte array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of src to append.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb put(byte[] src, int offset, int length) {
	ensureRoom(length).put(src, offset, length);
	m_in += length;
	return this;
    }

    /**
     * Appends the contents of a ByteBuffer to the end of the buffer
     * (at 'in').  The ByteBulb is grown as needed and the out mark is
     * cleared.
     *
     * @param src the ByteBuffer to append.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb put(ByteBuffer src) {
	int length = src.remaining();
	ensureRoom(length).put(src);
	m_in += length;
	return this;
    }

    /**
     * Appends a boolean encoded as a byte (1=true, 0=false) to the
     * end of the buffer (at 'in').  The ByteBulb is grown as needed
     * and the out mark is cleared.
     *
     * @param x the boolean value to append.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb putBoolean(boolean x) {
	put(x ? (byte)1 : (byte)0);
	return this;
    }

    /**
     * Appends a char value to the end of the buffer (at 'in').  The
     * ByteBulb is grown as needed and the out mark is cleared.
     *
     * @param x the char value to append.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb putChar(char x) {
	int length = Types.CHAR_SIZE;
	ensureRoom(length).putChar(x);
	m_in += length;
	return this;
    }

    /**
     * Appends a short value to the end of the buffer (at 'in').  The
     * ByteBulb is grown as needed and the out mark is cleared.
     *
     * @param x the short value to append.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb putShort(short x) {
	int length = Types.SHORT_SIZE;
	ensureRoom(length).putShort(x);
	m_in += length;
	return this;
    }

    /**
     * Appends a int value to the end of the buffer (at 'in').  The
     * ByteBulb is grown as needed and the out mark is cleared.
     *
     * @param x the int value to append.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb putInt(int x) {
	int length = Types.INT_SIZE;
	ensureRoom(length).putInt(x);
	m_in += length;
	return this;
    }

    /**
     * Appends a long value to the end of the buffer (at 'in').  The
     * ByteBulb is grown as needed and the out mark is cleared.
     *
     * @param x the long value to append.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb putLong(long x) {
	int length = Types.LONG_SIZE;
	ensureRoom(length).putLong(x);
	m_in += length;
	return this;
    }

    /**
     * Appends a float value to the end of the buffer (at 'in').  The
     * ByteBulb is grown as needed and the out mark is cleared.
     *
     * @param x the float value to append.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb putFloat(float x) {
	int length = Types.FLOAT_SIZE;
	ensureRoom(length).putFloat(x);
	m_in += length;
	return this;
    }

    /**
     * Appends a double value to the end of the buffer (at 'in').  The
     * ByteBulb is grown as needed and the out mark is cleared.
     *
     * @param x the double value to append.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb putDouble(double x) {
	int length = Types.DOUBLE_SIZE;
	ensureRoom(length).putDouble(x);
	m_in += length;
	return this;
    }

    /**
     * Gets a byte value from the buffer and advances 'out'.
     * The in mark is cleared.
     *
     * @return the next available byte value in the buffer.
     */
    public byte get() {
	m_inMark = -1;
	byte x = getOutBuffer().get();
	m_out++;
	return x;
    }

    /**
     * Gets an array of byte values from the buffer and advances 'out'.
     * The in mark is cleared.
     *
     * @param dst an array of bytes to be filled from the buffer.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb get(byte[] dst) {
	return get(dst, 0, dst.length);
    }

    /**
     * Gets an array of byte values from the buffer and advances 'out'.
     * The in mark is cleared.
     *
     * @param dst an array of bytes to be filled from the buffer.
     * @param offset the offset into the destination array where bytes should
     *  be placed.
     * @param length the number of bytes to be transferred.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb get(byte[] dst, int offset, int length) {
	m_inMark = -1;
	getOutBuffer().get(dst, offset, length);
	m_out += length;
	return this;
    }

    /**
     * Gets a boolean value decoded from the next byte in the buffer
     * and advances 'out' where (byte)1 == true and (byte)0 == false.
     * The in mark is cleared.
     *
     * @return the decoded boolean value.
     */
    public boolean getBoolean() {
	// HACK! Throw an exception if value isn't 1 or 0?
	m_inMark = -1;
	return (byte)0 != get();
    }

    /**
     * Gets a char value from the buffer and advances 'out'.
     * The in mark is cleared.
     *
     * @return the next char value in the buffer.
     */
    public char getChar() {
	m_inMark = -1;
	char x = getOutBuffer().getChar();
	m_out = m_buffer.position();
	return x;
    }

    /**
     * Gets a short value from the buffer and advances 'out'.
     * The in mark is cleared.
     *
     * @return the next short value in the buffer.
     */
    public short getShort() {
	m_inMark = -1;
	short x = getOutBuffer().getShort();
	m_out = m_buffer.position();
	return x;
    }

    /**
     * Gets a int value from the buffer and advances 'out'.
     * The in mark is cleared.
     *
     * @return the next int value in the buffer.
     */
    public int getInt() {
	m_inMark = -1;
	int x = getOutBuffer().getInt();
	m_out = m_buffer.position();
	return x;
    }

    /**
     * Gets a long value from the buffer and advances 'out'.
     * The in mark is cleared.
     *
     * @return the next long value in the buffer.
     */
    public long getLong() {
	m_inMark = -1;
	long x = getOutBuffer().getLong();
	m_out = m_buffer.position();
	return x;
    }

    /**
     * Gets a float value from the buffer and advances 'out'.
     * The in mark is cleared.
     *
     * @return the next float value in the buffer.
     */
    public float getFloat() {
	m_inMark = -1;
	float x = getOutBuffer().getFloat();
	m_out = m_buffer.position();
	return x;
    }

    /**
     * Gets a double value from the buffer and advances 'out'.
     * The in mark is cleared.
     *
     * @return the next double value in the buffer.
     */
    public double getDouble() {
	m_inMark = -1;
	double x = getOutBuffer().getDouble();
	m_out = m_buffer.position();
	return x;
    }

    /**
     * Gets a byte value from the buffer without advancing 'out'.
     *
     * @return the next available byte value in the buffer.
     */
    public byte peek() {
	byte x = getOutBuffer().get();
	return x;
    }

    /**
     * Gets an array of byte values from the buffer without advancing 'out'.
     *
     * @param dst an array of bytes to be filled from the buffer.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb peek(byte[] dst) {
	return peek(dst, 0, dst.length);
    }

    /**
     * Gets an array of byte values from the buffer without advancing 'out'.
     *
     * @param dst an array of bytes to be filled from the buffer.
     * @param offset the offset into the destination array where bytes should
     *  be placed.
     * @param length the number of bytes to be transferred.
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb peek(byte[] dst, int offset, int length) {
	getOutBuffer().get(dst, offset, length);
	return this;
    }

    /**
     * Gets a boolean value decoded from the next byte in the buffer
     * without advancing 'out' where (byte)1 == true and (byte)0 == false.
     *
     * @return the decoded boolean value.
     */
    public boolean peekBoolean() {
	return (byte)1 == peek();
    }

    /**
     * Gets a char value from the buffer without advancing 'out'.
     *
     * @return the next char value in the buffer.
     */
    public char peekChar() {
	char x = getOutBuffer().getChar();
	return x;
    }

    /**
     * Gets a short value from the buffer without advancing 'out'.
     *
     * @return the next short value in the buffer.
     */
    public short peekShort() {
	short x = getOutBuffer().getShort();
	return x;
    }

    /**
     * Gets a int value from the buffer without advancing 'out'.
     *
     * @return the next int value in the buffer.
     */
    public int peekInt() {
	int x = getOutBuffer().getInt();
	return x;
    }

    /**
     * Gets a long value from the buffer without advancing 'out'.
     *
     * @return the next long value in the buffer.
     */
    public long peekLong() {
	long x = getOutBuffer().getLong();
	return x;
    }

    /**
     * Gets a float value from the buffer without advancing 'out'.
     *
     * @return the next float value in the buffer.
     */
    public float peekFloat() {
	float x = getOutBuffer().getFloat();
	return x;
    }

    /**
     * Gets a double value from the buffer without advancing 'out'.
     *
     * @return the next double value in the buffer.
     */
    public double peekDouble() {
	double x = getOutBuffer().getDouble();
	return x;
    }

    /**
     * Advances 'out' as if the specified number of bytes had been read from
     * the buffer.  The in mark is cleared.
     *
     * @param count the number of bytes to consume.  This may be a larger
     *  number than the bytes available in the buffer, in which case all
     *  bytes will be consumed.
     * @return the number of bytes actually consumed.
     */
    public int consume(int count) {
	if (count < 0) {
	    throw new IllegalArgumentException("count(" + count + " < 0");
	}
	m_inMark = -1;
	int used = m_in - m_out;
	if (count > used) {
	    throw new IllegalArgumentException("count(" + count +
	      ") > used(" + used + ')');
	}
	m_out += count;
	return count;
    }

    /**
     * Compacts the contents of the ByteBulb to fit exactly within
     * an underlying ByteBuffer.  The out mark is cleared.
     *
     * @return this ByteBulb so that invocations can be chained.
     */
    public ByteBulb trim() {
	m_outMark = -1;
	if (m_out != 0 || m_in != m_buffer.capacity()) {
	    if (m_inMark != -1) {
		m_inMark -= m_out;
	    }
	    int used = m_in - m_out;
	    ByteBuffer newbuff = makeBuffer(used);
	    newbuff.put(getOutBuffer());
	    m_buffer = newbuff;
	    m_out = 0;
	    m_in = used;
	}
	return this;
    }

    /**
     * Attempts to read at least a specified amount of data from a
     * channel.  If data is read, 'in' is adjusted accordingly.  The
     * out mark is cleared.
     *
     * @param channel the ReadableByteChannel to read bytes from.
     * @param length the minimum number of bytes to attempt to read.
     * @return the number of bytes actually read.
     */
    public int read(ReadableByteChannel channel, int length)
	throws IOException
    {
	int n = channel.read(ensureRoom(length));
	m_in += n;
	return n;
    }

    /**
     * Attempts to write the ByteBulb's used bytes to a channel.  If
     * data is written, 'out' is adjusted accordingly.  The in mark is
     * cleared.
     *
     * @param channel the WritableByteChannel to write bytes to.
     * @return the number of bytes actually written.
     */
    public int write(WritableByteChannel channel)
	throws IOException
    {
	m_inMark = -1;
	int n = channel.write(getOutBuffer());
	m_out += n;
	return n;
    }

    /**
     * Formats the ByteBulb and its contents into a provided StringBuffer.
     *
     * @param sb the StringBuffer in which to format the data.
     * @return the StringBuffer that was provided (sb).
     */
    public StringBuffer toStringBuffer(StringBuffer sb) {
	sb.append("ByteBulb: ");
	sb.append(getOutBuffer()).append('\n');
	Ascii.dump(sb, m_buffer);
	return sb;
    }

    /**
     * Formats the ByteBulb and its contents.
     *
     * @return a String containing the formatted data.
     */
    public String toString() {
	StringBuffer sb = new StringBuffer(1000);
	return toStringBuffer(sb).toString();
    }
}

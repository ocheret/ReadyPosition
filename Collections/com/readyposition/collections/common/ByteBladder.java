package com.readyposition.collections.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static com.readyposition.collections.common.Types.*;

/**
 * The ByteBladder class is a convenience wrapper for the
 * java.nio.ByteBuffer class.  ByteBuffer has a number of deficiencies
 * that make it difficult to use.  These include:
 * 
 * <ul>
 * 
 * <li>A ByteBuffer is fixed size.  If you need more space, you need a
 *     new ByteBuffer.  This is inconvenient as it may be difficult to
 *     know how large the buffer needs to be ahead of time without
 *     performing multiple passes over data.  It is also inconvenient
 *     because the size of a buffer needed to hold the incoming bytes
 *     that form a logical message may be unknown ahead of time.  With
 *     ByteBuffer programmers need to pay a lot of attention to buffer
 *     size management and overflow issues.
 * 
 * <li>ByteBuffer presents a strange bimodal interface where the
 *     buffer is either set up for input or output at any given time.
 *     There is no simple test to determine what mode the ByteBuffer
 *     is in (was flip() called last, or compact()...? Multiple
 *     pointers within the buffer must be tested).  There are no
 *     convenient ways to repeatedly switch back and forth between
 *     input and output modes to do efficient event driven
 *     asynchronous I/O.  This is error prone and common source of
 *     errors with ByteBuffers.
 * 
 * <li>ByteBuffer has a limited number of methods for reading and
 *     writing different types of values (e.g. arrays).
 * 
 * </ul>
 * 
 * The benefits of ByteBladder include:
 *
 * <ul>
 *
 * <li>ByteBladder's dynamically manages the size of the underlying
 *     buffer so that programmers's don't have to keep track
 *     themselves.
 *
 * <li>ByteBladder maintains separate input and output pointers to
 *     make it trivial to determine where data should be read from and
 *     written to.
 *
 * <li>ByteBladder provides absolute and relateive get and put
 *     operations for primtive values and arrays.
 *
 * <li>ByteBladder allows for seperate input and output marks to make
 *     it easier to scrutinize changes to the buffers due to read and
 *     write operations.
 *
 * <li>ByteBladder provides a number of methods to get access to the
 *     underlying ByteBuffer set up in the correct manner for reading
 *     and writing.
 *
 * </ul>
 *
 * This is the structure of a ByteBladder. Empty cells are allocated
 * but have no valid data. Cells marked with 'X' contain valid
 * data. <br>
 *
 * <pre><code>
 *    +-+-+-   -+-+-+-+-+-+-+-   -+-+-+-+-+-+-+-   -+-+-+
 *    | | | ... | |X|X|X|X|X|X...X|X|X|X|X|X| | ... | | |
 *    +-+-+-   -+-+-+-+-+-+-+-   -+-+-+-+-+-+-+-   -+-+-+
 *     ^           ^                         ^           ^
 *     |           |                         |           |
 *     0          out                        in       capacity
 * </code></pre>
 *
 * XXX - It may be useful to have this implement java.io.DataInput and
 * java.io.DataOutput.
 */
public class ByteBladder
{
    /**
     * If true then direct ByteBuffers are used otherwise non-direct
     * ByteBuffers are used.
     */
    private boolean m_direct;

    /** The number of bytes to be allocated for new and cleared ByteBladders. */
    private int m_initialCapacity;

    /** The ByteOrder to be used or null to use the default. */
    private ByteOrder m_byteOrder;

    /** The ByteBuffer that holds the byte data for this ByteBladder. */
    protected ByteBuffer m_buffer;

    /** The index of the next byte to be consumed. */
    protected int m_out;

    /** The value of m_out remembered when markOut() is called. */
    protected int m_outMark = -1;

    /** The index of the next byte available to be filled. */
    protected int m_in;

    /** The value of m_in remembered when markIn() is called. */
    protected int m_inMark = -1;

    /**
     * Constructs a new ByteBladder with a specified initial capacity
     * and which uses direct or non-direct ByteBuffers as
     * specified. The in and out indices will both start out at 0,
     * implying a completely empty ByteBladder.
     *
     * The new ByteBladder defaults to LittleEndian to be compatible
     * with Windows clients.
     *
     * @param initialCapacity the initial number of bytes to be
     *            allocated.
     * @param direct if true then a direct ByteBuffer will be used
     *            otherwise a non-direct ByteBuffer will be used.
     */
    public ByteBladder(int initialCapacity, boolean direct) {
        m_initialCapacity = initialCapacity;
        m_direct = direct;
        m_buffer = makeBuffer(m_initialCapacity);
        // m_byteOrder = m_buffer.order();
        setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Constructs a new ByteBladder with a specified initial capacity
     * and which uses non-direct ByteBuffers. The in and out indices
     * will both start out at 0, implying a completely empty
     * ByteBladder.
     *
     * @param initialCapacity
     *            the initial number of bytes to be allocated.
     */
    public ByteBladder(int initialCapacity) {
        this(initialCapacity, false);
    }

    /**
     * Constructs a new ByteBladder with an initial capacity of 128
     * bytes and which uses direct or non-direct ByteBuffers as
     * specified. The in and out indices will both start out at 0,
     * implying a completely empty ByteBladder.
     *
     * @param direct
     *            if true then a direct ByteBuffer will be used otherwise a
     *            non-direct ByteBuffer will be used.
     */
    public ByteBladder(boolean direct) {
        this(128, direct);
    }

    /**
     * Constructs a new ByteBladder with an initial capacity of 128
     * bytes and which will use non-direct ByteBuffers. The in and out
     * indices will both start out at 0, implying a completely empty
     * ByteBladder.
     */
    public ByteBladder() {
        this(128, false);
    }

    /**
     * Allocates a ByteBuffer with a specified capacity.
     * 
     * @param capacity
     *            the capacity of the ByteBuffer.
     */
    protected ByteBuffer makeBuffer(int capacity) {
        ByteBuffer buffer = m_direct ?
            ByteBuffer.allocateDirect(capacity) :
            ByteBuffer.allocate(capacity);
        if (m_byteOrder != null) {
            buffer.order(m_byteOrder);
        }
        return buffer;
    }

    /** Returns true if direct ByteBuffers are used otherwise false. */
    public boolean isDirect() {
        return m_direct;
    }

    /**
     * Gets the number of bytes to be allocated for new and cleared
     * ByteBladders.
     */
    public int getInitialCapacity() {
        return m_initialCapacity;
    }

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
     * Gets the ByteBuffer managed by this ByteBladder such that it is
     * ready to have bytes read from it (suitable for passing to a
     * write() method). That is, 'position' is set to 'out' and
     * 'limit' is set to 'in'.
     *
     * @return a ByteBuffer representing the valid data in the
     *         ByteBladder.  Modification of bytes in this ByteBuffer
     *         will affect the data in the ByteBladder. The
     *         application is free to manipulate the ByteBuffer
     *         pointers at will.
     */
    public ByteBuffer getOutBuffer() {
        m_buffer.limit(m_in).position(m_out);
        return m_buffer;
    }

    /**
     * Gets the ByteBuffer managed by this ByteBladder such that it is
     * ready to have bytes written into it (suitable for passing to a
     * read() method).  That is, 'position' is set to 'in' and 'limit'
     * is set to 'capacity'.
     *
     * @return a ByteBuffer representing the unpopulated data at the
     *         end of the ByteBladder. Modification of bytes in this
     *         ByteBuffer will affect the data in the ByteBladder. The
     *         application is free to manipulate the ByteBuffer
     *         pointers at will.
     */
    public ByteBuffer getInBuffer() {
        m_buffer.limit(m_buffer.capacity()).position(m_in);
        return m_buffer;
    }

    /**
     * Gets the ByteBuffer managed by this ByteBladder set up with
     * position and limit as explicitly specified.  The caller is in
     * total control here.  This does not affect the internal in or
     * out positions.  Note that limit gets set first on the
     * ByteBuffer followed by position.  This must be kept in mind as
     * position must always be less than or equal to limit.
     *
     * @param limit where to set the ByteBuffer's limit.
     * @param position where to set the ByteBuffer's position.
     * @return the appropriately set up ByteBuffer.
     */
    public ByteBuffer getCustomBuffer(int limit, int position) {
        m_buffer.limit(limit).position(position);
        return m_buffer;
    }

    /**
     * Gets the ByteBuffer managed by this ByteBladder such that it is
     * set up to have the data between the out mark and the out
     * pointer read. This is useful for things like logging a set of
     * bytes that were just written out.
     *
     * @return a ByteBuffer representing the the section of bytes
     *         output since the out mark was set.
     */
    public ByteBuffer getOutMarkedBuffer() {
        if (m_outMark == -1) {
            throw new InvalidMarkException();
        }
        m_buffer.limit(m_out).position(m_outMark);
        return m_buffer;
    }

    /**
     * Gets the ByteBuffer managed by this ByteBladder such that it is
     * set up to have the data between the in mark and the in pointer
     * read. This is useful for things like logging a set of bytes
     * that were just read in.
     *
     * @return a ByteBuffer representing the the section of bytes
     *         input since the in mark was set.
     */
    public ByteBuffer getInMarkedBuffer() {
        if (m_inMark == -1) {
            throw new InvalidMarkException();
        }
        m_buffer.limit(m_in).position(m_inMark);
        return m_buffer;
    }

    /** Gets the index of the next byte to be consumed. */
    public int getOut() {
        return m_out;
    }

    /** Gets the out mark. */
    public int getOutMark() {
        if (m_outMark == -1) {
            throw new InvalidMarkException();
        }
        return m_outMark;
    }

    /** Sets the out mark to the current out pointer. Clears the in mark. */
    public ByteBladder markOut() {
        m_outMark = m_out;
        m_inMark = -1;
        return this;
    }

    /**
     * Resets out to the out mark.
     *
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder resetOut() {
        if (m_outMark == -1) {
            throw new InvalidMarkException();
        }
        m_out = m_outMark;
        return this;
    }

    /** Gets the index of the next byte available to be filled. */
    public int getIn() {
        return m_in;
    }

    /** Gets the in mark. */
    public int getInMark() {
        if (m_inMark == -1) {
            throw new InvalidMarkException();
        }
        return m_inMark;
    }

    /** Sets the in mark to the current in pointer. Clears the out mark. */
    public ByteBladder markIn() {
        m_inMark = m_in;
        m_outMark = -1;
        return this;
    }

    /**
     * Resets in to the in mark.
     *
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder resetIn() {
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
     * Returns the amount of space available for inserting new data at the end
     * of the buffer.
     */
    public int getRoom() {
        return m_buffer.capacity() - m_in;
    }

    /** Returns the amount of space allocated for this ByteBladder. */
    public int getCapacity() {
        return m_buffer.capacity();
    }

    /**
     * Resets the ByteBladder to its originally allocated state, discarding all
     * data.
     */
    public void reset() {
        m_buffer = makeBuffer(m_initialCapacity);
        clear();
    }

    /**
     * Discards all data in the ByteBladder but does not reduce the size of the
     * allocation.
     */
    public void clear() {
        m_out = m_in = 0;
        m_outMark = m_inMark = -1;
    }

    /**
     * Ensures that at least the specified amount of space exists at
     * the end of the buffer (after the in pointer). After this call
     * the ByteBuffer is ready to have data appended (as if compact()
     * were just called). The outMark is always cleared since this
     * method is called in preparation of getting new input.
     *
     * @param room the amount of space required at the end of the
     *        ByteBladder.
     * @return the underlying ByteBuffer set up to have new data
     *         appended to the end.
     */
    protected ByteBuffer ensureRoom(int room) {
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
     * the end of the buffer (after the in pointer). After this call
     * the ByteBuffer is ready to have data appended (as if compact()
     * were just called). The outMark is always cleared since this
     * method is called in preparation of getting new input.
     *
     * @param room the amount of space required at the end of the
     *        ByteBladder.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder makeRoom(int room) {
        ensureRoom(room);
        return this;
    }

    /**
     * Appends the contents of a ByteBuffer to the end of the buffer
     * (at 'in').  The ByteBladder is grown as needed and the out mark
     * is cleared.
     *
     * @param src
     *            the ByteBuffer to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder put(ByteBuffer src) {
        int length = src.remaining();
        ensureRoom(length).put(src);
        m_in += length;
        return this;
    }

    /**
     * Converts a span of an array of booleans to an array of bytes.
     * 
     * @param x array of booleans
     * @param offset offset of span within the array
     * @param length length of span within the array
     * @return an array of bytes with the length of the span containing 0's for
     *         false entries, and 1's for true
     */
    public static byte[] booleansToBytes(boolean[] x, int offset, int length) {
        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            b[i] = x[offset + i] ? (byte)1 : (byte)0;
        }
        return b;
    }

    /**
     * Decodes an array of bytes into a span of array of booleans.
     * 
     * @param dst the array of booleans
     * @param src the array of bytes
     * @param offset the offset of the span within the array of booleans
     * @param length the length of the span within the array of booleans
     */
    public static void bytesToBooleans(boolean[] dst, byte[] src, int offset, int length) {
        for (int i = 0; i < length; i++) {
            dst[offset + i] = (src[i] != (byte)0);
        }
    }

    /**
     * Appends a boolean value to the end of the buffer (at 'in'). The
     * ByteBladder is grown as needed and the out mark is cleared.
     *
     * @param x the boolean value to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putBoolean(boolean x) {
        return putByte(x ? (byte)1 : (byte)0);
    }

    /**
     * Ensures room in the buffer and places a value in the buffer at
     * the index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the value
     */
    public void putBoolean(int index, boolean x) {
        putByte(index, x ? (byte)1 : (byte)0);
    }

    /**
     * Appends an array of boolean values to the end of the buffer (at
     * 'in'). The ByteBladder is grown as needed and the out mark is
     * cleared.
     *
     * @param x the boolean array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putBooleans(boolean[] x) {
        byte[] bytes = booleansToBytes(x, 0, x.length);
        return putBytes(bytes);
    }

    /**
     * Appends a portion of a boolean array to the end of the buffer
     * (at 'in'). The ByteBladder is grown as needed and the out mark
     * is cleared.
     *
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of src to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putBooleans(boolean[] x, int offset, int length) {
        byte[] bytes = booleansToBytes(x, offset, length);
        return putBytes(bytes);
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putBooleans(int index, boolean[] x) {
        return putBooleans(index, x, 0, x.length);
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of x to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putBooleans(int index, boolean[] x, int offset,
      int length)
    {
        byte[] bytes = booleansToBytes(x, offset, length);
        return putBytes(index, bytes);
    }

    /**
     * Gets a boolean value from the buffer and advances 'out'. The in mark is
     * cleared.
     *
     * @return the next boolean value in the buffer.
     */
    public boolean getBoolean() {
        return (getByte() != (byte)0);
    }

    /**
     * Gets a boolean value from the buffer at the index based on 'out'. If 'in'
     * changes or the bladder produces, the indexes remain valid, even if the
     * bladder is resized. If 'out' changes or the bladder is consumed, any
     * known indexes become invalid.
     *
     * @param index The index for the boolean value based on 'out'.
     * @return the boolean value at the index.
     */
    public boolean getBoolean(int index) {
        return (getByte(index) != (byte)0);
    }

    /**
     * Gets an array of boolean values from the buffer and advances
     * 'out'. The in mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public boolean[] getBooleans(boolean[] x) {
        return getBooleans(x, 0, x.length);
    }

    /**
     * Gets an array of boolean values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public boolean[] getBooleans(boolean[] x, int offset, int length) {
        byte[] bytes = new byte[length];
        getBytes(bytes, 0, length);
        bytesToBooleans(x, bytes, offset, length);
        return x;
    }

    /**
     * Gets an array of boolean values from a specified location in the buffer.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public boolean[] getBooleans(int index, boolean[] x) {
        return getBooleans(index, x, 0, x.length);
    }

    /**
     * Gets an array of boolean values from a specified location in the buffer.
     *
     * @param index the location to place the value
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public boolean[] getBooleans(int index, boolean[] x, int offset, int length) {
        byte[] bytes = new byte[length];
        getBytes(index, bytes, 0, length);
        bytesToBooleans(x, bytes, offset, length);
        return x;
    }

    /**
     * Appends a char value to the end of the buffer (at 'in'). The
     * ByteBladder is grown as needed and the out mark is cleared.
     *
     * @param x the char value to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putChar(char x) {
        int length = CHAR.getSize();
        ensureRoom(length).putChar(x);
        m_in += length;
        return this;
    }

    /**
     * Ensures room in the buffer and places a value in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value.
     * @param x the value.
     */
    public void putChar(int index, char x) {
        ensureRoom(m_out + index + CHAR.getSize() - m_in);
        m_buffer.putChar(m_out + index, x);
    }

    /**
     * Appends an array of char values to the end of the buffer (at
     * 'in'). The ByteBladder is grown as needed and the out mark is
     * cleared.
     *
     * @param x the char array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putChars(char[] x) {
        return putChars(x, 0, x.length);
    }

    /**
     * Appends a portion of a char array to the end of the buffer (at
     * 'in'). The ByteBladder is grown as needed and the out mark is
     * cleared.
     *
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of src to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putChars(char[] x, int offset, int length) {
        int room = CHAR.getSize() * length;
        ensureRoom(room);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            m_buffer.putChar(x[i]);
        }
        m_in += room;
        return this;
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putChars(int index, char[] x) {
        return putChars(index, x, 0, x.length);
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of x to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putChars(int index, char[] x, int offset, int length) {
        int room = CHAR.getSize() * length;
        ensureRoom(m_out + index + room - m_in);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            m_buffer.putChar(x[i]);
        }
        return this;
    }

    /**
     * Gets a char value from the buffer and advances 'out'. The in mark is
     * cleared.
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
     * Gets a char value from the buffer at the index based on 'out'. If 'in'
     * changes or the bladder produces, the indexes remain valid, even if the
     * bladder is resized. If 'out' changes or the bladder is consumed, any
     * known indexes become invalid.
     *
     * @param index The index for the char value based on 'out'.
     * @return the char value at the index.
     */
    public char getChar(int index) {
        return m_buffer.getChar(m_out + index);
    }

    /**
     * Gets an array of char values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public char[] getChars(char[] x) {
        return getChars(x, 0, x.length);
    }

    /**
     * Gets an array of char values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public char[] getChars(char[] x, int offset, int length) {
        ByteBuffer bb = getOutBuffer();
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            x[i] = bb.getChar();
        }
        m_out = m_buffer.position();
        return x;
    }

    /**
     * Gets an array of char values from a specified location in the buffer.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public char[] getChars(int index, char[] x) {
        return getChars(index, x, 0, x.length);
    }

    /**
     * Gets an array of char values from a specified location in the buffer.
     *
     * @param index the location to place the value
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public char[] getChars(int index, char[] x, int offset, int length) {
        int bufferOffset = m_out + index;
        for (int i = 0; i < length; i++) {
            x[offset + i] = m_buffer.getChar(bufferOffset + i);
        }
        return x;
    }

    /**
     * Appends a byte to the end of the buffer (at 'in'). The ByteBladder is
     * grown as needed and the out mark is cleared.
     *
     * @param b the byte to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putByte(byte b) {
        ensureRoom(1).put(b);
        m_in++;
        return this;
    }

    /**
     * Ensures room in the buffer and places a value in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value.
     * @param x the value.
     */
    public void putByte(int index, byte x) {
        ensureRoom(m_out + index + BYTE.getSize() - m_in);
        m_buffer.put(m_out + index, x);
    }

    /**
     * Appends bytes to the end of the buffer (at 'in'). The ByteBladder is
     * grown as needed and the out mark is cleared.
     *
     * @param src the byte array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putBytes(byte[] src) {
        int length = src.length;
        ensureRoom(length).put(src);
        m_in += length;
        return this;
    }

    /**
     * Appends a portion of a byte array to the end of the buffer (at 'in'). The
     * ByteBladder is grown as needed and the out mark is cleared.
     *
     * @param src the byte array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of src to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putBytes(byte[] src, int offset, int length) {
        ensureRoom(length).put(src, offset, length);
        m_in += length;
        return this;
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param src the char array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putBytes(int index, byte[] src) {
        return putBytes(index, src, 0, src.length);
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param src the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of src to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putBytes(int index, byte[] src, int offset, int length) {
        ensureRoom(m_out + length - m_in).put(src, offset, length);
        return this;
    }

    /**
     * Gets a byte value from the buffer and advances 'out'. The in mark is
     * cleared.
     *
     * @return the next available byte value in the buffer.
     */
    public byte getByte() {
        m_inMark = -1;
        byte x = getOutBuffer().get();
        m_out++;
        return x;
    }

    /**
     * Gets a byte value from the buffer at the index based on
     * 'out'. If 'in' changes or the bladder produces, the indexes
     * remain valid, even if the bladder is resized. If 'out' changes
     * or the bladder is consumed, any known indexes become invalid.
     *
     * @param index The index for the byte value based on 'out'.
     * @return the byte value at the index.
     */
    public byte getByte(int index) {
        return m_buffer.get(m_out + index);
    }

    /**
     * Gets an array of byte values from the buffer and advances
     * 'out'. The in mark is cleared.
     *
     * @param dst an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public byte[] getBytes(byte[] dst) {
        return getBytes(dst, 0, dst.length);
    }

    /**
     * Gets an array of byte values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param dst an array of bytes to be filled from the buffer.
     * @param offset the offset into the destination array where bytes should be placed.
     * @param length the number of bytes to be transferred.
     * @return this ByteBladder so that invocations can be chained.
     */
    public byte[] getBytes(byte[] dst, int offset, int length) {
        m_inMark = -1;
        getOutBuffer().get(dst, offset, length);
        m_out += length;
        return dst;
    }

    /**
     * Gets an array of byte values from a specified location in the buffer.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public byte[] getBytes(int index, byte[] x) {
        return getBytes(index, x, 0, x.length);
    }

    /**
     * Gets an array of byte values from a specified location in the buffer.
     *
     * @param index the location to place the value
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public byte[] getBytes(int index, byte[] x, int offset, int length) {
        m_buffer.limit(m_buffer.capacity()).position(m_out + index);
        m_buffer.get(x, offset, length);
        return x;
    }

    /**
     * Appends a short value to the end of the buffer (at 'in'). The ByteBladder
     * is grown as needed and the out mark is cleared.
     *
     * @param x the short value to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putShort(short x) {
        int length = SHORT.getSize();
        ensureRoom(length).putShort(x);
        m_in += length;
        return this;
    }

    /**
     * Ensures room in the buffer and places a value in the buffer at
     * the index relative to 'out'.
     *
     * @param index the location to place the value.
     * @param x the value.
     */
    public void putShort(int index, short x) {
        ensureRoom(m_out + index + SHORT.getSize() - m_in);
        m_buffer.putShort(m_out + index, x);
    }

    /**
     * Appends an array of short values to the end of the buffer (at
     * 'in'). The ByteBladder is grown as needed and the out mark is
     * cleared.
     *
     * @param x the short array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putShorts(short[] x) {
        return putShorts(x, 0, x.length);
    }

    /**
     * Appends a portion of a short array to the end of the buffer (at
     * 'in'). The ByteBladder is grown as needed and the out mark is
     * cleared.
     *
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of src to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putShorts(short[] x, int offset, int length) {
        int room = SHORT.getSize() * length;
        ensureRoom(room);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            m_buffer.putShort(x[i]);
        }
        m_in += room;
        return this;
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putShorts(int index, short[] x) {
        return putShorts(index, x, 0, x.length);
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of x to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putShorts(int index, short[] x, int offset, int length) {
        int room = SHORT.getSize() * length;
        ensureRoom(m_out + index + room - m_in);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            m_buffer.putShort(x[i]);
        }
        return this;
    }

    /**
     * Gets a short value from the buffer and advances 'out'. The in mark is
     * cleared.
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
     * Gets a short value from the buffer at the index based on 'out'. If 'in'
     * changes or the bladder produces, the indexes remain valid, even if the
     * bladder is resized. If 'out' changes or the bladder is consumed, any
     * known indexes become invalid.
     *
     * @param index The index for the short value based on 'out'.
     * @return the short value at the index.
     */
    public short getShort(int index) {
        return m_buffer.getShort(m_out + index);
    }

    /**
     * Gets an array of short values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public short[] getShorts(short[] x) {
        return getShorts(x, 0, x.length);
    }

    /**
     * Gets an array of short values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public short[] getShorts(short[] x, int offset, int length) {
        ByteBuffer bb = getOutBuffer();
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            x[i] = bb.getShort();
        }
        m_out = m_buffer.position();
        return x;
    }

    /**
     * Gets an array of short values from a specified location in the buffer.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public short[] getShorts(int index, short[] x) {
        return getShorts(index, x, 0, x.length);
    }

    /**
     * Gets an array of short values from a specified location in the buffer.
     *
     * @param index the location to place the value
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public short[] getShorts(int index, short[] x, int offset, int length) {
        int bufferOffset = m_out + index;
        for (int i = 0; i < length; i++) {
            x[offset + i] = m_buffer.getShort(bufferOffset + i);
        }
        return x;
    }

    /**
     * Appends a int value to the end of the buffer (at 'in'). The ByteBladder
     * is grown as needed and the out mark is cleared.
     *
     * @param x the int value to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putInt(int x) {
        int length = INT.getSize();
        ensureRoom(length).putInt(x);
        m_in += length;
        return this;
    }

    /**
     * Ensures room in the buffer and places a value in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value.
     * @param x the value.
     */
    public void putInt(int index, int x) {
        ensureRoom(m_out + index + INT.getSize() - m_in);
        m_buffer.putInt(m_out + index, x);
    }

    /**
     * Appends an array of int values to the end of the buffer (at
     * 'in'). The ByteBladder is grown as needed and the out mark is
     * cleared.
     *
     * @param x the int array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putInts(int[] x) {
        return putInts(x, 0, x.length);
    }

    /**
     * Appends a portion of a int array to the end of the buffer (at
     * 'in'). The ByteBladder is grown as needed and the out mark is
     * cleared.
     *
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of src to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putInts(int[] x, int offset, int length) {
        int room = INT.getSize() * length;
        ensureRoom(room);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            m_buffer.putInt(x[i]);
        }
        m_in += room;
        return this;
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putInts(int index, int[] x) {
        return putInts(index, x, 0, x.length);
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of x to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putInts(int index, int[] x, int offset, int length) {
        int room = INT.getSize() * length;
        ensureRoom(m_out + index + room - m_in);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            m_buffer.putInt(x[i]);
        }
        return this;
    }

    /**
     * Gets a int value from the buffer and advances 'out'. The in mark is
     * cleared.
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
     * Gets a int value from the buffer at the index based on 'out'. If 'in'
     * changes or the bladder produces, the indexes remain valid, even if the
     * bladder is resized. If 'out' changes or the bladder is consumed, any
     * known indexes become invalid.
     *
     * @param index The index for the int value based on 'out'.
     * @return the int value at the index.
     */
    public int getInt(int index) {
        return m_buffer.getInt(m_out + index);
    }

    /**
     * Gets an array of int values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public int[] getInts(int[] x) {
        return getInts(x, 0, x.length);
    }

    /**
     * Gets an array of int values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public int[] getInts(int[] x, int offset, int length) {
        ByteBuffer bb = getOutBuffer();
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            x[i] = bb.getInt();
        }
        m_out = m_buffer.position();
        return x;
    }

    /**
     * Gets an array of int values from a specified location in the buffer.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public int[] getInts(int index, int[] x) {
        return getInts(index, x, 0, x.length);
    }

    /**
     * Gets an array of int values from a specified location in the buffer.
     *
     * @param index the location to place the value
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public int[] getInts(int index, int[] x, int offset, int length) {
        int bufferOffset = m_out + index;
        for (int i = 0; i < length; i++) {
            x[offset + i] = m_buffer.getInt(bufferOffset + i);
        }
        return x;
    }

    /**
     * Appends a long value to the end of the buffer (at 'in'). The ByteBladder
     * is grown as needed and the out mark is cleared.
     *
     * @param x the long value to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putLong(long x) {
        int length = LONG.getSize();
        ensureRoom(length).putLong(x);
        m_in += length;
        return this;
    }

    /**
     * Ensures room in the buffer and places a value in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value.
     * @param x the value.
     */
    public void putLong(int index, long x) {
        ensureRoom(m_out + index + LONG.getSize() - m_in);
        m_buffer.putLong(m_out + index, x);
    }

    /**
     * Appends an array of long values to the end of the buffer (at
     * 'in'). The ByteBladder is grown as needed and the out mark is
     * cleared.
     *
     * @param x the long array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putLongs(long[] x) {
        return putLongs(x, 0, x.length);
    }

    /**
     * Appends a portion of a long array to the end of the buffer (at
     * 'in'). The ByteBladder is grown as needed and the out mark is
     * cleared.
     *
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of src to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putLongs(long[] x, int offset, int length) {
        int room = LONG.getSize() * length;
        ensureRoom(room);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            m_buffer.putLong(x[i]);
        }
        m_in += room;
        return this;
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putLongs(int index, long[] x) {
        return putLongs(index, x, 0, x.length);
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of x to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putLongs(int index, long[] x, int offset, int length) {
        int room = LONG.getSize() * length;
        ensureRoom(m_out + index + room - m_in);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            m_buffer.putLong(x[i]);
        }
        return this;
    }

    /**
     * Gets a long value from the buffer and advances 'out'. The in mark is
     * cleared.
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
     * Gets a long value from the buffer at the index based on 'out'. If 'in'
     * changes or the bladder produces, the indexes remain valid, even if the
     * bladder is resized. If 'out' changes or the bladder is consumed, any
     * known indexes become invalid.
     *
     * @param index The index for the long value based on 'out'.
     * @return the long value at the index.
     */
    public long getLong(int index) {
        return m_buffer.getLong(m_out + index);
    }

    /**
     * Gets an array of long values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public long[] getLongs(long[] x) {
        return getLongs(x, 0, x.length);
    }

    /**
     * Gets an array of long values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public long[] getLongs(long[] x, int offset, int length) {
        ByteBuffer bb = getOutBuffer();
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            x[i] = bb.getLong();
        }
        m_out = m_buffer.position();
        return x;
    }

    /**
     * Gets an array of long values from a specified location in the buffer.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public long[] getLongs(int index, long[] x) {
        return getLongs(index, x, 0, x.length);
    }

    /**
     * Gets an array of long values from a specified location in the buffer.
     *
     * @param index the location to place the value
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public long[] getLongs(int index, long[] x, int offset, int length) {
        int bufferOffset = m_out + index;
        for (int i = 0; i < length; i++) {
            x[offset + i] = m_buffer.getLong(bufferOffset + i);
        }
        return x;
    }

    /**
     * Appends a float value to the end of the buffer (at 'in'). The ByteBladder
     * is grown as needed and the out mark is cleared.
     *
     * @param x
     *            the float value to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putFloat(float x) {
        int length = FLOAT.getSize();
        ensureRoom(length).putFloat(x);
        m_in += length;
        return this;
    }

    /**
     * Ensures room in the buffer and places a value in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value.
     * @param x the value.
     */
    public void putFloat(int index, float x) {
        ensureRoom(m_out + index + FLOAT.getSize() - m_in);
        m_buffer.putFloat(m_out + index, x);
    }

    /**
     * Appends an array of float values to the end of the buffer (at
     * 'in'). The ByteBladder is grown as needed and the out mark is
     * cleared.
     *
     * @param x the float array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putFloats(float[] x) {
        return putFloats(x, 0, x.length);
    }

    /**
     * Appends a portion of a float array to the end of the buffer (at
     * 'in'). The ByteBladder is grown as needed and the out mark is
     * cleared.
     *
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of src to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putFloats(float[] x, int offset, int length) {
        int room = FLOAT.getSize() * length;
        ensureRoom(room);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            m_buffer.putFloat(x[i]);
        }
        m_in += room;
        return this;
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putFloats(int index, float[] x) {
        return putFloats(index, x, 0, x.length);
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of x to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putFloats(int index, float[] x, int offset, int length) {
        int room = FLOAT.getSize() * length;
        ensureRoom(m_out + index + room - m_in);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            m_buffer.putFloat(x[i]);
        }
        return this;
    }

    /**
     * Gets a float value from the buffer and advances 'out'. The in mark is
     * cleared.
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
     * Gets a float value from the buffer at the index based on 'out'. If 'in'
     * changes or the bladder produces, the indexes remain valid, even if the
     * bladder is resized. If 'out' changes or the bladder is consumed, any
     * known indexes become invalid.
     *
     * @param index The index for the float value based on 'out'.
     * @return the float value at the index.
     */
    public float getFloat(int index) {
        return m_buffer.getFloat(m_out + index);
    }

    /**
     * Gets an array of float values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public float[] getFloats(float[] x) {
        return getFloats(x, 0, x.length);
    }

    /**
     * Gets an array of float values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public float[] getFloats(float[] x, int offset, int length) {
        ByteBuffer bb = getOutBuffer();
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            x[i] = bb.getFloat();
        }
        m_out = m_buffer.position();
        return x;
    }

    /**
     * Gets an array of float values from a specified location in the buffer.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public float[] getFloats(int index, float[] x) {
        return getFloats(index, x, 0, x.length);
    }

    /**
     * Gets an array of float values from a specified location in the buffer.
     *
     * @param index the location to place the value
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public float[] getFloats(int index, float[] x, int offset, int length) {
        int bufferOffset = m_out + index;
        for (int i = 0; i < length; i++) {
            x[offset + i] = m_buffer.getFloat(bufferOffset + i);
        }
        return x;
    }

    /**
     * Appends a double value to the end of the buffer (at 'in'). The
     * ByteBladder is grown as needed and the out mark is cleared.
     *
     * @param x the double value to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putDouble(double x) {
        int length = DOUBLE.getSize();
        ensureRoom(length).putDouble(x);
        m_in += length;
        return this;
    }

    /**
     * Ensures room in the buffer and places a value in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value.
     * @param x the value.
     */
    public void putDouble(int index, double x) {
        ensureRoom(m_out + index + DOUBLE.getSize() - m_in);
        m_buffer.putDouble(m_out + index, x);
    }

    /**
     * Appends an array of double values to the end of the buffer (at
     * 'in'). The ByteBladder is grown as needed and the out mark is
     * cleared.
     *
     * @param x the double array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putDoubles(double[] x) {
        return putDoubles(x, 0, x.length);
    }

    /**
     * Appends a portion of a double array to the end of the buffer (at
     * 'in'). The ByteBladder is grown as needed and the out mark is
     * cleared.
     *
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of src to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putDoubles(double[] x, int offset, int length) {
        int room = DOUBLE.getSize() * length;
        ensureRoom(room);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            m_buffer.putDouble(x[i]);
        }
        m_in += room;
        return this;
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putDoubles(int index, double[] x) {
        return putDoubles(index, x, 0, x.length);
    }

    /**
     * Ensures room in the buffer and places an array in the buffer at the
     * index relative to 'out'.
     *
     * @param index the location to place the value
     * @param x the array to append.
     * @param offset the first position in src to append.
     * @param length the number of elements of x to append.
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder putDoubles(int index, double[] x, int offset, int length) {
        int room = DOUBLE.getSize() * length;
        ensureRoom(m_out + index + room - m_in);
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            m_buffer.putDouble(x[i]);
        }
        return this;
    }

    /**
     * Gets a double value from the buffer and advances 'out'. The in mark is
     * cleared.
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
     * Gets a double value from the buffer at the index based on 'out'. If 'in'
     * changes or the bladder produces, the indexes remain valid, even if the
     * bladder is resized. If 'out' changes or the bladder is consumed, any
     * known indexes become invalid.
     *
     * @param index The index for the double value based on 'out'.
     * @return the double value at the index.
     */
    public double getDouble(int index) {
        return m_buffer.getDouble(m_out + index);
    }

    /**
     * Gets an array of double values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public double[] getDoubles(double[] x) {
        return getDoubles(x, 0, x.length);
    }

    /**
     * Gets an array of double values from the buffer and advances 'out'. The in
     * mark is cleared.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public double[] getDoubles(double[] x, int offset, int length) {
        ByteBuffer bb = getOutBuffer();
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            x[i] = bb.getDouble();
        }
        m_out = m_buffer.position();
        return x;
    }

    /**
     * Gets an array of double values from a specified location in the buffer.
     *
     * @param x an array of bytes to be filled from the buffer.
     * @return this ByteBladder so that invocations can be chained.
     */
    public double[] getDoubles(int index, double[] x) {
        return getDoubles(index, x, 0, x.length);
    }

    /**
     * Gets an array of double values from a specified location in the buffer.
     *
     * @param index the location to place the value
     * @param x an array of bytes to be filled from the buffer.
     * @param offset the offset into x at which to start putting values.
     * @param length the number of values to copy.
     * @return this ByteBladder so that invocations can be chained.
     */
    public double[] getDoubles(int index, double[] x, int offset, int length) {
        int bufferOffset = m_out + index;
        for (int i = 0; i < length; i++) {
            x[offset + i] = m_buffer.getDouble(bufferOffset + i);
        }
        return x;
    }

    /**
     * Advances 'in' as if the specified number of bytes had be written to the
     * buffer. The out mark is cleared.
     *
     * Example of usage - Advance the in buffer
     *
     * @param count the number of bytes to advance. This may be a
     *              larger number than the capacity of the buffer, in
     *              which case the buffer will be enlarged.
     * @throws IllegalArgumentException If count < 0
     */
    public void produce(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count(" + count + " < 0");
        }
        m_outMark = -1;
        ensureRoom(count);
        m_in += count;
    }

    /**
     * Advances 'out' as if the specified number of bytes had been read from the
     * buffer. The in mark is cleared.
     *
     * @param count the number of bytes to consume.
     * @throws IllegalArgumentException If count < 0 or count > used
     */
    public void consume(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count(" + count + ") < 0");
        }
        m_inMark = -1;
        int used = m_in - m_out;
        if (count > used) {
            throw new IllegalArgumentException("count(" + count + ") > used("
                    + used + ')');
        }
        m_out += count;
    }

    /**
     * Compacts the contents of the ByteBladder to fit exactly within an
     * underlying ByteBuffer. The out mark is cleared.
     *
     * @return this ByteBladder so that invocations can be chained.
     */
    public ByteBladder trim() {
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
     * Attempts to read at least a specified amount of data from a channel. If
     * data is read, 'in' is adjusted accordingly. The out mark is cleared.
     *
     * @param channel the ReadableByteChannel to read bytes from.
     * @param length the minimum number of bytes to attempt to read.
     * @return the number of bytes actually read.
     */
    public int read(ReadableByteChannel channel, int length)
        throws IOException
    {
        int n = channel.read(ensureRoom(length));
        if (n > 0) {
            m_in += n;
        }
        return n;
    }

    /**
     * Attempts to write the ByteBladder's used bytes to a channel. If data is
     * written, 'out' is adjusted accordingly. The in mark is cleared.
     *
     * @param channel the WritableByteChannel to write bytes to.
     * @return the number of bytes actually written.
     */
    public int write(WritableByteChannel channel) throws IOException {
        m_inMark = -1;
        int n = channel.write(getOutBuffer());
        if (n > 0) {
            m_out += n;
        }
        return n;
    }

    /**
     * Formats the ByteBladder and its contents into a provided StringBuilder.
     *
     * @param sb
     *            the StringBuilder in which to format the data.
     * @return the StringBuilder that was provided (sb).
     */
    public StringBuilder toStringBuilder(StringBuilder sb) {
        sb.append("ByteBladder: ");
        sb.append(getOutBuffer()).append('\n');
        Ascii.dump(sb, m_buffer);
        return sb;
    }

    /**
     * Formats the ByteBladder and its contents.
     *
     * @return a String containing the formatted data.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(1000);
        return toStringBuilder(sb).toString();
    }
}

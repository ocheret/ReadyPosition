/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;

/**
 * XXX - javadoc
 */
public class ByteBladder {
    /** Source code version info. */
    private final static String s_rcsid =
	"$Id: ByteBladder.java 242 2004-05-06 16:50:14Z cocheret $";

    /** XXX - javadoc */
    public final static int DEFAULT_ALLOCATION_UNIT = 512;

    /** XXX - javadoc */
    public final static int MINIMUM_ALLOCATION_UNIT = 64;

    /** XXX - javadoc */
    public ByteBladder() {
	this(DEFAULT_ALLOCATION_UNIT);
    }

    /** XXX - javadoc */
    public ByteBladder(int allocationUnit) {
	if (allocationUnit <= MINIMUM_ALLOCATION_UNIT) {
	    throw new IllegalArgumentException(
		"The allocationUnit must be > " + MINIMUM_ALLOCATION_UNIT);
	}
	m_allocationUnit = allocationUnit;
    }

    /** XXX - javadoc */
    protected int m_allocationUnit;

    /** XXX - javadoc */
    public int getAllocationUnit() { return m_allocationUnit; }

    /** XXX - javadoc */
    protected ByteOrder m_byteOrder = ByteOrder.BIG_ENDIAN;

    /** XXX - javadoc */
    public ByteOrder getByteOrder() { return m_byteOrder; }

    /** XXX - javadoc */
    public ByteBladder setByteOrder(ByteOrder byteOrder) {
	m_byteOrder = byteOrder;
	if (m_current != null) {
	    m_current.order(byteOrder);
	}
	return this;
    }

    /** XXX - javadoc */
    protected BufferNode m_buffers = new BufferNode(null);

    /** XXX - javadoc */
    protected ByteBuffer m_current;

    /** XXX - javadoc */
    protected int m_bufferCount;

    /** XXX - javadoc */
    protected int m_remaining;

    /** XXX - javadoc */
    public int getRemaining() { return m_remaining; }

    /** XXX - javadoc */
    public ByteBladder makeRoom(int room) {
	if (m_current != null) {
	    if (room <= m_current.remaining()) {
		// There is room in the current ByteBuffer.
		return this;
	    }

	    // We're done filling the current ByteBuffer so prepare it
	    // for draining.
	    m_current.flip();
	}

	// We will make a new ByteBuffer to hold the new bytes.
	if (room < m_allocationUnit) {
	    // Allocate at least an allocationUnit's worth of space so
	    // that we are likely to be able to add more to the new
	    // ByteBuffer.
	    room = m_allocationUnit;
	}
	m_current = ByteBuffer.allocateDirect(room).order(m_byteOrder);
	m_buffers.insert(new BufferNode(m_current));
	m_bufferCount++;
	return this;
    }

    /** XXX - javadoc */
    public ByteBladder put(byte b) {
	makeRoom(1);
	m_current.put(b);
	m_remaining++;
	return this;
    }

    /** XXX - javadoc */
    public ByteBladder put(byte[] src) {
	makeRoom(src.length);
	m_current.put(src);
	m_remaining += src.length;
	return this;
    }

    /** XXX - javadoc */
    public ByteBladder put(byte[] src, int offset, int length) {
	makeRoom(length);
	m_current.put(src, offset, length);
	m_remaining += length;
	return this;
    }

    /** XXX - javadoc */
    public ByteBladder putChar(char value) {
	makeRoom(2);
	m_current.putChar(value);
	m_remaining += 2;
	return this;
    }

    /** XXX - javadoc */
    public ByteBladder putDouble(double value) {
	makeRoom(8);
	m_current.putDouble(value);
	m_remaining += 8;
	return this;
    }

    /** XXX - javadoc */
    public ByteBladder putFloat(float value) {
	makeRoom(4);
	m_current.putFloat(value);
	m_remaining += 4;
	return this;
    }

    /** XXX - javadoc */
    public ByteBladder putInt(int value) {
	makeRoom(4);
	m_current.putInt(value);
	m_remaining += 4;
	return this;
    }

    /** XXX - javadoc */
    public ByteBladder putLong(long value) {
	makeRoom(8);
	m_current.putLong(value);
	m_remaining += 8;
	return this;
    }

    /** XXX - javadoc */
    public ByteBladder putShort(short value) {
	makeRoom(2);
	m_current.putShort(value);
	m_remaining += 2;
	return this;
    }

    /** XXX - javadoc */
    protected void closeLast() {
	if (m_current != null) {
	    // Close off the last buffer.
	    m_current.flip();
	    m_current = null;
	}
    }

    /** XXX - javadoc */
    public ByteBladder append(ByteBuffer bb) {
	closeLast();
	BufferNode bn = new BufferNode(bb);
	m_buffers.insert(bn);
	m_bufferCount++;
	m_remaining += bb.remaining();
	return this;
    }

    /** XXX - javadoc */
    public ByteBladder prepend(ByteBuffer bb) {
	BufferNode bn = new BufferNode(bb);
	m_buffers.append(bn);
	m_bufferCount++;
	m_remaining += bb.remaining();
	return this;
    }

    /** XXX - javadoc */
    public ByteBladder moveToBack(ByteBladder src) {
	closeLast();
	src.closeLast();
	m_buffers.insert(src.m_buffers);
	m_bufferCount += src.m_bufferCount;
	m_remaining += src.m_remaining;
	src.clear();
	return this;
    }

    /** XXX - javadoc */
    public ByteBladder moveToFront(ByteBladder src) {
	closeLast();
	src.closeLast();
	m_buffers.append(src.m_buffers);
	m_bufferCount += src.m_bufferCount;
	m_remaining += src.m_remaining;
	src.clear();
	return this;
    }

    /** XXX - javadoc */
    public ByteBladder clear() {
	m_current = null;
	m_buffers.remove();
	m_bufferCount = 0;
	m_remaining = 0;
	return this;
    }

    /** XXX - javadoc */
    public ByteBuffer toByteBuffer() {
	closeLast();

	ByteBuffer bb = ByteBuffer.allocateDirect(m_remaining);
	for (BufferNode bn = (BufferNode)m_buffers.getNext();
	     bn != m_buffers;
	     bn = (BufferNode)bn.getNext())
	{
	    bb.put(bn.getBuffer());
	}
	bb.flip();
	return bb;
    }

    /** XXX - javadoc */
    public long write(GatheringByteChannel channel)
	throws IOException
    {
	closeLast();

	// Collect the buffers we want to write out
	ByteBuffer[] srcs = new ByteBuffer[m_bufferCount];
	BufferNode bn = (BufferNode)m_buffers.getNext();
	int i = 0;
	while (bn != m_buffers) {
	    srcs[i++] = bn.getBuffer();
	    bn = (BufferNode)bn.getNext();
	}

	// Write out some data.
	long result = channel.write(srcs);
	m_remaining -= result;

	// Remove leading buffers that are fully drained.
	for (bn = (BufferNode)m_buffers.getNext();
	     bn != m_buffers;
	     bn = (BufferNode)bn.getNext())
	{
	    if (bn.getBuffer().remaining() != 0) {
		break;
	    }
	    bn.remove();
	}

	return result;
    }
}

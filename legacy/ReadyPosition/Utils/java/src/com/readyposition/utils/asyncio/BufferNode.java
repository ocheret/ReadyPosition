/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import java.nio.ByteBuffer;

/**
 * A node in a linked list of nodes that can hold a ByteBuffer.
 */
public class BufferNode extends Node {
    /**
     * Constructs a node to hold a ByteBuffer.
     *
     * @param buffer the ByteBuffer to be managed by this node.
     */
    public BufferNode(ByteBuffer buffer) {
	m_buffer = buffer;
    }

    /** The ByteBuffer managed by this node. */
    protected ByteBuffer m_buffer;

    /** Gets the ByteBuffer managed by this node. */
    public ByteBuffer getBuffer() { return m_buffer; }
}

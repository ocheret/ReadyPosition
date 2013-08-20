/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Interface that allows tracking of a Connector's progress during
 * connection establishment.
 */
public interface ConnectorListener {
    /**
     * Gets the address to connect to.
     *
     * @return the SocketAddress to connect to.
     */
    SocketAddress getSocketAddress();

    /**
     * This method is invoked right after the socketChannel is created
     * and set to non-blocking and before connection is initiated to
     * allow customization of socket options.
     *
     * @param socketChannel the SocketChannel to set up.
     */
    void setup(SocketChannel socketChannel);

    /** This is invoked when connection is in progress. */
    void connecting();

    /**
     * This is invokved when connection fails.
     *
     * @param t the Throwable that indicates why connection failed.
     * @return the number of milliseconds until the next connection attempt or
     *	Long.MIN_VALUE if no reconnection should be attempted.
     */
    long connectionFailed(Throwable t);

    /**
     * This is invoked when a connection succeeds.  The Connector that
     * invokes this is no longer used after this call and should
     * eventually be garbage collected by the JVM.
     *
     * @param mainLoop the mainLoop used for event dispatch.
     * @param socketChannel the successfully connected SocketChannel.
     */
    void connected(MainLoop mainLoop, SocketChannel socketChannel);
}

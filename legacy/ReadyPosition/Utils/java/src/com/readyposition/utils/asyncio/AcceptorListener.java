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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Interface that allows tracking of an Acceptor's progress in
 * listening for connection requests.
 */
public interface AcceptorListener
{
    /**
     * Gets the address at which to listen for connections.
     *
     * @return the SocketAddress to connect to.
     */
    SocketAddress getSocketAddress();

    /**
     * This method is invoked right after the serverChannel is created
     * and set to non-blocking and before listening is initiated to
     * allow customization of socket options.
     *
     * @param serverChannel the SocketChannel to set up.
     */
    void setup(ServerSocketChannel serverChannel);

    /**
     * This is invoked when the server socket is successfully
     * listening for connections.
     */
    void listening();

    /**
     * This is invokved when listener setup fails.
     *
     * @param t the Throwable that indicates why connection failed.
     * @return the number of milliseconds until the next listen attempt or
     *	Long.MIN_VALUE if no more listening should be attempted.
     */
    long listenFailed(Throwable t);

    /**
     * This is invoked upon successful acceptance of a connection.
     *
     * @param mainLoop the mainLoop used for event dispatch.
     * @param socketChannel a connected SocketChannel.
     */
    void accepted(MainLoop mainLoop, SocketChannel socketChannel);

    /**
     * This is invokved when an attempt to accept a connection failed.
     *
     * @param t the Throwable that indicates why the attempt failed.
     */
    void acceptFailed(Throwable t);
}

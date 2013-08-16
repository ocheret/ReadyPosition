package com.readyposition.reactor;

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
     * @param ioReactor the IOReactor used for event dispatch.
     * @param socketChannel a connected SocketChannel.
     */
    void accepted(IOReactor ioReactor, SocketChannel socketChannel);

    /**
     * This is invokved when an attempt to accept a connection failed.
     *
     * @param t the Throwable that indicates why the attempt failed.
     */
    void acceptFailed(Throwable t);
}

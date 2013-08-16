package com.readyposition.reactor;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Interface that allows tracking of a Connector's progress during
 * connection establishment.
 */
public interface ConnectorListener
{
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
     *	       Long.MIN_VALUE if no reconnection should be attempted.
     */
    long connectionFailed(Throwable t);

    /**
     * This is invoked when a connection succeeds.  The Connector that
     * invokes this is no longer used after this call and should
     * eventually be garbage collected by the JVM.
     *
     * @param ioReactor the IO Reactor used for event dispatch.
     * @param socketChannel the successfully connected SocketChannel.
     */
    void connected(IOReactor ioReactor, SocketChannel socketChannel);
}

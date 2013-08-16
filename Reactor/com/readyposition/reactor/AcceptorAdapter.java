package com.readyposition.reactor;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenient abstract implementation of AcceptorListener that
 * provides useful behavior.
 */
public abstract class AcceptorAdapter implements AcceptorListener {
    /** Logger for this class. */
    private final static Logger s_logger =
        LoggerFactory.getLogger(AcceptorAdapter.class);

    /** Whether or not to set SO_REUSEADDR to true on the listening port. */
    private final static boolean s_reuseAddr =
    	!System.getProperty("os.name").startsWith("Windows");
    	
    /** The name of this adapter. */
    protected String m_name;

    /** The host name or null for a wildcard address. */
    protected String m_host;

    /** The listening port. */
    protected int m_port;

    /** Delay for first connection retry. Defaults to 1 millisecond. */
    protected long m_minRetryInterval = 1L;

    /** Delay for last connection retry. Defaults to 30 seconds. */
    protected long m_maxRetryInterval = 30000L;

    /** Used for exponential backoff on listening attempts. */
    protected BackOff m_backOff;

    /**
     * Creates an AcceptorAdapter where the IP address is the wildcard
     * address and the port number a specified value.
     *
     * @param name the name to be associated with the acceptor.
     * @param port the port number
     */
    public AcceptorAdapter(String name, int port) {
        this(name, null, port);
    }

    /**
     * Creates an AcceptorAdapter from a hostname and a port number.
     *
     * @param name the name to be associated with the acceptor.
     * @param host the host name.
     * @param port the port number.
     */
    public AcceptorAdapter(String name, String host, int port) {
        m_name = name;
        m_host = host;
        m_port = port;
    }

    /** Gets the name of this adapter. */
    public String getName() {
        return m_name;
    }

    /** Gets the host name. */
    public String getHost() {
        return m_host;
    }

    /** Gets the listening port. */
    public int getPort() {
        return m_port;
    }

    /**
     * Sets the min and max retry intervals for exponential backoff on
     * listening attempts.
     *
     * @param min the minimum retry interval.
     * @param max the maximum retry interval.
     */
    public void setRetryParams(long min, long max) {
        // Argument checking
        if (min <= 0) {
            throw new IllegalArgumentException("min == " + min +
                                               " needs to be > 0");
        }
        if (max < min) {
            throw new IllegalArgumentException("max < min, max == " +
                                               max + ", min == " + min);
        }
        m_minRetryInterval = min;
        m_maxRetryInterval = max;
    }

    // AcceptorListener methods start here

    // Javadoc from interface
    public SocketAddress getSocketAddress() {
        return (m_host == null) ?
            new InetSocketAddress(m_port) :
            new InetSocketAddress(m_host, m_port);
    }

    // Javadoc from interface
    public void setup(ServerSocketChannel serverChannel) {
        ServerSocket socket = serverChannel.socket();
        try {
            socket.setReuseAddress(s_reuseAddr);
        } catch (Throwable t) {
            s_logger.warn("Couldn't enable SO_REUSEADDR", t);
        }
    }

    // Javadoc from interface
    public void listening() {
        s_logger.info("Acceptor " + m_name + " listening.");
        m_backOff = null;
    }

    // Javadoc from interface
    public long listenFailed(Throwable t) {
        if (m_backOff == null) {
            m_backOff = new BackOff(m_minRetryInterval, m_maxRetryInterval);
        }
        long nextDelay = m_backOff.getNext();
        s_logger.info("Acceptor " + m_name +
                      " listen failed - trying again in " + nextDelay + " ms.", t);
        return nextDelay;
    }

    // Javadoc from interface
    public void accepted(IOReactor ioReactor, SocketChannel socketChannel) {
        // XXX - print more detail about the connection? - CAO 12/03/2003
        s_logger.info("Acceptor " + m_name + " accepted: " + socketChannel);
        try {
            socketChannel.socket().setKeepAlive(true);
        } catch (Throwable t) {
            s_logger.warn("Couldn't enable SO_KEEPALIVE", t);
        }
        peerCreate(ioReactor, socketChannel);
    }

    // Javadoc from interface
    public void acceptFailed(Throwable t) {
        s_logger.info("Acceptor " + m_name + " accept failed.", t);
    }

    // AcceptorListener methods end here

    /**
     * Invoked to allow subclasses to do something useful with the
     * socketChannel after a connection is successfully accepted.
     *
     * @param ioReactor the IOReactor used for event dispatch.
     * @param socketChannel the successfully connected socketChannel.
     */
    protected abstract void peerCreate(IOReactor ioReactor,
                                       SocketChannel socketChannel);
}

package com.readyposition.reactor;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * The Acceptor class is used to initiate socket listeners.
 */
public class Acceptor
    implements TimerHandler, ValveHandler
{
    /** The multiplexed asynchronous I/O reactor. */
    protected IOReactor m_ioReactor;

    /** The object informed of connection events and other acceptor status. */
    protected AcceptorListener m_listener;

    /** The Valve that manages ACCEPT events. */
    protected Valve m_valve;

    /** The server channel that will listen for connections. */
    protected ServerSocketChannel m_serverChannel;

    /** A timer used to schedule retry attempts after failures. */
    protected Timer m_timer;

    /**
     * Constructs an Acceptor that will listen for connections and will notify
     * an AcceptorListener of its progress.
     * 
     * @param ioReactor the asynchronous I/O reactor used to manage
     *                  activities.
     * @param listener the object to be informed of connections and
     *                 other progress.
     */
    public Acceptor(IOReactor ioReactor, AcceptorListener listener) {
        m_ioReactor = ioReactor;
        m_listener = listener;
    }

    /** Attempts to establish a listening socket. */
    public void listen() {
        SocketAddress sa = m_listener.getSocketAddress();
        try {
            m_serverChannel = ServerSocketChannel.open();
            m_serverChannel.configureBlocking(false);
            m_listener.setup(m_serverChannel);
            m_serverChannel.socket().bind(sa, 1000);
            m_listener.listening();
            m_valve = m_ioReactor.valveCreate(m_serverChannel, this);
            m_valve.enable(SelectionKey.OP_ACCEPT);
        } catch (Throwable t) {
            failure(t);
        }
    }

    /** Closes the listening socket. It is possible to call listen() again. */
    public void shutdown() {
        if (m_timer != null) {
            m_timer.cancel();
            m_timer = null;
        }
        if (m_valve != null) {
            m_valve.disable(SelectionKey.OP_ACCEPT);
            m_valve.close();
            m_valve = null;
            m_serverChannel = null;
        }
    }

    /**
     * This is called when we fail to establish a listening socket. This cleans
     * up, notifies the listener, and schedules a retry.
     * 
     * @param t the Throwable that indicates the reason for failure.
     */
    protected void failure(Throwable t) {
        if (m_valve != null) {
            m_valve.close();
        }
        m_valve = null;
        m_serverChannel = null;

        long nextDelay = m_listener.listenFailed(t);

        // If the returned value is Long.MIN_VALUE then we won't retry.
        if (nextDelay != Long.MIN_VALUE) {
            m_timer = m_ioReactor.timerCreateRel(nextDelay, this);
        }
    }

    /**
     * Invoked when there is an I/O operation ready to be
     * performed. This handles the acceptance of new connections.
     */
    public void valveFire(Valve valve, SelectionKey selectionKey)
    {
        try {
            SocketChannel socketChannel = m_serverChannel.accept();
            socketChannel.configureBlocking(false);
            m_listener.accepted(m_ioReactor, socketChannel);
        } catch (Throwable t) {
            m_listener.acceptFailed(t);
        }
    }

    /**
     * Performs work that was previously scheduled to be invoked by a
     * timer.  This is used to reattempt listening after a failure.
     * 
     * @param time the time at which the Timer actually fired.
     */
    public long timerFire(long time, long actualTime) {
        if (m_timer == null) {
            // Be extra careful not to fire the timer after a shutdown.
            return 0;
        }

        m_timer = null;

        // Initiate a new connection attempt.
        listen();
        return 0;
    }
}

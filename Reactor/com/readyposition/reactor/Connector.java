package com.readyposition.reactor;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * The Connector class facilitates the initiation of socket
 * connections.  It relies on a ConnectorListener implementation to
 * customize its behavior.
 */
public class Connector
    implements TimerHandler, ValveHandler
{
    /** The multiplexed asynchronous I/O reactor loop. */
    protected IOReactor m_ioReactor;

    /** The object informed of connection events and other connector status. */
    protected ConnectorListener m_listener;

    /** The socket channel that will be connected. */
    protected SocketChannel m_socketChannel;

    /** The Valve that manages CONNECT events. */
    protected Valve m_valve;

    /** A timer used to schedule retry attempts after failures. */
    protected Timer m_timer;

    /**
     * Constructs a Connector that will attempt to connect and will notify a
     * ConnectorListener of its progress.
     *
     * @param ioReactor the asynchronous I/O reactor used to manage activities.
     * @param listener the object to be informed of connections and other progress.
     */
    public Connector(IOReactor ioReactor, ConnectorListener listener) {
        m_ioReactor = ioReactor;
        m_listener = listener;
    }

    /** Attempts to establish a connected socket. */
    public void connect() {
        SocketAddress sa = m_listener.getSocketAddress();
        try {
            m_socketChannel = SocketChannel.open();
            m_socketChannel.configureBlocking(false);
            m_listener.setup(m_socketChannel);
            m_socketChannel.connect(sa);
            if (m_socketChannel.isConnected()) {
                // Sucessful connection.
                connected();
            } else {
                // Our connection process is in progress.
                m_listener.connecting();
                m_valve = m_ioReactor.valveCreate(m_socketChannel, this);
                m_valve.enable(SelectionKey.OP_CONNECT);
            }
        } catch (Throwable t) {
            // We failed to connect.
            failure(t);
        }
    }

    /** This is invoked when a successful connection is established. */
    protected void connected() {
        m_listener.connected(m_ioReactor, m_socketChannel);
    }

    /**
     * This is called when we fail to establish a connection. This cleans up,
     * notifies the listener, and schedules a retry.
     *
     * @param t the Throwable that indicates the reason for failure.
     */
    protected void failure(Throwable t) {
        if (m_valve != null)
            {
                m_valve.close();
            }
        m_valve = null;
        m_socketChannel = null;

        long nextDelay = m_listener.connectionFailed(t);

        // If the returned value is Long.MIN_VALUE then we won't retry.
        if (nextDelay != Long.MIN_VALUE) {
            m_timer = m_ioReactor.timerCreateRel(nextDelay, this);
        }
    }

    /**
     * Invoked when there is an I/O operation ready to be performed. This
     * handles the completion of pending connections.
     */
    public void valveFire(Valve valve, SelectionKey selectionKey) {
        boolean result;
        try {
            result = m_socketChannel.finishConnect();
        } catch (Throwable t) {
            // We failed to connect.
            failure(t);
            return;
        }

        if (!result) {
            // Still trying to connect.
            m_listener.connecting();
        } else {
            // We're all connected!
            m_valve.disable(SelectionKey.OP_CONNECT);
            m_valve = null;
            connected();
        }
    }

    /**
     * Performs work that was previously scheduled to be invoked by a timer.
     * This is used to reattempt connection after a failure.
     *
     * @param time the time at which the Timer actually fired.
     */
    public long timerFire(long time, long actualTime) {
        m_timer = null;

        // Initiate a new connection attempt.
        connect();
        return 0L;
    }
}

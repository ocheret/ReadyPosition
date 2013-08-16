package com.readyposition.reactor;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * A Valve represents a unit of work to be done during a pass through
 * an IOReactor duty cycle.
 */
public class Valve implements ValveHandler
{
    /** The IOReactor that is controlling this Valve. */
    protected IOReactor m_ioReactor;

    /** The channel managed by this Valve. */
    protected SelectableChannel m_channel;

    /** Current set of enabled operations. */
    protected int m_ops;

    /**
     * The ValveHandler that will be invoked the next time through
     * the reactor loop.
     */
    protected ValveHandler m_handler;

    /** Constructor. */
    public Valve() {}

    /**
     * Constructor for a Valve with a handler.
     *
     * @param handler the ValveHandler that will be invoked when the
     *	              valve is ripe.
     */
    public Valve(IOReactor ioReactor, SelectableChannel channel,
                 ValveHandler handler)
    {
        m_ioReactor = ioReactor;
        m_channel = channel;
        m_handler = handler;
    }

    /** TODO - javadoc */
    public void enable(int ops) {
        if (m_ioReactor == null) {
            throw new IllegalStateException("Attempt to enable operations on an unregistered Valve.");
        }

        // Don't do anything if the operations are already enabled
        if ((m_ops & ops) != ops) {
            m_ioReactor.enable(this, ops);
            m_ops |= ops;
        }
    }

    /** XXX - javadoc */
    public void disable(int ops) {
        if (m_ioReactor == null) {
            throw new IllegalStateException("Attempt to disable operations on an unregistered Valve.");
        }
        
        // Don't do anything if the operations aren't enabled
        if ((m_ops & ops) != 0) {
            m_ioReactor.disable(this, ops);
            m_ops &= ~ops;
        }
    }

    /**
     * Closes the channel being managed by this Valve.
     */
    public void close() {
        if (m_ioReactor == null) {
            // Close as often as you like
            return;
        }
        m_ioReactor.close(this);
    }

    /**
     * Sets the IOReactor that will control this Valve
     *
     * @param ioReactor the IOReactor that will control this Valve
     */
    protected void setIOReactor(IOReactor ioReactor) {
        m_ioReactor = ioReactor;
    }

    /**
     * Gets the IOReactor that will control this Valve
     *
     * @return the IOReactor that will control this Valve
     */
    public IOReactor getIOReactor() {
        return m_ioReactor;
    }

    /**
     * Sets the channel that will be managed by this Valve.
     *
     * @param channel the channel that will be managed by this Valve.
     */
    protected void setChannel(SelectableChannel channel) {
        m_channel = channel;
    }

    /**
     * Gets the channel that will be managed by this Valve.
     *
     * @return the channel that will be managed by this Valve.
     */
    public SelectableChannel getChannel() {
        return m_channel;
    }

    /**
     * Sets the ValveHandler that will be invoked via this Valve.
     *
     * @param handler the ValveHandler that will be invoked via this Valve.
     */
    public void setHandler(ValveHandler handler) {
        m_handler = handler;
    }

    /**
     * Gets the ValveHandler that will be invoked via this Valve.
     *
     * @return the ValveHandler that will be invoked via this Valve.
     */
    public ValveHandler getHandler() {
        return m_handler;
    }

    /**
     * This is a stub method that should be overridden in a subclass,
     * or the Valve class can be provided a ValveHandler as a target
     * (much like Thread can be either be subclassed with run()
     * overridden or it can be provided a Runnable).
     *
     * @param reactor the IOReactor controlling this Valve.
     * @param channel the SelectableChannel that is ready for I/O.
     * @param selectedKey a SelectionKey that tells us what I/O
     *                    operations are pending.
     */
    public void valveFire(Valve valve, SelectionKey selectedKey) {
        // If there is a handler we'll invoke it.  Otherwise, we'll do
        // nothing.
        if (m_handler != null) {
            m_handler.valveFire(valve, selectedKey);
        }
    }
}

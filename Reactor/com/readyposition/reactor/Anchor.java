package com.readyposition.reactor;

import java.nio.channels.SelectionKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation class representing a collection of Valves for each
 * type of I/O operation.
 */
class Anchor {
    /** Logger for this class. */
    private final static Logger s_logger = LoggerFactory.getLogger(Anchor.class);

    /** The IOReactor that is controlling this Valve. */
    protected IOReactor m_ioReactor;

    /** The SelectableChannel this Anchor is for. */
    protected SelectionKey m_selectionKey;

    /**
     * An array of all of the possible IO operations.  This determines
     * the purpose of the valves in the m_valves array.
     */
    protected final static int[] s_ops = {
        SelectionKey.OP_ACCEPT,
        SelectionKey.OP_CONNECT,
        SelectionKey.OP_READ,
        SelectionKey.OP_WRITE
    };

    /**
     * An array of strings describing all of the possible IO
     * operations.  This is parallel to the s_ops array.
     */
    protected final static String[] s_opNames = {
        "ACCEPT",
        "CONNECT",
        "READ",
        "WRITE"
    };

    /**
     * Valve registered for varios IO operations.  The position in the
     * array correspons to the operations in s_ops.
     */
    protected final Valve m_valves[] = new Valve[s_ops.length];

    /**
     * Constructs an Anchor that represents all of the enabled Valves
     * for a SelectableChannel.
     *
     * @param selectableChannel the selectableChannel this Anchor serves.
     */
    Anchor(IOReactor ioReactor, SelectionKey selectionKey) {
        m_ioReactor = ioReactor;
        m_selectionKey = selectionKey;
    }

    /**
     * Dumb method to format SelectionKey operation masks as a string.
     *
     * @param ops that operations bitmask to format.
     * @return the formatted string of operations.
     */
    public static String formatOps(int ops) {
        StringBuilder sb = null;
        for (int i = 0; i < s_ops.length; i++) {
            if ((ops & s_ops[i]) != 0) {
                if (sb == null) {
                    sb = new StringBuilder();
                } else {
                    sb.append('|');
                }
                sb.append(s_opNames[i]);
            }
        }
        return (sb == null) ? "" : sb.toString();
    }

    /**
     * Registers a valve for specific I/O operations.
     *
     * @param valve the Valve to register.
     * @param ops the operations to register for.
     */
    protected void register(Valve valve, int ops) {
        for (int i = 0; i < s_ops.length; i++) {
            if ((ops & s_ops[i]) != 0) {
                m_valves[i] = valve;
            }
        }
    }

    /**
     * Unregisters valves for specific I/O operations.
     *
     * @param ops the operations to register for.
     * @return the I/O operations actually disabled.
     */
    protected int unregister(Valve valve, int ops) {
        int result = 0;
        for (int i = 0; i < s_ops.length; i++) {
            if ((ops & s_ops[i]) != 0 && m_valves[i] == valve) {
                m_valves[i] = null;
                result |= s_ops[i];
            }
        }
        return result;
    }

    /**
     * Invokes the handlers for Valves that have operations ready.  If
     * the same Valve is registered for multiple operations that are
     * ready then we will only fire that Valve a single time so that
     * it can deal with all of the operations at once.
     *
     * @param ops the mask containing the list of ready operations.
     */
    protected void fire(int ops) {
    	for (int i = 0; ops != 0 && i < s_ops.length; i++) {
            if (m_valves[i] == null || (ops & s_ops[i]) == 0) {
                // This operation has no Valve or does not
                // match any ready operations
                continue;
            }
            int callOps = s_ops[i];
            for (int j = i + 1; j < s_ops.length; j++) {
                if (m_valves[j] == null || (ops & s_ops[j]) == 0) {
                    // This operation has no Valve or does not
                    // match any ready operations
                    continue;
                }
                callOps |= s_ops[j];
            }

            // Invoke the Valve that is registered for the callOps
            try {
                m_valves[i].getHandler().valveFire(m_valves[i],
                                                   m_selectionKey);
            } catch (Throwable t) {
                s_logger.warn("During " + formatOps(callOps) +
                              " channel processing:" + m_selectionKey.channel(), t);
            }

            // Clear out pending operations that have already been handled.
            ops &= ~callOps;
    	}
    }

    /** Disables all Valves. */
    protected void close() {
        for (int i = 0; i < s_ops.length; i++) {
            m_valves[i] = null;
        }
    }
}

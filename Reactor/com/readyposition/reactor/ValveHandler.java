package com.readyposition.reactor;

import java.nio.channels.SelectionKey;

/**
 * The ValveHandler interface is used to invoke work when an I/O
 * operation has been selected to be performed.
 */
public interface ValveHandler {
    /**
     * This method is invoked when I/O is ready to be performed.
     * 
     * @param valve the Valve with I/O ready to be performed.
     * @param selectedKey a SelectionKey that tells us what I/O operations are pending.
     */
    void valveFire(Valve valve, SelectionKey selectedKey);
}

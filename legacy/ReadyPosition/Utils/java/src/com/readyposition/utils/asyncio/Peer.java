/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class Peer
{
    /** Logger for this class. */
    private Logger s_logger = Logger.getLogger(Peer.class.getName());

    /** XXX - javadoc */
    public Peer(String name, MainLoop mainLoop, SocketChannel socketChannel) {
	m_name = name;
	m_mainLoop = mainLoop;
	m_socketChannel = socketChannel;
    }

    /** The name of this instance. */
    private String m_name;

    /** Gets the name of this instance. */
    public String getName() { return m_name; }

    /** The main loop used for event dispatch. */
    MainLoop m_mainLoop;

    /** Gets the main loop used for event dispatch. */
    MainLoop getMainLoop() { return m_mainLoop; }

    /** XXX - javadoc */
    private SocketChannel m_socketChannel;

    /** Gets the port number on the remote host to which this socket is
     *  connected.
     */
    public int getPort() {
	return m_socketChannel.socket().getPort();
    }

    /** Gets the remote host to which this socket is connected. */
    public InetAddress getInetAddress() {
	return m_socketChannel.socket().getInetAddress();
    }
}

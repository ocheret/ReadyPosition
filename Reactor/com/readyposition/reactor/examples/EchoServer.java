package com.readyposition.reactor.examples;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.readyposition.collections.common.Ascii;
import com.readyposition.collections.common.ByteBladder;

import com.readyposition.reactor.Acceptor;
import com.readyposition.reactor.AcceptorAdapter;
import com.readyposition.reactor.IOReactor;
import com.readyposition.reactor.Valve;
import com.readyposition.reactor.ValveHandler;

/**
 * And example class that listens for connections on a port. It receives any
 * data sent over connected sockets, formats it to the standard output, and
 * also echos the bytes read back verbatim to the socket that sent them. If
 * any socket delivers the character 'EOT' (ctrl-D), then the program will
 * exit.
 */
public class EchoServer {
    /** Class wide logger. */
    private final static Logger s_logger =
        LoggerFactory.getLogger(EchoServer.class);

    /** Prints a usage message. */
    private static void usage(String msg) {
        s_logger.error(msg + "\n" +
                       "usage: java " + EchoServer.class.getName() +
                       " [port=6789]");
    }

    /** The port on which to listen for incoming connections. */
    private int m_port;

    /** The reactor loop dispatching our events. */
    private IOReactor m_ioReactor;

    /** Returns the reactor loop. */
    public IOReactor getIOReactor() { return m_ioReactor; }

    /**
     * Constructor.
     * @param port the port on which to listen for incoming connections.
     */
    public EchoServer(int port) {
    	m_port = port;
    }

    /** Represents an active connection to this server. */
    protected class EchoServerPeer implements ValveHandler {
        /** The socket channel for this connection. */
        protected SocketChannel m_socketChannel;

        /** We will use the same buffer for reading and writing. */
        protected ByteBladder m_buf = new ByteBladder(true);

        /**
         * We will use the same valve for reading and writing. We could have
         * used a separate valve for each action.
         */
        protected Valve m_valve;

        /**
         * Constructor.
         * @param socketChannel the socket channel for this connection.
         */
        public EchoServerPeer(SocketChannel socketChannel) {
            m_socketChannel = socketChannel;
        }

        /** Initiates activities for this connection. */
        public void init() {
            m_valve = m_ioReactor.valveCreate(m_socketChannel, this);
            m_valve.enable(SelectionKey.OP_READ);
            s_logger.info(id() + " Peer initiated");
        }

        /** Shuts down this connection. */
        public void shutdown(String msg) {
            // Perform any necessary cleanup for this EchoPeer
            m_valve.close();
            s_logger.info(id() + ": " + msg);
        }

        @Override
        public void valveFire(Valve valve, SelectionKey selectedKey) {
            if (selectedKey.isWritable()) {
                doWrite();
            }
            if (selectedKey.isReadable()) {
                doRead();
            }
        }

        /** Returns a unique string for this connection. */
        protected String id() {
            return m_socketChannel.socket().getRemoteSocketAddress().toString();
        }

        /** Attempt to write to the remote peer. */
        protected void doWrite() {
            int w = -1;
            try {
                m_buf.markOut();
                w = m_buf.write(m_socketChannel);
            } catch (IOException e) {
                s_logger.error(id(), e);
                shutdown("Shutting down peer due to write exception");
            }
            if (w >= 0) {
                s_logger.info(id() + " wrote " + w + " bytes:\n" +
                              Ascii.dump(m_buf.getOutMarkedBuffer()));
            }

            int used = m_buf.getUsed();

            if (used == 0) {
                // There is nothing left to write
                m_valve.disable(SelectionKey.OP_WRITE);
            }

            if (used > 4096) {
                // Stop reading while we have 'a lot' of bytes still in the buffer
                m_valve.disable(SelectionKey.OP_READ);
            } else {
                m_valve.enable(SelectionKey.OP_READ);
            }
        }

        /** Attempt to read from the remote peer. */
        public void doRead() {
            int r = -1;
            try {
                m_buf.markIn();
                r = m_buf.read(m_socketChannel, 1024);
            } catch (IOException e) {
                s_logger.error(id(), e);
                shutdown("Shutting down peer due to read exception");
            }
            if (r >= 0) {
                s_logger.info(id() + " read " + r + " bytes:\n" +
                              Ascii.dump(m_buf.getInMarkedBuffer()));

                // Make sure we're enabled for writing stuff out
                m_valve.enable(SelectionKey.OP_WRITE);
            } else {
                shutdown(" EOF: closing");
            }
        }
    }

    /** Simple AcceptorListener that spawns peers as connections are accepted */
    protected class EchoAcceptorAdapter extends AcceptorAdapter {
    	protected EchoAcceptorAdapter(String name, int port) {
            super(name, port);
    	}

    	public void peerCreate(IOReactor ioReactor, SocketChannel socketChannel) {
            EchoServerPeer peer = new EchoServerPeer(socketChannel);
            peer.init();
    	}
    }

    /** Start accepting connections */
    public void init() {
    	m_ioReactor = IOReactor.getDefaultIOReactor();
    	Acceptor a = new Acceptor(m_ioReactor,
                                  new EchoAcceptorAdapter("EchoServerAcceptor",
                                                          m_port));
    	a.listen();
    }


    /** The main event. */
    public static void main(String[] args) {
        if (args.length > 1) {
            usage("Too many arguments");
            System.exit(1);
        }

        int port = 6789;

        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                s_logger.error("Couldn't parse port number", e);
                usage(e.getMessage());
                System.exit(1);
            }
        }

        EchoServer server = new EchoServer(port);
        server.init();
        for (;;) {
            try {
                // Sleep until the reactor exits
                server.getIOReactor().getThread().join();
                break;
            } catch (InterruptedException e) {
            }
        }
        s_logger.info("IOReactor has terminated: cycle count = " +
                      server.getIOReactor().getCycleCount());
        System.exit(0);
    }
}

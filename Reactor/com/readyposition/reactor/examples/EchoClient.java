package com.readyposition.reactor.examples;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.readyposition.collections.common.Ascii;
import com.readyposition.collections.common.ByteBladder;

import com.readyposition.reactor.Connector;
import com.readyposition.reactor.ConnectorAdapter;
import com.readyposition.reactor.IOReactor;
import com.readyposition.reactor.Valve;
import com.readyposition.reactor.ValveHandler;

public class EchoClient {
    /** Class wide logger. */
    private final static Logger s_logger =
        LoggerFactory.getLogger(EchoClient.class);

    /** Displays a usage message. */
    private static void usage(String msg) {
        s_logger.error(msg + "\n" +
          "usage: java " + EchoClient.class.getName() +
          " [host=\"localhost\"]" +
          " [port=6789]" +
          " [msg=\"ECHO\"]");
    }

    /** The host to connect to. */
    private String m_host;

    /** The port to connect to. */
    private int m_port;

    /** The message to send to the echo server. */
    private String m_msg;

    /** The reactor loop dispatching events. */
    private IOReactor m_ioReactor;

    /** Returns the reactor loop. */
    public IOReactor getIOReactor() { return m_ioReactor; }

    /**
     * Constructor.
     * @param host the host to connect to.
     * @param port the port to connect to.
     * @param msg the message to send to the echo server.
     */
    public EchoClient(String host, int port, String msg) {
        m_host = host;
        m_port = port;
        m_msg = msg;
    }

    /** Represents the connection to the server. */
    protected class EchoClientPeer implements ValveHandler {
        /** The socket channel for this connection. */
        protected SocketChannel m_socketChannel;

        /** Buffer used for writing */
        protected ByteBladder m_outBuf = new ByteBladder(true);

        /** Buffer used for reading */
        protected ByteBladder m_inBuf = new ByteBladder(true);

        /** The number of bytes we will expect to be echoed back. */
        protected int m_byteCount;

        /**
         * We will use the same valve for reading and writing. We could have
         * used a separate valve for each action.
         */
        protected Valve m_valve;

        /**
         * Constructor.
         * @param socketChannel the socket channel for this connection.
         */
        public EchoClientPeer(SocketChannel socketChannel) {
            m_socketChannel = socketChannel;
        }

        /** Starts the interaction with the server. */
        public void init() {
            m_valve = m_ioReactor.valveCreate(m_socketChannel, this);
            m_outBuf.putBytes(m_msg.getBytes());
            m_byteCount = m_outBuf.getUsed();
            m_valve.enable(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            s_logger.info(id() + " Peer initiated");
        }

        /** Terminates the connection and the reactor loop. */
        public void shutdown(String msg) {
            // Perform any necessary cleanup for this EchoClientPeer
            m_valve.close();
            s_logger.info(id() + ": " + msg);
            m_ioReactor.shutdown();
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

        /** Attempts to write bytes to the server. */
        protected void doWrite() {
            int w = -1;
            try {
                m_outBuf.markOut();
                w = m_outBuf.write(m_socketChannel);
            } catch (IOException e) {
                s_logger.error(id(), e);
                shutdown("Shutting down due to write exception");
            }
            if (w >= 0) {
                s_logger.info(id() + " wrote " + w + " bytes:\n" +
                        Ascii.dump(m_outBuf.getOutMarkedBuffer()));
            }

            int used = m_outBuf.getUsed();

            if (used == 0) {
                // There is nothing left to write
                m_valve.disable(SelectionKey.OP_WRITE);
            }
        }

        /** Attempts to read bytes from the server. */
        public void doRead() {
            int r = -1;
            try {
                m_inBuf.markIn();
                r = m_inBuf.read(m_socketChannel, 1024);
            } catch (IOException e) {
                s_logger.error(id(), e);
                shutdown("Shutting down due to read exception");
            }
            if (r >= 0) {
                s_logger.info(id() + " read " + r + " bytes:\n" +
                        Ascii.dump(m_inBuf.getInMarkedBuffer()));
                if (m_inBuf.getUsed() == m_byteCount) {
                    shutdown("All bytes written have been echoed back");
                }
            } else {
                shutdown(" EOF: closing");
            }
        }
    }

    /** Simple AcceptorListener that spawns peers as connections are accepted */
    protected class EchoConnectorAdapter extends ConnectorAdapter {
        protected EchoConnectorAdapter(String name, String host, int port) {
            super(name, host, port);
        }

        public void peerCreate(IOReactor ioReactor, SocketChannel socketChannel) {
            EchoClientPeer peer = new EchoClientPeer(socketChannel);
            peer.init();
        }
    }

    /** Start connection */
    public void init() {
        m_ioReactor = IOReactor.getDefaultIOReactor();
        Connector c = new Connector(m_ioReactor,
          new EchoConnectorAdapter("EchoClientAcceptor", m_host, m_port));
        c.connect();
    }


    /** The main event. */
    public static void main(String[] args) {
        String host = "localhost";
        int port = 6789;
        String msg = "ECHO";

        // This is a silly way to do this. Should use properties or real
        // command line processing.
        switch (args.length) {
        case 3:
            msg = args[2];
            // FALLTHROUGH
        case 2:
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                s_logger.error("Couldn't parse port number", e);
                usage(e.getMessage());
                System.exit(1);
            }
            // FALLTHROUGH
        case 1:
            host = args[0];
            // FALLTHROUGH
        case 0:
            break;
        default:
            usage("Too many arguments");
            System.exit(1);
            // NOTREACHED
        }

        EchoClient client = new EchoClient(host, port, msg);
        client.init();
        for (;;) {
            try {
                // Sleep until the reactor exits
                client.getIOReactor().getThread().join();
                break;
            } catch (InterruptedException e) {
            }
        }
        s_logger.info("IOReactor has terminated: cycle count = " +
          client.getIOReactor().getCycleCount());
        System.exit(0);
    }
}

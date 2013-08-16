package com.readyposition.reactor.examples;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
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
 * And example class that listens for connections on a port. It
 * receives any data sent over connected sockets and formats it to the
 * standard output. If any socket delivers the character 'EOT'
 * (ctrl-D), then the program will exit.
 */
public class DumpServer {
    /** Class wide logger. */
    private final static Logger s_logger =
        LoggerFactory.getLogger(DumpServer.class);

    private static void usage(String msg) {
        s_logger.error(msg + "\n" +
                       "usage: java " + DumpServer.class.getName() +
                       " [port=6789]");
    }

    private int m_port;
    public int getPort() { return m_port; }

    private IOReactor m_ioReactor;
    public IOReactor getIOReactor() { return m_ioReactor; }

    public DumpServer(int port) {
    	m_port = port;
    }

    protected class DumpAcceptorAdapter
    	extends AcceptorAdapter
    	implements ValveHandler
    {
    	protected ByteBladder m_buf = new ByteBladder(true);

    	protected DumpAcceptorAdapter(String name, int port) {
            super(name, port);
    	}

    	public void peerCreate(IOReactor ioReactor, SocketChannel socketChannel) {
            Valve valve = ioReactor.valveCreate(socketChannel, this);
            valve.enable(SelectionKey.OP_READ);
    	}

    	public void valveFire(Valve valve, SelectionKey selectedKey) {
            int r = 0;
            ReadableByteChannel channel = 
                (ReadableByteChannel)valve.getChannel();
            m_buf.clear();
            try {
                r = m_buf.read(channel, 1024);
            } catch (IOException e) {
                s_logger.error("Failed to read", e);
                r = -1;
            }
            if (r >= 0) {
                s_logger.info(channel.toString() + '\n' +
                              Ascii.dump(m_buf.getOutBuffer()));
                byte[] bytes = m_buf.getBytes(new byte[m_buf.getUsed()]);
                for (int i = 0; i < bytes.length; i++) {
                    if (bytes[i] == Ascii.EOT) {
                        m_ioReactor.shutdown();
                    }
                }
            } else {
                s_logger.info("EOF");
                valve.close();
            }
    	}
    }

    public void init() {
    	m_ioReactor = IOReactor.getDefaultIOReactor();
    	Acceptor a = new Acceptor(m_ioReactor,
                                  new DumpAcceptorAdapter("DumpServerAcceptor", m_port));
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

        DumpServer server = new DumpServer(port);
        server.init();
        for (;;) {
            try {
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

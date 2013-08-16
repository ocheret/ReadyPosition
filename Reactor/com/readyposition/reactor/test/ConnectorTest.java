package com.readyposition.reactor.test;

import java.nio.channels.SocketChannel;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.readyposition.reactor.Acceptor;
import com.readyposition.reactor.AcceptorAdapter;
import com.readyposition.reactor.AcceptorListener;
import com.readyposition.reactor.Connector;
import com.readyposition.reactor.ConnectorAdapter;
import com.readyposition.reactor.ConnectorListener;
import com.readyposition.reactor.IOReactor;
import com.readyposition.reactor.Timer;
import com.readyposition.reactor.TimerHandler;

public class ConnectorTest
{
    /** Used for synchronization. */
    protected Object m_lock;

    /** The Connector succeeded. */
    protected boolean m_connectDone;

    /** The Acceptor succeeded. */
    protected boolean m_acceptDone;

    /** The timer went off. */
    protected boolean m_timedOut;

    /** Asynchronous multiplexed I/O reactor. */
    private IOReactor m_ioReactor;

    /** Used to time out operations. */
    protected Timer m_timer;

    @BeforeClass
    public static void setUpBeforeClass() {}

    @AfterClass
    public static void tearDownAfterClass() {}

    /** Set up tests. */
    @Before
    public void setUp() {
        m_ioReactor = IOReactor.getIOReactor("ConnectorTestIOReactor");
        m_connectDone = m_acceptDone = m_timedOut = false;
        m_lock = new Object();
    }

    /** Tear down tests. */
    @After
    public void tearDown() {
        m_ioReactor.shutdown();
        m_ioReactor = null;
        m_timer = null;
        m_lock = null;
    }

    /** Simple ConnectorListener that notes if connection is successful. */
    protected class TestConnectorAdapter extends ConnectorAdapter {
        TestConnectorAdapter(String name, String host, int port) {
            super(name, host, port);
        }

        public void peerCreate(IOReactor ioReactor, SocketChannel socketChannel) {
            m_timer.cancel();
            try {
                socketChannel.close();
                synchronized (m_lock) {
                    m_connectDone = true;
                    m_lock.notify();
                }
            } catch (Throwable t) {
            }
        }
    }

    /** Simple AcceptorListener that notes if connection is successful. */
    protected class TestAcceptorAdapter extends AcceptorAdapter {
        TestAcceptorAdapter(String name, String host, int port) {
            super(name, host, port);
        }

        public void peerCreate(IOReactor ioReactor, SocketChannel socketChannel) {
            m_timer.cancel();
            try {
                socketChannel.close();
                synchronized (m_lock) {
                    m_acceptDone = true;
                    m_lock.notify();
                }
            } catch (Throwable t) {
            }
        }
    }

    /**
     * Spawn a Connector and Acceptor and time out if they don't connect soon.
     */
    @Test
    public void testConnector() {
        //        m_timer = m_ioReactor.timerCreateRel(1000L, new TimerHandler() {
        //            public long timerFire(long time, long actualTime) {
        //                m_timedOut = true;
        //                synchronized (m_lock) {
        //                    m_lock.notify();
        //                    return 0L;
        //                }
        //            }
        //        });
        //
        //         ConnectorListener cl = new TestConnectorAdapter("testConnector",
        //           "localhost", 9876);
        //         Connector conn = new Connector(m_ioReactor, cl);
        //         conn.connect();
        //
        //         AcceptorListener al = new TestAcceptorAdapter("testAcceptor",
        //           "localhost", 9876);
        //         Acceptor acc = new Acceptor(m_ioReactor, al);
        //         acc.listen();
        //
        //         // XXX
        //         synchronized (m_lock) {
        //             while ((!m_acceptDone || !m_connectDone) && !m_timedOut) {
        //                 try {
        //                     m_lock.wait();
        //                 } catch (InterruptedException t) {
        //                 }
        //             }
        //         }
        //
        //         assertTrue("Connection not established.", !m_timedOut);
    }

    /**
     * Spawn a connector and acceptor that are guaranteed not to connect and
     * ensure that the timeout occurs.
     */
    //    @Test
    public void testBadConnector() {
        m_timer = m_ioReactor.timerCreateRel(1000L, new TimerHandler() {
                public long timerFire(long time, long actualTime) {
                    synchronized (m_lock) {
                        m_timedOut = true;
                        m_lock.notify();
                        return 0L;
                    }
                }
            });

        ConnectorListener cl = new TestConnectorAdapter("testConnector",
                                                        "localhost", 9877);
        Connector conn = new Connector(m_ioReactor, cl);
        conn.connect();

        AcceptorListener al = new TestAcceptorAdapter("testAcceptor",
                                                      "localhost", 9878);
        Acceptor acc = new Acceptor(m_ioReactor, al);
        acc.listen();

        synchronized (m_lock) {
            while ((!m_acceptDone || !m_connectDone) && !m_timedOut) {
                try {
                    m_lock.wait();
                } catch (InterruptedException t) {
                }
            }
        }

        assertTrue("Connection not established.", m_timedOut);
    }
}

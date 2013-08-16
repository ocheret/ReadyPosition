package com.readyposition.reactor;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOReactor extends TimerReactor
{
    /** Class wide logger. */
    private final static Logger s_logger = LoggerFactory.getLogger(IOReactor.class);

    /** Default singleton instance returned by getDefaultInstance(). */
    private static IOReactor s_defaultInstance;

    /** Count of IOReactor thread created so far. */
    private static AtomicLong s_timerReactorCount = new AtomicLong();

    /** The Selector object used to do I/O multiplexing. */
    protected Selector m_selector;

    /**
     * Returns a default IOReactor instance. Every call to this method
     * returns the same IOReactor instance.  In general, there should
     * only be on IOReactor per process (for efficiency).
     *
     * @return the default IOReactor instance.
     */
    public static synchronized IOReactor getDefaultIOReactor() {
        if (s_defaultInstance == null) {
            s_defaultInstance = new IOReactor("DefaultIOReactor");
            s_defaultInstance.getThread().start();
        }
        return s_defaultInstance;
    }

    /**
     * Returns an IOReactor instance. In general, there should only be
     * on IOReactor per process (for efficiency).
     *
     * @return an IOReactor instance.
     */
    public static IOReactor getIOReactor() {
        IOReactor reactor = new IOReactor();
        reactor.getThread().start();
        return reactor;
    }

    /**
     * Returns a named IOReactor instance. In general, there should only be
     * on IOReactor per process (for efficiency).
     *
     * @return an IOReactor instance.
     */
    public static IOReactor getIOReactor(String name) {
        IOReactor reactor = new IOReactor(name);
        reactor.getThread().start();
        return reactor;
    }

    /**
     * Constructs a IOReactor instance. The constructor does not start the
     * thread. It is necessary to call start() after the IOReactor has been
     * constructed.
     *
     * @param name
     *            the name to assign to the IOReactor thread.
     */
    protected IOReactor(String name) {
        super(name);
        s_timerReactorCount.incrementAndGet();
        try {
            m_selector = Selector.open();
        } catch (Throwable t) {
            s_logger.error("Could not open Selector", t);
        }
    }

    /**
     * Constructs a IOReactor instance. The constructor does not start the
     * thread. It is necessary to call start() after the IOReactor has been
     * constructed. This constructor will create a Thread with the name
     * "IOReactor_N" where N is the number of IOReactors created so far.
     */
    protected IOReactor() {
        this("IOReactor_" + s_timerReactorCount.incrementAndGet());
    }

    /**
     * Wait until there is something to do.  If there are ripe timers or there
     * is any work registered, then this will return right away.  Otherwise,
     * we will wait until either a timer becomes ripe or until we are woken
     * up (e.g. when work is submitted or a new timer is registered.
     */
    protected void waitForWork() {
        try {
            if (isWorkPending()) {
                // There is work to do and/or ripe timers so don't
                // block when selecting for IO.
                m_selector.selectNow();
            } else {
                long sleepTime = getSleepTime();
                if (sleepTime < 0) {
                    // There are no timers registered and there is
                    // no work to do so we will sleep until
                    // something wakes us up.
                    m_selector.select();
                } else {
                    // There is no work to do yet but there is at least
                    // one timer registered.  We will wait until the timer
                    // should be fired or until something wakes us up
                    // first.
                    m_selector.select(sleepTime);
                }
            }
        } catch (Throwable t) {
            // Should never happen
            s_logger.warn(t.toString(), t);
        }
    }

    /** Perform any work that the reactor needs to do. */
    protected void doWork() {
        Set<SelectionKey> keys = m_selector.selectedKeys();
        Iterator<SelectionKey> iterator = keys.iterator();

        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            Anchor anchor = (Anchor)key.attachment();
            int ops = 0;
            try {
                if (key.isValid()) {
                    ops = key.readyOps();
                }
            } catch (Throwable t) {
                s_logger.warn(t.toString(), t);
            } finally {
                iterator.remove();
            }
            anchor.fire(ops);
        }

        // We're done with any SelectableChannels that were ready so
        // we can now handle timers and work.
        super.doWork();
    }

    /** Ensures that the reactor notices newly registered work. */
    protected void wakeup() {
        m_selector.wakeup();
    }

    /**
     * Creates a registered Valve for a SelectableChannel.  No
     * operations will be selected for this Valve yet.  The Valve's
     * enable and disable methods can be used to express interest in
     * I/O operations.
     *
     * @param channel the SelectableChannel for which a Valve is needed.
     * @param handler the SelectableChannel for which a Valve is needed.
     * @return the Valve for this channel
     */
    public Valve valveCreate(SelectableChannel channel, ValveHandler handler) {
        return new Valve(this, channel, handler);
    }

    /**
     * Registers a Valve for a SelectableChannel.  No operations will be
     * selected for this Valve yet.  The Valve's enable and disable methods can
     * be used to express interest in I/O operations.  The Valve can not be
     * currently registered at the time this method is invoked.
     *
     * @param channel the SelectableChannel for which a Valve is needed.
     * @param valve the Valve that is being registered.
     */
    public void valveRegisgter(SelectableChannel channel, Valve valve) {
        if (valve.getIOReactor() != null) {
            throw new IllegalStateException(
                                            "An attempt was made to register a registered Valve");
        }
        valve.setIOReactor(this);
        valve.setChannel(channel);
    }
    
    /** XXX - javadoc */
    protected void enable(final Valve valve, final int ops) {
        if (getThread() == Thread.currentThread()) {
            doEnable(valve, ops);
        } else {
            // We're not in the same thread as the selector so we will submit
            // this as work to that selector's thread
            workSubmit(new Work() {
                    public boolean workFire() {
                        doEnable(valve, ops);
                        return false;
                    }
                });
        }
    }

    /** XXX - javadoc */
    protected void doEnable(Valve valve, int ops) {
        SelectionKey key = getSelectionKey(valve.getChannel());
        Anchor anchor = (Anchor)key.attachment();
        anchor.register(valve, ops);
        key.interestOps(key.interestOps() | ops);
    }

    /** XXX - javadoc */
    protected void disable(final Valve valve, final int ops) {
        if (getThread() == Thread.currentThread()) {
            doDisable(valve, ops);
        } else {
            // We're not in the same thread as the selector so we will submit
            // this as work to that selector's thread
            workSubmit(new Work() {
                    public boolean workFire() {
                        doDisable(valve, ops);
                        return false;
                    }
                });
        }
    }

    /** XXX - javadoc */
    protected void doDisable(Valve valve, int ops) {
        SelectionKey key = getSelectionKey(valve.getChannel());
        Anchor anchor = (Anchor)key.attachment();
        anchor.unregister(valve, ops);
        key.interestOps(key.interestOps() & ~ops);
    }

    /** XXX - javadoc */
    protected void close(final Valve valve) {
        if (getThread() == Thread.currentThread()) {
            doClose(valve);
        } else {
            // We're not in the same thread as the selector so we will submit
            // this as work to that selector's thread
            workSubmit(new Work() {
                    public boolean workFire() {
                        doClose(valve);
                        return false;
                    }
                });
        }
    }

    /** XXX - javadoc */
    protected void doClose(Valve valve) {
        if (valve.getIOReactor() == null || valve.getChannel() == null) {
            return;
        }
        SelectionKey key = getSelectionKey(valve.getChannel());
        if (!key.isValid()) {
            return;
        }
        Anchor anchor = (Anchor)key.attachment();
        if (anchor != null) {
            anchor.close();
        }
        try {
            valve.getChannel().close();
        } catch (Throwable t) {
        }
        valve.setIOReactor(null);
        valve.setChannel(null);
    }

    /**
     * Sets the Valve for this channel replacing any previously registered
     * valve and disabling all registered I/O operations previously set.

     * @param channel the SelectableChannel for which a Valve is needed.
     * @param valve the Valve to be associated with this channel.
     * @return the old set of I/O operations enabled on this channel
     *         before this call.
     */
    public int valveRegister(SelectableChannel channel, Valve valve) {
        SelectionKey key = getSelectionKey(channel);
        int oldOps = key.interestOps();
        key.interestOps(0);
        key.attach(valve);
        return oldOps;
    }

    /** XXX - javadoc */
    protected Anchor getAnchor(SelectableChannel channel) {
        return (Anchor)(getSelectionKey(channel).attachment());
    }

    /**
     * Retrieves the SelectionKey that connects a SelectableChannel with
     * the Selector for this IOReactor.
     *
     * @param channel a channel associated with this IOReactor
     * @return the SelectionKey that connects this channel with the
     *         Selector for this IOReactor.
     */
    protected SelectionKey getSelectionKey(SelectableChannel channel) {
        SelectionKey key = channel.keyFor(m_selector);
        if (key == null) {
            // We have not previously registered this channel.
            try {
                key = channel.register(m_selector, 0);
                Anchor anchor = new Anchor(this, key);
                key.attach(anchor);
            } catch (Throwable t) {
                s_logger.error("Could not register channel with selector", t);
                return null;
            }
        }
        return key;
    }
}

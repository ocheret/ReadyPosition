/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import com.readyposition.utils.heap.LongObjectHeap;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A "main loop" thread that provides a simple way to do asynchronous
 * multiplexed I/O, timers, and work procedures.
 */
public class MainLoop extends WorkLoop {
    /** Logger for this class. */
    private final static Logger s_logger =
	Logger.getLogger(MainLoop.class.getName());

    /** Default singleton instance returned by getDefaultInstance(). */
    private static MainLoop s_defaultInstance;

    /**
     * Returns a default MainLoop instance.  Every call to this method
     * returns the same MainLoop instance.
     *
     * @return the default MainLoop instance.
     */
    public static synchronized MainLoop getDefaultInstance() {
	if (s_defaultInstance == null) {
	    s_defaultInstance = new MainLoop("DefaultMainLoop");
	}
	return s_defaultInstance;
    }


    public static synchronized WorkLoop getDefaultWorkLoop() {
        return getDefaultInstance();
    }


    /**
     * Constructs a MainLoop instance.  The constructor does not start
     * the thread.  It is necessary to call start() after the MainLoop
     * has been constructed.
     *
     * @param name the name to assign to the MainLoop thread.
     */
    public MainLoop(String name) {
	super(name);
	try {
	    m_selector = Selector.open();
	} catch (Throwable t) {
	    s_logger.log(Level.SEVERE, "Couldn't open selector", t);
	}
    }

    /**
     * Constructs a MainLoop instance.  The constructor does not start
     * the thread.  It is necessary to call start() after the MainLoop
     * has been constructed.  This constructor will create a Thread with
     * the name "MainLoop".
     */
    public MainLoop() {
	this("MainLoop");
    }

    /** The Selector object used to do I/O multiplexing. */
    protected Selector m_selector;

    /** An heap of the registered timers ordered by firing time. */
    protected LongObjectHeap m_timers = new LongObjectHeap();

    /** The thread's run method.  Handles events until shutdown() is called. */
    public void run() {
	// These are internally managed queues of Workers and Timers
	// that are ready to run.  This allows us to avoid race
	// conditions related to new Workers and Timers being added
	// during callbacks.
	Worker headWorker = new Worker();
	Timer headTimer = new Timer();

	// Loop until shutdown is called.
	while (!m_done) {
	    // Determine if the workers queue is empty.
	    boolean haveWorkers;
	    synchronized (m_workers) {
		haveWorkers = (m_workers != m_workers.getNext());
	    }

	    // Determine if any timers are 'ripe'.
	    int numTimers;
	    long firstTime = 0;
	    synchronized (m_timers) {
		numTimers = m_timers.getSize();
		if (numTimers > 0)
		    firstTime =
			((Timer)m_timers.getValue(m_timers.peek())).getTime();
	    }
	    long sleepTime = firstTime - System.currentTimeMillis();

//  	    Iterator keys = m_selector.keys().iterator();
//  	    while (keys.hasNext()) {
//  		SelectionKey key = (SelectionKey)keys.next();
//  		try {
//  		    s_logger.info("chan=" + key.channel() +
//  		      ", ops=" + Ops.opsToString(key.interestOps()));
//  		} catch (Throwable t) {
//  		}
//  	    }

	    int numSelected = 0;
	    try {
		if (haveWorkers || (numTimers > 0 && sleepTime <= 0)) {
		    // We either have workers or a timer is past due.
		    // Don't block.
		    numSelected = m_selector.selectNow();
//  		    s_logger.info("selectNow() returned " + numSelected);
		} else if (numTimers > 0) {
		    // There are no workers but there are timers.
		    // Block until there is I/O work to do or the
		    // first timer is due.
		    numSelected = m_selector.select(sleepTime);
//  		    s_logger.info("select(" + sleepTime + ") returned " +
//  		      numSelected);
		} else {
		    // There are no workers and no timers.  Block
		    // until there is I/O work to do or until another
		    // thread wakes us up by registering I/O, a timer,
		    // or a worker.
		    numSelected = m_selector.select();
//  		    s_logger.info("select() returned " + numSelected);
		}
	    } catch (Throwable t) {
		// We can expect IOException, ClosedSelectorException.
		// We should not see IllegalArgumentException because
		// we are guaranteed to have a positive sleepTime in
		// the case where we select with a timeout.  Any
		// Throwable is bad news so we might as well close the
		// selector.
		s_logger.log(Level.SEVERE, t.toString(), t);
		try {
		    m_selector.close();
		} catch (Throwable tt) {
		    s_logger.log(Level.WARNING, "Couldn't close selector", tt);
		}
		return;
	    }

	    // Handle ready I/O channels
	    Set selectedKeys = m_selector.selectedKeys();
	    if (selectedKeys.size() > 0) {
		Iterator it = selectedKeys.iterator();
		while (it.hasNext()) {
		    SelectionKey key = (SelectionKey)it.next();
		    Anchor anchor = (Anchor)key.attachment();

		    int ops = 0;
		    try {
			if (key.isValid()) {
			    ops = key.readyOps();
			}
		    } catch (Throwable t) {
			s_logger.log(Level.WARNING,
			  "Could not determine ready operations for channel " +
			  key.channel(), t);
			continue;
		    } finally {
			it.remove();
		    }

		    anchor.fire(ops);
		}
	    }

	    // Compile a queue of timers that are ripe.
	    long now = System.currentTimeMillis();
	    while (true) {
		int entry = -1;
		long fireTime = -1L;
		Timer timer = null;
		synchronized (m_timers) {
		    if (m_timers.getSize() == 0)
			break;
		    entry = m_timers.peek();
		    fireTime = m_timers.getKey(entry);
		    if (fireTime > now)
			break;
		    timer = (Timer)m_timers.getValue(entry);
		    m_timers.remove(entry);
		    if (timer.getEntry() != -1)
			headTimer.insert(timer);
		}
	    }

	    // We can execute the ripe timers without worrying about
	    // synchronization issues.
	    for (Timer timer = (Timer)headTimer.getNext();
		 timer != headTimer;
		 timer = (Timer)headTimer.getNext())
	    {
		timer.remove();
		try {
		    timer.getHandler().timerFire(now);
		    timer.setEntry(-1);
		} catch (Throwable t) {
		    s_logger.log(Level.WARNING, t.toString(), t);
		}
	    }

	    // Transfer the workers to another queue that can't be
	    // touched by callbacks.
	    synchronized (m_workers) {
		if (m_workers == (Worker)m_workers.getNext()) {
		    // List is empty
		    continue;
		}
		headWorker.insert(m_workers);
		m_workers.remove();
	    }

	    doWorkers(headWorker);
	}
    }

    /** Ensures that the loop notices newly registered work. */
    protected void wakeup() {
	m_selector.wakeup();
    }

    /**
     * Creates a timer that will fire in a specified number of milliseconds.
     *
     * @param delta the number of milliseconds from the current time in which
     *	the timer should fire.  If delta is negative the timer will fire
     *	the next time through the main loop.
     * @param handler the TimerHandler that should be invoked when the timer
     *	is ripe.
     * @return the Timer that was created.
     */
    public Timer timerCreate(long delta, TimerHandler handler) {
	return timerCreateAbs(System.currentTimeMillis() + delta, handler);
    }

    /**
     * Creates a timer that will fire at a specific time.
     *
     * @param time the time at which the timer should fire
     *	(a la System.currentTimeMillis()).  If the time is in the past then
     *	the timer will fire the next time through the main loop.
     * @param handler the TimerHandler that should be invoked when the timer
     *	is ripe.
     * @return the Timer that was created.
     */
    public Timer timerCreateAbs(long time, TimerHandler handler) {
	Timer t = new Timer(this, time, handler);
	synchronized (m_timers) {
	    t.setEntry(m_timers.insert(time, t));
	}
	if (this != Thread.currentThread()) {
	    // Make sure the main loop doesn't sleep through the
	    // specified time.
	    wakeup();
	}
	return t;
    }

    /**
     * Cancels a timer.
     *
     * @param timer the timer to be cancelled.
     */
    protected void timerCancel(Timer timer) {
	synchronized (m_timers) {
	    int entry = timer.getEntry();
	    if (entry == -1)
		return;
	    // TODO - the remove() method causes problems for some
	    // reason so we just mark the entry as invalid with -1.
// 	    m_timers.remove(entry);
	    timer.setEntry(-1);
	}
    }

    /**
     * Creates a Channeler for a SelectableChannel.  Initially, the
     * Channeler is not enabled for any I/O operations.  The
     * Channeler.enable() and Channeler.disable() methods are used to
     * regulate this.
     *
     * @param channel the SelectableChannel monitored by this Channeler.
     * @param handler the ChannelerHandler that will be invoked when I/O
     * operations are ready to be performed.
     */
    public Channeler channelerCreate(SelectableChannel channel,
      ChannelerHandler handler)
    {
	return new Channeler(this, channel, handler);
    }

    /**
     * Enables a channeler for certain I/O operations.
     *
     * @param channeler the Channler to enable.
     * @param ops the operations to enable.
     * @return the I/O operations actually enabled.
     */
    protected int enable(Channeler channeler, int ops) {
	SelectableChannel chan = channeler.getChannel();
	SelectionKey sk = chan.keyFor(m_selector);
	Anchor anchor;
	if (sk == null) {
	    // There is no association between this channel and selector yet.
	    anchor = new Anchor(chan);
	    try {
		sk = chan.register(m_selector, 0, anchor);
	    } catch (Throwable t) {
		s_logger.log(Level.SEVERE,
		  "Could not enable channel with selector", t);
		// TODO - any other action to take?  Close the channel?
		return 0;
	    }
	} else {
	    // We already have an association between the channel and selector.
	    anchor = (Anchor)sk.attachment();
	}

	try {
	    sk.interestOps(ops | sk.interestOps());
	} catch (Throwable t) {
	    s_logger.log(Level.SEVERE,
	      "Could not enable channel with selector", t);
	    // TODO - any other action to take?  Close the channel?
	    return 0;
	}

	return anchor.register(channeler, ops);
    }

    /**
     * Disables a channeler for certain I/O operations.
     *
     * @param channeler the Channler to disable.
     * @param ops the operations to disable.
     * @return the I/O operations actually disabled.
     */
    protected int disable(Channeler channeler, int ops) {
	SelectionKey sk = channeler.getChannel().keyFor(m_selector);
	if (sk == null) {
	    s_logger.warning(
		"Can't unregister channel not registered with a selector");
	    return 0;
	}
	Anchor anchor = (Anchor)sk.attachment();
	if (anchor == null) {
	    s_logger.warning("Can't disable an unanchored channel");
	    return 0;
	}

	try {
	    sk.interestOps((~ops) & sk.interestOps());
	} catch (Throwable t) {
	    s_logger.log(Level.SEVERE,
	      "Could not disable channel with selector", t);
	    // TODO - any other action to take?  Close the channel?
	    return 0;
	}

	return anchor.unregister(channeler, ops);
    }

    /**
     * Used internally by Channeler.close() to disable
     * operations on all Channelers associated with a socket
     * and to close the socket.
     *
     * @param channeler the Channeler to disable operations on.
     */
    protected void close(Channeler channeler) {
	SelectableChannel channel = channeler.getChannel();
	SelectionKey sk = channel.keyFor(m_selector);
	try {
	    channel.close();
	} catch (Throwable t) {
	    s_logger.log(Level.WARNING, "Couldn't close channel " + channel,
	      t);
	}
	if (sk == null) {
	    s_logger.warning(
		"Closed channel not registered with a selector");
	    return;
	}
	Anchor anchor = (Anchor)sk.attachment();
	if (anchor == null) {
	    s_logger.warning("Can't unregister an unanchored channel");
	    return;
	}
	anchor.close();
    }
}

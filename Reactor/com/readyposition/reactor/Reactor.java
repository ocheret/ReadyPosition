package com.readyposition.reactor;

import java.nio.channels.SelectableChannel;

public interface Reactor
{
    /**
     * Gets the Thread for this reactor.
     *
     * @return the Thread for this reactor.
     */
    public Thread getThread();

    /**
     * Creates a Work that will be invoked the next time through the
     * reactor loop.
     *
     * @param handler the WorkHandler that will be invoked the next
     *                time through the loop.
     * @return the Work that was created.
     */
    public Work workCreate(WorkHandler handler);

    /**
     * Place an existing Work on the work queue.
     *
     * @param work A Work created previously by workCreate or a work
     *             subclass created elsewhere
     */
    public void workSubmit(Work work);

    /** Retrieves the number of cycles the reactor loop has executed. */
    public long getCycleCount();

    /** Instructs the WorkReactor to stop running in the near future. */
    public void shutdown();

    /**
     * Creates a timer that will fire in a specified number of milliseconds.
     *
     * @param delta the number of milliseconds from the current time
     *              in which the timer should fire. If delta is
     *              negative the timer will fire the next time through
     *              the main reactor.
     * @param handler the TimerHandler that should be invoked when the
     *                timer is ripe.
     * @return the Timer that was created.
     */
    public Timer timerCreateRel(long delta, TimerHandler handler);

    /**
     * Creates a timer that will fire at a specific time.
     *
     * @param time the time at which the timer should fire (a la
     *             System.currentTimeMillis()). If the time is in the
     *             past then the timer will fire the next time through
     *             the main reactor.
     * @param handler the TimerHandler that should be invoked when the
     *                timer is ripe.
     * @return the Timer that was created.
     */
    public Timer timerCreateAbs(long time, TimerHandler handler);

    /**
     * XXX - javadoc
     */
    public void timerSubmitRel(long delta, Timer timer);

    /**
     * XXX - javadoc
     */
    public void timerSubmitAbs(final long time, final Timer timer);

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
    public Valve valveCreate(SelectableChannel channel, ValveHandler handler);

    /**
     * Registers a Valve for a SelectableChannel.  No operations will be
     * selected for this Valve yet.  The Valve's enable and disable methods can
     * be used to express interest in I/O operations.  The Valve can not be
     * currently registered at the time this method is invoked.
     *
     * @param channel the SelectableChannel for which a Valve is needed.
     * @param valve the Valve that is being registered.
     */
    public void valveRegisgter(SelectableChannel channel, Valve valve);

    /**
     * Sets the Valve for this channel replacing any previously registered
     * valve and disabling all registered I/O operations previously set.

     * @param channel the SelectableChannel for which a Valve is needed.
     * @param valve the Valve to be associated with this channel.
     * @return the old set of I/O operations enabled on this channel
     *         before this call.
     */
    public int valveRegister(SelectableChannel channel, Valve valve);
}

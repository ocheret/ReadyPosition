package com.readyposition.reactor;

import java.util.concurrent.atomic.AtomicInteger;

import com.readyposition.collections.list.Yoke;

/**
 * An abstract superclass for all types of tasks that can be handed to
 * the reactor loop (e.g. Valves, Timers, Works).
 */
public abstract class Task<T extends Task<T>> extends Yoke<T>
{
    /** Pending state. */
    protected final static int STATE_PENDING = 1;

    /** Active state. */
    protected final static int STATE_ACTIVE = 2;

    /** Canceled state. */
    protected final static int STATE_CANCELED = 3;

    /** The state of this reactor task. */
    protected AtomicInteger m_state = new AtomicInteger(STATE_PENDING);

    /**
     * Cancels this task.
     *
     * @return true if the Task was able to be canceled or it was
     * already canceled.
     */
    public boolean cancel() {
        return (m_state.compareAndSet(STATE_PENDING, STATE_CANCELED) ||
                (m_state.get() == STATE_CANCELED));
    }

    /**
     * Activate this task.
     *
     * @return true if the Task was able to be activated.
     */
    protected boolean activate() {
        return m_state.compareAndSet(STATE_PENDING, STATE_ACTIVE);
    }

    /**
     * Makes this task pending
     *
     * @return true if the task was able to be made pending
     */
    protected boolean complete() {
        return m_state.compareAndSet(STATE_ACTIVE, STATE_PENDING);
    }

    /**
     * Sets this task to the Pending state.  This should be invoked
     * when it is being resubmitted.
     */
    protected void setToPending() {
        m_state.set(STATE_PENDING);
    }

    /**
     * Sets this task to the Active state.  This should be invoked
     * when it it is in progress.
     */
    protected void setToActive() {
        m_state.set(STATE_ACTIVE);
    }

    /**
     * Sets this task to the Canceled state.  This should be invoked
     * when it has been canceled.  A public interface is provided by
     * subclasses that validates the cancelation.
     */
    protected void setToCanceled() {
        m_state.set(STATE_CANCELED);
    }

    /** Determines if this task has been cancelled. */
    public boolean isPending() {
        return m_state.get() == STATE_PENDING;
    }

    /** Determines if this task has been cancelled. */
    public boolean isActive() {
        return m_state.get() == STATE_ACTIVE;
    }

    /** Determines if this task has been cancelled. */
    public boolean isCanceled() {
        return m_state.get() == STATE_CANCELED;
    }
}

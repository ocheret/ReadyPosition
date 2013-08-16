package com.readyposition.reactor;

/** Implements a simple exponential backoff timer. */
public class BackOff {
    /** The minimum amount of time to wait between retries. */
    protected long m_min;

    /** The maximum amount of time to wait between retries. */
    protected long m_max;

    /** The next amount of time to wait between retries. */
    protected long m_next;

    /**
     * Constructs a BackOff object.
     *
     * @param min the minimum amount of time to wait between retries.
     * @param max the maximum amount of time to wait between retries.
     */
    public BackOff(long min, long max) {
        // Argument checking
        if (min <= 0) {
            throw new IllegalArgumentException("min == " + min +
                                               " needs to be > 0");
        }
        if (max < min) {
            throw new IllegalArgumentException("max < min, max == " +
                                               max + ", min == " + min);
        }
        m_min = min;
        m_max = max;
        reset();
    }

    /**
     * Indicates to the BackOff a successful retry.  This means that the
     * next time we need to retry the minimum time will be used.
     */
    public void reset() {
        m_next = m_min;
    }

    /**
     * Gets the number of milliseconds to wait until the next retry.
     * Each time this is called the length of time will increase
     * exponentially but will never exceed the maximum retry time
     * passed into the constructor.
     *
     * @return the number of milliseconds to wait until the next retry.
     */
    public long getNext() {
        long result = m_next;
        if (m_next < m_max) {
            // We need to back off some more.
            m_next *= 2;
            if (m_next > m_max) {
                // We're at maximum backoff.
                m_next = m_max;
            }
        }
        return result;
    }
}

package com.readyposition.collections.hash;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

import com.readyposition.collections.itemizer.Itemizable;
import com.readyposition.collections.itemizer.Itemizer;

/**
 * This is an abstract base class for array oriented hash tables
 * (sets, maps, and tables). This provides the common machinery used
 * by subclasses to implement hash data structures with keys and
 * values of arbitrary types and consisting of an arbitrary number of
 * fields. Keys and values are maintained by subclasses in parallel
 * arrays to reduce the number of Object references.  This abstract
 * base class provides an array of buckets to which keys are
 * hashed. Hash collisions are resolved using a linked list that is
 * maintained in a 'next' array that is parallel to the key and value
 * arrays.
 */
public abstract class AbstractHash implements Itemizable {
    /** The default initial capacity. */
    protected static final int DEFAULT_INITIAL_CAPACITY = 16;

    /** The maximum capacity. */
    protected static final int MAX_CAPACITY = 1 << 30;

    /** The default load factor. */
    protected static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /** The number of entries in this table. */
    protected int m_size;

    /** The first free element in the parallel arrays. */
    protected int m_firstFree = -1;

    /** Each bucket can reference the head of a linked list of entries. */
    protected int[] m_buckets;

    /** Parallels the key/value arrays and represents entry linked lists. */
    protected int[] m_next;

    /** Used by iterators to ensure consistency. */
    protected int m_modCount = 0;

    /** Determines when we need to grow the table. */
    protected int m_threshold;

    /** Used to compute the threshold for growing the table. */
    protected float m_loadFactor;

    /** The next available entry. */
    protected int m_nextUnused = 0;

    /**
     * Constructs an empty AbstractHash with the specified inital
     * capacity and load factor.
     *
     * @param initialCapacity the initial capacity.
     * @param loadFactor the load factor.
     */
    protected AbstractHash(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: "
                    + initialCapacity);
        }
        if (initialCapacity > MAX_CAPACITY) {
            initialCapacity = MAX_CAPACITY;
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal Load Factor: "
                    + loadFactor);

        // Find a power of 2 >= initialCapacity
        int capacity = HashUtils.powerOfTwo(initialCapacity);

        m_loadFactor = loadFactor;
        m_threshold = (int) (capacity * loadFactor);
        m_buckets = new int[capacity];
        Arrays.fill(m_buckets, -1);
        m_next = new int[m_threshold];
        growKeys(m_threshold);
        growValues(m_threshold);
    }

    /**
     * Constructs an empty AbstractHash with the specified inital
     * capacity and the default load factor (0.75)
     *
     * @param initialCapacity the initial capacity.
     */
    protected AbstractHash(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty AbstractHash with default capacity (16) and
     * the default load factor (0.75)
     */
    protected AbstractHash() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /** Gets the number of entries in this table. */
    public int getSize() {
        return m_size;
    }

    /** Determines if the set is empty. */
    public boolean isEmpty() {
        return m_size == 0;
    }

    /**
     * Retrieves all of the entries in the table. Subclasses should
     * provide appropriately typed getKeys and getValues methods that
     * will return arrays parallel to the one returned here.
     *
     * @return an array of integer entries.
     */
    public int[] getEntries() {
        return getEntries(null);
    }

    /**
     * Retrieves all of the entries in the table. Subclasses should
     * provide appropriately typed getKeys and getValues methods that
     * will return arrays parallel to the one returned here.
     *
     * @param dst an array into which the entry numbers will be
     *            placed. If the array isn't long enough, then a new
     *            array will be allocated and returned.
     * @return an array of integer entries.
     */
    public int[] getEntries(int[] dst) {
        if (dst == null || dst.length < m_size) {
            dst = new int[m_size];
        }
        int count = 0;
        for (int bucket = 0; bucket < m_buckets.length; bucket++) {
            for (int i = m_buckets[bucket]; i != -1; i = m_next[i]) {
                dst[count++] = i;
            }
        }
        return dst;
    }

    /** Removes all entries (mappings) from the table. */
    public void clear() {
        m_modCount++;
        Arrays.fill(m_buckets, -1);
        clearKeys();
        clearValues();
        m_size = 0;
        m_firstFree = -1;
        m_nextUnused = 0;
    }

    /**
     * Grow the keys array. All keys are copied to the expanded array.
     *
     * @param size the new size of the keys array.
     */
    protected abstract void growKeys(int size);

    /** Clears the keys array. */
    protected abstract void clearKeys();

    /**
     * Grow the values array. All values are copied to the expanded array.
     *
     * @param size the new size of the values array.
     */
    protected abstract void growValues(int size);

    /** Clears the values array. */
    protected abstract void clearValues();

    /**
     * Clean up data for the value at a specified entry. This is
     * mostly important when values are Object to ensure that they get
     * garbage collected as early as possible.
     *
     * @param entry the entry number of the value to clean up.
     */
    protected abstract void removeValue(int entry);

    /**
     * Clean up data for the key at a specified entry. This is mostly
     * important when values are Object to ensure that they get
     * garbage collected as early as possible.
     *
     * @param entry the entry number of the key to clean up.
     */
    protected abstract void removeKey(int entry);

    /** Returns an itemizer for traversing the entries in the hash table. */
    public Itemizer itemizer() {
        HashItemizer i = new HashItemizer();
        i.init(this);
        return i;
    }

    /**
     * Returns an itemizer for traversing the entries in the
     * hashtable, reusing the itemizer that was passed in.
     */
    public Itemizer itemizer(Itemizer oldItemizer) {
        HashItemizer i = (HashItemizer) oldItemizer;
        if (i == null) {
            i = new HashItemizer();
        }
        i.init(this);
        return i;
    }

    /** Class allowing iteration through a hash table's entries. */
    protected static class HashItemizer implements Itemizer {
        /** The current hash table this itemizer is traversing. */
        protected AbstractHash m_table;

        /** The bucket for the current entry. */
        protected int m_bucket;

        /** The index of the current entry. */
        protected int m_entry;

        /**
         * Used to manage removal. Normally points to previous returned index
         * for the current bucket. If m_prev == -1, then the current entry is
         * the first for the current bucket.
         */
        protected int m_prev;

        /** Makes sure hash table hasn't been messed with during traversal. */
        protected int m_expectedModCount;

        /**
         * Set to true after a remove, indicating that we have already advanced
         * to the next element.
         */
        private boolean m_justRemoved;

        /** Constructor */
        HashItemizer() {}

        protected void init(AbstractHash table) {
            m_table = table;
            m_expectedModCount = table.m_modCount;
            m_justRemoved = false;
            m_bucket = 0;
            findNextEntry(-1, table.m_buckets[0]);
        }

        /** Returns true if there are more entries. */
        public boolean hasMore() {
            if (m_table.m_modCount != m_expectedModCount) {
                throw new ConcurrentModificationException();
            }

            return (-1 != m_entry);
        }

        /** Gets the entry number of the next entry. */
        public void advance() {
            if (m_table.m_modCount != m_expectedModCount) {
                throw new ConcurrentModificationException();
            }

            // Special case for post-removal
            if (m_justRemoved) {
                // Under the covers we have already advanced to the entry after
                // the one we just removed.
                m_justRemoved = false;
                return;
            }

            if (-1 == m_entry) {
                // This iterator has already been exhausted.
                throw new NoSuchElementException(
                        "Can't advance() an exhausted Itemizer");
            }

            // Advance to the next entry
            findNextEntry(m_entry, m_table.m_next[m_entry]);
        }

        public int entry() {
            if (m_table.m_modCount != m_expectedModCount) {
                throw new ConcurrentModificationException();
            }

            if (m_justRemoved) {
                // The current entry was just removed.
                throw new NoSuchElementException(
                        "Can't get the entry for a removed element.");
            }
            if (-1 == m_entry) {
                throw new NoSuchElementException(
                        "Can't get an entry from an exhausted Itemizer");
            }
            return m_entry;
        }

        public int next() {
            advance();
            return entry();
        }

        /** Removes the current entry. */
        public void remove() {
            if (m_table.m_modCount != m_expectedModCount) {
                throw new ConcurrentModificationException();
            }

            if (m_justRemoved) {
                throw new NoSuchElementException(
                        "Can't remove() an element more than once.");
            }

            if (-1 == m_entry) {
                throw new NoSuchElementException(
                        "Can't remove an entry after Itemizer exhausted.");
            }

            // Remove the entry from the bucket's list
            int next = m_table.m_next[m_entry];
            if (-1 == m_prev) {
                m_table.m_buckets[m_bucket] = next;
            } else {
                m_table.m_next[m_prev] = next;
            }

            // Add the entry to free list
            m_table.m_next[m_entry] = m_table.m_firstFree;
            m_table.m_firstFree = m_entry;

            // Clean up value
            m_table.removeValue(m_entry);

            // Keep stats up to date
            m_table.m_size--;
            m_table.m_modCount++;
            m_expectedModCount++;

            // Advance to the next entry (m_prev stays the same if we stay in
            // same bucket)
            findNextEntry(m_prev, next);
            m_justRemoved = true;
        }

        private void findNextEntry(int prev, int next) {
            m_prev = prev;
            m_entry = next;
            if (-1 == m_entry) {
                // We've reached the end of this bucket.
                m_prev = -1;

                // Find the next nonempty bucket and get the first entry there.
                while (-1 == m_entry &&
                       ++m_bucket < m_table.m_buckets.length)
                    {
                        m_entry = m_table.m_buckets[m_bucket];
                    }
            }
        }
    }

    /**
     * Returns the maximum number of key collisions. Slow, but good
     * for testing
     */
    public int getMaxCollisions() {
        int max = 0;
        for (int bucket = 0; bucket < m_buckets.length; bucket++) {
            int cnt = 0;
            for (int i = m_buckets[bucket]; i != -1; i = m_next[i]) {
                cnt++;
            }
            if (cnt > max) {
                max = cnt;
            }
        }
        return max;
    }
}

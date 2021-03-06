// _Warning_

package com.readyposition.collections.vector;

import java.util.Arrays;

import com.readyposition.collections.comparator.*;
import com.readyposition.collections.search.*;
import com.readyposition.collections.sort.*;
import com.readyposition.collections.common.*;

/**
 * Vector_ValueName_ provides a resizable array of type _ValueType_
 * with an assortment of capabilities such as the ability to fill
 * sections, insert, remove, append, sort, and search.
 */
public class Vector_ValueName_ {
    /** Default initial capacity. */
    protected final static int DEFAULT_InitialCapacity = 16;

    /** Default growth factor. */
    protected final static float DEFAULT_GrowthFactor = 1.5f;

    /** The initial capacity. */
    protected int m_initialCapacity;

    /** The growth factor. */
    protected float m_growthFactor;

    /** The array of values. */
    protected _ValueType_[] m_values;

    /** The number of elements in use. */
    protected int m_used;

    /**
     * Constructor.
     *
     * @param initialCapacity the initial number of preallocated elements
     * @param growthFactor the factor by which to increase the tree size when it needs to
     *                     expand.
     */
    public Vector_ValueName_(int initialCapacity, float growthFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal capacity: "
                                               + initialCapacity);
        }

        if (growthFactor <= 1.0 || java.lang.Float.isNaN(growthFactor)) {
            throw new IllegalArgumentException("Illegal growth factor: "
                                               + growthFactor);
        }

        m_initialCapacity = (initialCapacity == 0) ? 1 : initialCapacity;
        m_growthFactor = growthFactor;

        reset(initialCapacity);
    }

    /**
     * Constructor.
     *
     * @param initialCapacity the initial number of preallocated elements.
     */
    public Vector_ValueName_(int initialCapacity) {
        this(initialCapacity, DEFAULT_GrowthFactor);
    }

    /** Constructor. */
    public Vector_ValueName_() {
        this(DEFAULT_InitialCapacity, DEFAULT_GrowthFactor);
    }

    /** Constructor. */
    public Vector_ValueName_(_ValueType_[] values) {
        this(values.length, DEFAULT_GrowthFactor);
        add(values);
    }

    /**
     * Clears any values and resets value storage capacity.
     */
    public void reset() {
        reset(m_initialCapacity);
    }

    /**
     * Clears any values and resets value storage capacity.
     *
     * @param count the new required storage capacity for values.
     */
    public void reset(int count) {
        m_values = new _ValueType_[count];
        m_used = 0;
    }

    /**
     * Appends a single value to the end of the vector. The vector will be
     * resized to a size large enough to accommodate the new value.
     *
     * @param value the new value to append to the vector.
     * @return the integer index where the new value was placed in the vector.
     */
    public int add(_ValueType_ value) {
        int pos = m_used;
        resize(pos + 1);
        m_values[pos] = value;
        return pos;
    }

    /**
     * Appends an array to the end of the vector. The vector will be
     * resized to a size large enough to accommodate the new values.
     *
     * @param values the array of values to append to the vector.
     * @return the index where the first element of the new data was placed.
     */
    public int add(_ValueType_[] values) {
        return add(values, 0, values.length);
    }

    /**
     * Appends an array segment to the end of the vector. The vector
     * will be resized to a size large enough to accommodate the new
     * values.
     *
     * @param values the array containing the values to append to the vector.
     * @param offset the offset of the start of the segment from the values.
     * @param length the number of elements in the segment from values.
     * @return the index where the first element of the new data was placed.
     */
    public int add(_ValueType_[] values, int offset, int length) {
        int pos = m_used;
        resize(pos + length);
        System.arraycopy(values, offset, m_values, pos, length);
        return pos;
    }

    /**
     * Sets all entries to a specified value.
     *
     * @param value the new value for this entry.
     */
    public void fill(_ValueType_ value) {
        fill(0, m_used, value);
    }

    /**
     * Sets a range of entries to a specified value.
     *
     * @param start the first entry whose value we want to set.
     * @param end one past the last entry whose value we want to set.
     * @param value the new value for this entry.
     */
    public void fill(int start, int end, _ValueType_ value) {
        if (start > m_used) {
            throw new ArrayIndexOutOfBoundsException(start);
        }
        if (end > m_used) {
            throw new ArrayIndexOutOfBoundsException(end);
        }
        Arrays.fill(m_values, start, end, value);
    }

    /**
     * Inserts a single value at the specified index in the
     * vector. Any elements currently at the index or later are copied
     * after the inserted value.
     *
     * @param index the index at which to insert the data.
     * @param value the value to insert.
     */
    public void insert(int index, _ValueType_ value) {
        if (index == m_used) {
            add(value);
        } else {
            int moveCount = m_used - index;
            resize(m_used + 1);
            System.arraycopy(m_values, index, m_values, index + 1, moveCount);
            m_values[index] = value;
        }
    }

    /**
     * Inserts a all values from an array at the specified index in
     * the vector.  Any elements currently at the index or later are
     * copied after the inserted values.
     *
     * @param index the index at which to insert the data.
     * @param values the value to insert.
     */
    public void insert(int index, _ValueType_[] values) {
        insert(index, values, 0, values.length);
    }

    /**
     * Inserts values from an array segment at the specified index in
     * the vector. Any elements currently at the index or later are
     * copied after the inserted values.
     *
     * @param index the index at which to insert the data.
     * @param values the value to insert.
     * @param offset the offset into values of the segment to insert.
     * @param length the length of the segment in values.
     */
    public void insert(int index, _ValueType_[] values, int offset, int length) {
        if (index == m_used) {
            add(values, offset, length);
        } else {
            int moveCount = m_used - index;
            resize(m_used + length);
            System.arraycopy(m_values, index, m_values, index + length,
                    moveCount);
            System.arraycopy(values, offset, m_values, index, length);
        }
    }

    /**
     * Removes a single value from the vector and moves any subsequent
     * values to lower indices to fill the gap.
     *
     * @param index the index of the value to remove.
     * @return the removed value.
     */
    public _ValueType_ remove(int index) {
        _ValueType_ value = m_values[index];
        remove(index, 1);
        return value;
    }

    /**
     * Removes a span of values from the vector and moves any
     * subsequent values to lower indices to fill the gap.
     *
     * @param index the index of the start of the span of values to remove.
     * @param length the number of values in the span to remove.
     */
    public void remove(int index, int length) {
        if (index < 0 || index >= m_used) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        if (index + length == m_used) {
            // Removing data at the end of the array
            Arrays.fill(m_values, m_used - length, m_used,
                        Types.get_ValueName_For(0));
        } else {
            // Removing data not at the end of the array
            System.arraycopy(m_values, index + length, m_values, index,
                             m_used - (index + length));
            Arrays.fill(m_values, m_used - length, m_used,
                        Types.get_ValueName_For(0));
        }
        m_used -= length;
    }

    /**
     * Removes the first occurrence of a specified value from the
     * vector and moves any subsequent values to lower indices to fill
     * the gap. The vector is not modified if the value isn't found.
     *
     * @param value the value to remove.
     * @return true if a value was removed.
     */
    public boolean removeValue(_ValueType_ value) {
        return removeValue(value, Comparators._ValueName_Asc);
    }

    /**
     * Removes the first occurrence of a specified value from the
     * vector and moves any subsequent values to lower indices to fill
     * the gap. The vector is not modified if the value isn't found.
     *
     * @param value the value to remove.
     * @param eq the equivalence tester to use
     * @return true if a value was removed.
     */
    public boolean removeValue(_ValueType_ value, Eq_ValueName_ eq) {
        int index = indexOf(value, eq);
        if (index >= 0) {
            remove(index);
            return true;
        }
        return false;
    }

    /**
     * Sorts all values in the vector in ascending order using the
     * default comparator.
     */
    public void sort() {
        Sort_ValueName_.quick(m_values, 0, m_used, Comparators._ValueName_Asc);
    }

    /**
     * Sorts a span of values in the vector in ascending order using
     * the default comparator.
     *
     * @param start the start of the span of values (inclusive)
     * @param end the end of the span of values (exclusive)
     */
    public void sort(int start, int end) {
        Sort_ValueName_.quick(m_values, start, end, Comparators._ValueName_Asc);
    }

    /**
     * Sorts all values in the vector in some order using a provided
     * comparator.
     *
     * @param cmp the comparator used to compare values.
     */
    public void sort(Cmp_ValueName_ cmp) {
        Sort_ValueName_.quick(m_values, 0, m_used, cmp);
    }

    /**
     * Sorts a span of values in the vector in some order using a provided
     * comparator.
     *
     * @param start the start of the span of values (inclusive).
     * @param end the end of the span of values (exclusive).
     * @param cmp the comparator used to compare values.
     */
    public void sort(int start, int end, Cmp_ValueName_ cmp) {
        Sort_ValueName_.quick(m_values, start, end, cmp);
    }

    /**
     * Performs a binary search of the entire vector for a specific value using
     * the default comparator.
     *
     * @param value the value to search for.
     * @return the offset of the value in the vector or, if the value
     *         is not, found, the negative of the index of the
     *         insertion point (suitable for passing to the insert
     *         methods).
     */
    public int binarySearch(_ValueType_ value) {
        return Search_ValueName_.binary(m_values, 0, m_used, value);
    }

    /**
     * Performs a binary search of the entire vector for a specific value using
     * a specified comparator.
     *
     * @param value the value to search for.
     * @return the offset of the value in the vector or, if the value
     *         is not, found, the negative of the index of the
     *         insertion point (suitable for passing to the insert
     *         methods).
     */
    public int binarySearch(_ValueType_ value, Cmp_ValueName_ cmp) {
        return Search_ValueName_.binary(m_values, 0, m_used, value, cmp);
    }

    /**
     * Performs a binary search of a span of the vector for a specific
     * value using the default comparator.
     *
     * @param index the start of the span in which to search.
     * @param length the length of the span to search.
     * @param value the value to search for.
     * @return the offset of the value in the vector or, if the value
     *         is not, found, the negative of the index of the
     *         insertion point (suitable for passing to the insert
     *         methods).
     */
    public int binarySearch(int index, int length, _ValueType_ value) {
        return Search_ValueName_.binary(m_values, index, length, value);
    }

    /**
     * Performs a binary search of a span of the vector for a specific
     * value using a specified comparator.
     *
     * @param index the start of the span in which to search.
     * @param length the length of the span to search.
     * @param value the value to search for.
     * @param cmp the comparator to use when searching.
     * @return the offset of the value in the vector or, if the value
     *         is not, found, the negative of the index of the
     *         insertion point (suitable for passing to the insert
     *         methods).
     */
    public int binarySearch(int index, int length, _ValueType_ value, Cmp_ValueName_ cmp) {
        return Search_ValueName_.binary(m_values, index, length, value, cmp);
    }

    /**
     * Searches the vector front to back for the index of
     * value using a default equialence tester.
     *
     * @param value value to search for
     * @return the first offset of the value, or -1 if it is not in
     * the vector
     */
    public int indexOf(_ValueType_ value) {
        return indexOf(0, value, Comparators._ValueName_Asc);
    }

    /**
     * Searches the vector front to back for the index of value using
     * a specified equialence tester.
     *
     * @param value value to search for
     * @param eq the equivalence tester to use
     * @return the first offset of the value, or -1 if it is not in
     *         the vector
     */
    public int indexOf(_ValueType_ value, Eq_ValueName_ eq) {
        return indexOf(0, value, eq);
    }

    /**
     * Searches the vector front to back for the index of a value,
     * starting at an offset using a default equialence tester.
     *
     * @param offset the offset at which to start the linear search
     *               (inclusive)
     * @param value value to search for
     * @return the first offset of the value, or -1 if it is not in
     *         the vector
     */
    public int indexOf(int offset, _ValueType_ value) {
        return indexOf(offset, value, Comparators._ValueName_Asc);
    }

    /**
     * Searches the vector front to back for the index of a value,
     * starting at an offset using a specified equialence tester.
     *
     * @param offset the offset at which to start the linear search
     *               (inclusive)
     * @param value value to search for
     * @param eq the equivalence tester to use
     * @return the first offset of the value, or -1 if it is not in
     *         the vector
     */
    public int indexOf(int offset, _ValueType_ value, Eq_ValueName_ eq) {
        for (int i = offset; i < m_used; i++) {
            if (eq.equals(m_values[i], value)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Determins if a value is present in the list.
     *
     * @param value value to look for.
     * @return true if value is in the list.
     */
    public boolean contains(_ValueType_ value) {
        return indexOf(value) >= 0;
    }

    /**
     * Determins if a value is present in the list.
     *
     * @param value value to look for.
     * @param eq the equivalence tester to use
     * @return true if value is in the list.
     */
    public boolean contains(_ValueType_ value, Eq_ValueName_ eq) {
        return indexOf(value, eq) >= 0;
    }

    /**
     * Gets the number of elements in the vector.
     *
     * @return the number of elements in the vector.
     */
    public int getSize() {
        return m_used;
    }

    /**
     * Determines if the vector doesn't not contain any values.
     *
     * @return true if the vector doesn't contain any values.
     */
    public boolean isEmpty() {
        return m_used == 0;
    }

    /**
     * Gets the number of elements that can be put into the vector without
     * requiring additional allocation.
     *
     * @return the current capacity of the vector.
     */
    public int getCapacity() {
        return m_values.length;
    }

    /**
     * Gets the value for a specified entry.
     *
     * @param i the entry whose value we want to retrieve.
     * @return the value for the specified entry.
     */
    public _ValueType_ get(int i) {
        if (i >= m_used) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        return m_values[i];
    }

    /**
     * Gets the value for a specified entry or return a defaultValue if the
     * entry is out of the vector bounds.
     *
     * @param i the entry whose value we want to retrieve.
     * @param defaultValue value to return if i is beyond the end of the vector.
     * @return the value for the specified entry.
     */
    public _ValueType_ get(int i, _ValueType_ defaultValue) {
        return (i >= m_used) ? defaultValue : m_values[i];
    }

    /**
     * Gets the value for a specified entry without bounds checking.
     *
     * @param i the entry whose value we want to retrieve.
     * @return the value for the specified entry.
     */
    public _ValueType_ getQuick(int i) {
        return m_values[i];
    }

    /**
     * Modifies the value for a specified entry.
     *
     * @param index the entry whose value we want to set.
     * @param value the new value for this entry.
     */
    public void set(int index, _ValueType_ value) {
        if (index >= m_used) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        m_values[index] = value;
    }

    /**
     * Modifies the value for a specified entry without bounds checking.
     *
     * @param index the entry whose value we want to set.
     * @param value the new value for this entry.
     */
    public void setQuick(int index, _ValueType_ value) {
        m_values[index] = value;
    }

    /**
     * Modifies the values for a specified span of entries.
     *
     * @param index the entry whose value we want to set.
     * @param values the array of values to place in the vector.
     */
    public void set(int index, _ValueType_[] values) {
        set(index, values, 0, values.length);
    }

    /**
     * Modifies the values for a specified span of entries from a span of an
     * array.
     *
     * @param index the entry whose value we want to set.
     * @param values the array of values containing the span to place
     *               in the vector.
     * @param offset the start of the span in the values array.
     * @param length the length of the span in the values array.
     */
    public void set(int index, _ValueType_[] values, int offset, int length) {
        if ((index + length) > m_used) {
            throw new ArrayIndexOutOfBoundsException(index + length);
        }
        System.arraycopy(values, offset, m_values, index, length);
    }

    /**
     * Modifies the value for a specified entry. If the specified
     * entry is beyond the end of the vector, the vector will grow to
     * accommodate the new data and any new entries will be
     * initialized to a default missing value (0 or null).
     *
     * @param index the entry whose value we want to set.
     * @param value the new value for this entry.
     */
    public void put(int index, _ValueType_ value) {
        put(index, value, Types.get_ValueName_For(0));
    }

    /**
     * Modifies the value for a specified entry. If the specified
     * entry is beyond the end of the vector, the vector will grow to
     * accommodate the new data and any new entries will be
     * initialized to a specified missing value.
     *
     * @param index the entry whose value we want to set.
     * @param value the new value for this entry.
     * @param missingValue the value to initialize new allocated entries to.
     */
    public void put(int index, _ValueType_ value, _ValueType_ missingValue) {
        if (index >= m_used) {
            ensureCapacity(index + 1);
            Arrays.fill(m_values, m_used, index, missingValue);
            m_used = index + 1;
        }
        m_values[index] = value;
    }

    /**
     * Modifies the values for a specified span of entries. If data
     * would be placed beyond the end of the vector, the vector will
     * grow to accommodate the new data, and any new entries will be
     * initialized to a default missing value (0 or null).
     *
     * @param index the entry whose value we want to set.
     * @param values the array of values to place in the vector.
     */
    public void put(int index, _ValueType_[] values) {
        put(index, values, 0, values.length, Types.get_ValueName_For(0));
    }

    /**
     * Modifies the values for a specified span of entries from a span
     * of an array. If data would be placed beyond the end of the
     * vector, the vector will grow to accommodate the new data, and
     * any new entries will be initialized to a default missing value
     * (0 or null).
     *
     * @param index the entry whose value we want to set.
     * @param values the array of values containing the span to place in the vector.
     * @param offset the start of the span in the values array.
     * @param length the length of the span in the values array.
     */
    public void put(int index, _ValueType_[] values, int offset, int length) {
        put(index, values, offset, length, Types.get_ValueName_For(0));
    }

    /**
     * Modifies the values for a specified span of entries from a span
     * of an array. If data would be placed beyond the end of the
     * vector, the vector will grow to accommodate the new data, and
     * any new entries will be initialized to a specified missing
     * value.
     *
     * @param index the entry whose value we want to set.
     * @param values the array of values containing the span to place
     *               in the vector.
     * @param offset the start of the span in the values array.
     * @param length the length of the span in the values array.
     * @param missingValue the value to initialize new allocated
     *                     entries to.
     */
    public void put(int index, _ValueType_[] values, int offset, int length,
                    _ValueType_ missingValue)
    {
        if ((index + length) > m_used) {
            ensureCapacity(index + length);
            if (index >= m_used) {
                Arrays.fill(m_values, m_used, index, missingValue);
                m_used = index + length;
            }
        }
        System.arraycopy(values, offset, m_values, index, length);
    }

    /**
     * If necessary reallocates the vector so that capacity exactly matches
     * the number of elements in use.
     */
    public void trim() {
        if (m_used != m_values.length) {
            _ValueType_[] newValues = new _ValueType_[m_used];
            System.arraycopy(m_values, 0, newValues, 0, m_used);
            m_values = newValues;
        }
    }

    /**
     * Grow the values array. All values are copied to the expanded array.
     *
     * @param size the new size of the values array.
     */
    public void resize(int size) {
        resize(size, Types.get_ValueName_For(0));
    }

    /**
     * Grow the values array. All values are copied to the expanded array.
     *
     * @param size the new size of the values array.
     * @param missingValue the value to initialize new allocated entries to.
     */
    public void resize(int size, _ValueType_ missingValue) {
        if (size == m_used) {
            return;
        }

        if (size < m_used) {
            // Clear values for the extra elements
            Arrays.fill(m_values, size, m_used, missingValue);
        } else {
            if (size > m_values.length) {
                int newCapacity = (int) (m_values.length * m_growthFactor);
                if (size > newCapacity) {
                    newCapacity = size;
                }
                _ValueType_[] newValues = new _ValueType_[newCapacity];
                System.arraycopy(m_values, 0, newValues, 0, m_used);
                m_values = newValues;
                Arrays.fill(m_values, m_used, newCapacity, missingValue);
            } else {
                Arrays.fill(m_values, m_used, size, missingValue);
            }
        }
        m_used = size;
    }

    /**
     * Removes all values from this vector.  Equivalent to calling resize(0).
     */
    public void clear() {
        resize(0);
    }

    /**
     * Ensures that the vector can grow to contain at least a
     * specified number of elements without requiring reallocation.
     *
     * @param newCapacity the new capacity.
     */
    public void ensureCapacity(int newCapacity) {
        int capacity = m_values.length;
        if (newCapacity > capacity) {
            int minNewCapacity = (int) (m_values.length * m_growthFactor);
            if (minNewCapacity > newCapacity) {
                newCapacity = minNewCapacity;
            }
            _ValueType_[] newValues = new _ValueType_[newCapacity];
            System.arraycopy(m_values, 0, newValues, 0, m_used);
            m_values = newValues;
            Arrays.fill(m_values, m_used, newCapacity,
                        Types.get_ValueName_For(0));
        }
    }

    /**
     * Ensures that the vector can grow to contain at least a specified number
     * of elements without requiring reallocation.
     *
     * @param newCapacity the new capacity.
     * @param missingValue the value to initialize new allocated entries to.
     */
    public void ensureCapacity(int newCapacity, _ValueType_ missingValue) {
        int capacity = m_values.length;
        if (newCapacity > capacity) {
            int minNewCapacity = (int) (m_values.length * m_growthFactor);
            if (minNewCapacity > newCapacity) {
                newCapacity = minNewCapacity;
            }
            _ValueType_[] newValues = new _ValueType_[newCapacity];
            System.arraycopy(m_values, 0, newValues, 0, m_used);
            m_values = newValues;
            Arrays.fill(m_values, m_used, newCapacity, missingValue);
        }
    }
    
    /**
     * Returns an array containing all of the elements in this vector in the
     * same order as in the vector.
     * 
     * <p>
     * The returned array will be "safe" in that no references to it are
     * maintained by this collection. (In other words, this method must allocate
     * a new array even if this collection is backed by an array). The caller is
     * thus free to modify the returned array.
     * 
     * @return an array containing all of the elements in this vector
     */
    public _ValueType_[] toArray() {
        return toArray(null);
    }

    /**
     * Returns an array containing all of the elements in this vector in the
     * same order as in the vector. The results are placed in the supplied array
     * if there is room. Otherwise, a new array of the correct size is allocated
     * and returned.
     *
     * <p>
     * The returned array will be "safe" in that no references to it are
     * maintained by this collection. (In other words, this method must allocate
     * a new array even if this collection is backed by an array). The caller is
     * thus free to modify the returned array.
     *
     * @param a the array into which the elements of this vector are
     *          to be stored, if it is big enough; otherwise, a new
     *          array is allocated for this purpose.
     * @return an array containing all of the elements in this vector
     */
    public _ValueType_[] toArray(_ValueType_[] a) {
        if (a == null || a.length < m_used) {
            // The supplied array isn't big enough so we'll allocate a properly
            // sized array
            a = new _ValueType_[m_used];
        }
        System.arraycopy(m_values, 0, a, 0, m_used);
        return a;
    }
}

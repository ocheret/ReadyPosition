// _Warning_

package com.readyposition.collections.common;

/** Stub class to allow templates to compile. */
public class _ValueType_ implements Comparable<_ValueType_>
{
    /** An integer value. */
    private int m_value;

    public _ValueType_() { }

    public _ValueType_(int value) { m_value = value; }

    public void setValue(int value) { m_value = value; }

    public int compareTo(_ValueType_ v) {
        if (v == null) {
            return 1;
        }
        int result =
            (m_value < v.m_value ? -1 : (m_value == v.m_value ? 0 : 1));
        return result;
    }

    public boolean equals(Object obj) {
        if (obj instanceof _ValueType_) {
            return m_value == ((_ValueType_) obj).m_value;
        }
        return false;
    }

    public int hashCode() {
        return m_value;
    }

    public String toString() {
        return String.valueOf(m_value);
    }
}

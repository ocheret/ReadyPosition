// _Warning_

package com.readyposition.collections.common;

/** Stub class to allow templates to compile. */
public class _KeyType_ implements Comparable<_KeyType_>
{
    /** An integer value. */
    private int m_value;

    public _KeyType_() { }

    public _KeyType_(int value) {
        m_value = value;
    }

    public void setValue(int value) {
        m_value = value;
    }

    public int compareTo(_KeyType_ v) {
        if (v == null) {
            return 1;
        }
        int result =
            (m_value < v.m_value ? -1 : (m_value == v.m_value ? 0 : 1));
        return result;
    }

    public boolean equals(Object obj) {
        if (obj instanceof _KeyType_) {
            return m_value == ((_KeyType_) obj).m_value;
        }
        return false;
    }

    public int hashCode() {
        int code = m_value;
        return code;
    }

    public String toString() {
        return String.valueOf(m_value);
    }
}

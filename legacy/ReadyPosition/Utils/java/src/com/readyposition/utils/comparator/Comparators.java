/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.comparator;

/**
 * This class holds commonly used comparators useful for various utilities.
 */
public class Comparators {
    /**
     * Comparator for char in ascending order.
     */
    public final static CharComparator CHAR_ASC = new CharComparator()
	{
	    public boolean equals(char a, char b)
	    {
		return (a == b);
	    }

	    public int compare(char a, char b)
	    {
		return (a == b) ? 0 : ((a > b) ? 1 : -1);
	    }
	};

    /**
     * Comparator for char in descending order.
     */
    public final static CharComparator CHAR_DESC = new CharComparator()
	{
	    public boolean equals(char a, char b)
	    {
		return (a == b);
	    }

	    public int compare(char a, char b)
	    {
		return (a == b) ? 0 : ((a < b) ? 1 : -1);
	    }
	};

    /**
     * Comparator for byte in ascending order.
     */
    public final static ByteComparator BYTE_ASC = new ByteComparator()
	{
	    public boolean equals(byte a, byte b)
	    {
		return (a == b);
	    }

	    public int compare(byte a, byte b)
	    {
		return (a == b) ? 0 : ((a > b) ? 1 : -1);
	    }
	};

    /**
     * Comparator for byte in descending order.
     */
    public final static ByteComparator BYTE_DESC = new ByteComparator()
	{
	    public boolean equals(byte a, byte b)
	    {
		return (a == b);
	    }

	    public int compare(byte a, byte b)
	    {
		return (a == b) ? 0 : ((a < b) ? 1 : -1);
	    }
	};

    /**
     * Comparator for short in ascending order.
     */
    public final static ShortComparator SHORT_ASC = new ShortComparator()
	{
	    public boolean equals(short a, short b)
	    {
		return (a == b);
	    }

	    public int compare(short a, short b)
	    {
		return (a == b) ? 0 : ((a > b) ? 1 : -1);
	    }
	};

    /**
     * Comparator for short in descending order.
     */
    public final static ShortComparator SHORT_DESC = new ShortComparator()
	{
	    public boolean equals(short a, short b)
	    {
		return (a == b);
	    }

	    public int compare(short a, short b)
	    {
		return (a == b) ? 0 : ((a < b) ? 1 : -1);
	    }
	};

    /**
     * Comparator for int in ascending order.
     */
    public final static IntComparator INT_ASC = new IntComparator()
	{
	    public boolean equals(int a, int b)
	    {
		return (a == b);
	    }

	    public int compare(int a, int b)
	    {
		return (a == b) ? 0 : ((a > b) ? 1 : -1);
	    }
	};

    /**
     * Comparator for int in descending order.
     */
    public final static IntComparator INT_DESC = new IntComparator()
	{
	    public boolean equals(int a, int b)
	    {
		return (a == b);
	    }

	    public int compare(int a, int b)
	    {
		return (a == b) ? 0 : ((a < b) ? 1 : -1);
	    }
	};

    /**
     * Comparator for long in ascending order.
     */
    public final static LongComparator LONG_ASC = new LongComparator()
	{
	    public boolean equals(long a, long b)
	    {
		return (a == b);
	    }

	    public int compare(long a, long b)
	    {
		return (a == b) ? 0 : ((a > b) ? 1 : -1);
	    }
	};

    /**
     * Comparator for long in descending order.
     */
    public final static LongComparator LONG_DESC = new LongComparator()
	{
	    public boolean equals(long a, long b)
	    {
		return (a == b);
	    }

	    public int compare(long a, long b)
	    {
		return (a == b) ? 0 : ((a < b) ? 1 : -1);
	    }
	};

    /**
     * Comparator for float in ascending order.
     */
    public final static FloatComparator FLOAT_ASC = new FloatComparator()
	{
	    public boolean equals(float a, float b)
	    {
		return (a == b);
	    }

	    public int compare(float a, float b)
	    {
		return (a == b) ? 0 : ((a > b) ? 1 : -1);
	    }
	};

    /**
     * Comparator for float in descending order.
     */
    public final static FloatComparator FLOAT_DESC = new FloatComparator()
	{
	    public boolean equals(float a, float b)
	    {
		return (a == b);
	    }

	    public int compare(float a, float b)
	    {
		return (a == b) ? 0 : ((a < b) ? 1 : -1);
	    }
	};

    /**
     * Comparator for double in ascending order.
     */
    public final static DoubleComparator DOUBLE_ASC = new DoubleComparator()
	{
	    public boolean equals(double a, double b)
	    {
		return (a == b);
	    }

	    public int compare(double a, double b)
	    {
		return (a == b) ? 0 : ((a > b) ? 1 : -1);
	    }
	};

    /**
     * Comparator for double in descending order.
     */
    public final static DoubleComparator DOUBLE_DESC = new DoubleComparator()
	{
	    public boolean equals(double a, double b)
	    {
		return (a == b);
	    }

	    public int compare(double a, double b)
	    {
		return (a == b) ? 0 : ((a < b) ? 1 : -1);
	    }
	};

    /**
     * Identity equalizer for Objects.
     */
    public final static ObjectEq OBJECT_ID_EQ = new ObjectEq()
	{
	    public boolean equals(Object a, Object b)
	    {
		return (a == b);
	    }
	};

    /**
     * Equalizer for Objects.
     */
    public final static ObjectEq OBJECT_EQ = new ObjectEq()
	{
	    public boolean equals(Object a, Object b)
	    {
		return (a.equals(b));
	    }
	};

    /**
     * Comparator for Comparables in ascending order.
     */
    public final static ObjectCmp OBJECT_ASC = new ObjectCmp()
	{
	    public int compare(Object a, Object b)
	    {
		return ((Comparable)a).compareTo(b);
	    }
	};

    /**
     * Comparator for Comparables in ascending order.
     */
    public final static ObjectCmp OBJECT_DESC = new ObjectCmp()
	{
	    public int compare(Object a, Object b)
	    {
		return ((Comparable)b).compareTo(a);
	    }
	};

    /**
     * Compares two values.
     *
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(char a, char b)
    {
	return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     *
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(byte a, byte b)
    {
	return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     *
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(short a, short b)
    {
	return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     *
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(int a, int b)
    {
	return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     *
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(long a, long b)
    {
	return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     *
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(float a, float b)
    {
	return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     *
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(double a, double b)
    {
	return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     *
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(Comparable a, Comparable b)
    {
	if (a == b) {
	    return 0;
	}
	if (a == null) {
	    return -1;
	}
	if (b == null) {
	    return 1;
	}
	return a.compareTo(b);
    }
}

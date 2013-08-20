/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils;

/**
 * Class containing static functions to assist with hashing.
 */
public class Hash
{
    /**
     * Returns a hash value for the specified hash code.  This method applies
     * a "supplemental hash function," which defends against poor quality hash
     * code functions.  This is critical because we don't use prime hash table
     * lengths.  This is the exact same function used by java.util.HashMap.
     *
     * @param code The hash code value to be supplementally hashed.
     * @return The supplementally hashed value.
     */
    public static int supp(int code)
    {
        code += ~(code << 9);
        code ^=  (code >>> 14);
        code +=  (code << 4);
        code ^=  (code >>> 10);
        return code;
    }

    /**
     * Computes the hash code for the given long value.  This is the same hash
     * code calculation as in java.lang.Long.
     *
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int code(long value)
    {
	return (int)(value ^ (value >>> 32));
    }

    /**
     * Computes the hash code for the given int value.  This is the same hash
     * code calculation as in java.lang.Integer.
     *
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int code(int value)
    {
	return value;
    }

    /**
     * Computes the hash code for the given short value.  This is the same
     * hash code value as used by java.lang.Short.
     *
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int code(short value)
    {
	return (int)value;
    }

    /**
     * Computes the hash code for the given character value.
     *
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int code(char value)
    {
	return (int)value;
    }

    /**
     * Computes the hash code for the given byte value.  This is the same
     * hash code calculation as used for java.lang.Byte.
     *
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int code(byte value)
    {
	return (int)value;
    }

    /**
     * Computes the hash code for the given boolean value.
     *
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int code(boolean value)
    {
	return value ? 1 : 0;
    }

    /**
     * Computes the hash code for the given double value.  This is the same
     * hash code calculation as used by java.util.Double.
     *
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int code(double value)
    {
	long bits = Double.doubleToRawLongBits(value);
	return (int)(bits ^ (bits >>> 32));
    }

    /**
     * Computes the hash code for the given float value.
     *
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int code(float value)
    {
	return Float.floatToRawIntBits(value);
    }

    /**
     * Computes the hash code for the given object value.  If value is
     * null, the hash code is -1.  If value is not null, this is just
     * a proxy for the value.hashcode() method.
     *
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int code(Object value)
    {
	if (value == null) {
	    return -1;
	}
	return value.hashCode();
    }

    /**
     * Computes the hash code for the given two part long key.
     *
     * @param a The first part of the two part key.
     * @param b The second part of the two part key.
     * @return The hash code for the two part key.
     */
    public static int code(long a, long b)
    {
        int code = 0;
        code = build(0, code, a);
        code = build(1, code, b);
        return code;
    }

    /**
     * Computes the hash code for the given three part long key.
     *
     * @param a The first part of the three part key.
     * @param b The second part of the three part key.
     * @param c The second part of the three part key.
     * @return The hash code for the three part key.
     */
    public static int hash(long a, long b, long c)
    {
        int code = 0;
        code = build(0, code, a);
        code = build(1, code, b);
        code = build(2, code, c);
        return code;
    }

    /**
     * Computes the hash code for  the given multi-part int key.
     *
     * @param values The array of key parts.
     * @return The hash code forthe multi-pary key.
     */
    protected static int code(int[] values)
    {
        int code = 0;
        for (int i = 0; i < values.length; i++) {
            code = Hash.build(i, code, values[i]);
        }
        return code;
    }

    /**
     * Used in building the hash code for a multi-part key.  The location,
     * current hash code and the next value are given.  The function returns
     * the new hash code, which can be used for a subsequent call to one of the
     * build methods or as the hash code for the multi-part key, once all parts
     * have been considered.
     *
     * @param loc The location of the given value in the multi-part key.
     * @param code The hash code computed thus far.
     * @param value The next value to be used in the hash code computation..
     * @return The hash code after inclusion of the new value.
     */
    public static int build(int loc, int code, int value)
    {
        // implementation assumes code(int value) returns value
        switch (loc % 4) {
        case 0:
            return code ^ value;
	case 1:
            return code ^ ~BitRotate.right(value, 10);
	case 2:
            return code ^ BitRotate.left(value, 11);
	default: // case 3
            return code ^ ~BitRotate.right(value, 12);
        }
    }

    /**
     * Used in building the hash code for a multi-part key.  The location,
     * current hash code and the next value are given.  The function returns
     * the new hash code, which can be used for a subsequent call to one of the
     * build methods or as the hash code for the multi-part key, once all parts
     * have been considered.
     *
     * @param loc The location of the given value in the multi-part key.
     * @param code The hash code computed thus far.
     * @param value The next value to be used in the hash code computation..
     * @return The hash code after inclusion of the new value.
     */
    public static int build(int loc, int code, long value)
    {
        switch (loc % 4) {
        case 0:
            return code ^ code(value);
	case 1:
            return code ^ ~code(BitRotate.right(value, 10));
	case 2:
            return code ^ code(BitRotate.left(value, 11));
	default: // case 3
            return code ^ ~code(BitRotate.right(value, 12));
        }
    }
}

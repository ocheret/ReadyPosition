package com.readyposition.collections.hash;

import com.readyposition.collections.common.BitRotate;

public class HashUtils
{
    /**
     * Applies a supplemental hash function to a given hashCode, which
     * defends against poor quality hash functions. This is critical
     * because our hash tables use power-of-two length arrays, that
     * otherwise encounter collisions for hashCodes that do not differ
     * in lower bits. Note: Null keys always map to hash 0, thus index
     * 0.
     * 
     * @param h The value for which the hash code is to be supplemented
     * @return hash code value.
     */
    static int supp(int h) {
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load
        // factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * Computes the hash code for the given long value. This is the
     * same hash code calculation as in java.lang.Long.
     * 
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int hash(long value) {
        return (int)(value ^ (value >>> 32));
    }

    /**
     * Computes the hash code for the given int value. This is the
     * same hash code calculation as in java.lang.Integer.
     * 
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int hash(int value) {
        return value;
    }

    /**
     * Computes the hash code for the given short value. This is the
     * same hash code value as used by java.lang.Short.
     * 
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int hash(short value) {
        return (int)value;
    }

    /**
     * Computes the hash code for the given character value.
     * 
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int hash(char value) {
        return (int)value;
    }

    /**
     * Computes the hash code for the given byte value. This is the
     * same hash code calculation as used for java.lang.Byte.
     * 
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int hash(byte value) {
        return (int)value;
    }

    /**
     * Computes the hash code for the given boolean value.
     * 
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int hash(boolean value) {
        return value ? 1 : 0;
    }

    /**
     * Computes the hash code for the given double value. This is the
     * same hash code calculation as used by java.util.Double.
     * 
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int hash(double value) {
        long bits = Double.doubleToRawLongBits(value);
        return (int)(bits ^ (bits >>> 32));
    }

    /**
     * Computes the hash code for the given float value.
     * 
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int hash(float value) {
        return Float.floatToRawIntBits(value);
    }

    /**
     * Computes the hash code for the given Object value.
     * 
     * @param value The value for which the hash code is to be computed.
     * @return the hash code value.
     */
    public static int hash(Object value) {
        return (value == null) ? 0 : value.hashCode();
    }

    /**
     * Returns the first power of 2 >= initialCapacity
     * 
     * @param initialCapacity expected number of initial entries to support.
     * @return the first power of 2 >= initialCapacity
     */
    public static int powerOfTwo(int initialCapacity) {
        int capacity = 1;
        while (capacity < initialCapacity)
            capacity = capacity << 1;
        return capacity;
    }

    /**
     * Computes the hash code for the given two part long key.
     * 
     * @param a The first part of the two part key.
     * @param b The second part of the two part key.
     * @return The hash code for the two part key.
     */
    public static int code(long a, long b) {
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
    public static int hash(long a, long b, long c) {
        int code = 0;
        code = build(0, code, a);
        code = build(1, code, b);
        code = build(2, code, c);
        return code;
    }

    /**
     * Computes the hash code for the given multi-part int key.
     * 
     * @param values The array of key parts.
     * @return The hash code forthe multi-pary key.
     */
    public static int code(int[] values) {
        int code = 0;
        for (int i = 0; i < values.length; i++) {
            code = build(i, code, values[i]);
        }
        return code;
    }

    /**
     * Used in building the hash code for a multi-part key. The location,
     * current hash code and the next value are given. The function returns the
     * new hash code, which can be used for a subsequent call to one of the
     * build methods or as the hash code for the multi-part key, once all parts
     * have been considered.
     * 
     * @param loc The location of the given value in the multi-part key.
     * @param code The hash code computed thus far.
     * @param value The next value to be used in the hash code computation..
     * @return The hash code after inclusion of the new value.
     */
    public static int build(int loc, int code, int value) {
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
     * Used in building the hash code for a multi-part key. The
     * location, current hash code and the next value are given. The
     * function returns the new hash code, which can be used for a
     * subsequent call to one of the build methods or as the hash code
     * for the multi-part key, once all parts have been considered.
     * 
     * @param loc The location of the given value in the multi-part key.
     * @param code The hash code computed thus far.
     * @param value The next value to be used in the hash code computation.
     * @return The hash code after inclusion of the new value.
     */
    public static int build(int loc, int code, long value) {
        switch (loc % 4) {
        case 0:
            return code ^ hash(value);
        case 1:
            return code ^ ~hash(BitRotate.right(value, 10));
        case 2:
            return code ^ hash(BitRotate.left(value, 11));
        default: // case 3
            return code ^ ~hash(BitRotate.right(value, 12));
        }
    }
}

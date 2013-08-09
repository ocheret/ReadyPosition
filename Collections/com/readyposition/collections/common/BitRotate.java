package com.readyposition.collections.common;

/** Utility class to perform bit rotations on integer types. */
public class BitRotate {
    /**
     * Rotates the bits in a byte right by a specified number of bits.
     * 
     * @param b the value to rotate
     * @param bits the number of bits to rotate the value
     */
    public static byte right(byte b, int bits) {
        bits &= 7;
        byte one = (byte)1;
        return (byte)(((b & 0xff) >>> bits) |
                      ((((one << bits) - one) & b) << (8 - bits)));
    }

    /**
     * Rotates the bits in a byte left by a specified number of bits.
     * 
     * @param b the value to rotate
     * @param bits the number of bits to rotate the value
     */
    public static byte left(byte b, int bits) {
        bits &= 7;
        int shift = 8 - bits;
        byte one = (byte)1;
        return (byte)((b << bits) |
                      (((b & (~((one << shift) - one))) & 0xff) >>> shift));
    }

    /**
     * Rotates the bits in a short right by a specified number of bits.
     * 
     * @param s the value to rotate
     * @param bits the number of bits to rotate the value
     */
    public static short right(short s, int bits) {
        bits &= 15;
        short one = (short)1;
        return (short)(((s & 0xffff) >>> bits) |
                       ((((one << bits) - one) & s) << (16 - bits)));
    }

    /**
     * Rotates the bits in a short left by a specified number of bits.
     * 
     * @param s the value to rotate
     * @param bits the number of bits to rotate the value
     */
    public static short left(short s, int bits) {
        bits &= 15;
        int shift = 16 - bits;
        return (short)((s << bits) |
                       (((s & (~((1 << shift) - 1))) & 0xffff) >>> shift));
    }

    /**
     * Rotates the bits in an int right by a specified number of bits.
     * 
     * @param i the value to rotate
     * @param bits the number of bits to rotate the value
     */
    public static int right(int i, int bits) {
        bits &= 31;
        return (i >>> bits) |
            ((((1 << bits) - 1) & i) << (32 - bits));
    }

    /**
     * Rotates the bits in an int left by a specified number of bits.
     * 
     * @param i the value to rotate
     * @param bits the number of bits to rotate the value
     */
    public static int left(int i, int bits) {
        bits &= 31;
        int shift = 32 - bits;
        return (i << bits) |
            ((i & (~((1 << shift) - 1))) >>> shift);
    }

    /**
     * Rotates the bits in a long right by a specified number of bits.
     * 
     * @param l the value to rotate
     * @param bits the number of bits to rotate the value
     */
    public static long right(long l, int bits) {
        bits &= 63;
        return (l >>> bits) | ((((1L << bits) - 1) & l) << (64 - bits));
    }

    /**
     * Rotates the bits in a long left by a specified number of bits.
     * 
     * @param l the value to rotate
     * @param bits the number of bits to rotate the value
     */
    public static long left(long l, int bits) {
        bits &= 63;
        int shift = 64 - bits;
        return (l << bits) | ((l & (~((1L << shift) - 1))) >>> shift);
    }
}

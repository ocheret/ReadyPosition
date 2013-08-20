/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils;

import java.util.Random;

/**
 * The Hilbert class provides utilities for dealing with n-dimensional
 * Hilbert curves.
 */
public class Hilbert
{
    /**
     * Convert a Hilbert value to a point in n-space
     *
     * This basically uses the algorithm described in
     *
     * "Alternative Algorithm for Hillbert's Space-Filling Curve," A R Butz,
     * IEEE Trans. on Computers, April 1971, pp 424-426.
     *
     * @param n	dimensionality of the space.
     * @param m	order of the Hilbert curve.
     * @param h	Hilbert value to be converted.
     * @return an array of length n holding the coordinates of the point.
     */
    public static long[] valueToPoint(int n, int m, long h) {
	// Loop indices
	int i, k;

	// Make an n-bit mask that will be used to chop up h and to
	// perform rotations.
	long mask = (1 << n) - 1;

	// This will contain h split up into m chunks of n bits
	long[] rho = new long[m];

	// Each j is an integer between 0 and n-1 equal to the
	// subscript of the principal position for rho[i].  The
	// principal position is the last position bit position in
	// rho[i] such that bit[j] != bit[n-1].  If all bits are
	// equal, then j == n - 1.  In the Butz paper these values are
	// between 1 and n but starting at 0 simplifies things.  For
	// example, the principal positions are marked with a * below
	// and the resulting value of j is shown on the right...
	//
	//	1	1	1	1	1*	-> 4
	//	1	0*	1	1	1	-> 1
	//	0	0	1	1*	0	-> 3
	//	0	0	0	0	0*	-> 4
	int[] j = new int[m];

	// Each sigma is an n-bit value such that
	//
	//	sigma[i](0) = rho[i](0)
	//	sigma[i](1) = rho[i](1) ^ rho[i](0)
	//	sigma[i](2) = rho[i](2) ^ rho[i](1)
	//	...
	//	sigma[i](n-1) = rho[i](n-1) ^ rho[i](n-2)
	//
	// where the notation a[b](c) represents the c'th bit of a[b].
	long[] sigma = new long[m];

	// Each tao is an n-bit value obtained by complementing the
	// corresponding sigma in the (n-1)th position and then, if
	// and only if the resulting value is of odd parity, complementing
	// in the principal position.  Hence, each tao is always of even
	// parity.  Note that the parity of sigma[i] is given by the bit
	// rho[i](n-1).
	long[] tao = new long[m];
	
	// This is sigma~ in the Butz paper.  Each delta[i] is an
	// n-bit value obtained by rotating right sigma[i] by a number
	// of positions equal to j[0] + j[1] + ... + j[i-1].  We keep
	// track of this quantity in rotation.
	long[] delta = new long[m];

	// This is tao~ in the Butz paper.  Each gamma[i] is an
	// n-bit value obtained by rotating right tao[i] by a number
	// of positions equal to j[0] + j[1] + ... + j[i-1].  We keep
	// track of this quantity in rotation.
	long[] gamma = new long[m];

	// Each omega is an n-bit value where omega[0] = 0 and
	// omega[i] = omega[i-1] ^ gamma[i-1]
	long[] omega = new long[m];

	// Each alpha is an n-bit value where alpha[i] = omega[i] ^ delta[i]
	long[] alpha = new long[m];

	// An array of length n of m-bit value obtained by bit transposing
	// the alpha array of length m of n-bit values.
	long[] a = new long[n];

	// This is a running sum of j[i] used to determine how much to rotate
	// right sigma and tao to produce delta and gamma, respectively.
	int rotation = 0;

	for (i = 0; i < m; i++) {
	    // Break up the m*n bits of the hilbert value into an
	    // array of m n-bit values.
	    long tmp = rho[i] = (h >>> ((m - i - 1) * n)) & mask;

	    // Calculate sigma from rho
	    sigma[i] = tmp ^ (tmp >>> 1);

	    // Calculate principal position of rho
	    long parity = tmp & 1;
	    j[i] = n - 1;
	    for (k = 1; k < n; k++) {
		if (((tmp >>> k) & 1) != parity) {
		    j[i] = n - k - 1;
		    break;
		}
	    }

	    // Calculate tao
	    tao[i] = sigma[i] ^ 1;
	    if (parity == 0)
		tao[i] ^= 1 << (n - j[i] - 1);

	    // Calculate delta as a right rotation of sigma
	    delta[i] = ((sigma[i] << (n - rotation)) |
	      (sigma[i] >>> rotation)) & mask;

	    // Calculate gamma as a right rotation of tao
	    gamma[i] = ((tao[i] << (n - rotation)) |
	      (tao[i] >>> rotation)) & mask;

	    rotation += j[i];
	    if (rotation > n)
		rotation -= n;
	}

	// Calculate omegas and alphas
	omega[0] = 0L;
	alpha[0] = delta[0];
	for (i = 1; i < m; i++) {
	    omega[i] = omega[i -1] ^ gamma[i - 1];
	    alpha[i] = omega[i] ^ delta[i];
	}

	// Calculate the result
	for (k = 0; k < n; k++) {
	    a[k] = 0;
	    int shift = n - k - 1;
	    for (i = 0; i < m; i++)
		a[k] |= (((alpha[i] >>> shift) & 1) << (m - i - 1));
	}

//  	System.out.print("i:");
//  	for (i = 0; i < m; i++)
//  	    System.out.print("\t" + i);
//  	System.out.println();

//  	System.out.print("rho:");
//  	for (i = 0; i < m; i++)
//  	    System.out.print("\t" +  Misc.bitString(rho[i], n));
//  	System.out.println();

//  	System.out.print("j:");
//  	for (i = 0; i < m; i++)
//  	    System.out.print("\t" + j[i]);
//  	System.out.println();

//  	System.out.print("sigma:");
//  	for (i = 0; i < m; i++)
//  	    System.out.print("\t" +  Misc.bitString(sigma[i], n));
//  	System.out.println();

//  	System.out.print("tao:");
//  	for (i = 0; i < m; i++)
//  	    System.out.print("\t" +  Misc.bitString(tao[i], n));
//  	System.out.println();

//  	System.out.print("delta:");
//  	for (i = 0; i < m; i++)
//  	    System.out.print("\t" +  Misc.bitString(delta[i], n));
//  	System.out.println();

//  	System.out.print("gamma:");
//  	for (i = 0; i < m; i++)
//  	    System.out.print("\t" +  Misc.bitString(gamma[i], n));
//  	System.out.println();

//  	System.out.print("omega:");
//  	for (i = 0; i < m; i++)
//  	    System.out.print("\t" +  Misc.bitString(omega[i], n));
//  	System.out.println();

//  	System.out.print("alpha:");
//  	for (i = 0; i < m; i++)
//  	    System.out.print("\t" +  Misc.bitString(alpha[i], n));
//  	System.out.println();

	return a;
    }

    /**
     * Convert a point in n-space to a Hilbert value.
     *
     * This uses an algorithm that inverts the algorithm in valueToPoint().
     *
     * @param m	order of the Hilbert curve.
     * @param a	an array of length n holding the coordinates of the point to
     *        convert.
     * @return the Hilbert value of the point.
     */
    public static long pointToValue(int m, long[] a) {
	int n = a.length;

	int i, k;

	// Make mask for relevant bits
	long mask = (1 << n) - 1;

	long[] rho = new long[m];
	int[] j = new int[m];
	long[] sigma = new long[m];
	long[] tao = new long[m];
	long[] delta = new long[m];
	long[] gamma = new long[m];
	long[] omega = new long[m];
	long[] alpha = new long[m];

	// Compute the alpha values from a[]
	for (i = 0; i < m; i++) {
	    alpha[i] = 0;
	    int shift = m - i - 1;
	    for (k = 0; k < n; k++)
		alpha[i] |= (((a[k] >>> shift) & 1) << (n - k - 1));
	}

	// Calculate values for i == 0 in preperation for loop
	omega[0] = 0L;
	sigma[0] = delta[0] = alpha[0];

	// This computes rho[0] from sigma[0] as the inverse of
	// sigma[0] = rho[0] ^ (rho[0] >>> 1)
	long bit = (1 << (n - 1));
	rho[0] = sigma[0] & bit;
	for (k = n - 2; k >= 0; k--) {
	    bit >>>= 1;
	    rho[0] |= ((sigma[0] ^ (rho[0] >>> 1)) & bit);
	}

	int rotation = 0;

	long r = rho[0];

	for (i = 1; i < m; i++) {
	    int p = i - 1;

	    // Calculate principal position of previous rho
	    j[p] = n - 1;
	    long tmp = rho[p];
	    long parity = tmp & 1;
	    for (k = 1; k < n; k++) {
		if (((tmp >>> k) & 1) != parity) {
		    j[p] = n - k - 1;
		    break;
		}
	    }

	    // Calculate previous tao
	    tao[p] = sigma[p] ^ 1;
	    if (parity == 0)
		tao[p] ^= 1 << (n - j[p] - 1);

	    // Calculate gamma as a right rotation of tao
	    gamma[p] = ((tao[p] >>> rotation) |
	      (tao[p] << (n - rotation))) & mask;

	    // Calculate omega
	    omega[i] = omega[p] ^ gamma[p];

	    // Calculate delta
	    delta[i] = alpha[i] ^ omega[i];

	    rotation += j[p];
	    if (rotation > n)
		rotation -= n;

	    // Calculate sigma as a left rotation of delta
	    sigma[i] = ((delta[i] << rotation) |
	      (delta[i] >>> (n - rotation))) & mask;

	    // This computes rho[i] from sigma[i] as the inverse of
	    // sigma[i] = rho[i] ^ (rho[i] >>> 1)
	    bit = (1 << (n - 1));
	    rho[i] = sigma[i] & bit;
	    for (k = n - 2; k >= 0; k--) {
		bit >>>= 1;
		rho[i] |= ((sigma[i] ^ (rho[i] >>> 1)) & bit);
	    }

	    r = (r << n) | rho[i];
	}

//  	System.out.println(
//  	    "i\talpha\tomega\tdelta\tsigma\trho\tJ\ttao\tgamma");
//  	for (i = 0; i < m; i++) {
//  	    System.out.print(i);
//  	    System.out.print("\t" + Misc.bitString(alpha[i], n));
//  	    System.out.print("\t" + Misc.bitString(omega[i], n));
//  	    System.out.print("\t" + Misc.bitString(delta[i], n));
//  	    System.out.print("\t" + Misc.bitString(sigma[i], n));
//  	    System.out.print("\t" + Misc.bitString(rho[i], n));
//  	    System.out.print("\t" + j[i]);
//  	    System.out.print("\t" + Misc.bitString(tao[i], n));
//  	    System.out.println("\t" + Misc.bitString(gamma[i], n));
//  	}

	return r;
    }

    // XXX - Replace with a JUnit test

    public static void main(String[] args) {
	if (args.length < 3) {
	    System.err.println("usage: java ...Hilbert n m h ?h ...?");
	    System.exit(1);
	}

	int n = Integer.parseInt(args[0]);
	int m = Integer.parseInt(args[1]);
	for (int j = 2; j < args.length; j++) {
	    long hin = Long.parseLong(args[j]);
	    long a[] = Hilbert.valueToPoint(n, m, hin);
	    long hout = Hilbert.pointToValue(m, a);
	    System.out.print("hin = " + hin + ": a =");
	    for (int i = 0; i < a.length; i++)
		System.out.print(" " + a[i]);
	    System.out.println(": hout = " + hout);
	}

	long elapsed = 0;
	Random rnd = new Random(System.currentTimeMillis());
	for (int i = 0; i < 10000; i++) {
	    long[] a = new long[] {
		rnd.nextLong(), rnd.nextLong(), rnd.nextLong()
	    };
	    long start = System.currentTimeMillis();
	    long x = Hilbert.pointToValue(16, a);
	    long end = System.currentTimeMillis();
	    elapsed += (end - start);
	}
	System.out.println("Time for 10000 iterations = " + elapsed + "ms.");
	System.out.println("Time for 1 iteration = " +
	  ((double)elapsed / 10000.0) + "ms.");
    }
}

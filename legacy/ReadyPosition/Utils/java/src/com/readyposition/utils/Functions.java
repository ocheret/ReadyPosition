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
 * Class providing generally useful static functions.
 */
public class Functions {
    /**
     * Method to convert a value to the lowest power of 2 >= value.
     *
     * @param length the value to round up to the nearest power of 2.
     * @return the power of 2 >= length.
     */
    public static int lengthToSize(int length) {
	if (length <= (1 << 16))
	    if (length <= (1 << 8))
		if (length <= (1 << 4))
		    if (length <= (1 << 2))
			if (length <= (1 << 1))
			    if (length < 0) return 0;
			    else return length;
			else return (1 << 2);
		    else
			if (length <= (1 << 3)) return (1 << 3);
			else return (1 << 4);
		else
		    if (length <= (1 << 6))
			if (length <= (1 << 5)) return (1 << 5);
			else return (1 << 6);
		    else
			if (length <= (1 << 7)) return (1 << 7);
			else return (1 << 8);
	    else
		if (length <= (1 << 12))
		    if (length <= (1 << 10))
			if (length <= (1 << 9)) return (1 << 9);
			else return (1 << 10);
		    else
			if (length <= (1 << 11)) return (1 << 11);
			else return (1 << 12);
		else
		    if (length <= (1 << 14))
			if (length <= (1 << 13)) return (1 << 13);
			else return (1 << 14);
		    else
			if (length <= (1 << 15)) return (1 << 15);
			else return (1 << 16);
	else
	    if (length <= (1 << 24))
		if (length <= (1 << 20))
		    if (length <= (1 << 18))
			if (length <= (1 << 17)) return (1 << 17);
			else return (1 << 18);
		    else
			if (length <= (1 << 19)) return (1 << 19);
			else return (1 << 20);
		else
		    if (length <= (1 << 22))
			if (length <= (1 << 21)) return (1 << 21);
			else return (1 << 22);
		    else
			if (length <= (1 << 23)) return (1 << 23);
			else return (1 << 24);
	    else
		if (length <= (1 << 28))
		    if (length <= (1 << 26))
			if (length <= (1 << 25)) return (1 << 25);
			else return (1 << 26);
		    else
			if (length <= (1 << 27)) return (1 << 27);
			else return (1 << 28);
		else
		    if (length <= (1 << 30))
			if (length <= (1 << 29)) return (1 << 29);
			else return (1 << 30);
		    else
			return Integer.MAX_VALUE;
    }
}

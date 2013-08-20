/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.search;

import com.readyposition.utils.comparator.BooleanCmp;
import com.readyposition.utils.comparator.CharCmp;
import com.readyposition.utils.comparator.ByteCmp;
import com.readyposition.utils.comparator.ShortCmp;
import com.readyposition.utils.comparator.IntCmp;
import com.readyposition.utils.comparator.LongCmp;
import com.readyposition.utils.comparator.FloatCmp;
import com.readyposition.utils.comparator.DoubleCmp;
import com.readyposition.utils.comparator.ObjectCmp;

// XXX - can this stuff be generated?

public class Search
{
    public static int binary(boolean [] values, boolean value,
      BooleanCmp cmp)
    {
	return binary(values, values.length, value, cmp);
    }

    public static int binary(boolean [] values, int length, boolean value,
      BooleanCmp cmp)
    {
	int low = 0;
	int high = length - 1;
	int mid = 0;
	while (low <= high) {
	    mid = (low + high) / 2;
	    int comparison = cmp.compare(value, values[mid]);
	    if (0 < comparison)
		low = mid + 1;
	    else if (0 > comparison)
		high = mid - 1;
	    else
		return mid;
	}
	return -(low + 1);
    }

    public static int binary(char [] values, char value,
      CharCmp cmp)
    {
	return binary(values, values.length, value, cmp);
    }

    public static int binary(char [] values, int length, char value,
      CharCmp cmp)
    {
	int low = 0;
	int high = length - 1;
	int mid = 0;
	while (low <= high) {
	    mid = (low + high) / 2;
	    int comparison = cmp.compare(value, values[mid]);
	    if (0 < comparison)
		low = mid + 1;
	    else if (0 > comparison)
		high = mid - 1;
	    else
		return mid;
	}
	return -(low + 1);
    }

    public static int binary(byte [] values, byte value,
      ByteCmp cmp)
    {
	return binary(values, values.length, value, cmp);
    }

    public static int binary(byte [] values, int length, byte value,
      ByteCmp cmp)
    {
	int low = 0;
	int high = length - 1;
	int mid = 0;
	while (low <= high) {
	    mid = (low + high) / 2;
	    int comparison = cmp.compare(value, values[mid]);
	    if (0 < comparison)
		low = mid + 1;
	    else if (0 > comparison)
		high = mid - 1;
	    else
		return mid;
	}
	return -(low + 1);
    }

    public static int binary(short [] values, short value,
      ShortCmp cmp)
    {
	return binary(values, values.length, value, cmp);
    }

    public static int binary(short [] values, int length, short value,
      ShortCmp cmp)
    {
	int low = 0;
	int high = length - 1;
	int mid = 0;
	while (low <= high) {
	    mid = (low + high) / 2;
	    int comparison = cmp.compare(value, values[mid]);
	    if (0 < comparison)
		low = mid + 1;
	    else if (0 > comparison)
		high = mid - 1;
	    else
		return mid;
	}
	return -(low + 1);
    }

    public static int binary(int [] values, int value,
      IntCmp cmp)
    {
	return binary(values, values.length, value, cmp);
    }

    public static int binary(int [] values, int length, int value,
      IntCmp cmp)
    {
	int low = 0;
	int high = length - 1;
	int mid = 0;
	while (low <= high) {
	    mid = (low + high) / 2;
	    int comparison = cmp.compare(value, values[mid]);
	    if (0 < comparison)
		low = mid + 1;
	    else if (0 > comparison)
		high = mid - 1;
	    else
		return mid;
	}
	return -(low + 1);
    }

    public static int binary(long [] values, long value,
      LongCmp cmp)
    {
	return binary(values, values.length, value, cmp);
    }

    public static int binary(long [] values, int length, long value,
      LongCmp cmp)
    {
	int low = 0;
	int high = length - 1;
	int mid = 0;
	while (low <= high) {
	    mid = (low + high) / 2;
	    int comparison = cmp.compare(value, values[mid]);
	    if (0 < comparison)
		low = mid + 1;
	    else if (0 > comparison)
		high = mid - 1;
	    else
		return mid;
	}
	return -(low + 1);
    }

    public static int binary(float [] values, float value,
      FloatCmp cmp)
    {
	return binary(values, values.length, value, cmp);
    }

    public static int binary(float [] values, int length, float value,
      FloatCmp cmp)
    {
	int low = 0;
	int high = length - 1;
	int mid = 0;
	while (low <= high) {
	    mid = (low + high) / 2;
	    int comparison = cmp.compare(value, values[mid]);
	    if (0 < comparison)
		low = mid + 1;
	    else if (0 > comparison)
		high = mid - 1;
	    else
		return mid;
	}
	return -(low + 1);
    }

    public static int binary(double [] values, double value,
      DoubleCmp cmp)
    {
	return binary(values, values.length, value, cmp);
    }

    public static int binary(double [] values, int length, double value,
      DoubleCmp cmp)
    {
	int low = 0;
	int high = length - 1;
	int mid = 0;
	while (low <= high) {
	    mid = (low + high) / 2;
	    int comparison = cmp.compare(value, values[mid]);
	    if (0 < comparison)
		low = mid + 1;
	    else if (0 > comparison)
		high = mid - 1;
	    else
		return mid;
	}
	return -(low + 1);
    }

    public static int binary(Object [] values, Object value,
      ObjectCmp cmp)
    {
	return binary(values, values.length, value, cmp);
    }

    public static int binary(Object [] values, int length, Object value,
      ObjectCmp cmp)
    {
	int low = 0;
	int high = length - 1;
	int mid = 0;
	while (low <= high) {
	    mid = (low + high) / 2;
	    int comparison = cmp.compare(value, values[mid]);
	    if (0 < comparison)
		low = mid + 1;
	    else if (0 > comparison)
		high = mid - 1;
	    else
		return mid;
	}
	return -(low + 1);
    }
}

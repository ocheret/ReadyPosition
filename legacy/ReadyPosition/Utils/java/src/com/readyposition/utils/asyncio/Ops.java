/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import java.nio.channels.SelectionKey;

/**
 * XXX - javadoc
 */
public class Ops {
    /**
     * Returns a string representation of an operations set.
     *
     * @return a string showing a pipe separated list of operations,
     *  e.g. READ|WRITE
     */
    public static String opsToString(int ops) {
	StringBuffer buf = new StringBuffer();
	String prefix = "";
	if ((ops & SelectionKey.OP_ACCEPT) != 0) {
	    buf.append("ACCEPT");
	    prefix = "|";
	}
	if ((ops & SelectionKey.OP_CONNECT) != 0) {
	    buf.append(prefix).append("CONNECT");
	    prefix = "|";
	}
	if ((ops & SelectionKey.OP_READ) != 0) {
	    buf.append(prefix).append("READ");
	    prefix = "|";
	}
	if ((ops & SelectionKey.OP_WRITE) != 0) {
	    buf.append(prefix).append("WRITE");
	}
	return buf.toString();
    }
}

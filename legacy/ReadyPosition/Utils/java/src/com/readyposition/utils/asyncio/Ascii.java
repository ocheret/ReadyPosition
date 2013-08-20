/*
 * Copyright 1997, 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils.asyncio;

import java.nio.ByteBuffer;

/**
 * Convenience class for looking at ASCII data.
 */
public class Ascii
{
    /** Printable representations of ASCII characters. */
    protected final static String[] s_str = {
	/*   0 */ "nul", "soh", "stx", "etx", "eot", "enq", "ack", "bel",
	/*   8 */ " bs", " ht", " nl", " vt", " np", " cr", " so", " si",
	/*  16 */ "dle", "dc1", "dc2", "dc3", "dc4", "nak", "syn", "etb",
	/*  24 */ "can", " em", "sub", "esc", " fs", " gs", " rs", " us",
	/*  32 */ " sp", "  !", "  \"","  #", "  $", "  %", "  &", "  '",
	/*  40 */ "  (", "  )", "  *", "  +", "  ,", "  -", "  .", "  /",
	/*  48 */ "  0", "  1", "  2", "  3", "  4", "  5", "  6", "  7",
	/*  56 */ "  8", "  9", "  :", "  ;", "  <", "  =", "  >", "  ?",
	/*  64 */ "  @", "  A", "  B", "  C", "  D", "  E", "  F", "  G",
	/*  72 */ "  H", "  I", "  J", "  K", "  L", "  M", "  N", "  O",
	/*  80 */ "  P", "  Q", "  R", "  S", "  T", "  U", "  V", "  W",
	/*  88 */ "  X", "  Y", "  Z", "  [", "  \\","  ]", "  ^", "  _",
	/*  96 */ "  `", "  a", "  b", "  c", "  d", "  e", "  f", "  g",
	/* 104 */ "  h", "  i", "  j", "  k", "  l", "  m", "  n", "  o",
	/* 112 */ "  p", "  q", "  r", "  s", "  t", "  u", "  v", "  w",
	/* 120 */ "  x", "  y", "  z", "  {", "  |", "  }", "  ~", "del"
	/* 128 */
    };

    /** Ascii nul character. */
    public final static byte NUL = 0x00;

    /** Ascii soh character. */
    public final static byte SOH = 0x01;

    /** Ascii stx character. */
    public final static byte STX = 0x02;

    /** Ascii etx character. */
    public final static byte ETX = 0x03;

    /** Ascii eot character. */
    public final static byte EOT = 0x04;

    /** Ascii enq character. */
    public final static byte ENQ = 0x05;

    /** Ascii ack character. */
    public final static byte ACK = 0x06;

    /** Ascii bel character. */
    public final static byte BEL = 0x07;

    /** Ascii bs character. */
    public final static byte BS  = 0x08;

    /** Ascii ht character. */
    public final static byte HT  = 0x09;

    /** Ascii nl character. */
    public final static byte NL  = 0x0a;

    /** Ascii vt character. */
    public final static byte VT  = 0x0b;

    /** Ascii np character. */
    public final static byte NP  = 0x0c;

    /** Ascii cr character. */
    public final static byte CR  = 0x0d;

    /** Ascii so character. */
    public final static byte SO  = 0x0e;

    /** Ascii si character. */
    public final static byte SI  = 0x0f;

    /** Ascii dle character. */
    public final static byte DLE = 0x10;

    /** Ascii dc1 character. */
    public final static byte DC1 = 0x11;

    /** Ascii dc2 character. */
    public final static byte DC2 = 0x12;

    /** Ascii dc3 character. */
    public final static byte DC3 = 0x13;

    /** Ascii dc4 character. */
    public final static byte DC4 = 0x14;

    /** Ascii nak character. */
    public final static byte NAK = 0x15;

    /** Ascii syn character. */
    public final static byte SYN = 0x16;

    /** Ascii etb character. */
    public final static byte ETB = 0x17;

    /** Ascii can character. */
    public final static byte CAN = 0x18;

    /** Ascii em character. */
    public final static byte EM  = 0x19;

    /** Ascii sub character. */
    public final static byte SUB = 0x1a;

    /** Ascii esc character. */
    public final static byte ESC = 0x1b;

    /** Ascii fs character. */
    public final static byte FS  = 0x1c;

    /** Ascii gs character. */
    public final static byte GS  = 0x1d;

    /** Ascii rs character. */
    public final static byte RS  = 0x1e;

    /** Ascii us character. */
    public final static byte US  = 0x1f;

    /** Ascii sp character. */
    public final static byte SP  = 0x20;

    /** Ascii del character. */
    public final static byte DEL = 0x1f;


    /** Hex characters used for formatting bytes (hack for speed) */
    protected final static char[] s_hex = {
	'0', '1', '2', '3', '4', '5', '6', '7',
	'8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /** Padding in front of byte addresses (hack for speed) */
    protected final static String[] s_pad = {
	"           ",
	"          ",
	"         ",
	"        ",
	"       ",
	"      ",
	"     ",
	"    ",
	"   ",
	"  ",
	" ",
	""
    };

    /**
     * Fills a StringBuffer with a representation of a ByteBuffer.
     *
     * @param sb a StringBuffer to append to.  If this is a null a new
     *           StringBuffer will be made.
     * @param bb the ByteBufer to dump.
     * @return the StringBuffer that was appended to.
     */
    public static StringBuffer dump(StringBuffer sb, ByteBuffer bb) {
	int len = bb.remaining();
	byte[] bytes = new byte[len];
	bb.mark();
	bb.get(bytes);
	bb.reset();
	if (sb == null)
	    sb = new StringBuffer();

	int pos = bb.position();
	for (int i = 0; i < len; i++) {
	    if ((i & 0xf) == 0) {
		if (sb.length() != 0)
		    sb.append('\n');
		String addr = String.valueOf(i + pos);
		int addrLen = addr.length();
		if (addrLen < s_pad.length)
		    sb.append(s_pad[addrLen]);
		sb.append(addr).append(':');
	    }
	    sb.append(' ');
	    byte b = bytes[i];
	    if (b >= 0 && b <= (byte)127)
		sb.append(s_str[(int)b]);
	    else
		sb.append('x').
		    append(s_hex[(b >> 4) & 0xf]).
		    append(s_hex[b & 0xf]);
	}

	return sb;
    }

    /**
     * Generates a String representation of a ByteBuffer.
     *
     * @param bb the ByteBufer to dump.
     * @return the String representation of the ByteBuffer.
     */
    public static String dump(ByteBuffer bb) {
	return dump(null, bb).toString();
    }

    /**
     * Generates a String representation of a byte.
     *
     * @param b the byte to dump.
     * @return the String representation of the byte.
     */
    public static String dump(byte b) {
	if (b >= 0 && b <= (byte)127)
	    return s_str[(int)b].trim();
	return "x" + s_hex[(b >> 4) & 0xf] + s_hex[b & 0xf];
    }
}

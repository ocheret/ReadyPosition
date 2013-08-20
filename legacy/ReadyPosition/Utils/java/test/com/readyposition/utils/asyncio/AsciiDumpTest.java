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
import junit.framework.TestCase;

public class AsciiDumpTest extends TestCase {
    private final static String s_expected =
	"          0:" +
	" nul soh stx etx eot enq ack bel  bs  ht  nl  vt  np  cr  so  si\n" +
	"         16:" +
	" dle dc1 dc2 dc3 dc4 nak syn etb can  em sub esc  fs  gs  rs  us\n" +
	"         32:" +
	"  sp   !   \"   #   $   %   &   '   (   )   *   +   ,   -   .   /\n" +
	"         48:" +
	"   0   1   2   3   4   5   6   7   8   9   :   ;   <   =   >   ?\n" +
	"         64:" +
	"   @   A   B   C   D   E   F   G   H   I   J   K   L   M   N   O\n" +
	"         80:" +
	"   P   Q   R   S   T   U   V   W   X   Y   Z   [   \\   ]   ^   _\n" +
	"         96:" +
	"   `   a   b   c   d   e   f   g   h   i   j   k   l   m   n   o\n" +
	"        112:" +
	"   p   q   r   s   t   u   v   w   x   y   z   {   |   }   ~ del\n" +
	"        128:" +
	" x80 x81 x82 x83 x84 x85 x86 x87 x88 x89 x8A x8B x8C x8D x8E x8F\n" +
	"        144:" +
	" x90 x91 x92 x93 x94 x95 x96 x97 x98 x99 x9A x9B x9C x9D x9E x9F\n" +
	"        160:" +
	" xA0 xA1 xA2 xA3 xA4 xA5 xA6 xA7 xA8 xA9 xAA xAB xAC xAD xAE xAF\n" +
	"        176:" +
	" xB0 xB1 xB2 xB3 xB4 xB5 xB6 xB7 xB8 xB9 xBA xBB xBC xBD xBE xBF\n" +
	"        192:" +
	" xC0 xC1 xC2 xC3 xC4 xC5 xC6 xC7 xC8 xC9 xCA xCB xCC xCD xCE xCF\n" +
	"        208:" +
	" xD0 xD1 xD2 xD3 xD4 xD5 xD6 xD7 xD8 xD9 xDA xDB xDC xDD xDE xDF\n" +
	"        224:" +
	" xE0 xE1 xE2 xE3 xE4 xE5 xE6 xE7 xE8 xE9 xEA xEB xEC xED xEE xEF\n" +
	"        240:" +
	" xF0 xF1 xF2 xF3 xF4 xF5 xF6 xF7 xF8 xF9 xFA xFB xFC xFD xFE xFF";

    public static void testAsciiDump() {
	ByteBuffer bb = ByteBuffer.allocate(256);
	for (int i = 0; i < 256; i++) {
	    bb.put((byte)i);
	}
	bb.flip();
	String result = Ascii.dump(bb);
	assertEquals("Ascii.dump produced unexpected result",
	  s_expected, result);
	System.out.println("Ascii.dump(bb) successfully produced\n" + result);
    }
}

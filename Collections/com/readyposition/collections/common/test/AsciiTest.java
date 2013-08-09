package com.readyposition.collections.common.test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.readyposition.collections.common.Ascii;

public class AsciiTest
{
    private final static String s_expectedDump =
    "   0    0: 00 01 02 03 04 05 06 07 | nul soh stx etx eot enq ack bel \n" +
    "   8    8: 08 09 0A 0B 0C 0D 0E 0F |  bs  ht  nl  vt  np  cr  so  si \n" +
    "  16   10: 10 11 12 13 14 15 16 17 | dle dc1 dc2 dc3 dc4 nak syn etb \n" +
    "  24   18: 18 19 1A 1B 1C 1D 1E 1F | can  em sub esc  fs  gs  rs  us \n" +
    "  32   20: 20 21 22 23 24 25 26 27 |  sp   !   \"   #   $   %   &   ' \n" +
    "  40   28: 28 29 2A 2B 2C 2D 2E 2F |   (   )   *   +   ,   -   .   / \n" +
    "  48   30: 30 31 32 33 34 35 36 37 |   0   1   2   3   4   5   6   7 \n" +
    "  56   38: 38 39 3A 3B 3C 3D 3E 3F |   8   9   :   ;   <   =   >   ? \n" +
    "  64   40: 40 41 42 43 44 45 46 47 |   @   A   B   C   D   E   F   G \n" +
    "  72   48: 48 49 4A 4B 4C 4D 4E 4F |   H   I   J   K   L   M   N   O \n" +
    "  80   50: 50 51 52 53 54 55 56 57 |   P   Q   R   S   T   U   V   W \n" +
    "  88   58: 58 59 5A 5B 5C 5D 5E 5F |   X   Y   Z   [   \\   ]   ^   _ \n" +
    "  96   60: 60 61 62 63 64 65 66 67 |   `   a   b   c   d   e   f   g \n" +
    " 104   68: 68 69 6A 6B 6C 6D 6E 6F |   h   i   j   k   l   m   n   o \n" +
    " 112   70: 70 71 72 73 74 75 76 77 |   p   q   r   s   t   u   v   w \n" +
    " 120   78: 78 79 7A 7B 7C 7D 7E 7F |   x   y   z   {   |   }   ~ del \n" +
    " 128   80: 80 81 82 83 84 85 86 87 | 128 129 130 131 132 133 134 135 \n" +
    " 136   88: 88 89 8A 8B 8C 8D 8E 8F | 136 137 138 139 140 141 142 143 \n" +
    " 144   90: 90 91 92 93 94 95 96 97 | 144 145 146 147 148 149 150 151 \n" +
    " 152   98: 98 99 9A 9B 9C 9D 9E 9F | 152 153 154 155 156 157 158 159 \n" +
    " 160   a0: A0 A1 A2 A3 A4 A5 A6 A7 | 160 161 162 163 164 165 166 167 \n" +
    " 168   a8: A8 A9 AA AB AC AD AE AF | 168 169 170 171 172 173 174 175 \n" +
    " 176   b0: B0 B1 B2 B3 B4 B5 B6 B7 | 176 177 178 179 180 181 182 183 \n" +
    " 184   b8: B8 B9 BA BB BC BD BE BF | 184 185 186 187 188 189 190 191 \n" +
    " 192   c0: C0 C1 C2 C3 C4 C5 C6 C7 | 192 193 194 195 196 197 198 199 \n" +
    " 200   c8: C8 C9 CA CB CC CD CE CF | 200 201 202 203 204 205 206 207 \n" +
    " 208   d0: D0 D1 D2 D3 D4 D5 D6 D7 | 208 209 210 211 212 213 214 215 \n" +
    " 216   d8: D8 D9 DA DB DC DD DE DF | 216 217 218 219 220 221 222 223 \n" +
    " 224   e0: E0 E1 E2 E3 E4 E5 E6 E7 | 224 225 226 227 228 229 230 231 \n" +
    " 232   e8: E8 E9 EA EB EC ED EE EF | 232 233 234 235 236 237 238 239 \n" +
    " 240   f0: F0 F1 F2 F3 F4 F5 F6 F7 | 240 241 242 243 244 245 246 247 \n" +
    " 248   f8: F8 F9 FA FB FC FD FE FF | 248 249 250 251 252 253 254 255 ";

    private final static String s_expectedFrags =
        "| |:|  20|  14|014|0014|20 21 22 23 24 25 26 27 " +
        "| 20 21 22 23  24 25 26 27| sp   !   \"   #   $   %   &   ' " +
        "| !\"#$%&'|";

    @Before
    public void setUp()  {
    }

    @After
    public void tearDown()  {
    }

    @Test
    public void testAsciiDump() {
        ByteBuffer bb = ByteBuffer.allocate(256);
        for (int i = 0; i < 256; i++) {
            bb.put((byte)i);
        }
        bb.flip();
        String result = Ascii.dump(bb);
        assertEquals("Ascii.dump produced unexpected result",
          s_expectedDump, result);
        // System.out.println("Ascii.dump(bb) successfully produced\n" +
        //                    result);
    }

    @Test
    public void testFragments() {
        int NUM = 20;
        byte[] bytes = new byte[20];
        for (int i = 0; i < 20; i++) {
            bytes[i] = (byte)(32 + i);
        }
        StringBuilder sb = new StringBuilder();
        Ascii.BAR.format(sb, bytes, 0, 0, 0, 0);
        Ascii.SPACE.format(sb, bytes, 0, 0, 0, 0);
        Ascii.BAR.format(sb, bytes, 0, 0, 0, 0);
        Ascii.COLON.format(sb, bytes, 0, 0, 0, 0);
        Ascii.BAR.format(sb, bytes, 0, 0, 0, 0);
        Ascii.DECIMAL_ADDR.format(sb, bytes, 20, 1, 19, 8);
        Ascii.BAR.format(sb, bytes, 0, 0, 0, 0);
        Ascii.HEX_ADDR.format(sb, bytes, 20, 1, 19, 8);
        Ascii.BAR.format(sb, bytes, 0, 0, 0, 0);
        Ascii.THREE_DIGIT_HEX_ADDR.format(sb, bytes, 20, 1, 19, 8);
        Ascii.BAR.format(sb, bytes, 0, 0, 0, 0);
        Ascii.FOUR_DIGIT_HEX_ADDR.format(sb, bytes, 20, 1, 19, 8);
        Ascii.BAR.format(sb, bytes, 0, 0, 0, 0);
        Ascii.HEX_BYTES.format(sb, bytes, 0, 0, 20, 8);
        Ascii.BAR.format(sb, bytes, 0, 0, 0, 0);
        Ascii.SPLIT_HEX_BYTES.format(sb, bytes, 0, 0, 20, 8);
        Ascii.BAR.format(sb, bytes, 0, 0, 0, 0);
        Ascii.GLYPH_BYTES.format(sb, bytes, 0, 0, 20, 8);
        Ascii.BAR.format(sb, bytes, 0, 0, 0, 0);
        Ascii.PRINT_BYTES.format(sb, bytes, 0, 0, 20, 8);
        Ascii.BAR.format(sb, bytes, 0, 0, 0, 0);
        // System.out.println("sb = '" + sb + "'");
        assertEquals("Fragment formatting produced unexpected result",
                     s_expectedFrags, sb.toString());
    }
}

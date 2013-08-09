package com.readyposition.collections.common;

import java.nio.ByteBuffer;

/** ASCII data utilities. */
public class Ascii
{
    /** ASCII character printable representations. */
    private final static String[] s_glyphs = {
        /* 0 */   "nul", "soh",  "stx", "etx",  "eot", "enq", "ack", "bel",
        /* 8 */   " bs", " ht",  " nl", " vt",  " np", " cr", " so", " si",
        /* 16 */  "dle", "dc1",  "dc2", "dc3",  "dc4", "nak", "syn", "etb",
        /* 24 */  "can", " em",  "sub", "esc",  " fs", " gs", " rs", " us",
        /* 32 */  " sp", "  !", "  \"", "  #",  "  $", "  %", "  &", "  '",
        /* 40 */  "  (", "  )",  "  *", "  +",  "  ,", "  -", "  .", "  /",
        /* 48 */  "  0", "  1",  "  2", "  3",  "  4", "  5", "  6", "  7",
        /* 56 */  "  8", "  9",  "  :", "  ;",  "  <", "  =", "  >", "  ?",
        /* 64 */  "  @", "  A",  "  B", "  C",  "  D", "  E", "  F", "  G",
        /* 72 */  "  H", "  I",  "  J", "  K",  "  L", "  M", "  N", "  O",
        /* 80 */  "  P", "  Q",  "  R", "  S",  "  T", "  U", "  V", "  W",
        /* 88 */  "  X", "  Y",  "  Z", "  [", "  \\", "  ]", "  ^", "  _",
        /* 96 */  "  `", "  a",  "  b", "  c",  "  d", "  e", "  f", "  g",
        /* 104 */ "  h", "  i",  "  j", "  k",  "  l", "  m", "  n", "  o",
        /* 112 */ "  p", "  q",  "  r", "  s",  "  t", "  u", "  v", "  w",
        /* 120 */ "  x", "  y",  "  z", "  {",  "  |", "  }", "  ~", "del"
        /* 128 */
    };

    /** NUL character. */
    public final static byte NUL = 0x00;

    /** SOH character. */
    public final static byte SOH = 0x01;

    /** STX character. */
    public final static byte STX = 0x02;

    /** ETX character. */
    public final static byte ETX = 0x03;

    /** EOT character. */
    public final static byte EOT = 0x04;

    /** ENQ character. */
    public final static byte ENQ = 0x05;

    /** ACK character. */
    public final static byte ACK = 0x06;

    /** BEL character. */
    public final static byte BEL = 0x07;

    /** BS character. */
    public final static byte BS = 0x08;

    /** HT character. */
    public final static byte HT = 0x09;

    /** NL character. */
    public final static byte NL = 0x0a;

    /** VT character. */
    public final static byte VT = 0x0b;

    /** NP character. */
    public final static byte NP = 0x0c;

    /** CR character. */
    public final static byte CR = 0x0d;

    /** SO character. */
    public final static byte SO = 0x0e;

    /** SI character. */
    public final static byte SI = 0x0f;

    /** DLE character. */
    public final static byte DLE = 0x10;

    /** DC1 character. */
    public final static byte DC1 = 0x11;

    /** DC2 character. */
    public final static byte DC2 = 0x12;

    /** DC3 character. */
    public final static byte DC3 = 0x13;

    /** DC4 character. */
    public final static byte DC4 = 0x14;

    /** NAK character. */
    public final static byte NAK = 0x15;

    /** SYN character. */
    public final static byte SYN = 0x16;

    /** ETB character. */
    public final static byte ETB = 0x17;

    /** CAN character. */
    public final static byte CAN = 0x18;

    /** EM character. */
    public final static byte EM = 0x19;

    /** SUB character. */
    public final static byte SUB = 0x1a;

    /** ESC character. */
    public final static byte ESC = 0x1b;

    /** FS character. */
    public final static byte FS = 0x1c;

    /** GS character. */
    public final static byte GS = 0x1d;

    /** RS character. */
    public final static byte RS = 0x1e;

    /** US character. */
    public final static byte US = 0x1f;

    /** SP character. */
    public final static byte SP = 0x20;

    /** DEL character. */
    public final static byte DEL = 0x1f;

    /** Hex characters used for formatting bytes. */
    private final static char[] s_hex = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /** Padding in front of byte addresses */
    private final static char[] s_pad = { ' ', ' ', ' ', ' ' };

    /** Padding in front of byte addresses */
    private final static char[] z3_pad = { '0', '0', '0' };

    /** Padding in front of byte addresses */
    private final static char[] z4_pad = { '0', '0', '0', '0' };

    /** Simple interface for a format fragment of a dumped line. */
    public interface Fragment {
        /**
         * Appends a formatted text fragment to a dumped line representation.
         *
         * @param sb the StringBuilder to append to
         * @param bytes the source of data for byte formatting fragments
         * @param index the byte past offset of the byte to start the line with
         * @param offset the offset of the area of interest within the
         *        bytes array
         * @param length the length of the area of interest in the bytes array
         * @param rowBytes the number of bytes represented per dumped row
         */
        void format(StringBuilder sb, byte[] bytes, int index, int offset,
                    int length, int rowBytes);
    }

    /** Format fragment to insert a space. */
    public static Fragment SPACE = new Fragment() {
        public void format(StringBuilder sb, byte[] bytes, int index,
                int offset, int length, int rowBytes) {
            sb.append(' ');
        }
    };

    /** Format fragment to insert a vertical bar. */
    public static Fragment BAR = new Fragment() {
        public void format(StringBuilder sb, byte[] bytes, int index,
                int offset, int length, int rowBytes) {
            sb.append('|');
        }
    };

    /** Format fragment to insert a colon. */
    public static Fragment COLON = new Fragment() {
        public void format(StringBuilder sb, byte[] bytes, int index,
                int offset, int length, int rowBytes) {
            sb.append(':');
        }
    };

    /** Format fragment to insert a decimal address. */
    public static Fragment DECIMAL_ADDR = new Fragment() {
            public void format(StringBuilder sb, byte[] bytes, int index,
                               int offset, int length, int rowBytes) {
                String addr = Integer.toString(index);
                int addrLen = addr.length();
                if (addrLen < s_pad.length) {
                    sb.append(s_pad, addrLen, s_pad.length - addrLen);
                }
                sb.append(addr);
            }
        };

    /** Format fragment to insert a hexadecimal address. */
    public static Fragment HEX_ADDR = new Fragment() {
            public void format(StringBuilder sb, byte[] bytes, int index,
                               int offset, int length, int rowBytes) {
                String addr = Integer.toHexString(index);
                int addrLen = addr.length();
                if (addrLen < s_pad.length) {
                    sb.append(s_pad, addrLen, s_pad.length - addrLen);
                }
                sb.append(addr);
            }
        };

    /** Format fragment to insert a 3 digit hexadecimal address. */
    public static Fragment THREE_DIGIT_HEX_ADDR = new Fragment() {
        public void format(StringBuilder sb, byte[] bytes, int index,
                int offset, int length, int rowBytes) {
            String addr = Integer.toHexString(index);
            int addrLen = addr.length();
            if (addrLen < z3_pad.length) {
                sb.append(z3_pad, addrLen, z3_pad.length - addrLen);
            }
            sb.append(addr);
        }
    };

    /** Format fragment to insert a 4 digit hexadecimal address. */
    public static Fragment FOUR_DIGIT_HEX_ADDR = new Fragment() {
        public void format(StringBuilder sb, byte[] bytes, int index,
                int offset, int length, int rowBytes) {
            String addr = Integer.toHexString(index);
            int addrLen = addr.length();
            if (addrLen < z4_pad.length) {
                sb.append(z4_pad, addrLen, z4_pad.length - addrLen);
            }
            sb.append(addr);
        }
    };

    /** Format fragment to insert bytes as hexadecimal. */
    public static Fragment HEX_BYTES = new Fragment() {
        public void format(StringBuilder sb, byte[] bytes, int index,
                int offset, int length, int rowBytes) {
            int lim = Math.min(length, index + rowBytes);

            // Output the bytes as 3 character hexadecimal numbers
            for (int j = index; j < lim; j++) {
                byte b = bytes[offset + j];
                sb.append(s_hex[(b >> 4) & 0xf]);
                sb.append(s_hex[b & 0xf]).append(' ');
            }

            // Pad for the last row
            for (int j = lim; j < index + rowBytes; j++) {
                sb.append("   ");
            }
        }
    };

    /** Format fragment to insert bytes as separate hexadecimals. */
    public static Fragment SPLIT_HEX_BYTES = new Fragment() {
        public void format(StringBuilder sb, byte[] bytes, int index,
                int offset, int length, int rowBytes) {
            int lim = Math.min(length, index + rowBytes);
            int split = rowBytes / 2;

            // Output the bytes as 3 character hexadecimal numbers
            for (int j = index; j < lim; j++) {
                byte b = bytes[offset + j];
                if ((j != index) && (j % split) == 0)
                    sb.append(' ');
                sb.append(' ').append(s_hex[(b >> 4) & 0xf]);
                sb.append(s_hex[b & 0xf]);
            }

            // Pad for the last row
            for (int j = lim; j < index + rowBytes; j++) {
                if ((j != index) && (j % split) == 0)
                    sb.append(' ');
                sb.append("   ");
            }
        }
    };

    /** Format fragment to insert bytes as glyphs. */
    public static Fragment GLYPH_BYTES = new Fragment() {
        public void format(StringBuilder sb, byte[] bytes, int index,
                int offset, int length, int rowBytes) {
            int lim = Math.min(length, index + rowBytes);

            // Output the bytes as 3 character hexadecimal numbers
            for (int j = index; j < lim; j++) {
                byte b = bytes[offset + j];
                if (b >= 0 && b <= (byte) 127) {
                    sb.append(s_glyphs[(int) b]);
                } else {
                    sb.append(b & 0xff);
                }
                sb.append(' ');
            }

            // Pad for the last row
            for (int j = lim; j < index + rowBytes; j++) {
                sb.append("    ");
            }
        }
    };

    /** Format fragment to insert bytes as 3 digit hexadecimal numbers.. */
    public static Fragment PRINT_BYTES = new Fragment() {
        public void format(StringBuilder sb, byte[] bytes, int index,
                int offset, int length, int rowBytes) {
            int lim = Math.min(length, index + rowBytes);

            // Output the bytes as 3 character hexadecimal numbers
            for (int j = index; j < lim; j++) {
                byte b = bytes[offset + j];
                if (isPrintable(b)) {
                    sb.append((char) b);
                } else {
                    sb.append('.');
                }
            }

            // Pad for the last row
            for (int j = lim; j < index + rowBytes; j++) {
                sb.append(' ');
            }
        }
    };

    /** A nice format for a row of dumped bytes. */
    public static Fragment[] NiceFormat = {
        DECIMAL_ADDR, SPACE, HEX_ADDR, COLON,
        SPACE, HEX_BYTES, BAR, SPACE, GLYPH_BYTES };

    /** The default per row format to use. */
    public final static Fragment[] DEFAULT_FORMAT = NiceFormat;

    /** The default number of bytes to represent per row. */
    public final static int DEFAULT_ROW_BYTES = 8;
    
    /** Returns true if the Ascii character is printable. */
    public static boolean isPrintable(byte b) {
        // Should probably use something more sophisticated like
        // http://stackoverflow.com/questions/220547/printable-char-in-java
        // but for ASCII this is ok.
        return (b >= 32 && b < 127);
    }

    /**
     * Generates a String representation of a ByteBuffer.
     * 
     * @param bb the ByteBufer to dump.
     * @return the String representation of the ByteBuffer.
     */
    public static String dump(ByteBuffer bb) {
        return dump(null, DEFAULT_FORMAT, bb, DEFAULT_ROW_BYTES).toString();
    }

    /**
     * Generates a String representation of a ByteBuffer.
     * 
     * @param bb the ByteBufer to dump.
     * @param rowBytes the number of bytes to dump per row of output.
     * @return the String representation of the ByteBuffer.
     */
    public static String dump(ByteBuffer bb, int rowBytes) {
        return dump(null, DEFAULT_FORMAT, bb, rowBytes).toString();
    }

    /**
     * Generates a String representation of a ByteBuffer.
     * 
     * @param bb the ByteBufer to dump.
     * @param frags array of Fragments to apply to each row
     * @param rowBytes the number of bytes to dump per row of output.
     * @return the String representation of the ByteBuffer.
     */
    public static String dump(ByteBuffer bb, Fragment[] frags, int rowBytes) {
        return dump(null, frags, bb, rowBytes).toString();
    }

    /**
     * Fills a StringBuilder with a representation of a ByteBuffer.
     * 
     * @param sb a StringBuilder to append to. If this is a null a new
     *    StringBuilder will be made.
     * @param frags array of Fragments to apply to each row
     * @param bb the ByteBufer to dump.
     * @param rowBytes the number of bytes to dump per row of output.
     * @return the StringBuilder that was appended to.
     */
    public static StringBuilder dump(StringBuilder sb, Fragment[] frags,
                                     ByteBuffer bb, int rowBytes)
    {
        int len = bb.remaining();
        byte[] bytes = new byte[len];
        bb.mark();
        bb.get(bytes);
        bb.reset();
        return dump(sb, frags, bytes, 0, len, rowBytes);
    }

    /**
     * Fills a StringBuilder with a representation of a ByteBuffer.
     * 
     * @param sb a StringBuilder to append to. If this is a null a new
     *    StringBuilder will be made.
     * @param bb the ByteBufer to dump.
     * @return the StringBuilder that was appended to.
     */
    public static StringBuilder dump(StringBuilder sb, ByteBuffer bb) {
        return dump(sb, DEFAULT_FORMAT, bb, DEFAULT_ROW_BYTES);
    }

    /**
     * Fills a StringBuilder with a representation of a section of a byte
     * array.
     * 
     * @param sb a StringBuilder to append to. If this is a null a new
     *            StringBuilder will be made.
     * @param bytes the byte array to dump.
     * @param rowBytes the number of bytes to dump per row of output.
     * @return the StringBuilder that was appended to.
     */
    public static StringBuilder dump(StringBuilder sb, byte[] bytes,
                                     int rowBytes)
    {
        return dump(sb, NiceFormat, bytes, 0, bytes.length, rowBytes);
    }

    /**
     * Fills a StringBuilder with a representation of a section of a
     * byte array.
     * 
     * @param sb a StringBuilder to append to. If this is a null a new
     *            StringBuilder will be made.
     * @param bytes the byte array to dump.
     * @param offset the offset of the start of data of interest within
     *            the byte array.
     * @param length the number of bytes of interest within the byte
     *            array.
     * @param rowBytes the number of bytes to dump per row of output.
     * @return the StringBuilder that was appended to.
     */
    public static StringBuilder dump(StringBuilder sb, byte[] bytes,
                                     int offset, int length, int rowBytes)
    {
        return dump(sb, NiceFormat, bytes, offset, length, rowBytes);
    }

    /**
     * Fills a StringBuilder with a representation of a section of a
     * byte array.
     * 
     * @param sb a StringBuilder to append to. If this is a null a new
     *            StringBuilder will be made.
     * @param frags an array of Fragments used to build up each line
     *            of output
     * @param bytes the byte array to dump.
     * @param offset the offset of the start of data of interest
     *            within the byte array.
     * @param length the number of bytes of interest within the byte
     *            array.
     * @param rowBytes the number of bytes to dump per row of output.
     * @return the StringBuilder that was appended to.
     */
    public static StringBuilder dump(StringBuilder sb, Fragment[] frags,
                                     byte[] bytes, int offset, int length,
                                     int rowBytes)
    {
        if (sb == null) {
            sb = new StringBuilder();
        }

        for (int i = 0; i < length; i += rowBytes) {
            // Prepare to do another row.
            if (i != 0) {
                // Separate rows with newlines.
                sb.append('\n');
            }

            for (int j = 0; j < frags.length; j++) {
                frags[j].format(sb, bytes, i, offset, length, rowBytes);
            }
        }
        return sb;
    }

    /**
     * Generates a String representation of a byte.
     * 
     * @param b the byte to dump.
     * @return the String representation of the byte.
     */
    public static String dump(byte b) {
        if (b >= 0 && b <= (byte)127) {
            return s_glyphs[(int)b].trim();
        }
        return "x" + s_hex[(b >> 4) & 0xf] + s_hex[b & 0xf];
    }
}

package com.readyposition.collections.hash;

import com.readyposition.collections.common.*;

/** This class holds commonly used hash functors. */
public class Hashers
{
    /** Hash for char */
    public final static HashChar CharHash = new HashChar() {
            public int code(char v) {
                return HashUtils.supp(HashUtils.hash(v));
            }
        };

    /** Hash for byte */
    public final static HashByte ByteHash = new HashByte() {
            public int code(byte v) {
                return HashUtils.supp(HashUtils.hash(v));
            }
        };

    /** Hash for short */
    public final static HashShort ShortHash = new HashShort() {
            public int code(short v) {
                return HashUtils.supp(HashUtils.hash(v));
            }
        };

    /** Hash for int */
    public final static HashInt IntHash = new HashInt() {
            public int code(int v) {
                return HashUtils.supp(HashUtils.hash(v));
            }
        };

    /** Hash for long */
    public final static HashLong LongHash = new HashLong() {
            public int code(long v) {
                return HashUtils.supp(HashUtils.hash(v));
            }
        };

    /** Hash for float */
    public final static HashFloat FloatHash = new HashFloat() {
            public int code(float v) {
                return HashUtils.supp(HashUtils.hash(v));
            }
        };

    /** Hash for double */
    public final static HashDouble DoubleHash = new HashDouble() {
            public int code(double v) {
                return HashUtils.supp(HashUtils.hash(v));
            }
        };

    /** Hash for Objects */
    public final static HashObject ObjectHash = new HashObject() {
        public int code(Object v) {
            return HashUtils.supp(HashUtils.hash(v));
        }
    };

    /** Hash for Generics */
    public final static HashG<Object> GenericObjectHash = new HashG<Object>() {
        public int code(Object v) {
            return HashUtils.supp(HashUtils.hash(v));
        }
    };

    /** Stub required to allow templates to compile. */
    public final static Hash_KeyName_ _KeyName_Hash = new Hash_KeyName_() {
            public int code(_KeyType_ v)
            {
                return HashUtils.supp(HashUtils.hash(v));
            }
        };
}

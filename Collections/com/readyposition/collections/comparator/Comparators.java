package com.readyposition.collections.comparator;

import com.readyposition.collections.common.*;

/**
 * This class holds commonly used comparators that developers
 * shouldn't need to write again and again.
 */
public class Comparators {
    /** Comparator for char in ascending order. */
    public final static ComparatorChar CharAsc = new ComparatorChar() {
        public boolean equals(char a, char b) {
            return (a == b);
        }

        public int compare(char a, char b) {
            return (a == b) ? 0 : ((a > b) ? 1 : -1);
        }
    };

    /** Comparator for char in descending order. */
    public final static ComparatorChar CharDesc = new ComparatorChar() {
        public boolean equals(char a, char b) {
            return (a == b);
        }

        public int compare(char a, char b) {
            return (a == b) ? 0 : ((a < b) ? 1 : -1);
        }
    };

    /** Comparator for byte in ascending order. */
    public final static ComparatorByte ByteAsc = new ComparatorByte() {
        public boolean equals(byte a, byte b) {
            return (a == b);
        }

        public int compare(byte a, byte b) {
            return (a == b) ? 0 : ((a > b) ? 1 : -1);
        }
    };

    /** Comparator for byte in descending order. */
    public final static ComparatorByte ByteDesc = new ComparatorByte() {
        public boolean equals(byte a, byte b) {
            return (a == b);
        }

        public int compare(byte a, byte b) {
            return (a == b) ? 0 : ((a < b) ? 1 : -1);
        }
    };

    /** Comparator for short in ascending order. */
    public final static ComparatorShort ShortAsc = new ComparatorShort() {
        public boolean equals(short a, short b) {
            return (a == b);
        }

        public int compare(short a, short b) {
            return (a == b) ? 0 : ((a > b) ? 1 : -1);
        }
    };

    /** Comparator for short in descending order. */
    public final static ComparatorShort ShortDesc = new ComparatorShort() {
        public boolean equals(short a, short b) {
            return (a == b);
        }

        public int compare(short a, short b) {
            return (a == b) ? 0 : ((a < b) ? 1 : -1);
        }
    };

    /** Comparator for int in ascending order. */
    public final static ComparatorInt IntAsc = new ComparatorInt() {
        public boolean equals(int a, int b) {
            return (a == b);
        }

        public int compare(int a, int b) {
            return (a == b) ? 0 : ((a > b) ? 1 : -1);
        }
    };

    /** Comparator for int in descending order. */
    public final static ComparatorInt IntDesc = new ComparatorInt() {
        public boolean equals(int a, int b) {
            return (a == b);
        }

        public int compare(int a, int b) {
            return (a == b) ? 0 : ((a < b) ? 1 : -1);
        }
    };

    /** Comparator for long in ascending order. */
    public final static ComparatorLong LongAsc = new ComparatorLong() {
        public boolean equals(long a, long b) {
            return (a == b);
        }

        public int compare(long a, long b) {
            return (a == b) ? 0 : ((a > b) ? 1 : -1);
        }
    };

    /** Comparator for long in descending order. */
    public final static ComparatorLong LongDesc = new ComparatorLong() {
        public boolean equals(long a, long b) {
            return (a == b);
        }

        public int compare(long a, long b) {
            return (a == b) ? 0 : ((a < b) ? 1 : -1);
        }
    };

    /** Comparator for float in ascending order. */
    public final static ComparatorFloat FloatAsc = new ComparatorFloat() {
        public boolean equals(float a, float b) {
            return (a == b);
        }

        public int compare(float a, float b) {
            return (a == b) ? 0 : ((a > b) ? 1 : -1);
        }
    };

    /** Comparator for float in descending order. */
    public final static ComparatorFloat FloatDesc = new ComparatorFloat() {
        public boolean equals(float a, float b) {
            return (a == b);
        }

        public int compare(float a, float b) {
            return (a == b) ? 0 : ((a < b) ? 1 : -1);
        }
    };

    /** Comparator for double in ascending order. */
    public final static ComparatorDouble DoubleAsc = new ComparatorDouble() {
        public boolean equals(double a, double b) {
            return (a == b);
        }

        public int compare(double a, double b) {
            return (a == b) ? 0 : ((a > b) ? 1 : -1);
        }
    };

    /** Comparator for double in descending order. */
    public final static ComparatorDouble DoubleDesc = new ComparatorDouble() {
        public boolean equals(double a, double b) {
            return (a == b);
        }

        public int compare(double a, double b) {
            return (a == b) ? 0 : ((a < b) ? 1 : -1);
        }
    };

    /** Identity equalizer for Objects. */
    public final static EqG<Object> ObjectIdentityEq = new EqG<Object>() {
        public boolean equals(Object a, Object b) {
            return (a == b);
        }
    };

    /** Equivalence Equalizer for Objects. */
    public final static EqG<Object> ObjectEq = new EqG<Object>() {
        public boolean equals(Object a, Object b) {
            return (a.equals(b));
        }
    };

    /** Comparator for Comparables in ascending order. */
    public final static ComparatorG<Object> ObjectAsc = new ComparatorG<Object>() {
        @SuppressWarnings("unchecked")
        public int compare(Object a, Object b) {
            return ((Comparable) a).compareTo(b);
        }

        public boolean equals(Object a, Object b) {
            return (a.equals(b));
        }
    };

    /** Comparator for Comparables in ascending order. */
    public final static ComparatorG<Object> ObjectDesc = new ComparatorG<Object>() {
        @SuppressWarnings("unchecked")
        public int compare(Object a, Object b) {
            return ((Comparable) b).compareTo(a);
        }

        public boolean equals(Object a, Object b) {
            return (a.equals(b));
        }
    };

    /** Stub comparator to allow templates to work. */
    public final static Comparator_KeyName_ _KeyName_Asc =
        new Comparator_KeyName_() {
            public boolean equals(_KeyType_ a, _KeyType_ b) {
                return (a.equals(b));
            }

            public int compare(_KeyType_ a, _KeyType_ b) {
                return a.compareTo(b);
            }
        };

    /** Stub comparator to allow templates to work. */
    public final static Comparator_KeyName_ _KeyName_Desc =
        new Comparator_KeyName_() {
            public boolean equals(_KeyType_ a, _KeyType_ b) {
                return (a == b);
            }

            public int compare(_KeyType_ a, _KeyType_ b) {
                return b.compareTo(a);
            }
        };

    /** Stub comparator to allow templates to work. */
    public final static Comparator_ValueName_ _ValueName_Asc =
        new Comparator_ValueName_() {
            public boolean equals(_ValueType_ a, _ValueType_ b) {
                return (a.equals(b));
            }

            public int compare(_ValueType_ a, _ValueType_ b) {
                return a.compareTo(b);
            }
        };

    /** Stub comparator to allow templates to work. */
    public final static Comparator_ValueName_ _ValueName_Desc =
        new Comparator_ValueName_() {
            public boolean equals(_ValueType_ a, _ValueType_ b) {
                return (a == b);
            }

            public int compare(_ValueType_ a, _ValueType_ b) {
                return b.compareTo(a);
            }
        };

    /**
     * Compares two values.
     * 
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(char a, char b) {
        return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     * 
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(byte a, byte b) {
        return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     * 
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(short a, short b) {
        return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     * 
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(int a, int b) {
        return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     * 
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(long a, long b) {
        return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     * 
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(float a, float b) {
        return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     * 
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    public static int compare(double a, double b) {
        return (a == b) ? 0 : ((a > b) ? 1 : -1);
    }

    /**
     * Compares two values.
     * 
     * @param a the first value to compare.
     * @param b the second value to compare.
     * @return -1 if a < b, 0 if a == b, 1 if a > b
     */
    @SuppressWarnings("unchecked")
    public static int compare(Comparable a, Comparable b) {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return a.compareTo(b);
    }
}

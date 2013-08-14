package com.readyposition.collections.common;

import java.util.EnumSet;

import com.readyposition.collections.hash.HashMapG;

/**
 * Provides per type information for the types used throughout the
 * ReadyPosition collection classes. Much of the code in the
 * ReadyPosition collections is template generated. However, some
 * functionality that is unique to each type cannot rely on the very
 * simple templating scheme in use and needs to be hand coded for each
 * type. By placing that functionality in this class, the templates
 * can call the type-specific functions found here.
 */
public enum Types {
    // name, code, size, type
    UNDEFINED("undefined", 0, 0, null),
    BOOLEAN("boolean", 1, 1, Boolean.TYPE),
    BYTE("byte", 2, 1, Byte.TYPE),
    CHAR("char", 3, 2, Character.TYPE),
    SHORT("short", 4, 2, Short.TYPE),
    INT("int", 5, 4, Integer.TYPE),
    LONG("long", 6, 8, Long.TYPE),
    FLOAT("float", 7, 4, Float.TYPE),
    DOUBLE("double", 8, 8, Double.TYPE),
    OBJECT("Object", 9, 0, null);

    /** An array of Types whose indices match the Types' code. */
    private static final Types[] s_types = {
        UNDEFINED, BOOLEAN, BYTE, CHAR,
        SHORT, INT, LONG, FLOAT, DOUBLE, OBJECT
    };

    /** Maps type names to Types objects. */
    private static final MapG<String, Types> m_map =
        new HashMapG<String, Types>();

    /** Value representing null for classes that need to mask null. */
    protected static final _KeyType_ NULL_KEY = new _KeyType_();

    // Populate a hash table to map type names back to the Types enum
    static {
        for (Types t : EnumSet.allOf(Types.class)) {
            m_map.put(t.getName(), t);
        }
    }

    /** Java name for this type (as opposed to the enum). */
    private String m_name;

    /** Unique code for this type. */
    private int m_code;

    /** The number of bytes for an item of this type. */
    private int m_size;

    /** The Java class representing this type. */
    private Class<?> m_type;

    /**
     * Constructor for enumeration instances.
     * 
     * @param name type name
     * @param code integer code for this type
     * @param size size of instances of this type
     * @param type Class corresponding to this type
     */
    private Types(String name, int code, int size, Class<?> type) {
        m_name = name;
        m_code = code;
        m_size = size;
        m_type = type;
    }

    /** Gets the name of this instance. */
    public String getName() {
        return m_name;
    }

    /** Gets the integer code for this instance. */
    public int getCode() {
        return m_code;
    }

    /** Gets the size of instances of this type. */
    public int getSize() {
        return m_size;
    }

    /** Gets the Java Class corresponding to this type. */
    public Class<?> getType() {
        return m_type;
    }

    /** Retrieves the Types instance given the type code. */
    public static Types fromCode(int code) {
        return s_types[code];
    }

    /** Retrieves the Types instance given the type name. */
    public static Types fromName(String name) {
        return (Types)m_map.get(name);
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static boolean maskNull(boolean key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static boolean unmaskNull(boolean key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static byte maskNull(byte key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static byte unmaskNull(byte key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static char maskNull(char key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static char unmaskNull(char key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static short maskNull(short key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static short unmaskNull(short key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static int maskNull(int key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static int unmaskNull(int key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static long maskNull(long key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static long unmaskNull(long key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static float maskNull(float key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static float unmaskNull(float key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static double maskNull(double key) {
        return key;
    }

    /**
     * Passthru method makes writing templates easier. See
     * maskNull(Object key) for the interesting case.
     */
    public static double unmaskNull(double key) {
        return key;
    }

    /** Returns internal representation for key. Use NULL_KEY if null key. */
    public static Object maskNull(Object key) {
        return (key == null ? NULL_KEY : key);
    }

    /** Returns key represented by specified internal representation. */
    public static Object unmaskNull(Object key) {
        return (key == (Object)NULL_KEY ? null : key);
    }

    /** Stub to faciliate use of our templating mechanism. */
    public static _KeyType_ maskNull(_KeyType_ key) {
        return (key == null ? NULL_KEY : key);
    }

    /** Stub to faciliate use of our templating mechanism. */
    public static _KeyType_ unmaskNull(_KeyType_ key) {
        return (key == NULL_KEY ? null : key);
    }

    /** Value generator for template based unit tests. */
    public static boolean getBooleanFor(int value) {
        return (value & 1) != 0;
    }

    /** Value generator for template based unit tests. */
    public static char getCharFor(int value) {
        return (char) (0xffff & value);
    }

    /** Value generator for template based unit tests. */
    public static byte getByteFor(int value) {
        return (byte) (0xff & value);
    }

    /** Value generator for template based unit tests. */
    public static short getShortFor(int value) {
        return (short) (0xffffffff & value);
    }

    /** Value generator for template based unit tests. */
    public static int getIntFor(int value) {
        return value;
    }

    /** Value generator for template based unit tests. */
    public static long getLongFor(int value) {
        return (long) value;
    }

    /** Value generator for template based unit tests. */
    public static float getFloatFor(int value) {
        return (float) value;
    }

    /** Value generator for template based unit tests. */
    public static double getDoubleFor(int value) {
        return (double) value;
    }

    /** Value generator for template based unit tests. */
    public static Object getObjectFor(int value) {
        return (value == 0) ? null : (new Integer(value));
    }

    /** Stub to support templating mechanism. */
    public static _ValueType_ get_ValueName_For(int value) {
        return new _ValueType_(value);
    }

    /** Stub to support templating mechanism. */
    public static _KeyType_ get_KeyName_For(int value) {
        return new _KeyType_(value);
    }

    /** Reasonable number of values to use to test this type. */
    public static int testSizeChar() {
        return 1000;
    }

    /** Reasonable number of values to use to test this type. */
    public static int testSizeByte() {
        return 100;
    }

    /** Reasonable number of values to use to test this type. */
    public static int testSizeShort() {
        return 1000;
    }

    /** Reasonable number of values to use to test this type. */
    public static int testSizeInt() {
        return 1000;
    }

    /** Reasonable number of values to use to test this type. */
    public static int testSizeLong() {
        return 10000;
    }

    /** Reasonable number of values to use to test this type. */
    public static int testSizeFloat() {
        return 1000;
    }

    /** Reasonable number of values to use to test this type. */
    public static int testSizeDouble() {
        return 10000;
    }

    /** Reasonable number of values to use to test this type. */
    public static int testSizeObject() {
        return 1000;
    }

    /** Reasonable number of values to use to test this type. */
    public static int testSize_KeyName_() {
        return 4;
    }

    /** Reasonable number of values to use to test this type. */
    public static int testSize_ValueName_() {
        return 4;
    }
}

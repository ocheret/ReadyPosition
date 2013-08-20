/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils;

import com.readyposition.utils.hash.ObjectObjectHash;

/**
 * A class that provides information about base Java types.  A base
 * type is any primitive type and Object.  In particular, it provides:
 * name, size, class, and a unique integer code.  There is also an
 * undefined type.
 */
public final class Types
{
    /** Type code for the undefined type. */
    public static final int UNDEFINED_CODE = 0;

    /** Type code for the boolean type. */
    public static final int BOOLEAN_CODE = 1;

    /** Type code for the byte type. */
    public static final int BYTE_CODE = 2;

    /** Type code for the char type. */
    public static final int CHAR_CODE = 3;

    /** Type code for the short type. */
    public static final int SHORT_CODE = 4;

    /** Type code for the int type. */
    public static final int INT_CODE = 5;

    /** Type code for the long type. */
    public static final int LONG_CODE = 6;

    /** Type code for the float type. */
    public static final int FLOAT_CODE = 7;

    /** Type code for the double type. */
    public static final int DOUBLE_CODE = 8;

    /** Type code for Object types. */
    public static final int OBJECT_CODE = 9;

    /** Types object describing the boolean type. */
    public static final Types UNDEFINED =
	new Types("undefined", 0, null, UNDEFINED_CODE);

    /** Size in bytes of a boolean. */
    public static final int BOOLEAN_SIZE = 1;

    /** Types object describing the boolean type. */
    public static final Types BOOLEAN =
	new Types("boolean", BOOLEAN_SIZE, Boolean.TYPE, BOOLEAN_CODE);

    /** Size in bytes of a byte. */
    public static final int BYTE_SIZE = 1;

    /** Types object describing the byte type. */
    public static final Types BYTE =
	new Types("byte", BYTE_SIZE, Byte.TYPE, BYTE_CODE);

    /** Size in bytes of a char. */
    public static final int CHAR_SIZE = 2;

    /** Types object describing the char type. */
    public static final Types CHAR =
	new Types("char", CHAR_SIZE, Character.TYPE, CHAR_CODE);

    /** Size in bytes of a short. */
    public static final int SHORT_SIZE = 2;

    /** Types object describing the short type. */
    public static final Types SHORT =
	new Types("short", SHORT_SIZE, Short.TYPE, SHORT_CODE);

    /** Size in bytes of an int. */
    public static final int INT_SIZE = 4;

    /** Types object describing the int type. */
    public static final Types INT =
	new Types("int", INT_SIZE, Integer.TYPE, INT_CODE);

    /** Size in bytes of a long. */
    public static final int LONG_SIZE = 8;

    /** Types object describing the long type. */
    public static final Types LONG =
	new Types("long", LONG_SIZE, Long.TYPE, LONG_CODE);

    /** Size in bytes of a float. */
    public static final int FLOAT_SIZE = 4;

    /** Types object describing the float type. */
    public static final Types FLOAT =
	new Types("float", FLOAT_SIZE, Float.TYPE, FLOAT_CODE);

    /** Size in bytes of a double. */
    public static final int DOUBLE_SIZE = 8;

    /** Types object describing the double type. */
    public static final Types DOUBLE =
	new Types("double", DOUBLE_SIZE, Double.TYPE, DOUBLE_CODE);

    /** Types object describing an Object type. */
    public static final Types OBJECT =
	new Types("Object", 0, null, OBJECT_CODE);

    /** Maps primitive type names to Types objects. */
    private static ObjectObjectHash m_map = new ObjectObjectHash();

    /**
     * Array mapping type code to type.  Note, type codes much match
     * indices in this array.
     */
    private static final Types[] s_types =
    {
	UNDEFINED,	// 0
	BOOLEAN,	// 1
	BYTE,		// 2
	CHAR,		// 3
	SHORT,		// 4
	INT,		// 5
	LONG,		// 6
	FLOAT,		// 7
	DOUBLE,		// 8
	OBJECT		// 9
    };

    /**
     * Constructor for a Types object.
     *
     * @param name the name of the primitive type.
     * @param size the size in bytes of the primitive type.
     * @param type the Class corresponding to the primitive type.
     */
    private Types(String name, int size, Class type, int code)
    {
	m_name = name;
	m_size = size;
	m_type = type;
	m_code = code;
	m_map.put(name, this);
    }

    /** The name of the primitive type. */
    private String m_name;

    /** Gets the name of the primitive type. */
    public String name()
    {
	return m_name;
    }

    /** The size in bytes of the primitive type. */
    private int m_size;

    /** Gets the size in bytes of the primitive type. */
    public int size() {
	return m_size;
    }

    /** The Class of the primitive type. */
    private Class m_type;

    /** Gets the Class of the primitive type. */
    public Class type() {
	return m_type;
    }

    /** The type code for this type. */
    private int m_code;

    /** Gets the type code for this type. */
    public int code() {
	return m_code;
    }

    /**
     * Retrieves a Types object with a specified name.
     *
     * @param name the name of the type to look up.
     * @return the Types object corresponding to the name or null if not found.
     */
    public static Types fromName(String name)
    {
	return (Types)m_map.get(name);
    }

    /**
     * Retrieves a Types object with a specified code.
     *
     * @param code the code of the type to look up.
     * @return the Types object corresponding to the name.
     * @exception ArrayIndexOutOfBoundsException if code is unknown.
     */
    public static Types fromCode(int code)
    {
	return s_types[code];
    }

    public String toString()
    {
	return "Type name=" + m_name +
	    ", size=" + m_size +
	    ", class=" + m_type;
    }
}

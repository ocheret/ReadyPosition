/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

// XXX - change this to use the new templating system instead of using all of
// these inner classes

/**
 * XXX - javadoc
 */
abstract public class FragArray
{
    /** XXX - javadoc */
    FragArray(int length, int leafSize, int nodeSize) {
	// XXX - check args
	m_length = length;
	m_leafSize = leafSize;
	m_nodeSize = nodeSize;

	m_levels = computeLevels(length);
	computeStrides();

	m_root = makeNode(m_levels, m_length);
    }

    /** XXX - javadoc */
    public int getLength() { return m_length; }

    /** XXX - javadoc */
    public void setLength(int length) {
	if (length == m_length)
	    // Nothing to do
	    return;

	int newLevels = computeLevels(length);

	// Decrease the number of levels if necessary
	while (newLevels < m_levels) {
	    m_root = ((Object[])m_root)[0];
	    m_levels--;
	}

	// Increase the number of levels if necessary
	while (newLevels > m_levels) {
	    Object node[] = new Object[m_nodeSize];
	    node[0] = m_root;
	    m_root = node;
	    m_levels++;
	}

	m_levels = newLevels;

	computeStrides();

	if (length < m_length) {
	    // We're shrinking
	    trimNode(m_root, m_levels, length);
	} else {
	    // We're growing
	    padNode(m_root, m_levels, length);
	}
	m_length = length;
    }

    /** XXX - javadoc */
    public void ensureLength(int length) {
	if (length > m_length)
	    setLength(length);
    }

    /** XXX - javadoc */
    void dumpNode(Object o, int start, int level) {
	if (level == 0) {
	    for (int i = 0; i < m_leafSize; i++) {
		if (i + start >= m_length)
		    break;
		for (int j = 0; j < m_levels; j++)
		    System.out.print("\t");
		System.out.println(i + start);
	    }
	    return;
	}

	Object node[] = (Object[])o;
	level--;
	for (int i = 0; i < m_nodeSize; i++) {
	    if (node[i] == null)
		return;
	    for (int j = 0; j < (m_levels - level - 1); j++)
		System.out.print("\t");
	    System.out.println(start);
	    dumpNode(node[i], start, level);
	    start += m_strides[level];
	}
    }

    /** XXX - javadoc */
    void dumpTree() {
	System.out.println("levels = " + m_levels);
	dumpNode(m_root, 0, m_levels);
    }

    /** XXX - javadoc */
    public int getLeafSize() { return m_leafSize; }

    /** XXX - javadoc */
    public int getNodeSize() { return m_nodeSize; }

    /** XXX - javadoc */
    public int getLevels() { return m_levels; }

    /** XXX - javadoc */
    int computeLevels(int length) {
	// Compute the number of leaf nodes required
	int leafCount = (length + m_leafSize - 1) / m_leafSize;

	// Compute the number of levels needed
	int q = leafCount - 1;
	int levels = 0;
	while (q > 0) {
	    q /= m_nodeSize;
	    levels++;
	}

	return levels;
    }

    /** XXX - javadoc */
    void computeStrides() {
	// Compute the increments for each of the levels
	if (m_levels > 0) {
	    m_strides = new int[m_levels];
	    m_strides[0] = m_leafSize;
	    for (int i = 1; i < m_levels; i++)
		m_strides[i] = m_strides[i - 1] * m_nodeSize;
	} else
	    m_strides = null;
    }

    /** XXX - javadoc */
    Object makeNode(int level, int length) {
	if (0 == level)
	    return makeLeaf();

	level--;
	Object node[] = new Object[m_nodeSize];
	for (int i = 0; i < m_nodeSize && length > 0; i++) {
	    node[i] = makeNode(level, length);
	    length -= m_strides[level];
	}
	return node;
    }

    /** XXX - javadoc */
    void trimNode(Object node, int level, int length) {
	if (0 == level)
	    return;

	Object n[] = (Object[])node;
	level--;
	for (int i = 0; i < m_nodeSize; i++) {
	    if (length > 0)
		trimNode(n[i], level, length);
	    else
		n[i] = null;
	    length -= m_strides[level];
	}
    }

    /** XXX - javadoc */
    void padNode(Object node, int level, int length) {
	if (0 == level)
	    return;

	Object n[] = (Object[])node;
	level--;
	for (int i = 0; i < m_nodeSize && length > 0; i++) {
	    if (null != n[i])
		padNode(n[i], level, length);
	    else
		n[i] = makeNode(level, length);
	    length -= m_strides[level];
	}
    }

    /** XXX - javadoc */
    Object findLeaf(int index) {
	Object o = m_root;
	for (int i = m_levels - 1; i >= 0; i--) {
	    int offset = index / m_strides[i];
	    o = ((Object[])o)[offset];
	    index -= offset * m_strides[i];
	}
	return o;
    }

    /** XXX - javadoc */
    protected abstract Object makeLeaf();

    // XXX - still need copy, fill in subclasses

    /** XXX - javadoc */
    int m_leafSize;

    /** XXX - javadoc */
    int m_nodeSize;

    /** XXX - javadoc */
    int m_length;

    /** XXX - javadoc */
    int m_levels;

    /** XXX - javadoc */
    int m_strides[];

    /** XXX - javadoc */
    Object m_root;

    /** XXX - javadoc */
    public static class Boolean extends FragArray {
	public Boolean(int length, int leafSize, int nodeSize) {
	    super(length, leafSize, nodeSize);
	}

	protected Object makeLeaf() {
	    return new boolean[m_leafSize];
	}

	public void set(int index, boolean value) {
	    boolean leaf[] = (boolean[])this.findLeaf(index);
	    index %= m_leafSize;
	    leaf[index] = value;
	}

	public boolean get(int index) {
	    boolean leaf[] = (boolean[])this.findLeaf(index);
	    index %= m_leafSize;
	    return leaf[index];
	}
    }

    /** XXX - javadoc */
    public static class Char extends FragArray {
	public Char(int length, int leafSize, int nodeSize) {
	    super(length, leafSize, nodeSize);
	}

	protected Object makeLeaf() {
	    return new char[m_leafSize];
	}

	public void set(int index, char value) {
	    char leaf[] = (char[])this.findLeaf(index);
	    index %= m_leafSize;
	    leaf[index] = value;
	}

	public char get(int index) {
	    char leaf[] = (char[])this.findLeaf(index);
	    index %= m_leafSize;
	    return leaf[index];
	}
    }

    /** XXX - javadoc */
    public static class Byte extends FragArray {
	public Byte(int length, int leafSize, int nodeSize) {
	    super(length, leafSize, nodeSize);
	}

	protected Object makeLeaf() {
	    return new byte[m_leafSize];
	}

	public void set(int index, byte value) {
	    byte leaf[] = (byte[])this.findLeaf(index);
	    index %= m_leafSize;
	    leaf[index] = value;
	}

	public byte get(int index) {
	    byte leaf[] = (byte[])this.findLeaf(index);
	    index %= m_leafSize;
	    return leaf[index];
	}
    }

    /** XXX - javadoc */
    public static class Short extends FragArray {
	public Short(int length, int leafSize, int nodeSize) {
	    super(length, leafSize, nodeSize);
	}

	protected Object makeLeaf() {
	    return new short[m_leafSize];
	}

	public void set(int index, short value) {
	    short leaf[] = (short[])this.findLeaf(index);
	    index %= m_leafSize;
	    leaf[index] = value;
	}

	public short get(int index) {
	    short leaf[] = (short[])this.findLeaf(index);
	    index %= m_leafSize;
	    return leaf[index];
	}
    }

    /** XXX - javadoc */
    public static class Integer extends FragArray {
	public Integer(int length, int leafSize, int nodeSize) {
	    super(length, leafSize, nodeSize);
	}

	protected Object makeLeaf() {
	    return new int[m_leafSize];
	}

	public void set(int index, int value) {
	    int leaf[] = (int[])this.findLeaf(index);
	    index %= m_leafSize;
	    leaf[index] = value;
	}

	public int get(int index) {
	    int leaf[] = (int[])this.findLeaf(index);
	    index %= m_leafSize;
	    return leaf[index];
	}
    }

    /** XXX - javadoc */
    public static class Long extends FragArray {
	public Long(int length, int leafSize, int nodeSize) {
	    super(length, leafSize, nodeSize);
	}

	protected Object makeLeaf() {
	    return new long[m_leafSize];
	}

	public void set(int index, long value) {
	    long leaf[] = (long[])this.findLeaf(index);
	    index %= m_leafSize;
	    leaf[index] = value;
	}

	public long get(int index) {
	    long leaf[] = (long[])this.findLeaf(index);
	    index %= m_leafSize;
	    return leaf[index];
	}
    }

    /** XXX - javadoc */
    public static class Float extends FragArray {
	public Float(int length, int leafSize, int nodeSize) {
	    super(length, leafSize, nodeSize);
	}

	protected Object makeLeaf() {
	    return new float[m_leafSize];
	}

	public void set(int index, float value) {
	    float leaf[] = (float[])this.findLeaf(index);
	    index %= m_leafSize;
	    leaf[index] = value;
	}

	public float get(int index) {
	    float leaf[] = (float[])this.findLeaf(index);
	    index %= m_leafSize;
	    return leaf[index];
	}
    }

    /** XXX - javadoc */
    public static class Double extends FragArray {
	public Double(int length, int leafSize, int nodeSize) {
	    super(length, leafSize, nodeSize);
	}

	protected Object makeLeaf() {
	    return new double[m_leafSize];
	}

	public void set(int index, double value) {
	    double leaf[] = (double[])this.findLeaf(index);
	    index %= m_leafSize;
	    leaf[index] = value;
	}

	public double get(int index) {
	    double leaf[] = (double[])this.findLeaf(index);
	    index %= m_leafSize;
	    return leaf[index];
	}
    }

    /** XXX - javadoc */
    public static class Obj extends FragArray {
	public Obj(int length, int leafSize, int nodeSize) {
	    super(length, leafSize, nodeSize);
	}

	protected Object makeLeaf() {
	    return new Object[m_leafSize];
	}

	public void set(int index, Object value) {
	    Object leaf[] = (Object[])this.findLeaf(index);
	    index %= m_leafSize;
	    leaf[index] = value;
	}

	public Object get(int index) {
	    Object leaf[] = (Object[])this.findLeaf(index);
	    index %= m_leafSize;
	    return leaf[index];
	}
    }

    /** XXX - javadoc */
    public static void main(String args[]) {
	try {
	    InputStreamReader isr = new InputStreamReader(System.in);
	    BufferedReader reader = new BufferedReader(isr);
	    String line;
	    FragArray.Integer fai = new FragArray.Integer(1, 2, 3);
	    for (System.out.print(">");
		 (line = reader.readLine()) != null;
		 System.out.print(">"))
	    {
		StringTokenizer tok = new StringTokenizer(line, " \t\n\r\f>");
		int count = tok.countTokens();
		if (0 == count)
		    continue;

		String command = tok.nextToken();
		if (command.equals("new")) {
		    if (4 != count) { usage(); continue; }
		    int length = java.lang.Integer.parseInt(tok.nextToken());
		    int leafSize = java.lang.Integer.parseInt(tok.nextToken());
		    int nodeSize = java.lang.Integer.parseInt(tok.nextToken());
		    fai = new FragArray.Integer(length, leafSize, nodeSize);
		    System.out.println("levels = " + fai.getLevels());
		} else if (command.equals("setLength")) {
		    if (2 != count) { usage(); continue; }
		    int length = java.lang.Integer.parseInt(tok.nextToken());
		    fai.setLength(length);
		    System.out.println("levels = " + fai.getLevels());
		} else if (command.equals("get")) {
		    switch (count) {
		    case 2:
			int index =
			    java.lang.Integer.parseInt(tok.nextToken());
			System.out.println(fai.get(index));
			break;
		    case 3:
			int start =
			    java.lang.Integer.parseInt(tok.nextToken());
			int end = java.lang.Integer.parseInt(tok.nextToken());
			for (int i = start; i <= end; i++)
			    System.out.println(i + ". " + fai.get(i));
			break;
		    default:
			usage();
		    }
		} else if (command.equals("set")) {
		    if (3 != count) { usage(); continue; }
		    int index = java.lang.Integer.parseInt(tok.nextToken());
		    int value = java.lang.Integer.parseInt(tok.nextToken());
		    fai.set(index, value);
		} else if (command.equals("dumpTree")) {
		    if (1 != count) { usage(); continue; }
		    fai.dumpTree();
		} else if (command.equals("quit")) {
		    if (1 != count) { usage(); continue; }
		    System.exit(0);
		} else
		    usage();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /** XXX - javadoc */
    static void usage() {
	System.out.println("usage:");
	System.out.println("  new length leafSize nodeSize");
	System.out.println("  setLength length");
	System.out.println("  get index [index]");
	System.out.println("  set index value");
	System.out.println("  dumpTree");
	System.out.println("  quit - quit program");
    }
}

/*
 * Copyright 2003 Charles A. Ocheret.  All Rights Reserved
 *
 * THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION AND
 * REMAINS THE UNPUBLISHED PROPERTY OF CHARLES A. OCHERET.
 * USE, DISCLOSURE, OR REPRODUCTION IS PROHIBITED EXCEPT AS PERMITTED
 * BY EXPRESS WRITTEN LICENSE AGREEMENT WITH CHARLES A. OCHERET.
 */

package com.readyposition.utils;

import com.readyposition.utils.hash.ObjectIntsHash;
import com.readyposition.utils.matrix.IntMatrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * A class for representing a web of interdependencies
 */
public class DeepEnd implements Serializable {
    /**
     * Constructor
     */
    public DeepEnd()
    {
	m_items = new ObjectIntsHash(2);
	m_dependencies = new IntMatrix(DEPEND_INIT_ROWS, DEPEND_COLUMNS);
    }

    /**
     * Records a dependency between objects.
     *
     * @param supplier the object which will be depended on by the consumer
     * @param consumer the object which depends on the supplier
     */
    public synchronized void addDependency(Object supplier, Object consumer)
    {
	int supplierIndex = internItem(supplier);
	int consumerIndex = internItem(consumer);

	// Obtain an unused row in the dependencies table
	int row; 
	if (-1 != m_firstFree) {
	    // There are entries on the free list to use
	    row = m_firstFree;
	    m_firstFree = m_dependencies.get(row, 0);
	} else {
	    // Nothing on the free list - grab a new row
	    if (m_size >= m_dependencies.getRows())
		m_dependencies.resize(m_dependencies.getRows() * 2,
		  m_dependencies.getCols());
	    row = m_size;
	}

	// Make the new row point to the consumer and supplier
	m_dependencies.set(row, C_INDEX, consumerIndex);
	m_dependencies.set(row, S_INDEX, supplierIndex);

	// Insert the row in the linked list of suppliers for this consumer
	insertRow(row, consumerIndex, I_SUPPLIER_HEAD, S_NEXT, S_PREV);

	// Insert the row in the linked list fo consumers for this supplier
	insertRow(row, supplierIndex, I_CONSUMER_HEAD, C_NEXT, C_PREV);

	m_size++;
    }

    /**
     * Removes a dependency between objects.
     *
     * @param supplier the object which was depended on by the consumer
     * @param consumer the object which depended on the supplier
     */
    public synchronized void removeDependency(
	Object supplier, Object consumer)
    {
	int supplierIndex = m_items.getEntry(supplier);
	int consumerIndex = m_items.getEntry(consumer);

	if (supplierIndex == -1 || consumerIndex == -1)
	    // At least one of the items is not in the table.  So
	    // there can't be any dependency to remove.
	    return;

	// Get the head of the linked list of consumers for the supplier
	int consumerHead = m_items.getEntryValue(supplierIndex,
	  I_CONSUMER_HEAD);

	// Get the head of the linked list of suppliers for the consumer
	int supplierHead = m_items.getEntryValue(consumerIndex,
	  I_SUPPLIER_HEAD);

	if (consumerHead == -1 || supplierHead == -1)
	    // Either the consumer has no suppliers or the supplier
	    // has no consumers.  In either case, there is no
	    // dependency that can be removed.
	    return;

	// We'll traverse the list of suppliers for the consumer to
	// look for the specified supplier.
	int row = supplierHead;
	do {
	    if (m_dependencies.get(row, S_INDEX) == supplierIndex) {
		// We have found a row which holds a dependency between
		// our consumer and supplier
		//
		// Remove the row from the list of suppliers
		removeRow(row, consumerIndex, I_SUPPLIER_HEAD, S_NEXT, S_PREV);

		// Remove the row from the list for consumers
		removeRow(row, supplierIndex, I_CONSUMER_HEAD, C_NEXT, C_PREV);

		// Place the row on the free list
		m_dependencies.set(row, 0, m_firstFree);
		m_firstFree = row;
		m_size--;
		return;
	    }
	    row = m_dependencies.get(row, S_NEXT);
	} while (row != supplierHead);

	// If we reach here, then we never found a dependency in the
	// table for the specified consumer and supplier
    }

    /**
     * Determines all of the direct suppliers for a consumer
     *
     * @param consumer the object for which we want to determine all direct
     *                 suppliers
     * @return a ArrayList containing all of the direct supplier
     *         objects for the specified consumer.
     */
    public synchronized ArrayList getSuppliers(Object consumer)
    {
	return getList(consumer, I_SUPPLIER_HEAD, S_INDEX, S_NEXT);
    }

    /**
     * Determines all of the direct consumers for a supplier
     *
     * @param supplier the object for which we want to determine all direct
     *                 consumers.
     * @return a ArrayList containing all of the direct consumer objects for
     *         the specified supplier.
     */
    public synchronized ArrayList getConsumers(Object supplier)
    {
	return getList(supplier, I_CONSUMER_HEAD, C_INDEX, C_NEXT);
    }

    /**
     * Each Object in the dependency graph gets a row on this table.
     * Each row consists of the Object and the heads of the linked lists
     * of consumers and suppliers for the object.  The list heads are
     * integer indices into the rows of the dependencies table.
     */
    private ObjectIntsHash m_items;

    /**
     * The integer column in m_items containing the head of the supplier list
     */
    private final static int I_SUPPLIER_HEAD = 0;

    /**
     * The integer column in m_items containing the head of the consumer list
     */
    private final static int I_CONSUMER_HEAD = 1;

    /**
     * Each consumer/supplier pair gets a row on this table.  Each row
     * of this table consists of 6 columns.  The columns are
     * 0 - Consumer Index is the index of the consumer in the m_items table
     * 1 - Consumer Next is the next row in the table for this row's supplier
     * 2 - Consumer Prev is the prev row in the table for this row's supplier
     * 3 - Supplier Index is the index of the supplier in the m_items table
     * 4 - Supplier Next is the next row in the table for this row's consumer
     * 5 - Supplier Prev is the prev row in the table for this row's consumer
     */
    private IntMatrix m_dependencies;

    /** Initial number of rows in association matrix and user data array. */
    protected final static int DEPEND_INIT_ROWS = 64;

    /** Number of columns in dense association matrix. */
    protected final static int DEPEND_COLUMNS = 6;

    /**
     * The m_dependencies column holding the index of the consumer in m_items
     */
    private final static int C_INDEX = 0;

    /**
     * The m_dependencies column with the next consumer row
     */
    private final static int C_NEXT = 1;

    /**
     * The m_dependencies column with the previous consumer row
     */
    private final static int C_PREV = 2;

    /**
     * The m_dependencies column holding the index of the supplier in m_items
     */
    private final static int S_INDEX = 3;

    /**
     * The m_dependencies column with the next supplier row
     */
    private final static int S_NEXT = 4;

    /**
     * The m_dependencies column with the previous supplier row
     */
    private final static int S_PREV = 5;

    /**
     * The total number of dependencies in the graph
     */
    private int m_size = 0;

    /**
     * The first free row in the m_dependencies table
     */
    private int m_firstFree = -1;

    /**
     * Inserts an m_dependencies row on one of the linked lists.  The
     * parameters if the row will be put onto the consumers or
     * suppliers list.
     *
     * @param newRow the index of the new row in m_dependencies
     * @param itemRow the row of the item in m_items
     * @param headCol the column of the list head in m_items
     * @param nextCol the column of the next link in m_dependencies
     * @param prevCol the column of the prev link in m_dependencies
     */
    void insertRow(
	int newRow, int itemRow, int headCol, int nextCol, int prevCol)
    {
	// Get the index of the head of the list for the item
	int headRow = m_items.getEntryValue(itemRow, headCol);
	if (headRow == -1) {
	    // No items are on this list yet
	    //
	    // Make the new row the head of the list for the item
	    m_items.putEntryValue(itemRow, headCol, newRow);

	    // Make the new row point to itself since it is the only
	    // item on the list
	    m_dependencies.set(newRow, nextCol, newRow);
	    m_dependencies.set(newRow, prevCol, newRow);
	    return;
	}

	// There are already items on this list so we must insert the
	// new row into the list.
	// int next = m_dependencies.get(headRow, nextCol);
	// m_dependencies.set(headRow, nextCol, newRow);
	// m_dependencies.set(next, prevCol, newRow);
	// m_dependencies.set(newRow, nextCol, next);
	// m_dependencies.set(newRow, prevCol, headRow);

	int prev = m_dependencies.get(headRow, prevCol);
	m_dependencies.set(headRow, prevCol, newRow);
	m_dependencies.set(prev, nextCol, newRow);
	m_dependencies.set(newRow, nextCol, headRow);
	m_dependencies.set(newRow, prevCol, prev);
    }

    /**
     * Interns an item in the graph.  If the item is already in the table
     * then the old reference is used.  Otherwise a new entry is made.
     *
     * @param obj the item to intern in the table
     * @return the row on which the interned item resides
     */
    int internItem(Object obj)
    {
	int row = m_items.getEntry(obj);
	if (-1 == row) {
	    // This is a new entry
	    int entry = m_items.put(obj, I_CONSUMER_HEAD, -1);
	    m_items.putEntryValue(entry, I_SUPPLIER_HEAD, -1);
	}
	return row;
    }

    /**
     * Removes an m_dependencies row from one of the linked lists.  The
     * parameters if the row will be removed from the consumers or
     * suppliers list.
     *
     * @param row the index of the row to be removed from m_dependencies
     * @param itemRow the row of the item in m_items
     * @param headCol the column of the list head in m_items
     * @param nextCol the column of the next link in m_dependencies
     * @param prevCol the column of the prev link in m_dependencies
     */
    void removeRow(int row, int itemRow, int headCol, int nextCol, int prevCol)
    {
	int next = m_dependencies.get(row, nextCol);
	if (next == row) {
	    // This is the only row on this list so we can mark the
	    // list as empty
	    m_items.putEntryValue(itemRow, headCol, -1);
	    return;
	}

	// Make the next and previous rows point to each other
	int prev = m_dependencies.get(row, prevCol);
	m_dependencies.set(prev, nextCol, next);
	m_dependencies.set(next, prevCol, prev);

	if (row == m_items.getEntryValue(itemRow, headCol)) {
	    // We're removing the head of the list so we'll change the
	    // head of the list to the next row
	    m_items.putEntryValue(itemRow, headCol, next);
	}
    }

    /**
     * Determines either all of the direct consumers or suppliers for an item
     * based on the arguments passed in.
     *
     * @param item the item whose consumers or suppliers are being searched
     * @param headCol the column in m_items holding the head of the
     *		list of interest
     * @param indexCol the column in m_dependencies which refers to items
     *		in the list of interest
     * @param nextCol the column in m_dependencies which specifies the next
     *		row in the list of interest
     * @return a ArrayList containing the items in the list
     */
    ArrayList getList(
	Object item, int headCol, int indexCol, int nextCol)
    {
	int itemRow = m_items.getEntry(item);
	if (-1 == itemRow)
	    // This item isn't even in the table
	    return null;

	// Get the head of the list
	int head = m_items.getEntryValue(itemRow, headCol);
	if (-1 == head)
	    // No list for this item
	    return null;

	ArrayList v = new ArrayList();
	int row = head;
	do {
	    int itemIndex = m_dependencies.get(row, indexCol);
	    v.add(m_items.getKey(itemIndex));;
	    row = m_dependencies.get(row, nextCol);
	} while (row != head);
	return v;
    }

    /**
     * Sample main program used as a unit test. XXX - replace with a
     * proper JUnit test.
     *
     * @param args command linen arguments - ignored
     */
    public static void main(String args[])
    {
	DeepEnd graph = new DeepEnd();
	InputStreamReader isr = new InputStreamReader(System.in);
	BufferedReader reader = new BufferedReader(isr);
	String line;
	try {
	    for (System.out.print(">");
		 (line = reader.readLine()) != null;
		 System.out.print(">"))
	    {
		StringTokenizer tok = new StringTokenizer(line, " \t\n\r\f>");
		String command;

		switch (tok.countTokens()) {
		case 0:
		    break;
		case 1:
		    command = tok.nextToken();
		    if (command.equals("q"))
			System.exit(0);
		    usage();
		    break;
		case 2:
		    command = tok.nextToken();
		    String item = tok.nextToken();
		    ArrayList list;
		    if (command.equals("c")) {
			printList(graph.getConsumers(item));
		    } else if (command.equals("s")) {
			printList(graph.getSuppliers(item));
		    } else
			usage();
		    break;
		case 3:
		    command = tok.nextToken();
		    String supplier = tok.nextToken();
		    String consumer = tok.nextToken();
		    if (command.equals("a")) {
			graph.addDependency(supplier, consumer);
		    } else if (command.equals("r")) {
			graph.removeDependency(supplier, consumer);
		    } else
			usage();
		    break;
		default:
		    usage();
		    break;
		}
	    }
	} catch (IOException ioe) {
	    System.out.println(ioe);
	}
	System.exit(0);
    }

    /**
     * Displays a arrayList of Strings
     *
     * @param v arrayList containing Strings or a null pointer
     */
    static void printList(ArrayList v)
    {
	if (null == v) {
	    System.out.println("null");
	    return;
	}

	int size = v.size();
	for (int i = 0; i < size; i++) {
	    if (0 != i)
		System.out.print(" ");
	    System.out.print(v.get(i));
	}
	System.out.println();
    }

    /**
     * Displays a usage message for the unittest interactive loop
     */
    static void usage()
    {
	System.out.println("usage:");
	System.out.println("  a supplier consumer - adds dependency");
	System.out.println("  r supplier consumer - removes dependency");
	System.out.println("  c supplier - list of consumers");
	System.out.println("  s consumer - list of suppliers");
	System.out.println("  q - quit program");
    }
}

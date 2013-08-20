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
import java.lang.reflect.Array;

/**
 * A class for representing many-to-many relationships.
 */
public class Many2Many
{
    // No constructor needed.

    /** Gets the total number of associations in the graph. */
    public int getSize() { return m_size; }

    /**
     * Records an association between objects.
     *
     * @param left the object which will be associated with the right object.
     * @param right the object which will be associated with the left object.
     * @return the entry number of this association.
     */
    public int associate(Object left, Object right) {
	return associate(left, right, null);
    }

    /**
     * Records an association between objects and some user data.
     *
     * @param left the object which will be associated with the right object.
     * @param right the object which will be associated with the left object.
     * @param userData user data to be kept with this association.
     * @return the entry number of this association.
     */
    public int associate(Object left, Object right, Object userData) {
	int leftIndex = internItem(m_left, left);
	int rightIndex = internItem(m_right, right);

	// Obtain an unused row in the associations table
	int row;
	if (-1 != m_firstFree) {
	    // There are entries on the free list to use
	    row = m_firstFree;
	    m_firstFree = m_associations.get(row, 0);
	    m_freeCount--;
	} else {
	    // Nothing on the free list - grab a new row
	    if (m_size >= m_associations.getRows()) {
		m_associations.resize(m_associations.getRows() * 2,
		  m_associations.getCols());
		Object[] newUserData =
		    new Object[m_associations.getRows() * 2];
		System.arraycopy(m_userData, 0, newUserData, 0,
		  m_userData.length);
		m_userData = newUserData;
	    }
	    row = m_size;
	}

	// Make the new row point to the left and right
	m_associations.set(row, L_INDEX, leftIndex);
	m_associations.set(row, R_INDEX, rightIndex);
	m_userData[row] = userData;

	// Insert the row in the linked list of rights for this left
	insertRow(m_left, leftIndex, row, R_NEXT, R_PREV);

	// Insert the row in the linked list fo lefts for this right
	insertRow(m_right, rightIndex, row, L_NEXT, L_PREV);

	m_size++;

	return row;
    }

    /**
     * Removes an association between objects.
     *
     * @param entry the entry number of the association between a left and
     *  right object (as returned by associate).
     */
    public void dissociate(int entry) {
	// Remove the row from the list of rights.
	removeRow(m_left, entry, m_associations.get(entry, L_INDEX),
	  R_NEXT, R_PREV);

	// Remove the row from the list for lefts
	removeRow(m_right, entry, m_associations.get(entry, R_INDEX),
	  L_NEXT, L_PREV);

	// Place the row on the free list
	m_associations.set(entry, 0, m_firstFree);
	m_userData[entry] = null;
	m_firstFree = entry;
	m_freeCount++;
	m_size--;
    }

    /**
     * Removes an association between objects.
     *
     * @param left the object which was associated with the right object.
     * @param right the object which was associated with the left object.
     */
    public void dissociate(Object left, Object right) {
	int entry = getEntry(left, right);
	if (entry != -1) {
	    dissociate(entry);
	}
    }

    /**
     * Determines if an association exists between two objects.
     *
     * @param left the object which may be associated with the right object.
     * @param right the object which may be associated with the left object.
     * @return true if there is an association, false otherwise.
     */
    public boolean isAssociated(Object left, Object right) {
	return (getEntry(left, right) != -1);
    }

    /**
     * Find the entry that records the association between objects.
     *
     * @param left the object which may be associated with the right object.
     * @param right the object which may be associated with the left object.
     * @return the entry number of the association or -1 if there is no
     *  association.
     */
    public int getEntry(Object left, Object right) {
	int leftIndex = m_left.getEntry(left);
	int rightIndex = m_right.getEntry(right);

	if (rightIndex == -1 || leftIndex == -1) {
	    // At least one of the items is not in the table.  So
	    // there can't be any association.
	    return -1;
	}

	// Get the head of the linked list of lefts for the right
	int leftHead = m_right.getEntryValue(rightIndex, I_ROW);

	// Get the head of the linked list of rights for the left
	int rightHead = m_left.getEntryValue(leftIndex, I_ROW);

	if (leftHead == -1 || rightHead == -1) {
	    // XXX - can this happen?
	    //
	    // Either the left has no rights or the right has no
	    // lefts.  In either case, there is no dependency that can
	    // be removed.
	    return -1;
	}

	// Get the sizes of the linked lists for each side.
	int leftCount = m_right.getEntryValue(rightIndex, I_COUNT);
	int rightCount = m_left.getEntryValue(leftIndex, I_COUNT);

	// We'll traverse the smaller of the lists (left or right).
	int head;
	int targetIndex;
	int indexCol;
	int nextCol;
	if (leftCount > rightCount) {
	    head = rightHead;
	    targetIndex = rightIndex;
	    indexCol = R_INDEX;
	    nextCol = R_NEXT;
	} else {
	    head = leftHead;
	    targetIndex = leftIndex;
	    indexCol = L_INDEX;
	    nextCol = L_NEXT;
	}

	// Traverse a list look for the associated value on the other side.
	int row = head;
	do {
	    if (m_associations.get(row, indexCol) == targetIndex) {
		// We have found a row which holds a dependency between
		// our left and right.
		return row;
	    }
	    row = m_associations.get(row, nextCol);
	} while (row != head);

	// If we reach here, then we never found a dependency in the
	// table for the specified left and right.
	return -1;
    }

    /**
     * Determines all of the right entries for a left.
     *
     * @param left the object for which we want to determine the right entries.
     * @param dst the array into which the entries are to be stored,
     *	if it is big enough; otherwise, a new array is allocated for
     *	this purpose.  If dst is null, then an array of the
     *	appropriate size is returned.
     * @return an array containing all entries associating the specified left
     *  with a right.
     */
    public int[] getRightEntries(Object left, int[] dst) {
	return getList(m_left, left, R_NEXT, dst);
    }

    /**
     * Determines all of the right entries for a left.
     *
     * @param left the object for which we want to determine the right entries.
     * @return an array containing all entries associating the specified left
     *  with a right.
     */
    public int[] getRightEntries(Object left) {
	return getList(m_left, left, R_NEXT, null);
    }

    /**
     * Determines all of the rights for a left.
     *
     * @param left the object for which we want to determine all rights.
     * @param dst the array into which the rights are to be stored, if
     *	it is big enough; otherwise, a new array of the same runtime
     *	type is allocated for this purpose.  If the array is longer than needed
     *  then the extra elements at the end of the array will be initialized
     *  to null.  If dst is null, then an Object[] of the appropriate size
     *  is returned.
     * @return an array containing the rights for a left.
     */
    public Object[] getRights(Object left, Object[] dst) {
	return getList(m_left, m_right, left, R_INDEX, R_NEXT, dst);
    }

    /**
     * Determines all of the rights for a left.
     *
     * @param left the object for which we want to determine all rights.
     * @return an Object array containing the rights for a left.
     */
    public Object[] getRights(Object left) {
	return getList(m_left, m_right, left, R_INDEX, R_NEXT, null);
    }

    /**
     * Determines all of the left entries for a right.
     *
     * @param right the object for which we want to determine the left entries.
     * @param dst the array into which the entries are to be stored,
     *	if it is big enough; otherwise, a new array is allocated for
     *	this purpose.  If the array is longer than needed
     *  then the extra elements at the end of the array will be initialized
     *  to null.  If dst is null, then an Object[] of the appropriate size
     *  is returned.
     * @return an array containing all entries associating the specified right
     *  with a left.
     */
    public int[] getLeftEntries(Object right, int[] dst) {
	return getList(m_right, right, L_NEXT, dst);
    }

    /**
     * Determines all of the left entries for a right.
     *
     * @param right the object for which we want to determine the left entries.
     * @return an array containing all entries associating the specified right
     *  with a left.
     */
    public int[] getLeftEntries(Object right) {
	return getList(m_right, right, L_NEXT, null);
    }

    /**
     * Determines all of the lefts for a right.
     *
     * @param right the object for which we want to determine all lefts.
     * @param dst the array into which the lefts are to be stored, if
     *	it is big enough; otherwise, a new array of the same runtime
     *	type is allocated for this purpose.  If the array is longer than needed
     *  then the extra elements at the end of the array will be initialized
     *  to null.  If dst is null, then an Object[] of the appropriate size
     *  is returned.
     * @return an array containing the lefts for a left.
     */
    public Object[] getLefts(Object right, Object[] dst) {
	return getList(m_right, m_left, right, L_INDEX, L_NEXT, dst);
    }

    /**
     * Determines all of the lefts for a right.
     *
     * @param right the object for which we want to determine all lefts.
     * @return an array containing the lefts for a left.
     */
    public Object[] getLefts(Object right) {
	return getList(m_right, m_left, right, L_INDEX, L_NEXT, null);
    }

    /** Gets the count of left objects with associations in the table. */
    public int getLeftCount() {
	return m_left.getSize();
    }

    /** Gets the count of right objects with associations in the table. */
    public int getRightCount() {
	return m_right.getSize();
    }

    /**
     * Gets all of the lefts that have any associations in the table.
     *
     * @param dst the array into which the lefts are to be stored, if
     *	it is big enough; otherwise, a new array of the same runtime
     *	type is allocated for this purpose.  If the array is longer than needed
     *  then the extra elements at the end of the array will be initialized
     *  to null.  If dst is null, then an Object[] of the appropriate size
     *  is returned.
     * @return an array containing the lefts.
     */
    public Object[] getAllLefts(Object[] dst) {
	return m_left.getKeys(dst);
    }

    /**
     * Gets all of the lefts that have any associations in the table.
     *
     * @return an array containing the lefts.
     */
    public Object[] getAllLefts() {
	return m_left.getKeys(null);
    }

    /**
     * Gets all of the rights that have any associations in the table.
     *
     * @param dst the array into which the rights are to be stored, if
     *	it is big enough; otherwise, a new array of the same runtime
     *	type is allocated for this purpose.  If the array is longer than needed
     *  then the extra elements at the end of the array will be initialized
     *  to null.  If dst is null, then an Object[] of the appropriate size
     *  is returned.
     * @return an array containing the rights.
     */
    public Object[] getAllRights(Object[] dst) {
	return m_right.getKeys(dst);
    }

    /**
     * Gets all of the rights that have any associations in the table.
     *
     * @return an array containing the rights.
     */
    public Object[] getAllRights() {
	return m_right.getKeys(null);
    }

    /**
     * Gets the number of rights associated with a left.
     *
     * @param left the left object whose right count we're looking for.
     * @return the number of right objects associated with the left.
     */
    public int getRightCount(Object left) {
	return getCount(m_left, left);
    }

    /**
     * Gets the number of lefts associated with a right.
     *
     * @param right the right object whose left count we're looking for.
     * @return the number of left objects associated with the right.
     */
    public int getLeftCount(Object right) {
	return getCount(m_right, right);
    }
    /**
     * Each left object in the graph gets a row on this table.  Each
     * row consists of the Object, the head of the linked list of
     * associated right objects, and a count of the associated right
     * objects.  The list heads are integer indices into the rows of
     * the associations table.
     */
    protected ObjectIntsHash m_left = new ObjectIntsHash(2);

    /**
     * Each right object in the graph gets a row on this table.  Each
     * row consists of the Object, the head of the linked list of
     * associated left objects, and a count of the associated left
     * objects.  The list heads are integer indices into the rows of
     * the associations table.
     */
    protected ObjectIntsHash m_right = new ObjectIntsHash(2);

    /** Initial number of rows in association matrix and user data array. */
    protected final static int ASSOC_INIT_ROWS = 64;

    /** Number of columns in dense association matrix. */
    protected final static int ASSOC_COLUMNS = 6;

    /**
     * Each left/right pair gets a row on this table.  Each row
     * of this table consists of 6 columns.  The columns are
     * 0 - Left Index is the index of the left in the m_left table
     * 1 - Left Next is the next row in the table for this row's right
     * 2 - Left Prev is the prev row in the table for this row's right
     * 3 - Right Index is the index of the right in the m_right table
     * 4 - Right Next is the next row in the table for this row's left
     * 5 - Right Prev is the prev row in the table for this row's left
     */
    protected IntMatrix m_associations =
	new IntMatrix(ASSOC_INIT_ROWS, ASSOC_COLUMNS);

    /** An array of user data that is parallel to m_associations. */
    protected Object[] m_userData = new Object[ASSOC_INIT_ROWS];

    /** The m_associations column holding the index of the left in m_left. */
    protected final static int L_INDEX = 0;

    /** The m_associations column with the next left row. */
    protected final static int L_NEXT = 1;

    /** The m_associations column with the previous left row. */
    protected final static int L_PREV = 2;

    /** The m_associations column holding the index of the right in m_right. */
    protected final static int R_INDEX = 3;

    /** The m_associations column with the next right row. */
    protected final static int R_NEXT = 4;

    /** The m_associations column with the previous right row. */
    protected final static int R_PREV = 5;

    /** The ObjectIntsHash column for an item's head row number. */
    protected final static int I_ROW = 0;

    /** The ObjectIntsHash column for an item's association count. */
    protected final static int I_COUNT = 1;

    /** The total number of associations in the graph. */
    protected int m_size = 0;

    /** The first free row in the m_associations table. */
    protected int m_firstFree = -1;

    /** Count of the number of elements on the free list. */
    protected int m_freeCount = 0;

    /**
     * Gets the number of objects associated with another object.
     *
     * @param items the table containing the object and its count.
     * @param obj the object whose count we're looking for.
     * @return the number of left objects associated with the right.
     */
    protected int getCount(ObjectIntsHash items, Object obj) {
	int entry = items.getEntry(obj);
	return (entry == -1) ? 0 : items.getEntryValue(entry, I_COUNT);
    }

    /**
     * Retrieves the left object of an association.
     *
     * @param entry the entry number of the association as returned by
     *  associate().
     * @return the left object of the association.
     */
    public Object getLeft(int entry) {
	return m_left.getKey(m_associations.get(entry, L_INDEX));
    }

    /**
     * Retrieves the right object of an association.
     *
     * @param entry the entry number of the association as returned by
     *  associate().
     * @return the right object of the association.
     */
    public Object getRight(int entry) {
	return m_right.getKey(m_associations.get(entry, R_INDEX));
    }

    /**
     * Retrieves the user data of an association of a left and right.
     *
     * @param left the left object of the association.
     * @param right the right object of the association.
     * @return the right object of the association.
     * @exception ArrayIndexOutOfBoundsException if there is no association
     *  between the left and right objects.
     */
    public Object getUserData(Object left, Object right) {
	int entry = getEntry(left, right);
	return m_userData[entry];
    }

    /**
     * Retrieves the user data of an association.
     *
     * @param entry the entry number of the association as returned by
     *  associate().
     * @return the right object of the association.
     */
    public Object getUserData(int entry) {
	return m_userData[entry];
    }

    /**
     * Interns an item into a table (either the left or right as
     * specified).  If the item is already in the table then the old
     * reference is used.  Otherwise a new entry is made.
     *
     * @param items the table (m_left or m_right) to intern into..
     * @param obj the item to intern in the table.
     * @return the row on which the interned item resides.
     */
    protected int internItem(ObjectIntsHash items, Object obj) {
	int entry = items.getEntry(obj);
	if (-1 == entry) {
	    // This is a new entry
	    entry = items.put(obj, I_ROW, -1);
	    items.putEntryValue(entry, I_COUNT, 0);
	}
	return entry;
    }

    /**
     * Cleans out the end of a destination array.
     *
     * @param dst the destination array.
     * @param count the number of valid entries in the array.  Elements
     *  beyond valid entries will be set to null.
     * @return if dst is null, then a new Object[0] is returned otherwise
     * the passed in destination array is returned.
     */
    protected Object[] cleanDst(Object[] dst, int count) {
	if (dst == null) {
	    dst = new Object[0];
	} else {
	    for (int i = dst.length - 1; i >= count; i--) {
		dst[i] = null;
	    }
	}
	return dst;
    }

    /**
     * Cleans out the end of a destination array.
     *
     * @param dst the destination array.
     * @param count the number of valid entries in the array.  Elements
     *  beyond valid entries will be set to -1.
     * @return if dst is null, then a new int[0] is returned otherwise
     * the passed in destination array is returned.
     */
    protected int[] cleanDst(int[] dst, int count) {
	if (dst == null) {
	    dst = new int[0];
	} else {
	    for (int i = dst.length - 1; i >= count; i--) {
		dst[i] = -1;
	    }
	}
	return dst;
    }

    /**
     * Determines either all of the direct lefts or rights for an item
     * based on the arguments passed in.
     *
     * @param xitems either m_left or m_right - item is one of these.
     * @param yitems if xitems is m_left then this is m_right and vice versa -
     *		     this contains the items we are looking for.
     * @param item the item whose lefts or rights are being searched
     * @param indexCol the column in m_associations which refers to items
     *		in the list of interest.
     * @param nextCol the column in m_associations which specifies the next
     *		row in the list of interest.
     * @return an ArrrayList containing the items in the list.
     */
    protected Object[] getList(ObjectIntsHash xitems, ObjectIntsHash yitems,
      Object item, int indexCol, int nextCol, Object[] dst)
    {
	int entry = xitems.getEntry(item);
	if (-1 == entry) {
	    // This item isn't even in the table
	    // XXX - the return type should match dst!
	    return cleanDst(dst, 0);
	}

	// Get the head of the list
	int head = xitems.getEntryValue(entry, I_ROW);
	if (-1 == head) {
	    // No list for this item
	    // XXX - the return type should match dst!
	    return cleanDst(dst, 0);
	}

	int count = xitems.getEntryValue(entry, I_COUNT);
	if (dst == null) {
	    dst = new Object[count];
	} else if (dst.length < count) {
	    dst = (Object[])Array.newInstance(
		dst.getClass().getComponentType(), count);
	}

	int row = head;
	for (int i = 0; i < count; i++) {
	    int itemIndex = m_associations.get(row, indexCol);
	    dst[i] = yitems.getKey(itemIndex);
	    row = m_associations.get(row, nextCol);
	}
	return cleanDst(dst, count);
    }

    /**
     * Determines either all of the direct lefts or rights for an item
     * based on the arguments passed in.
     *
     * @param xitems either m_left or m_right - item is one of these.
     * @param item the item whose lefts or rights are being searched
     * @param nextCol the column in m_associations which specifies the next
     *		row in the list of interest.
     * @return an array containing all entries associating the specified item
     *  with a counterpart.
     */
    protected int[] getList(ObjectIntsHash xitems, Object item,
      int nextCol, int[] dst)
    {
	int entry = xitems.getEntry(item);
	if (-1 == entry) {
	    // This item isn't even in the table
	    return cleanDst(dst, 0);
	}

	// Get the head of the list
	int head = xitems.getEntryValue(entry, I_ROW);
	if (-1 == head) {
	    // No list for this item
	    return cleanDst(dst, 0);
	}

	int count = xitems.getEntryValue(entry, I_COUNT);
	if (dst == null || dst.length < count) {
	    dst = new int[count];
	}

	int row = head;
	for (int i = 0; i < count; i++) {
	    dst[i] = row;
	    row = m_associations.get(row, nextCol);
	}
	return cleanDst(dst, count);
    }

    /**
     * Inserts an m_associations row on one of the linked lists.  The
     * parameters inticate if the row will be put onto the lefts or
     * rights list.
     *
     * @param items either m_left or m_right
     * @param itemRow the row of the item in items
     * @param newRow the index of the new row in m_associations
     * @param nextCol the column of the next link in m_associations
     * @param prevCol the column of the prev link in m_associations
     */
    protected void insertRow(ObjectIntsHash items, int itemRow,
      int newRow, int nextCol, int prevCol)
    {
	// Increment the reference count for this item
	items.putEntryValue(itemRow, I_COUNT,
	  items.getEntryValue(itemRow, I_COUNT) + 1);

	// Get the index of the head of the list for the item
	int headRow = items.getEntryValue(itemRow, I_ROW);
	if (headRow == -1) {
	    // No items are on this list yet
	    //
	    // Make the new row the head of the list for the item
	    items.putEntryValue(itemRow, I_ROW, newRow);

	    // Make the new row point to itself since it is the only
	    // item on the list
	    m_associations.set(newRow, nextCol, newRow);
	    m_associations.set(newRow, prevCol, newRow);
	    return;
	}

	// There are already items on this list so we must insert the
	// new row into the list.
	int prev = m_associations.get(headRow, prevCol);
	m_associations.set(headRow, prevCol, newRow);
	m_associations.set(prev, nextCol, newRow);
	m_associations.set(newRow, nextCol, headRow);
	m_associations.set(newRow, prevCol, prev);
    }

    /**
     * Removes an m_associations row from one of the linked lists.  The
     * parameters if the row will be removed from the lefts or
     * rights list.
     *
     * @param items either m_left or m_right
     * @param row the index of the row to be removed from m_associations
     * @param itemRow the row of the item in items
     * @param nextCol the column of the next link in m_associations
     * @param prevCol the column of the prev link in m_associations
     */
    protected void removeRow(ObjectIntsHash items, int row,
      int itemRow, int nextCol, int prevCol)
    {
	int next = m_associations.get(row, nextCol);
	if (next == row) {
	    // This is the only row on this list so we can remove the
	    // underlying item
	    items.remove(items.getKey(itemRow));
	    return;
	}

	// Make the next and previous rows point to each other
	int prev = m_associations.get(row, prevCol);
	m_associations.set(prev, nextCol, next);
	m_associations.set(next, prevCol, prev);

	// Decrement the reference count for this item
	items.putEntryValue(itemRow, I_COUNT,
	  items.getEntryValue(itemRow, I_COUNT) - 1);

	if (row == items.getEntryValue(itemRow, I_ROW)) {
	    // We're removing the head of the list so we'll change the
	    // head of the list to the next row
	    items.putEntryValue(itemRow, I_ROW, next);
	}
    }
}

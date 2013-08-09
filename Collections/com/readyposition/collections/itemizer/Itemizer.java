package com.readyposition.collections.itemizer;

/**
 * Interface for iterating over array based collections with integer
 * entry numbers. When an Itemizer is created it will be pointing to
 * the first item in the collection if there is one. To determine if
 * there are any entries remaining call hasMore(). The entry number
 * for the current item can be retrieved using entry(). To advance to
 * the next entry call advance(). Call the remove() method to remove
 * the current entry from the collection.
 * 
 * Here's a typical example of advancing through a collection, doing some work
 * and then removing entries from the collection:
 * <p><code><pre>
 *      for (Itemizer it = collection.itemizer(); it.hasMore(); it.advance()) {
 *          int entry = it.entry();
 *          System.out.println("  Key = " + collection.getEntryKey(entry));
 *          System.out.println("Value = " + collection.getEntryValue(entry));
 *          it.remove();
 *      }
 * </pre></code>
 */
public interface Itemizer {
    /**
     * Returns true if the itemizer has more entries.
     * 
     * @return true if the itemizer has more entries.
     */
    public boolean hasMore();

    /**
     * Advances to the next entry in the collection.
     */
    public void advance();

    /**
     * Gets the current entry number.
     * 
     * @return the current entry number.
     */
    public int entry();

    /**
     * Advances to the next entry in the collection and returns the
     * its entry number.  This is exactly equivalent to invoking
     * advance() followed by entry().
     * 
     * @return the entry number advanced to.
     */
    public int next();
    
    /**
     * Removes from the underlying collection the current entry. This
     * method can be called only once before advancing.
     */
    public void remove();
}

package com.readyposition.collections.itemizer;

/**
 * Interface for collections that can provide an Itemizer for iterating over
 * data structures with integer entry numbers
 */
public interface Itemizable {
    /**
     * Returns an Itemizer set up for traversing the entries in a collection.
     * 
     * @return an Itemizer for a collection.
     */
    Itemizer itemizer();

    /**
     * Returns an itemizer set up for traversing the entries in a collection,
     * reusing the itemizer that was passed in.
     * 
     * @param oldItemizer an Itemizer that was previous created from a tree
     */
    Itemizer itemizer(Itemizer oldItemizer);
}

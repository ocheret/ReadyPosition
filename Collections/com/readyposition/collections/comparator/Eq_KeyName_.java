// _Warning_

package com.readyposition.collections.comparator;

import com.readyposition.collections.common.*;

/** Interface that tests for equivalence of two values of type _KeyType_. */
public interface Eq_KeyName_ {
    /**
     * Tests two values of type _KeyType_ for equivalence. This is an
     * equivalence test and not an equality test.
     * 
     * @param a the first value to be compared.
     * @param b the second value to be compared.
     * @return true if the first value is equivalent to the second,
     *         false otherwise
     */
    boolean equals(_KeyType_ a, _KeyType_ b);
}

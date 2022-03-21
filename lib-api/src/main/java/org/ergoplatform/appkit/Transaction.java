package org.ergoplatform.appkit;

import java.util.List;

/**
 * This interface represents a transaction, providing methods that are available for all of
 * {@link ReducedTransaction}, {@link SignedTransaction} and {@link UnsignedTransaction}.
 */
public interface Transaction {
    /**
     * Transaction id as Base16 string.
     */
    String getId();

    /**
     * @return list of input boxes ids for this transaction
     */
    List<String> getInputBoxesIds();

    /**
     * Gets output boxes that will be created by this transaction
     */
    List<OutBox> getOutputs();
}

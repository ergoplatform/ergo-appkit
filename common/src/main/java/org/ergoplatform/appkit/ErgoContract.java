package org.ergoplatform.appkit;

import sigma.ast.ErgoTree;


/**
 * Representation of ErgoScript contract using source code and named constants.
 * This information is enough to compile contract into {@link ErgoTree}.
 * Once constructed the instances are immutable.
 * Methods which do transformations produce new instances.
 */
public interface ErgoContract {
    /**
     * Returns named constants used to compile this contract.
     */
    Constants getConstants();

    /**
     * Returns a source code of ErgoScript contract.
     */
    String getErgoScript();

    /**
     * Creates a new contract by substituting the constant {@code name} with the new {@code value}.
     */
    ErgoContract substConstant(String name, Object value);

    /**
     * Returns the underlying ErgoTree used by this contract
     */
    ErgoTree getErgoTree();

    /**
     * Get the base58 encoded address that represents this contract
     */
    Address toAddress();
}

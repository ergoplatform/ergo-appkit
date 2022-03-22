package org.ergoplatform.appkit;

import org.ergoplatform.appkit.NetworkType;

/**
 * Parameters of the blockchain
 */
public interface BlockchainParameters {
    /**
     * @return NetworkType of this data source
     */
    NetworkType getNetworkType();

    /**
     * @return cost of storing 1 byte in UTXO for four years, in nanoErgs
     */
    int getStorageFeeFactor();

    /**
     * @return cost of a transaction output, in computation unit
     */
    int getMinValuePerByte();

    /**
     * @return max block size, in bytes
     */
    int getMaxBlockSize();

    /**
     * @return cost of a token contained in a transaction, in computation unit
     */
    int getTokenAccessCost();

    /**
     * @return cost of a transaction input, in computation unit
     */
    int getInputCost();

    /**
     * @return cost of a transaction data input, in computation unit
     */
    int getDataInputCost();

    /**
     * @return cost of a transaction output, in computation unit
     */
    int getOutputCost();

    /**
     * @return computation units limit per block
     */
    long getMaxBlockCost();

    /**
     * @return Protocol version
     */
    byte getBlockVersion();
}

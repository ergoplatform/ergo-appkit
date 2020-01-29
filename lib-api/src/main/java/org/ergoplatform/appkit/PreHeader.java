package org.ergoplatform.appkit;

import special.collection.Coll;
import special.sigma.GroupElement;

/**
 * Only header fields that can be predicted by a miner.
 */
public interface PreHeader {
    /**
     * Block version, to be increased on every soft and hardfork.
     */
    byte getVersion();

    /**
     * Id of parent block
     */
    Coll<Byte> getParentId();

    /**
     * Block timestamp (in milliseconds since beginning of Unix Epoch)
     */
    long getTimestamp();

    /**
     * Current difficulty in a compressed view.
     * NOTE: actually it is unsigned Int
     */
    long getNBits();

    /**
     * Block height
     */
    int getHeight();

    /**
     * Miner public key. Should be used to collect block rewards.
     */
    GroupElement getMinerPk();

    Coll<Byte> getVotes();
}


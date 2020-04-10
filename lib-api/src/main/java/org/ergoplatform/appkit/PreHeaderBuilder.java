package org.ergoplatform.appkit;

import special.collection.Coll;
import special.sigma.GroupElement;

/**
 * Allow building of PreHeaders to be used for transaction signing.
 * Setting different parameters of the preheader allow to simulate execution of contracts
 * in the specific contexts resulting in the corresponding signatures (aka proofs) to be generated for the
 * transaction.
 */
public interface PreHeaderBuilder {
    /**
     * Block version, to be increased on every soft and hardfork.
     */
    PreHeaderBuilder version(byte version);

    /**
     * Id of parent block
     */
    PreHeaderBuilder parentId(Coll<Byte> parentId);

    /**
     * Block timestamp (in milliseconds since beginning of Unix Epoch)
     */
    PreHeaderBuilder timestamp(Long timestamp);

    /**
     * Current difficulty in a compressed view.
     * NOTE: actually it is unsigned Int
     */
    PreHeaderBuilder nBits(Long nbits);

    /** Block height */
    PreHeaderBuilder height(Integer height);

    /** Miner public key. Should be used to collect block rewards. */
    PreHeaderBuilder minerPk(GroupElement minerPk);

    PreHeaderBuilder votes(Coll<Byte> votes);

    PreHeader build();
}

package org.ergoplatform.appkit;

import java.math.BigInteger;

import sigma.AvlTree;
import sigma.Coll;
import sigma.GroupElement;

/**
 * Header of a block
 */
public interface BlockHeader extends PreHeader {
    String getId();

    AvlTree getStateRoot();

    Coll<Byte> getAdProofsRoot();

    Coll<Byte> getTransactionsRoot();

    Coll<Byte> getExtensionHash();

    GroupElement getPowSolutionsPk();

    GroupElement getPowSolutionsW();

    BigInteger getPowSolutionsD();

    Coll<Byte> getPowSolutionsN();
}

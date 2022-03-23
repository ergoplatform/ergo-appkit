package org.ergoplatform.appkit;

import java.math.BigInteger;

import special.sigma.AvlTree;

/**
 * Header of a block
 */
public interface BlockHeader extends PreHeader {
    String getId();

    AvlTree getStateRoot();

    String getAdProofsRoot();

    String getTransactionsRoot();

    String getExtensionHash();

    String getPowSolutionsPk();

    String getPowSolutionsW();

    BigInteger getPowSolutionsD();

    String getPowSolutionsN();
}

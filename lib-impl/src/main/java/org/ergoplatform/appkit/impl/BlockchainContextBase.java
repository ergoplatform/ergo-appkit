package org.ergoplatform.appkit.impl;

import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.ErgoLikeTransactionSerializer;
import org.ergoplatform.appkit.*;
import org.ergoplatform.sdk.ReducedErgoLikeTransaction;
import org.ergoplatform.sdk.ReducedErgoLikeTransactionSerializer;
import sigmastate.Values;
import sigmastate.serialization.SigmaSerializer;
import sigmastate.utils.SigmaByteReader;

public abstract class BlockchainContextBase implements BlockchainContext {
    protected final NetworkType _networkType;

    public BlockchainContextBase(NetworkType networkType) {
        _networkType = networkType;
    }

    @Override
    public ErgoContract newContract(Values.ErgoTree ergoTree) {
        return new ErgoTreeContract(ergoTree, _networkType);
    }

    @Override
    public ErgoContract compileContract(Constants constants, String ergoScript) {
        return ErgoScriptContract.create(constants, ergoScript, _networkType);
    }

    @Override
    public NetworkType getNetworkType() {
        return _networkType;
    }

    public abstract BlockchainParameters getParameters();

    @Override
    public ReducedTransaction parseReducedTransaction(byte[] txBytes) {
        SigmaByteReader r = SigmaSerializer.startReader(txBytes, 0);
        ReducedErgoLikeTransaction tx = ReducedErgoLikeTransactionSerializer.parse(r, getParameters().getBlockVersion());
        return new ReducedTransactionImpl(this, tx);
    }

    @Override
    public SignedTransaction parseSignedTransaction(byte[] txBytes) {
        SigmaByteReader r = SigmaSerializer.startReader(txBytes, 0);
        ErgoLikeTransaction tx = ErgoLikeTransactionSerializer.parse(r);
        int cost = (int)r.getUInt(); // TODO use java7.compat.Math.toIntExact when it will available in Sigma
        return new SignedTransactionImpl(this, tx, cost);
    }
}

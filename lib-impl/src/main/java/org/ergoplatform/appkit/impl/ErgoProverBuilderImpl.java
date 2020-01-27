package org.ergoplatform.appkit.impl;

import org.bouncycastle.math.ec.custom.sec.SecP256K1Point;
import org.ergoplatform.appkit.*;
import org.ergoplatform.restapi.client.Parameters;
import org.ergoplatform.wallet.protocol.context.ErgoLikeParameters;
import org.ergoplatform.wallet.secrets.ExtendedSecretKey;
import scala.Option;
import sigmastate.basics.DiffieHellmanTupleProverInput;
import special.sigma.GroupElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

public class ErgoProverBuilderImpl implements ErgoProverBuilder {

    private final BlockchainContextImpl _ctx;

    private ExtendedSecretKey _masterKey;
    private DiffieHellmanTupleProverInput _dhtSecret;

    public ErgoProverBuilderImpl(BlockchainContextImpl ctx) {
        _ctx = ctx;
    }

    public ErgoProverBuilder withMnemonic(SecretString mnemonicPhrase, SecretString mnemonicPass) {
        _masterKey = JavaHelpers.seedToMasterKey(mnemonicPhrase, mnemonicPass);
        return this;
    }

    @Override
    public ErgoProverBuilder withMnemonic(Mnemonic mnemonic) {
        return withMnemonic(mnemonic.getPhrase(), mnemonic.getPassword());
    }

    @Override
    public ErgoProverBuilder withSecretStorage(SecretStorage storage) {
        if (storage.isLocked())
            throw new IllegalStateException("SecretStorage is locked, call unlock(password) method");
        _masterKey = storage.getSecret();
        return this;
    }

    /**
     * This code uses original cryptographic notation known from the literature, see the following	     
     * <a href="https://github.com/ScorexFoundation/sigmastate-interpreter/blob/b3695bdb785c9b3a94545ffea506358ee3f8ed3d/sigmastate/src/test/scala/sigmastate/utxo/examples/DHTupleExampleSpecification.scala#L28">example</a>	     *   h = g^y
     * to understand the implementation
     */
    @Override
    public ErgoProverBuilder withDHTData(GroupElement g, GroupElement h, GroupElement u, GroupElement v, BigInteger x) {
        _dhtSecret = JavaHelpers.createDiffieHellmanTupleProverInput((SecP256K1Point)g.value(), (SecP256K1Point)h.value(), (SecP256K1Point)u.value(), (SecP256K1Point)v.value(), x);
        return this;
    }

    public ErgoProver build() {
        ErgoLikeParameters parameters = new ErgoLikeParameters() {
            Parameters _params = _ctx.getNodeInfo().getParameters();

            @Override
            public int storageFeeFactor() {
                return _params.getStorageFeeFactor();
            }

            @Override
            public int minValuePerByte() {
                return _params.getMinValuePerByte();
            }

            @Override
            public int maxBlockSize() {
                return _params.getMaxBlockSize();
            }

            @Override
            public int tokenAccessCost() {
                return _params.getTokenAccessCost();
            }

            @Override
            public int inputCost() {
                return _params.getInputCost();
            }

            @Override
            public int dataInputCost() {
                return _params.getDataInputCost();
            }

            @Override
            public int outputCost() {
                return _params.getOutputCost();
            }

            @Override
            public long maxBlockCost() {
                return _params.getMaxBlockCost();
            }

            @Override
            public Option<Object> softForkStartingHeight() {
                return Option.apply(0);
            }

            @Override
            public Option<Object> softForkVotesCollected() {
                return Option.apply(0);
            }

            @Override
            public byte blockVersion() {
                return _params.getBlockVersion().byteValue();
            }
        };
        List<ExtendedSecretKey> keys = Arrays.asList(_masterKey);
        ArrayList<DiffieHellmanTupleProverInput> dhtInputs = new ArrayList<>();
        if (_dhtSecret != null) dhtInputs.add(_dhtSecret);
        AppkitProvingInterpreter interpreter = new AppkitProvingInterpreter(keys, dhtInputs, parameters);
        return new ErgoProverImpl(_ctx, interpreter);
    }
}

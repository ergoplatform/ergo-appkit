package org.ergoplatform.appkit.impl;

import org.bouncycastle.math.ec.custom.sec.SecP256K1Point;
import org.ergoplatform.appkit.*;
import org.ergoplatform.restapi.client.Parameters;
import org.ergoplatform.wallet.protocol.context.ErgoLikeParameters;
import org.ergoplatform.wallet.secrets.ExtendedSecretKey;
import scala.Option;
import sigmastate.basics.DLogProtocol;
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
    private DLogProtocol.DLogProverInput _dLogSecret;

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
     * <a href="https://github.com/ScorexFoundation/sigmastate-interpreter/blob/b3695bdb785c9b3a94545ffea506358ee3f8ed3d/sigmastate/src/test/scala/sigmastate/utxo/examples/DHTupleExampleSpecification.scala#L28">example</a>
     * to understand the implementation, where this _masterKey belong to Bob
     *
     * ProveDHTuple is of the form (g, h, u, v) with secret x (and unknown y), where:
     *   h = g^y
     *   u = g^x
     *   v = g^xy
     *
     *   NOTE: We can swap x, y and obtain another tuple (g, u, h, v) with secret y (and unknown x).
     */
    @Override
    public ErgoProverBuilder withDHTData(GroupElement g, GroupElement h, GroupElement u, GroupElement v, BigInteger x) {
        _dhtSecret = JavaHelpers.createDiffieHellmanTupleProverInput((SecP256K1Point)g.value(), (SecP256K1Point)h.value(), (SecP256K1Point)u.value(), (SecP256K1Point)v.value(), x);
        return this;
    }

    /**
     * This allows adding additional secret for use in proveDlog, when the secret is not part of the wallet.
     *
     * Multiple secrets can be added by calling this method multiple times.
     *
     * Multiple secrets are necessary for statements that need multiple proveDlogs, such as proveDlog(a) && proveDlog(b), where a and b are two group elements.
     */
    @Override
    public ErgoProverBuilder withDLogSecret(BigInteger x) {
        _dLogSecret = new DLogProtocol.DLogProverInput(x);
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
        ArrayList<DLogProtocol.DLogProverInput> dLogInputs = new ArrayList<>();
        if (_dhtSecret != null) dhtInputs.add(_dhtSecret);
        if (_dLogSecret != null) dLogInputs.add(_dLogSecret);
        AppkitProvingInterpreter interpreter = new AppkitProvingInterpreter(keys, dLogInputs, dhtInputs, parameters);
        return new ErgoProverImpl(_ctx, interpreter);
    }
}

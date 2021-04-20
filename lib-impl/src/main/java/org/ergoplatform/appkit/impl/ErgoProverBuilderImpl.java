package org.ergoplatform.appkit.impl;

import org.bouncycastle.math.ec.custom.sec.SecP256K1Point;
import org.ergoplatform.appkit.*;
import org.ergoplatform.restapi.client.Parameters;
import org.ergoplatform.wallet.protocol.context.ErgoLikeParameters;
import org.ergoplatform.wallet.secrets.ExtendedSecretKey;
import scala.NotImplementedError;
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
    private ArrayList<DiffieHellmanTupleProverInput> _dhtSecrets = new ArrayList<>();
    private ArrayList<DLogProtocol.DLogProverInput> _dLogSecrets = new ArrayList<>();

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

    @Override
    public ErgoProverBuilder withDHTData(GroupElement g, GroupElement h, GroupElement u, GroupElement v, BigInteger x) {
        DiffieHellmanTupleProverInput dht = JavaHelpers.createDiffieHellmanTupleProverInput((SecP256K1Point)g.value(), (SecP256K1Point)h.value(), (SecP256K1Point)u.value(), (SecP256K1Point)v.value(), x);
        if (_dhtSecrets.contains(dht))
            throw new IllegalStateException("DHTuple secret already exists");
        _dhtSecrets.add(dht);
        return this;
    }

    @Override
    public ErgoProverBuilder withDLogSecret(BigInteger x) {
        DLogProtocol.DLogProverInput dLog = new DLogProtocol.DLogProverInput(x);
        if (_dLogSecrets.contains(dLog))
            throw new IllegalStateException("Dlog secret already exists");
        _dLogSecrets.add(dLog);
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
                throw new NotImplementedError();
            }

            @Override
            public Option<Object> softForkVotesCollected() {
                throw new NotImplementedError();
            }

            @Override
            public byte blockVersion() {
                return _params.getBlockVersion().byteValue();
            }
        };
        ArrayList<ExtendedSecretKey> keys = new ArrayList<>();
        if (_masterKey != null) keys.add(_masterKey);
        AppkitProvingInterpreter interpreter = new AppkitProvingInterpreter(keys, _dLogSecrets, _dhtSecrets, parameters);
        return new ErgoProverImpl(_ctx, interpreter);
    }
}

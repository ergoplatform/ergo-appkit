package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.ErgoProver;
import org.ergoplatform.appkit.ErgoProverBuilder;
import org.ergoplatform.appkit.JavaHelpers;
import org.ergoplatform.appkit.SecretStorage;
import org.ergoplatform.restapi.client.Parameters;
import org.ergoplatform.wallet.interpreter.ErgoProvingInterpreter;
import org.ergoplatform.wallet.protocol.context.ErgoLikeParameters;
import org.ergoplatform.wallet.secrets.ExtendedSecretKey;
import scala.Option;

public class ErgoProverBuilderImpl implements ErgoProverBuilder {

    private final BlockchainContextImpl _ctx;

    private ExtendedSecretKey _masterKey;

    public ErgoProverBuilderImpl(BlockchainContextImpl ctx) {
        _ctx = ctx;
    }

    public ErgoProverBuilder withMnemonic(String mnemonicPhrase, String mnemonicPass) {
        _masterKey = JavaHelpers.seedToMasterKey(mnemonicPhrase, mnemonicPass);
        return this;
    }

    public ErgoProverBuilder withStorage(SecretStorage storage) {
        _masterKey = storage.getSecret();
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
        ErgoProvingInterpreter interpreter = ErgoProvingInterpreter.apply(_masterKey, parameters);
        return new ErgoProverImpl(_ctx, interpreter);
    }
}

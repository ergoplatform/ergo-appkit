package org.ergoplatform.polyglot;

import org.ergoplatform.api.client.Parameters;
import org.ergoplatform.wallet.interpreter.ErgoProvingInterpreter;
import org.ergoplatform.wallet.protocol.context.ErgoLikeParameters;
import scala.Option;
import sigmastate.basics.DLogProtocol;

public class ErgoProverBuilderImpl implements ErgoProverBuilder {

    private final BlockchainContextImpl _ctx;
    String _seed = "";

    private DLogProtocol.DLogProverInput _secretInput;

    public ErgoProverBuilderImpl(BlockchainContextImpl ctx) {
        _ctx = ctx;
    }

    public ErgoProverBuilder withSeed(String seed) {
        _seed = seed;
        _secretInput = JavaHelpers.proverInputFromSeed(seed);
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
        ErgoProvingInterpreter interpreter = null; //ErgoProvingInterpreter.apply(_secretInput, parameters);
        return new ErgoProverImpl(interpreter);
    }
}

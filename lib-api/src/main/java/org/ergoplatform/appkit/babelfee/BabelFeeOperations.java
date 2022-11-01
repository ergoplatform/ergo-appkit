package org.ergoplatform.appkit.babelfee;

import org.ergoplatform.ErgoAddress;
import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.BlockchainContext;
import org.ergoplatform.appkit.BoxOperations;
import org.ergoplatform.appkit.ContextVar;
import org.ergoplatform.appkit.ErgoId;
import org.ergoplatform.appkit.ErgoToken;
import org.ergoplatform.appkit.ErgoValue;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.appkit.NetworkType;
import org.ergoplatform.appkit.OutBox;
import org.ergoplatform.appkit.OutBoxBuilder;
import org.ergoplatform.appkit.Parameters;
import org.ergoplatform.appkit.PreHeader;
import org.ergoplatform.appkit.UnsignedTransaction;
import org.ergoplatform.appkit.UnsignedTransactionBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BabelFeeOperations {
    /**
     * creates a new babel fee box for a given token id and price per token
     *
     * @param boxOperations prepared BoxOperations object defining babel fee box creator and amount
     *                      to spend
     * @param tokenId       tokenId to create the babel fee box for
     * @param pricePerToken the price per raw token value
     * @return prepared transaction to create the new babel fee box
     */
    public static UnsignedTransaction createNewBabelContractTx(
        BoxOperations boxOperations,
        ErgoId tokenId,
        long pricePerToken
    ) {
        BabelFeeBoxState babelFeeBoxState = BabelFeeBoxState.newBuilder().withBoxCreator(boxOperations.getSenders().get(0))
            .withPricePerToken(pricePerToken)
            .withTokenId(tokenId)
            .withValue(boxOperations.getAmountToSpend())
            .build();

        return boxOperations.buildTxWithDefaultInputs(txB -> {
            OutBox outBox = babelFeeBoxState.buildOutbox(txB, null);
            txB.outputs(outBox);
            return txB;
        });
    }

    public static UnsignedTransaction cancelBabelFeeContract(BoxOperations boxOperations, InputBox babelBox) {
        BabelFeeBoxState babelFeeBoxState = new BabelFeeBoxState(babelBox);

        Address creatorAddress = Address.fromSigmaBoolean(babelFeeBoxState.getBoxCreator().getSigmaBoolean(),
            boxOperations.getBlockchainContext().getNetworkType());

        // if we can't pay tx fee from the babel box content, we need to load another box
        List<InputBox> boxesToSpend;
        if (babelFeeBoxState.getValue() < boxOperations.getFeeAmount() + Parameters.MinChangeValue)
            boxesToSpend = boxOperations.loadTop();
        else
            boxesToSpend = new ArrayList<>();

        boxesToSpend.add(0, babelBox);

        UnsignedTransactionBuilder txB = boxOperations.getBlockchainContext().newTxBuilder();

        OutBoxBuilder outBoxBuilder = txB.outBoxBuilder()
            .value(Math.max(Parameters.MinChangeValue, babelFeeBoxState.getValue() - boxOperations.getFeeAmount()))
            .contract(creatorAddress.toErgoContract());

        if (!babelBox.getTokens().isEmpty())
            outBoxBuilder.tokens(babelBox.getTokens().get(0));

        return txB.boxesToSpend(boxesToSpend)
            .fee(boxOperations.getFeeAmount())
            .sendChangeTo(creatorAddress)
            .outputs(outBoxBuilder.build())
            .build();

    }

    public static InputBox findBabelFeeBox(ErgoId tokenId, long feeAmount) {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Creates a transaction builder for the given babel fee box and the amount to be covered by
     * babel fees. The returned transaction builder can be used like normal and automatically will
     * add the babel fee inbox and outbox to the transaction. You need to make sure that other
     * inboxes cover the amount of tokens needed.
     *
     * @param txB            transaction builder the new transaction builder uses under the hoods
     * @param babelBox       input babel box to make the swap with
     * @param nanoErgToCover nanoergs to be covered by babel box, usually the fee amount needed, maybe a change amount as well
     * @return transaction builder to be used
     */
    public static UnsignedTransactionBuilder getBabelFeeTransactionBuilder(
        UnsignedTransactionBuilder txB,
        InputBox babelBox,
        long nanoErgToCover
    ) {
        return new BabelFeeTransactionBuilder(txB, babelBox, nanoErgToCover);
    }

    private static class BabelFeeTransactionBuilder implements UnsignedTransactionBuilder {
        private final UnsignedTransactionBuilder superBuilder;
        private final InputBox inputBabelBox;
        private final OutBox outBabelBox;

        private List<InputBox> boxesToSpend;
        private OutBox[] outputs;

        private BabelFeeTransactionBuilder(UnsignedTransactionBuilder superBuilder, InputBox babelBox, long nanoErgToCover) {
            this.superBuilder = superBuilder;
            inputBabelBox = babelBox;
            BabelFeeBoxState babelBoxState = new BabelFeeBoxState(babelBox);
            BabelFeeBoxState outBabelBoxState = babelBoxState.buildSucceedingState(babelBoxState.tokensToSellForErgAmount(nanoErgToCover));
            outBabelBox = outBabelBoxState.buildOutbox(superBuilder, babelBox);
        }

        private void setBoxesToSpendAndOutputsToSuperBuilder() {
            // when both boxes to spend and outputs are defined, we can add the babel box to the list
            // and invoke the super transaction builder
            // this is not possible before because we need to know output position for the input box
            if (boxesToSpend != null && outputs != null) {
                List<InputBox> allBoxesToSpend = new LinkedList<>(boxesToSpend);
                allBoxesToSpend.add(inputBabelBox.withContextVars(ContextVar.of((byte) 0, ErgoValue.of(outputs.length))));
                OutBox[] allOutBoxes = new OutBox[outputs.length + 1];
                System.arraycopy(outputs, 0, allOutBoxes, 0, outputs.length);
                allOutBoxes[outputs.length] = outBabelBox;
                superBuilder.boxesToSpend(allBoxesToSpend);
                superBuilder.outputs(allOutBoxes);
            }
        }

        @Override
        public UnsignedTransactionBuilder preHeader(PreHeader ph) {
            return superBuilder.preHeader(ph);
        }

        @Override
        public UnsignedTransactionBuilder boxesToSpend(List<InputBox> boxes) {
            boxesToSpend = boxes;
            setBoxesToSpendAndOutputsToSuperBuilder();
            return this;
        }

        @Override
        public UnsignedTransactionBuilder withDataInputs(List<InputBox> boxes) {
            superBuilder.withDataInputs(boxes);
            return this;
        }

        @Override
        public UnsignedTransactionBuilder outputs(OutBox... outputs) {
            this.outputs = outputs;
            setBoxesToSpendAndOutputsToSuperBuilder();
            return this;
        }

        @Override
        public UnsignedTransactionBuilder fee(long feeAmount) {
            superBuilder.fee(feeAmount);
            return this;
        }

        @Override
        public UnsignedTransactionBuilder tokensToBurn(ErgoToken... tokens) {
            superBuilder.tokensToBurn(tokens);
            return this;
        }

        @Override
        public UnsignedTransactionBuilder sendChangeTo(ErgoAddress address) {
            superBuilder.sendChangeTo(address);
            return this;
        }

        @Override
        public UnsignedTransactionBuilder sendChangeTo(Address address) {
            superBuilder.sendChangeTo(address);
            return this;
        }

        @Override
        public UnsignedTransaction build() {
            return superBuilder.build();
        }

        @Override
        public BlockchainContext getCtx() {
            return superBuilder.getCtx();
        }

        @Override
        public PreHeader getPreHeader() {
            return superBuilder.getPreHeader();
        }

        @Override
        public NetworkType getNetworkType() {
            return superBuilder.getNetworkType();
        }

        @Override
        public OutBoxBuilder outBoxBuilder() {
            return superBuilder.outBoxBuilder();
        }

        @Override
        public List<InputBox> getInputBoxes() {
            return boxesToSpend;
        }
    }
}

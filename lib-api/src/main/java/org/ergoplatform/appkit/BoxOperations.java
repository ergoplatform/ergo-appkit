package org.ergoplatform.appkit;

import static org.ergoplatform.appkit.BlockchainContext.DEFAULT_LIMIT_FOR_API;
import static org.ergoplatform.appkit.Parameters.MinFee;

import org.ergoplatform.sdk.ErgoToken;
import org.ergoplatform.sdk.SecretString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A collection of utility operations implemented in terms of abstract Appkit interfaces.
 */
public class BoxOperations {
    private final BlockchainContext ctx;
    private final List<Address> senders;
    private final ErgoProver senderProver;
    private long amountToSpend = 0;
    private List<ErgoToken> tokensToSpend = Collections.emptyList();
    private long feeAmount = MinFee;
    private IUnspentBoxesLoader inputBoxesLoader = new ExplorerApiUnspentLoader();
    private BoxAttachment attachment;
    private int maxInputBoxesToSelect = 0;

    private static final long CHANGE_BOX_NANOERG = MinFee;

    BoxOperations(BlockchainContext ctx, List<Address> senders, @Nullable ErgoProver senderProver) {
        this.ctx = ctx;
        this.senders = senders;
        this.senderProver = senderProver;
    }

    /**
     * Construct BoxOperations with a single sender address
     *
     * @param sender sender the following methods should use
     */
    public static BoxOperations createForSender(Address sender, BlockchainContext ctx) {
        return createForSenders(Collections.singletonList(sender), ctx);
    }

    /**
     * Construct BoxOperations with a list of sender addresses
     *
     * @param senders list of senders the following methods should use
     */
    public static BoxOperations createForSenders(List<Address> senders, BlockchainContext ctx) {
        return new BoxOperations(ctx, senders, null);
    }

    /**
     * Construct BoxOperations with a prover, deriving list of senders from prover from the
     * EIP-3 addresses.
     * All the derived EIP-3 addresses of the prover can be used to collect unspent boxes.
     */
    public static BoxOperations createForEip3Prover(ErgoProver senderProver, BlockchainContext ctx) {
        List<Address> eip3Addresses = senderProver.getEip3Addresses();
        InternalUtil.checkState(!eip3Addresses.isEmpty(),
            "EIP-3 addresses are not derived in the prover (use ErgoProverBuilder.withEip3Secret)");
        return new BoxOperations(ctx, eip3Addresses, senderProver);
    }

    /**
     * Construct BoxOperations with a prover, deriving list of senders from prover from either the
     * MASTER address of the given prover.
     */
    public static BoxOperations createForProver(ErgoProver senderProver, BlockchainContext ctx) {
        return new BoxOperations(ctx, Collections.singletonList(senderProver.getAddress()), senderProver);
    }

    /**
     * @param amountToSpend nanoerg value to be collected in inboxes
     */
    public BoxOperations withAmountToSpend(long amountToSpend) {
        if (amountToSpend < 0) {
            throw new IllegalArgumentException("Amount to send must be >= 0");
        }

        this.amountToSpend = amountToSpend;
        return this;
    }

    /**
     * @param tokensToSpend tokens to be collected in inboxes
     */
    public BoxOperations withTokensToSpend(@Nonnull List<ErgoToken> tokensToSpend) {
        this.tokensToSpend = tokensToSpend;
        return this;
    }

    /**
     * @param feeAmount fee amount in nanoerg to be used for generated transactions
     */
    public BoxOperations withFeeAmount(long feeAmount) {
        if (feeAmount < MinFee) {
            throw new IllegalArgumentException("Amount to send must be >= " + MinFee);
        }

        this.feeAmount = feeAmount;
        return this;
    }

    /**
     * @param attachment attachment to be set for outboxes
     */
    public BoxOperations withAttachment(@Nullable BoxAttachment attachment) {
        this.attachment = attachment;
        return this;
    }

    public int getMaxInputBoxesToSelect() {
        return maxInputBoxesToSelect;
    }

    /**
     * @param maxInputBoxesToSelect if set greater than 0, {@link #loadTop()} will only select
     *                              up to this number of input boxes to satisfy ERG and token amount
     *                              needed and throws an {@link org.ergoplatform.appkit.InputBoxesSelectionException.InputBoxLimitExceededException}
     *                              otherwise
     *                              if set to <= 0 (or not set), there is no input box restriction
     *                              checked by loadTop.
     */
    public BoxOperations withMaxInputBoxesToSelect(int maxInputBoxesToSelect) {
        this.maxInputBoxesToSelect = maxInputBoxesToSelect;
        return this;
    }

    /**
     * @param message message to be set for outboxes as {@link BoxAttachmentPlainText}
     */
    public BoxOperations withMessage(@Nullable String message) {
        if (message != null) {
            withAttachment(BoxAttachmentPlainText.buildForText(message));
        } else {
            withAttachment(null);
        }
        return this;
    }

    /**
     * @param inputBoxesSource to use for {@link #getCoveringBoxesFor(long, List, boolean, Function)}
     *                         See {@link IUnspentBoxesLoader for more information}
     *                         Default is {@link ExplorerApiUnspentLoader}
     */
    public BoxOperations withInputBoxesLoader(@Nonnull IUnspentBoxesLoader inputBoxesSource) {
        this.inputBoxesLoader = inputBoxesSource;
        return this;
    }

    /**
     * @return context this class was created with
     */
    public BlockchainContext getBlockchainContext() {
        return ctx;
    }

    /**
     * @return senders this class was created with
     */
    public List<Address> getSenders() {
        return senders;
    }

    /**
     * @return currently set amount to spend
     */
    public long getAmountToSpend() {
        return amountToSpend;
    }

    /**
     * @return currently set tokens to spend
     */
    public List<ErgoToken> getTokensToSpend() {
        return tokensToSpend;
    }

    /**
     * @return currently set fee amount
     */
    public long getFeeAmount() {
        return feeAmount;
    }

    @Deprecated
    public static ErgoProver createProver(BlockchainContext ctx, Mnemonic mnemonic, Boolean usePre1627KeyDerivation) {
        ErgoProver prover = ctx.newProverBuilder()
            .withMnemonic(mnemonic.getPhrase(), mnemonic.getPassword(), usePre1627KeyDerivation)
            .build();
        return prover;
    }

    @Deprecated
    public static ErgoProverBuilder createProver(BlockchainContext ctx, String storageFile, SecretString storagePass) {
        return createProver(ctx, storageFile, storagePass.toStringUnsecure());
    }

    @Deprecated
    public static ErgoProverBuilder createProver(
        BlockchainContext ctx, String storageFile, String storagePass) {
        SecretStorage storage = SecretStorage.loadFrom(storageFile);
        storage.unlock(storagePass);
        ErgoProverBuilder proverB = ctx.newProverBuilder()
            .withSecretStorage(storage);
        return proverB;
    }

    /**
     * Send the specified amountToSpend and tokens to the recipient.
     *
     * @param recipient the recipient address
     * @return json of the signed transaction
     */
    public String send(Address recipient) {

        ErgoContract contract = recipient.toErgoContract();
        SignedTransaction signed = putToContractTx(contract);
        ctx.sendTransaction(signed);
        return signed.toJson(true);
    }

    /**
     * Load boxes for the given sender addresses covering the given amount of NanoErgs, fee and tokens.
     * The given page of boxes is loaded from each address and concatenated to a single
     * list.
     * The list is then used to select covering boxes.
     *
     * The method respects a max amount of boxes to be selected set by {@link #withMaxInputBoxesToSelect(int)}.
     * If this limit is exceeded, a {@link org.ergoplatform.appkit.InputBoxesSelectionException.InputBoxLimitExceededException}
     * is thrown and no further boxes are loaded.
     *
     * @return a list of boxes covering the given amount
     */
    public List<InputBox> loadTop() {
        return loadTop(0);
    }

    /**
     * Like {@link #loadTop()} loading and returning unspent boxes covering the given amount of
     * nanoergs, fee and tokens, but you can specify an amount of nanoergs that is already covered
     * by other input boxes and does not need to be satisfied.
     *
     * @param amountCovered nanoerg amount that is assumed to be covered by input boxes you provide
     */
    public List<InputBox> loadTop(long amountCovered) {
        List<InputBox> unspentBoxes = new ArrayList<>();
        long grossAmount = amountToSpend + feeAmount - amountCovered;
        long remainingAmount = grossAmount;
        boolean changeBoxConsidered = false;
        SelectTokensHelper tokensHelper = new SelectTokensHelper(tokensToSpend);
        List<ErgoToken> remainingTokens = tokensToSpend;

        inputBoxesLoader.prepare(ctx, senders, grossAmount, tokensToSpend);

        for (Address sender : senders) {
            inputBoxesLoader.prepareForAddress(sender);
            CoveringBoxes addressUnspentBoxes = getCoveringBoxesFor(remainingAmount, remainingTokens,
                changeBoxConsidered,
                page -> inputBoxesLoader.loadBoxesPage(ctx, sender, page),
                (maxInputBoxesToSelect <= 0) ? 0 : Math.max(1, maxInputBoxesToSelect - unspentBoxes.size())
            );

            // when a change box needed it needs some extra nanoergs to be sent
            if (!changeBoxConsidered && addressUnspentBoxes.isChangeBoxNeeded()) {
                changeBoxConsidered = true;
                remainingAmount = remainingAmount + CHANGE_BOX_NANOERG;
            }

            for (InputBox b : addressUnspentBoxes.getBoxes()) {
                unspentBoxes.add(b);
                tokensHelper.useTokens(b.getTokens());
                remainingAmount -= b.getValue();
                if (remainingAmount <= 0 && tokensHelper.areTokensCovered()) {
                    // collected enough boxes to cover the amount
                    break;
                }
            }
            if (remainingAmount <= 0 && tokensHelper.areTokensCovered()) break;
            remainingTokens = tokensHelper.getRemainingTokenList();
        }
        // check if we have enough tokens and ERG
        InputBoxesValidatorJavaHelper.validateBoxes(unspentBoxes, grossAmount, tokensToSpend);
        return unspentBoxes;
    }

    /**
     * Creates a new {@link SignedTransaction} which sends the given amount of NanoErgs and tokens
     * to the given contract.
     */
    public SignedTransaction putToContractTx(
        ErgoContract contract) {
        if (senderProver == null) {
            throw new IllegalStateException("Call this only when prover is set");
        }

        UnsignedTransaction tx = putToContractTxUnsigned(contract);
        SignedTransaction signed = senderProver.sign(tx);
        return signed;
    }

    /**
     * Creates a new {@link UnsignedTransaction} which sends the given amount of NanoErgs and tokens
     * to the given contract.
     */
    public UnsignedTransaction putToContractTxUnsigned(
        ErgoContract contract) {

        return buildTxWithDefaultInputs(txB -> {
            OutBoxBuilder outBoxBuilder = prepareOutBox(txB)
                .contract(contract);
            OutBox newBox = outBoxBuilder.build();
            txB.outputs(newBox);
            return txB;
        });
    }

    /**
     * @return OutBoxBuilder prepared with the properties set to this BoxOperations instance: tokens to
     * spend, amount to spend and attachment.
     */
    public OutBoxBuilder prepareOutBox(UnsignedTransactionBuilder txB) {
        OutBoxBuilder outBoxBuilder = txB.outBoxBuilder()
            .value(amountToSpend);
        if (!tokensToSpend.isEmpty())
            outBoxBuilder.tokens(tokensToSpend.toArray(new ErgoToken[]{}));
        if (attachment != null) {
            outBoxBuilder.registers(attachment.getOutboxRegistersForAttachment());
        }
        return outBoxBuilder;
    }

    /**
     * Creates a new {@link UnsignedTransaction} which sends the given amount of NanoErgs and a
     * newly minted token to the given contract.
     *
     * @param contract     contract to send the newly minted token to
     * @param tokenBuilder receives the id of the token to mint, must return the new token
     * @return unsigned transaction
     */
    public UnsignedTransaction mintTokenToContractTxUnsigned(ErgoContract contract, Function<String, Eip4Token> tokenBuilder) {
        if (!tokensToSpend.isEmpty()) {
            throw new IllegalArgumentException("Mint token not possible with spending tokens");
        }
        if (attachment != null) {
            throw new IllegalArgumentException("Mint token not possible with attachment");
        }

        return buildTxWithDefaultInputs(txB -> {
            OutBox newBox = txB.outBoxBuilder()
                .value(amountToSpend)
                .contract(contract)
                .mintToken(tokenBuilder.apply(txB.getInputBoxes().get(0).getId().toString()))
                .build();

            txB.outputs(newBox);
            return txB;
        });
    }

    /**
     * Creates a new {@link UnsignedTransaction} preparing inputs, fee and change address.
     * The given outputBuilder is used to prepare and add outboxes to the resulting transaction.
     *
     * See {@link #putToContractTxUnsigned(ErgoContract)} how to use.
     */
    public UnsignedTransaction buildTxWithDefaultInputs(Function<UnsignedTransactionBuilder, UnsignedTransactionBuilder> outputBuilder) {
        List<InputBox> boxesToSpend = loadTop();

        UnsignedTransactionBuilder txB = ctx.newTxBuilder();

        UnsignedTransactionBuilder unsignedTransactionBuilder = txB.boxesToSpend(boxesToSpend)
            .fee(feeAmount)
            .sendChangeTo(senders.get(0).getErgoAddress());

        return outputBuilder.apply(unsignedTransactionBuilder).build();
    }

    public static SignedTransaction spendBoxesTx(
        BlockchainContext ctx,
        UnsignedTransactionBuilder txB,
        List<InputBox> boxes,
        ErgoProver sender, Address recipient, long amount, long fee) {
        OutBox newBox = txB.outBoxBuilder()
            .value(amount)
            .contract(recipient.toErgoContract())
            .build();

        UnsignedTransaction tx = txB.boxesToSpend(boxes)
            .addOutputs(newBox)
            .fee(fee)
            .sendChangeTo(sender.getP2PKAddress())
            .build();
        SignedTransaction signed = sender.sign(tx);
        return signed;
    }

    /**
     * Get unspent boxes from a paged boxes source and selects the top ones needed to spent
     * to satisfy amountToSpend and tokensToSpend.
     * <p>
     * inputBoxesLoader must satisfy the following needs:
     * - receives a 0-based integer, the page that should be loaded
     * - returns a list of {@link InputBox} to select from. First items are preferred to be selected
     * - must not return null
     * - returning an empty list means the source of input boxes is drained and no further page will
     *   be loaded
     *
     * @param amountToSpend       amount of NanoErgs to be covered
     * @param tokensToSpend       ErgoToken to spent
     * @param changeBoxConsidered true if CHANGE_BOX_NANOERG amount for a change box is already
     *                            included in amountToSpend and does not need to be added any more
     * @param inputBoxesLoader    method returning paged sets of InputBoxes, see above
     * @return a new instance of {@link CoveringBoxes} set
     */
    public static CoveringBoxes getCoveringBoxesFor(long amountToSpend,
                                                    List<ErgoToken> tokensToSpend,
                                                    boolean changeBoxConsidered,
                                                    Function<Integer, List<InputBox>> inputBoxesLoader) {
        return getCoveringBoxesFor(amountToSpend, tokensToSpend, changeBoxConsidered, inputBoxesLoader, 0);
    }

    private static CoveringBoxes getCoveringBoxesFor(long amountToSpend,
                                                     List<ErgoToken> tokensToSpend,
                                                     boolean changeBoxConsidered,
                                                     Function<Integer, List<InputBox>> inputBoxesLoader,
                                                     int maxBoxesToSelect) {
        SelectTokensHelper tokensRemaining = new SelectTokensHelper(tokensToSpend);
        InternalUtil.checkArgument(amountToSpend > 0 ||
            !tokensRemaining.areTokensCovered(), "amountToSpend or tokens to spend should be > 0");
        ArrayList<InputBox> selectedCoveringBoxes = new ArrayList<>();
        long remainingAmountToCover = amountToSpend;
        int page = 0;
        while (true) {
            List<InputBox> chunk = inputBoxesLoader.apply(page);
            for (InputBox boxCandidate : chunk) {
                // on rare occasions, chunk can include entries that we already had received on a
                // previous chunk page. We make sure we don't add any duplicate entries.
                if (!isAlreadyAdded(selectedCoveringBoxes, boxCandidate)) {
                    boolean usefulTokens = tokensRemaining.areTokensNeeded(boxCandidate.getTokens());
                    if (usefulTokens || remainingAmountToCover > 0) {
                        selectedCoveringBoxes.add(boxCandidate);
                        remainingAmountToCover -= boxCandidate.getValue();
                        tokensRemaining.useTokens(boxCandidate.getTokens());

                        // if we haven't accounted for a change box so far, but now a change box is
                        // needed, we have to search for a little more amount to cover the change
                        if (!changeBoxConsidered && tokensRemaining.isChangeBoxNeeded()) {
                            changeBoxConsidered = true;
                            remainingAmountToCover = remainingAmountToCover + CHANGE_BOX_NANOERG;
                            amountToSpend = amountToSpend + CHANGE_BOX_NANOERG;
                        }
                    }
                    if (remainingAmountToCover <= 0 && tokensRemaining.areTokensCovered())
                        return new CoveringBoxes(amountToSpend, selectedCoveringBoxes, tokensToSpend, changeBoxConsidered);
                    else // check the maxBoxToSelect restriction, if it is set
                    if (maxBoxesToSelect > 0 && selectedCoveringBoxes.size() >= maxBoxesToSelect) {
                        List<ErgoToken> remainingTokenList = tokensRemaining.getRemainingTokenList();
                        throw new InputBoxesSelectionException.InputBoxLimitExceededException(
                            "Input box limit exceeded, could not cover " + remainingAmountToCover +
                                " nanoERG and " + remainingTokenList.size() + " tokens.",
                            remainingAmountToCover,
                            remainingTokenList,
                            maxBoxesToSelect
                        );
                    }
                }
            }
            // this chunk is not enough, go to the next (if any)
            if (chunk.size() == 0) {
                // this was the last chunk, but still remain to collect
                assert remainingAmountToCover > 0 || !tokensRemaining.areTokensCovered();
                // cannot satisfy the request, but still return cb, with cb.isCovered == false
                return new CoveringBoxes(amountToSpend, selectedCoveringBoxes, tokensToSpend, changeBoxConsidered);
            }
            // step to next chunk
            page++;
        }
    }

    /**
     * @return true when boxCandidate is already added to selectedBoxes list
     */
    private static boolean isAlreadyAdded(ArrayList<InputBox> selectedBoxes, InputBox boxCandidate) {
        boolean alreadyAdded = false;
        for (InputBox coveringBox : selectedBoxes) {
            if (coveringBox.getId().equals(boxCandidate.getId())) {
                alreadyAdded = true;
                break;
            }
        }
        return alreadyAdded;
    }

    /**
     * Use this interface to adapt behaviour of unspent boxes loading.
     */
    public interface IUnspentBoxesLoader {
        /**
         * Called before first call to {@link #loadBoxesPage(BlockchainContext, Address, Integer)} is done
         * Called before first call to {@link #prepareForAddress(Address)}
         *
         * @param ctx           {@link BlockchainContext} to work with, if needed
         * @param addresses     addresses boxes will be fetched for
         * @param grossAmount   overall amount of nanoergs needed to satisfy the caller
         * @param tokensToSpend overall amount of tokens needed to satisfy the caller
         */
        void prepare(@Nonnull BlockchainContext ctx, List<Address> addresses, long grossAmount,
                     @Nonnull List<ErgoToken> tokensToSpend);

        /**
         * Called before first call to {@link #loadBoxesPage(BlockchainContext, Address, Integer)}
         * for a single address.
         *
         * @param address address that will be fetched next
         */
        void prepareForAddress(Address address);

        /**
         * @param ctx     {@link BlockchainContext} to work with, if needed
         * @param address p2pk address unspent boxes list should be fetched for
         * @param page    page that should be loaded, 0-based integer
         * @return a list of InputBox to select from. First items are preferred to be selected.
         * Returning an empty list means the source of input boxes is drained and no further
         * page should be loaded
         */
        @Nonnull
        List<InputBox> loadBoxesPage(@Nonnull BlockchainContext ctx, @Nonnull Address address, @Nonnull Integer page);
    }

    /**
     * Adds a checker method to {@link ExplorerApiUnspentLoader} for boxes that should be omitted
     * when loading boxes from Explorer API
     */
    public abstract static class ExplorerApiWithCheckerLoader extends ExplorerApiUnspentLoader {
        protected abstract boolean canUseBox(InputBox box);

        @Override
        @Nonnull
        public List<InputBox> loadBoxesPage(@Nonnull BlockchainContext ctx, @Nonnull Address sender, @Nonnull Integer page) {
            List<InputBox> boxes = super.loadBoxesPage(ctx, sender, page);

            List<InputBox> returnedBoxes = new ArrayList<>(boxes.size());
            int pageOffset = 0;
            while (!boxes.isEmpty() && returnedBoxes.isEmpty()) {
                for (InputBox boxToCheck : boxes) {
                    if (canUseBox(boxToCheck))
                        returnedBoxes.add(boxToCheck);
                }

                // edge case: we had boxes loaded from Explorer, but all were blacklisted...
                // load next page!
                // This will make return the same set on next call by getCoveringBoxes, but
                // getCoveringBoxes handles duplicates so we just have a call too often
                if (returnedBoxes.isEmpty()) {
                    pageOffset++;
                    boxes = super.loadBoxesPage(ctx, sender, page + pageOffset);
                }
            }
            return returnedBoxes;
        }
    }

    /**
     * Default loader for unspent boxes. Loads unspent boxes for an address directly from Explorer API
     */
    public static class ExplorerApiUnspentLoader implements IUnspentBoxesLoader {
        @Override
        public void prepare(@Nonnull BlockchainContext ctx, List<Address> addresses, long grossAmount, @Nonnull List<ErgoToken> tokensToSpend) {
            // not needed
        }

        @Override
        public void prepareForAddress(Address address) {
            // not needed
        }

        @Override
        @Nonnull
        public List<InputBox> loadBoxesPage(@Nonnull BlockchainContext ctx, @Nonnull Address address, @Nonnull Integer page) {
            return ctx.getDataSource().getUnspentBoxesFor(address, page * DEFAULT_LIMIT_FOR_API, DEFAULT_LIMIT_FOR_API);
        }
    }
}

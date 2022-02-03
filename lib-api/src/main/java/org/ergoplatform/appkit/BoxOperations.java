package org.ergoplatform.appkit;

import org.ergoplatform.P2PKAddress;
import org.ergoplatform.appkit.impl.ErgoTreeContract;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.ergoplatform.appkit.Parameters.MinFee;

/**
 * A collection of utility operations implemented in terms of abstract Appkit interfaces.
 */
public class BoxOperations {

    public static List<InputBox> selectTop(
            List<InputBox> unspentBoxes,
            long amountToSpend) {
        return selectTop(unspentBoxes, amountToSpend, new ArrayList<>());
    }

    public static List<InputBox> selectTop(
            List<InputBox> unspentBoxes,
            long amountToSpend,
            List<ErgoToken> tokensToSpend) {
        List<InputBox> found = BoxSelectorsJavaHelpers.selectBoxes(unspentBoxes, amountToSpend, tokensToSpend);
        return found;
    }

    public static ErgoProver createProver(BlockchainContext ctx, Mnemonic mnemonic) {
        ErgoProver prover = ctx.newProverBuilder()
                .withMnemonic(mnemonic.getPhrase(), mnemonic.getPassword())
                .build();
        return prover;
    }

    public static ErgoProverBuilder createProver(BlockchainContext ctx, String storageFile, SecretString storagePass) {
        return createProver(ctx, storageFile, storagePass.toStringUnsecure());
    }

    public static ErgoProverBuilder createProver(
            BlockchainContext ctx, String storageFile, String storagePass) {
        SecretStorage storage = SecretStorage.loadFrom(storageFile);
        storage.unlock(storagePass);
        ErgoProverBuilder proverB = ctx.newProverBuilder()
                .withSecretStorage(storage);
        return proverB;
    }

    /**
     * Send the given `amountToSend` to the recipient from either the MASTER address of the
     * given prover or from the EIP-3 addresses.
     * All the derived EIP-3 addresses of the prover can be used to collect unspent boxes.
     *
     * @param ctx              blockchain context obtained from {@link ErgoClient}
     * @param senderProver     prover which is used to sign transaction
     * @param useEip3Addresses true if EIP-3 addresses of the prover should be used to
     *                         withdraw funds (use false for backwards compatibility)
     * @param recipient        the recipient address
     * @param amountToSend     amount of NanoErgs to send
     */
    public static String send(
        BlockchainContext ctx,
        ErgoProver senderProver, boolean useEip3Addresses,
        Address recipient, long amountToSend) {

        ErgoContract contract = new ErgoTreeContract(recipient.getErgoAddress().script());
        SignedTransaction signed = putToContractTx(ctx, senderProver, useEip3Addresses,
            contract, amountToSend, new ArrayList<>());
        ctx.sendTransaction(signed);
        return signed.toJson(true);
    }

    /**
     * Load boxes for the given sender address covering the given amount of NanoErgs.
     * The given page of boxes is loaded and selected for the resulting list.
     *
     * @param ctx    the blockchain context to use for loading
     * @param sender the address which owns the boxes
     * @param amount how much NanoErg the boxes should cover
     * @param tokensToSpend   list of ErgoToken to spent. Make sure every token is listed only once
     * @return a `limit` number of boxes starting from `offset`
     */
    public static List<InputBox> loadTop(
        BlockchainContext ctx,
        Address sender, long amount,
        List<ErgoToken> tokensToSpend) {
        CoveringBoxes unspent = ctx.getCoveringBoxesFor(sender, amount, tokensToSpend);
        List<InputBox> selected = selectTop(unspent.getBoxes(), amount, tokensToSpend);
        return selected;
    }

    /**
     * Load boxes for the given sender addresses covering the given amount of NanoErgs.
     * The given page of boxes is loaded from each address and concatenated to a single
     *  list.
     * The list is then used to select covering boxes.
     *
     * @param ctx             the blockchain context to use for loading
     * @param senderAddresses the addresses which owns the boxes
     * @param amount          how much NanoErg the boxes should cover
     * @param tokensToSpend   list of ErgoToken to spent. Make sure every token is listed only once
     * @return a list of boxes covering the given amount
     */
    public static List<InputBox> loadTop(
        BlockchainContext ctx,
        List<Address> senderAddresses, long amount,
        List<ErgoToken> tokensToSpend) {
        List<InputBox> unspentBoxes = new ArrayList<>();
        long remainingAmount = amount;
        SelectTokensHelper tokensHelper = new SelectTokensHelper(tokensToSpend);
        List<ErgoToken> remainingTokens = tokensToSpend;

        for (Address sender : senderAddresses) {
            CoveringBoxes unspent = ctx.getCoveringBoxesFor(sender, remainingAmount, remainingTokens);
            for (InputBox b : unspent.getBoxes()) {
                unspentBoxes.add(b);
                tokensHelper.foundNewTokens(b.getTokens());
                remainingAmount -= b.getValue();
                if (remainingAmount <= 0 && tokensHelper.areTokensCovered()) {
                    // collected enough boxes to cover the amount
                    break;
                }
            }
            if (remainingAmount <= 0 && tokensHelper.areTokensCovered()) break;
            remainingTokens = tokensHelper.getRemainingTokenList();
        }
        List<InputBox> selected = selectTop(unspentBoxes, amount, tokensToSpend);
        return selected;
    }

    /**
     * Creates a new {@link SignedTransaction} which sends the given amount of NanoErgs
     * to the given contract. The address of the given senderProver is used to collect
     * boxes for spending.
     */
    public static SignedTransaction putToContractTx(
            BlockchainContext ctx,
            ErgoProver senderProver, boolean useEip3Addresses,
            ErgoContract contract, long amountToSend,
            List<ErgoToken> tokensToSpend) {
        UnsignedTransaction tx = putToContractTxUnsigned(ctx,
            senderProver, useEip3Addresses, contract, amountToSend, tokensToSpend);
        SignedTransaction signed = senderProver.sign(tx);
        return signed;
    }

    /**
     * Creates a new {@link UnsignedTransaction} which sends the given amount of NanoErgs
     * to the given contract. The address of the given senderProver is used to collect
     * boxes for spending.
     */
    public static UnsignedTransaction putToContractTxUnsigned(
            BlockchainContext ctx,
            ErgoProver senderProver, boolean useEip3Addresses,
            ErgoContract contract, long amountToSend,
            List<ErgoToken> tokensToSpend) {
        List<Address> senders = new ArrayList<>();
        if (useEip3Addresses) {
            List<Address> eip3Addresses = senderProver.getEip3Addresses();
            checkState(eip3Addresses.size() > 0,
              "EIP-3 addresses are not derived in the prover (use ErgoProverBuilder.withEip3Secret)");
            senders.addAll(eip3Addresses);
        } else {
            senders.add(senderProver.getAddress());
        }
        return putToContractTxUnsigned(ctx, senders, contract, amountToSend, tokensToSpend);
    }

    /**
     * Creates a new {@link UnsignedTransaction} which sends the given amount of NanoErgs
     * to the given contract. The addresses of the given senders are used to collect
     * boxes for spending.
     */
    public static UnsignedTransaction putToContractTxUnsigned(
            BlockchainContext ctx,
            List<Address> senders,
            ErgoContract contract, long amountToSend,
            List<ErgoToken> tokensToSpend) {
        List<InputBox> boxesToSpend = loadTop(ctx, senders, amountToSend + MinFee, tokensToSpend);

        P2PKAddress changeAddress = senders.get(0).asP2PK();
        UnsignedTransactionBuilder txB = ctx.newTxBuilder();

        OutBoxBuilder outBoxBuilder = txB.outBoxBuilder()
            .value(amountToSend)
            .contract(contract);
        if (!tokensToSpend.isEmpty())
            outBoxBuilder.tokens(tokensToSpend.toArray(new ErgoToken[]{}));
        OutBox newBox = outBoxBuilder.build();

        UnsignedTransaction tx = txB.boxesToSpend(boxesToSpend)
                .outputs(newBox)
                .fee(Parameters.MinFee)
                .sendChangeTo(changeAddress)
                .build();
        return tx;
    }

    public static SignedTransaction spendBoxesTx(
            BlockchainContext ctx,
            UnsignedTransactionBuilder txB,
            List<InputBox> boxes,
            ErgoProver sender, Address recipient, long amount, long fee) {
        OutBox newBox = txB.outBoxBuilder()
                .value(amount)
                .contract(new ErgoTreeContract(recipient.getErgoAddress().script()))
                .build();

        UnsignedTransaction tx = txB.boxesToSpend(boxes)
                .outputs(newBox)
                .fee(fee)
                .sendChangeTo(sender.getP2PKAddress())
                .build();
        SignedTransaction signed = sender.sign(tx);
        return signed;
    }


}

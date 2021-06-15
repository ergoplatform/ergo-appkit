package org.ergoplatform.appkit;

import org.ergoplatform.P2PKAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        ErgoContract pkContract = ErgoContracts.sendToPK(ctx, recipient);
        SignedTransaction signed = putToContractTx(ctx, senderProver, useEip3Addresses,
            pkContract, amountToSend);
        ctx.sendTransaction(signed);
        return signed.toJson(true);
    }

    public static List<InputBox> loadTop(BlockchainContext ctx, Address sender, long amount) {
        List<InputBox> unspent = ctx.getUnspentBoxesFor(sender);
        List<InputBox> selected = selectTop(unspent, amount);
        return selected;
    }

    public static List<InputBox> loadTop(BlockchainContext ctx, List<Address> senderAddresses, long amount) {
        List<InputBox> unspentBoxes = new ArrayList<>();
        for (Address sender : senderAddresses) {
            List<InputBox> unspent = ctx.getUnspentBoxesFor(sender);
            unspentBoxes.addAll(unspent);
        }
        List<InputBox> selected = selectTop(unspentBoxes, amount);
        return selected;
    }

    public static SignedTransaction putToContractTx(
            BlockchainContext ctx,
            ErgoProver senderProver, boolean useEip3Addresses,
            ErgoContract contract, long amountToSend) {
        List<Address> senders = new ArrayList<>();
        if (useEip3Addresses) {
            List<Address> eip3Addresses = senderProver.getEip3Addresses();
            checkState(eip3Addresses.size() > 0,
              "EIP-3 addresses are not derived in the prover (use ErgoProverBuilder.withEip3Secret)");
            senders.addAll(eip3Addresses);
        } else {
            senders.add(senderProver.getAddress());
        }
        List<InputBox> boxesToSpend = loadTop(ctx, senders, amountToSend + MinFee);

        P2PKAddress changeAddress = (useEip3Addresses) ? senderProver.getEip3Addresses().get(0).asP2PK() : senderProver.getP2PKAddress();
        UnsignedTransactionBuilder txB = ctx.newTxBuilder();
        OutBox newBox = txB.outBoxBuilder()
                .value(amountToSend)
                .contract(contract)
                .build();
        UnsignedTransaction tx = txB.boxesToSpend(boxesToSpend)
                .outputs(newBox)
                .fee(Parameters.MinFee)
                .sendChangeTo(changeAddress)
                .build();

        SignedTransaction signed = senderProver.sign(tx);
        return signed;
    }

    public static SignedTransaction spendBoxesTx(
            BlockchainContext ctx,
            UnsignedTransactionBuilder txB,
            List<InputBox> boxes,
            ErgoProver sender, Address recipient, long amount, long fee) {
        OutBox aliceBox = txB.outBoxBuilder()
                .value(amount)
                .contract(ErgoContracts.sendToPK(ctx, recipient))
                .build();

        UnsignedTransaction tx = txB.boxesToSpend(boxes)
                .outputs(aliceBox)
                .fee(fee)
                .sendChangeTo(sender.getP2PKAddress())
                .build();
        SignedTransaction signed = sender.sign(tx);
        return signed;
    }


}

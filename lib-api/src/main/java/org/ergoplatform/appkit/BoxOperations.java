package org.ergoplatform.appkit;

import java.util.ArrayList;
import java.util.List;

import static org.ergoplatform.appkit.Parameters.MinFee;

public class BoxOperations {

    public static List<InputBox> selectTop(List<InputBox> unspentBoxes,
                                           long amountToSpend) {
        return selectTop(unspentBoxes, amountToSpend, new ArrayList<>());
    }

    public static List<InputBox> selectTop(List<InputBox> unspentBoxes,
                                           long amountToSpend,
                                           List<ErgoToken> tokenAmounts) {
        if (amountToSpend == 0 && tokenAmounts.isEmpty()) {
            // all unspent boxes are requested
            return unspentBoxes;
        }

        // collect boxes to cover requested amount
        ArrayList<InputBox> res = new ArrayList<InputBox>();
        long collected = 0;
        // TODO: check and collect tokens
        for (int i = 0; i < unspentBoxes.size() && collected < amountToSpend; ++i) {
            InputBox box = unspentBoxes.get(i);
            collected += box.getValue();
            res.add(box);
        }
        if (collected < amountToSpend)
            throw new RuntimeException("Not enough boxes to pay " + amountToSpend);
        return res;
    }

    public static ErgoProver createProver(BlockchainContext ctx, Mnemonic mnemonic) {
        ErgoProver prover = ctx.newProverBuilder()
                .withMnemonic( mnemonic.getPhrase(), mnemonic.getPassword())
                .build();
        return prover;
    }

    public static ErgoProver createProver(BlockchainContext ctx, String storageFile, String storagePass) {
        SecretStorage storage = SecretStorage.loadFrom(storageFile);
        storage.unlock(storagePass);
        ErgoProver prover = ctx.newProverBuilder()
                .withSecretStorage(storage)
                .build();
        return prover;
    }

    public static String send(
            BlockchainContext ctx, ErgoProver senderProver, Address recipient, long amountToSend) {
        Address sender = senderProver.getAddress();
        List<InputBox> unspent = ctx.getUnspentBoxesFor(sender);
        List<InputBox> boxesToSpend = selectTop(unspent, amountToSend + MinFee);

        UnsignedTransactionBuilder txB = ctx.newTxBuilder();
        OutBox newBox = txB.outBoxBuilder()
                .value(amountToSend)
                .contract(ctx.compileContract(
                        ConstantsBuilder.create()
                                .item("recipientPk", recipient.getPublicKey())
                                .build(),
                        "{ recipientPk }"))
                .build();
        UnsignedTransaction tx = txB.boxesToSpend(boxesToSpend)
                .outputs(newBox)
                .fee(Parameters.MinFee)
                .sendChangeTo(senderProver.getP2PKAddress())
                .build();

        SignedTransaction signed = senderProver.sign(tx);
        ctx.sendTransaction(signed);
        return signed.toJson(true);
    }
}

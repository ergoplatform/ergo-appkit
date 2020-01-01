package org.ergoplatform.appkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.ergoplatform.appkit.Parameters.MinFee;

public class BoxOperations {

    public static List<InputBox> selectTop(List<InputBox> unspentBoxes,
                                           long amountToSpend) {
        return selectTop(unspentBoxes, amountToSpend, Optional.empty());
    }

    public static List<InputBox> selectTop(List<InputBox> unspentBoxes,
                                           long amountToSpend,
                                           Optional<ErgoToken> tokenOptional) {
        if (amountToSpend == 0 && !tokenOptional.isPresent()) {
            // all unspent boxes are requested
            return unspentBoxes;
        }

        ErgoToken token = tokenOptional.get();

        // collect boxes to cover requested amount
        ArrayList<InputBox> res = new ArrayList<InputBox>();
        long collected = 0;
        long collectedToken = 0;
        for (int i = 0;
             i < unspentBoxes.size() && collected < amountToSpend && collectedToken < token.getValue();
             ++i) {
            InputBox box = unspentBoxes.get(i);
            collected += box.getValue();
            long tokenAmount = box.getTokens().stream()
                    .filter(t -> t.getId() == token.getId())
                    .map(ErgoToken::getValue)
                    .reduce(0L, Long::sum);
            collectedToken += tokenAmount;
            res.add(box);
        }
        if (collected < amountToSpend)
            throw new RuntimeException("Not enough coins in boxes to pay " + amountToSpend);
        if (collectedToken < token.getValue())
            throw new RuntimeException("Not enough tokens (id "+ token.getId().toString() +") in boxes to pay " + token.getValue());
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

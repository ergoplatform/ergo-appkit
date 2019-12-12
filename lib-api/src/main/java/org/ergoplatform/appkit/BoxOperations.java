package org.ergoplatform.appkit;

import java.util.ArrayList;
import java.util.List;

import static org.ergoplatform.appkit.Parameters.MinFee;

public class BoxOperations {

    public static List<InputBox> selectTop(List<InputBox> unspentBoxes, long amountToSpend) {
        if (amountToSpend == 0) {
            // all unspent boxes are requested
            return unspentBoxes;
        }

        // collect boxes to cover requested amount
        ArrayList<InputBox> res = new ArrayList<InputBox>();
        long collected = 0;
        for (int i = 0; i < unspentBoxes.size() && collected < amountToSpend; ++i) {
            InputBox box = unspentBoxes.get(i);
            collected += box.getValue();
            res.add(box);
        }
        if (collected < amountToSpend)
            throw new RuntimeException("Not enough boxes to pay " + amountToSpend);
        return res;
    }

    public static String send(
            BlockchainContext ctx, Mnemonic senderMnemonic, Address recipient, long amountToSend) {
        Address sender = Address.fromMnemonic(ctx.getNetworkType(), senderMnemonic);
        List<InputBox> unspent = ctx.getUnspentBoxesFor(sender);
        List<InputBox> boxesToSpend = selectTop(unspent, amountToSend + MinFee);
        ErgoProver prover = ctx.newProverBuilder()
                .withMnemonic(
                        senderMnemonic.getPhrase(),
                        senderMnemonic.getPassword())
                .build();

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
                .sendChangeTo(prover.getP2PKAddress())
                .build();

        SignedTransaction signed = prover.sign(tx);
        ctx.sendTransaction(signed);
        return signed.toJson(true);
    }
}

package org.ergoplatform.example;

import org.ergoplatform.ergotool.*;
import org.ergoplatform.example.util.RestApiErgoClient;
import org.ergoplatform.appkit.*;

import java.io.FileNotFoundException;
import java.util.*;

public class ErgoToolJava {
    public static void main(String[] args) {
        try {
            long amountToSend = Long.parseLong(args[0]);
            ErgoToolConfig conf = ErgoToolConfig.load("ergotool.json");
            ErgoNodeConfig nodeConf = conf.getNode();
            int newBoxSpendingDelay = Integer.parseInt(conf.getParameters().get("newBoxSpendingDelay"));

            ErgoClient ergoClient = RestApiErgoClient.create(nodeConf);

            String txJson = ergoClient.execute(ctx -> {
                ErgoWallet wallet = ctx.getWallet();
                long totalToSpend = amountToSend + Parameters.MinFee;
                Optional<List<InputBox>> boxes = wallet.getUnspentBoxes(totalToSpend);
                if (!boxes.isPresent())
                    throw new ErgoClientException("Not enough coins in the wallet to pay " + totalToSpend, null);

                ErgoProver prover = ctx.newProverBuilder()
                        .withMnemonic(
                                nodeConf.getWallet().getMnemonic(),
                                nodeConf.getWallet().getPassword())
                        .build();

                UnsignedTransactionBuilder txB = ctx.newTxBuilder();
                OutBox newBox = txB.outBoxBuilder()
                        .value(amountToSend)
                        .contract(ctx.compileContract(
                                ConstantsBuilder.create()
                                        .item("freezeDeadline", ctx.getHeight() + newBoxSpendingDelay)
                                        .item("walletOwnerPk", prover.getP2PKAddress().pubkey())
                                        .build(),
                                "{ sigmaProp(HEIGHT > freezeDeadline) && walletOwnerPk }"))
                        .build();
                UnsignedTransaction tx = txB.boxesToSpend(boxes.get())
                        .outputs(newBox)
                        .fee(Parameters.MinFee)
                        .sendChangeTo(prover.getP2PKAddress())
                        .build();

                SignedTransaction signed = prover.sign(tx);
                String txId = ctx.sendTransaction(signed);
                return signed.toJson(true);
            });
            System.out.println(txJson);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

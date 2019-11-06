package org.ergoplatform.example;

import org.ergoplatform.ergotool.ErgoNodeConfig;
import org.ergoplatform.ergotool.ErgoToolConfig;
import org.ergoplatform.example.util.RestApiErgoClient;
import org.ergoplatform.polyglot.*;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

public class ErgoToolJava {
    static int newBoxDelay = 30;

    public static void main(String[] args) {
        try {
            long amountToPay = Long.parseLong(args[0]);
            ErgoToolConfig conf = ErgoToolConfig.load("ergotool.json");
            ErgoNodeConfig nodeConf = conf.getNode();

            ErgoClient ergoClient = RestApiErgoClient.create(
                    nodeConf.getNodeApi().getApiUrl(),
                    nodeConf.getNetworkType(),
                    nodeConf.getNodeApi().getApiKey());

            String txJson = ergoClient.execute(ctx -> {
                ErgoWallet wallet = ctx.getWallet();
                long totalToSpend = amountToPay + Parameters.MinFee;
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
                        .value(amountToPay)
                        .contract(ctx.compileContract(
                                ConstantsBuilder.create()
                                        .item("deadline", ctx.getHeight() + newBoxDelay)
                                        .item("pkOwner", prover.getP2PKAddress().pubkey())
                                        .build(),
                                "{ sigmaProp(HEIGHT > deadline) && pkOwner }"))
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

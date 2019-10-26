package org.ergoplatform.example;

import org.ergoplatform.example.util.FileMockedRunner;
import org.ergoplatform.polyglot.*;

import static org.ergoplatform.example.MockData.*;

public class PrepareBoxJava {

    public static void main(String[] args) {
        SignedTransaction res = new FileMockedRunner(infoFile, lastHeadersFile, boxFile).run((BlockchainContext ctx) -> {
            UnsignedTransactionBuilder mockTxB = ctx.newTxBuilder();
            OutBox out = mockTxB.outBoxBuilder()
                    .contract(ConstantsBuilder.empty(), "{ sigmaProp(CONTEXT.headers.size == 9) }")
                    .build();
            UnsignedTransactionBuilder spendingTxB = ctx.newTxBuilder();
            UnsignedTransaction tx = spendingTxB
                    .boxesToSpend(out.convertToInputWith(mockTxId, (short)0))
                    .outputs(
                            spendingTxB.outBoxBuilder()
                                    .contract(ConstantsBuilder.empty(), "{ true }")
                                    .build())
                    .build();
            ErgoProverBuilder proverB = ctx.newProver();
            ErgoProver prover = proverB.withSeed("abc").build();
            SignedTransaction signed = prover.sign(tx);
            return signed;
        });

        System.out.println(res);
    }
}

package org.ergoplatform.example;

import org.ergoplatform.example.util.FileMockedRunner;
import org.ergoplatform.polyglot.*;

import static org.ergoplatform.example.MockData.*;

public class PrepareBoxJava {

    private static String seed = "abc";

    public static void main(String[] args) {
        SignedTransaction res =
                new FileMockedRunner(infoFile, lastHeadersFile, boxFile).run((BlockchainContext ctx) -> {
                    ExampleScenarios r = new ExampleScenarios(ctx);
                    SignedTransaction signed = r.prepareBox(
                            mockTxId, (short)0, ConstantsBuilder.empty(),
                            "{ sigmaProp(CONTEXT.headers.size == 9) }", seed);
                    return signed;
                });

        System.out.println(res);
    }
}

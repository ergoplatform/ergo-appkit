package org.ergoplatform.example;

import com.google.common.base.Strings;
import org.ergoplatform.example.util.FileMockedErgoClient;
import org.ergoplatform.polyglot.*;

import static org.ergoplatform.example.MockData.*;

public class PrepareBoxJava {

    private static String seed = "abc";

    public static void main(String[] args) {
        String ergoScript =
                (args.length == 0 || Strings.isNullOrEmpty(args[0]))
                ? "{ sigmaProp(CONTEXT.headers.size == 9) }"
                : args[0];
        SignedTransaction res =
                new FileMockedErgoClient(infoFile, lastHeadersFile, boxFile).execute((BlockchainContext ctx) -> {
                    ExampleScenarios r = new ExampleScenarios(ctx);
                    SignedTransaction signed = r.prepareBox(
                            mockTxId, (short)0,
                            ConstantsBuilder.empty(), ergoScript,
                            seed);
                    return signed;
                });

        System.out.println(res);
    }
}

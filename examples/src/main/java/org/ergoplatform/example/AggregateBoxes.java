package org.ergoplatform.example;

import org.ergoplatform.example.util.RestApiErgoClient;
import org.ergoplatform.polyglot.ErgoClient;
import org.ergoplatform.polyglot.NetworkType;
import org.ergoplatform.polyglot.SignedTransaction;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.UnsignedWord;

public class AggregateBoxes {

    static String nodeUrl = "http://localhost:9052";
    static String seed = "abc";
    static int deadline = 10;
    static String[] boxesToSpend = new String[]{"83b94f2df7e97586a9fe8fe43fa84d252aa74ecee5fe0871f85a45663927cd9a"};

//    static String nodeUrl = "http://209.97.134.210:9052"; // testnet


    public static void main(String[] args) {
        SignedTransaction res = getSignedTransaction(boxesToSpend);
        System.out.println(res);
    }

    private static SignedTransaction getSignedTransaction(String[] boxesToSpend) {
        ErgoClient ergoClient = RestApiErgoClient.create(nodeUrl, NetworkType.TESTNET, "");
        return ergoClient.execute(ctx -> {
            ExampleScenarios scenarios = new ExampleScenarios(ctx);
            return scenarios.aggregateUtxoBoxes(seed, deadline, boxesToSpend);
        });
    }

    @CEntryPoint(name = "aggregateBoxes")
    public static void aggregateBoxes(
            IsolateThread thread, CCharPointer cBoxId, CCharPointer resBuffer, UnsignedWord bufferSize) {
        // Convert the C string to the target Java string.
        String boxId = CTypeConversion.toJavaString(cBoxId);

        SignedTransaction res = getSignedTransaction(new String[]{boxId});

        // put resulting string into provided buffer
        CTypeConversion.toCString(res.toString(), resBuffer, bufferSize);
    }
}
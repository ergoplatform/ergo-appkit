package org.ergoplatform.example;

import org.ergoplatform.ErgoAddressEncoder;
import org.ergoplatform.example.util.BlockchainRunner;
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
    static String[] boxesToSpend = new String[] {"83b94f2df7e97586a9fe8fe43fa84d252aa74ecee5fe0871f85a45663927cd9a"};

//    static String nodeUrl = "http://209.97.134.210:9052"; // testnet

    public static void main(String[] args) {
        ExampleScenarios r = new ExampleScenarios();
        BlockchainRunner runner = new BlockchainRunner(nodeUrl, ErgoAddressEncoder.TestnetNetworkPrefix());
        SignedTransaction res = runner.run(ctx -> r.aggregateUtxoBoxes(ctx, seed, deadline, boxesToSpend));
        System.out.println(res);
    }

    @CEntryPoint(name = "sign")
    public static void sign(
            IsolateThread thread, CCharPointer cBoxId, CCharPointer resBuffer, UnsignedWord bufferSize) {
        // Convert the C string to the target Java string.
        String boxId = CTypeConversion.toJavaString(cBoxId);

        ExampleScenarios r = new ExampleScenarios();
        BlockchainRunner runner = new BlockchainRunner(nodeUrl, ErgoAddressEncoder.TestnetNetworkPrefix());
        SignedTransaction res = runner.run(ctx -> r.aggregateUtxoBoxes(ctx, seed, deadline, boxId));

        // put resulting string into provided buffer
        CTypeConversion.toCString(res.toString(), resBuffer, bufferSize);
    }
}
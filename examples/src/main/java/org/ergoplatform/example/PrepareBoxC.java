package org.ergoplatform.example;

import org.ergoplatform.example.util.FileMockedErgoClient;
import org.ergoplatform.polyglot.ConstantsBuilder;
import org.ergoplatform.polyglot.JavaHelpers;
import org.ergoplatform.polyglot.SignedTransaction;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.UnsignedWord;

import static org.ergoplatform.example.MockData.*;

public class PrepareBoxC {

    private static String seed = "abc";

    @CEntryPoint(name = "hashBlake2b256")  // void hashBlake2b256(graal_isolatethread_t*, char*, char*, size_t);
    public static void hash(
            IsolateThread thread,
            CCharPointer cEncodedTx,
            CCharPointer resBuffer, UnsignedWord bufferSize) {
        String encodedTx = CTypeConversion.toJavaString(cEncodedTx);
        String txHash = JavaHelpers.hash(encodedTx);
        CTypeConversion.toCString(txHash, resBuffer, bufferSize);
    }

    @CEntryPoint(name = "prepareBox")  // void prepareBox(graal_isolatethread_t*, char*, char*, char*, size_t);
    public static void prepareBox(
            IsolateThread thread,
            CCharPointer cMockTxId,
            CCharPointer cErgoScript,
            CCharPointer resBuffer, UnsignedWord bufferSize) {
        // Convert the C strings to the target Java strings.
        String mockTxId = CTypeConversion.toJavaString(cMockTxId);
        String ergoScript = CTypeConversion.toJavaString(cErgoScript);

        SignedTransaction res =
                new FileMockedErgoClient(infoFile, lastHeadersFile, boxFile).execute(ctx -> {
                    ExampleScenarios r = new ExampleScenarios(ctx);
                    SignedTransaction signed = r.prepareBox(
                            mockTxId, (short)0, ConstantsBuilder.empty(),
                            ergoScript, seed);
                    return signed;
                });

        // put resulting string into provided buffer
        CTypeConversion.toCString(res.toString(), resBuffer, bufferSize);
    }

}

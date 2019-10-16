package org.ergoplatform.polyglot.ni;

import org.ergoplatform.ErgoBox;
import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.UnsignedErgoLikeTransaction;
import org.ergoplatform.polyglot.ErgoBoxBuilder;
import org.ergoplatform.polyglot.ErgoProver;
import org.ergoplatform.polyglot.ErgoProverBuilder;
import org.ergoplatform.polyglot.ErgoTransactionBuilder;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.UnsignedWord;

public class Prove {
    static String sign(String boxId) {
        ErgoTransactionBuilder txB = new ErgoTransactionBuilder();
        ErgoBoxBuilder boxB = new ErgoBoxBuilder();
        ErgoBox box = boxB.withValue(10).build();
        UnsignedErgoLikeTransaction tx = txB
                .withInputs(boxId)
                .withCandidates(box)
                .build();

        ErgoProverBuilder proverB = new ErgoProverBuilder();
        ErgoProver prover = proverB.withSeed("abc").build();
        ErgoLikeTransaction signed = prover.sign(tx);
        return signed.toString();
    }
    
    public static void main(String[] args) {
        String res = sign("bb3c8c41611a9e6d469ebbf8f13de43666b2a92d03e14895e066a21fe62910d7");
        System.out.println(res);
    }

    @CEntryPoint(name = "sign")
    public static void sign(IsolateThread thread, CCharPointer cBoxId, CCharPointer resBuffer, UnsignedWord bufferSize) {
        /* Convert the C string to the target Java string. */
        String boxId = CTypeConversion.toJavaString(cBoxId);
        String res = sign(boxId);
        CTypeConversion.toCString(res, resBuffer, bufferSize);
    }
}
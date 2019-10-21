package org.ergoplatform.polyglot.ni;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.UnsignedWord;
import kotlin.Unit;

public class Prove {

    public static void main(String[] args) {
        Runner r = new Runner();
        r.request("http://209.97.134.210:9052");
//        String res = r.sign("bb3c8c41611a9e6d469ebbf8f13de43666b2a92d03e14895e066a21fe62910d7");
//        System.out.println(res);
    }

    @CEntryPoint(name = "sign")
    public static void sign(IsolateThread thread, CCharPointer cBoxId, CCharPointer resBuffer, UnsignedWord bufferSize) {
        Runner r = new Runner();
        r.request("http://209.97.134.210:9052");

        /* Convert the C string to the target Java string. */
//        String boxId = CTypeConversion.toJavaString(cBoxId);
//        String res = r.sign(boxId);
//        CTypeConversion.toCString(res, resBuffer, bufferSize);
    }
}
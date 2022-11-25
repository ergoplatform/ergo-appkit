package org.ergoplatform.appkit;

import special.collection.Coll;
import org.ergoplatform.sdk.JavaHelpers;

/**
 * set of Scala/Java conversion helper methods that need to be written in Java
 */
public class ScalaHelpers {

    /**
     * converts Coll<Byte> type into byte[] Bytearray.
     * Directly calling Scala code is not possible due to compile error (Coll<Object> expected
     * although Coll[Byte] defined in Scala. We need a recast which introduces a compiler warning.
     * Instead of having this all over the project, we do this in a single place here.
     */
    public static byte[] collByteToByteArray(Coll<Byte> byteColl) {
        return JavaHelpers.collToByteArray((Coll) byteColl);
    }
}

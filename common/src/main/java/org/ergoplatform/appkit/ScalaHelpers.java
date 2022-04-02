package org.ergoplatform.appkit;

import scala.Byte;
import special.collection.Coll;

/**
 * set of Scala/Java conversion helper methods that need to be written in Java
 */
public class ScalaHelpers {

    /**
     * converts Coll<Byte> type into byte[] Bytearray
     */
    public static byte[] collByteToByteArray(Coll<Byte> byteColl) {
        return JavaHelpers$.MODULE$.collToByteArray((Coll) byteColl);
    }
}

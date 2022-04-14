package org.ergoplatform.appkit;

import static org.junit.Assert.*;

import org.junit.Test;

import java.math.BigInteger;

import special.collection.Coll;
import special.sigma.BigInt;

public class ErgoValueTest {

    @Test
    public void testTypeDeclarations() {
        ErgoValue<Integer> intErgoValue = ErgoValue.of(1);
        ErgoValue<Long> longErgoValue = ErgoValue.of(0L);
        ErgoValue<Byte> byteErgoValue = ErgoValue.of((byte) 1);
        ErgoValue<BigInt> bigIntErgoValue = ErgoValue.of(BigInteger.ZERO);
        ErgoValue<Coll<Byte>> byteArrayErgoValue = ErgoValue.of(new byte[] {0, 1, 2});

        int intValue = Iso.jintToInt().from(intErgoValue.getValue());
        long longValue = Iso.jlongToLong().from(longErgoValue.getValue());
        byte byteValue = Iso.jbyteToByte().from(byteErgoValue.getValue());
        BigInteger bigIntValue = bigIntErgoValue.getValue().value();
        byte byteFromCollValue = Iso.jbyteToByte().from(byteArrayErgoValue.getValue().apply(0));

        assertEquals(1, intValue);
    }
}

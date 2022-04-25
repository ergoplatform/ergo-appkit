package org.ergoplatform.appkit;

import static org.junit.Assert.*;

import org.junit.Test;

import java.math.BigInteger;

import special.collection.Coll;
import special.sigma.BigInt;

public class ErgoValueTest {

    @Test
    public void testTypeDeclarations() {
        ErgoValue<Boolean> booleanErgoValue = ErgoValue.of(true);
        ErgoValue<Integer> intErgoValue = ErgoValue.of(1);
        ErgoValue<Long> longErgoValue = ErgoValue.of(0L);
        ErgoValue<Byte> byteErgoValue = ErgoValue.of((byte) 1);
        ErgoValue<Short> shortErgoValue = ErgoValue.of((short) 1);
        ErgoValue<BigInt> bigIntErgoValue = ErgoValue.of(BigInteger.ZERO);
        ErgoValue<Coll<Byte>> byteArrayErgoValue = ErgoValue.of(new byte[] {0, 1, 2});

        BigInteger bigIntValue = bigIntErgoValue.getValue().value();
        byte byteFromCollValue = byteArrayErgoValue.getValue().apply(0);
        boolean booleanValue = booleanErgoValue.getValue();
        byte byteValue = byteErgoValue.getValue();
        short shortValue = shortErgoValue.getValue();
        int intValue = intErgoValue.getValue();
        long longValue = longErgoValue.getValue();

        assertEquals(1, intValue);
        assertEquals(0L, longValue);
        assertEquals(true, booleanValue);
        assertEquals(1, byteValue);
        assertEquals(1, shortValue);
        assertEquals(0, byteFromCollValue);
        assertEquals(BigInteger.ZERO, bigIntValue);
    }
}

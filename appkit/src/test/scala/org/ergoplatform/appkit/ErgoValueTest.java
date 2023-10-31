package org.ergoplatform.appkit;

import static org.junit.Assert.*;

import org.junit.Test;

import java.math.BigInteger;

import sigmastate.eval.CBigInt;
import sigma.Coll;
import sigma.BigInt;

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
        ErgoValue<Coll<Short>> shortArrayErgoValue = ErgoValue.of(new short[] {1, 2, 3});
        ErgoValue<Coll<Integer>> intArrayErgoValue = ErgoValue.of(new int[] {2, 3, 4});
        ErgoValue<Coll<Boolean>> boolArrayErgoValue = ErgoValue.of(new boolean[] {false, true});
        ErgoValue<Coll<Long>> longArrayErgoValue = ErgoValue.of(new long[] {3, 4, 5});

        BigInteger bigIntValue = ((CBigInt)bigIntErgoValue.getValue()).wrappedValue();
        boolean booleanFromCollValue = boolArrayErgoValue.getValue().apply(0);
        byte byteFromCollValue = byteArrayErgoValue.getValue().apply(0);
        short shortFromCollValue = shortArrayErgoValue.getValue().apply(0);
        int intFromCollValue = intArrayErgoValue.getValue().apply(0);
        long longFromCollValue = longArrayErgoValue.getValue().apply(0);
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
        assertFalse(booleanFromCollValue);
        assertEquals(0, byteFromCollValue);
        assertEquals(1, shortFromCollValue);
        assertEquals(2, intFromCollValue);
        assertEquals(3, longFromCollValue);
        assertEquals(BigInteger.ZERO, bigIntValue);
    }
}

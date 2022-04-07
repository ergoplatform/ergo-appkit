package org.ergoplatform.appkit;

import static org.junit.Assert.*;

import org.junit.Test;

import java.math.BigInteger;

import scala.Byte;
import scala.Int;
import scala.Long;
import special.collection.Coll;
import special.sigma.BigInt;

public class ErgoValueTest {

    @Test
    public void testTypeDeclarations() {
        ErgoValue<Int> intErgoValue = ErgoValue.of(1);
        ErgoValue<Long> longErgoValue = ErgoValue.of(0L);
        ErgoValue<Byte> byteErgoValue = ErgoValue.of((byte) 1);
        ErgoValue<BigInt> bigIntErgoValue = ErgoValue.of(BigInteger.ZERO);
        ErgoValue<Coll<Byte>> byteArrayErgoValue = ErgoValue.of(new byte[] {0, 1, 2});

        BigInteger bigIntValue = bigIntErgoValue.getValue().value();
        byte byteFromCollValue = byteArrayErgoValue.getValue().apply(0).toByte();
        byte byteValue = byteErgoValue.getValue().toByte();
        int intValue = intErgoValue.getValue().toInt();
        long longValue = longErgoValue.getValue().toLong();

        assertEquals(1, intValue);
    }
}

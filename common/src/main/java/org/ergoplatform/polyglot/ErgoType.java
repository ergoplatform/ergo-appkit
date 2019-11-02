package org.ergoplatform.polyglot;

import scala.Option;
import scala.Tuple2;
import scalan.RType;
import special.collection.Coll;
import special.sigma.AvlTree;
import special.sigma.BigInt;
import special.sigma.GroupElement;
import special.sigma.SigmaProp;

public class ErgoType<T> {
    private static ErgoType<Byte> _byte = new ErgoType<>(RType.ByteType());
    private static ErgoType<Short> _short = new ErgoType<>(RType.ShortType());
    private static ErgoType<Integer> _integer = new ErgoType<>(RType.IntType());
    private static ErgoType<Long> _long = new ErgoType<>(RType.LongType());
    private static ErgoType<BigInt> _bigInt = new ErgoType<>(JavaHelpers.BigIntRType());
    private static ErgoType<GroupElement> _groupElement = new ErgoType<>(JavaHelpers.GroupElementRType());
    private static ErgoType<SigmaProp> _sigmaProp = new ErgoType<>(JavaHelpers.SigmaPropRType());
    private static ErgoType<AvlTree> _avlTree = new ErgoType<>(JavaHelpers.AvlTreeRType());

    public RType<T> getRType() {
        return (RType<T>)_rtype;
    }

    private final RType<?> _rtype;

    ErgoType(RType<?> rtype) {
        _rtype = rtype;
    }

    static public ErgoType<Byte> byteType() { return _byte; }

    static public ErgoType<Short> shortType() { return _short; }

    static public ErgoType<Integer> integerType() { return _integer; }

    static public ErgoType<Long> longType() { return _long; }

    static public ErgoType<BigInt> bigIntType() { return _bigInt; }

    static public ErgoType<GroupElement> groupElementType() { return _groupElement; }

    static public ErgoType<SigmaProp> sigmaPropType() { return _sigmaProp; }

    static public ErgoType<AvlTree> avlTreeType() { return _avlTree; }

    static public <A, B> ErgoType<Tuple2<A, B>> pairType(ErgoType<A> tA, ErgoType<B> tB) {
        return new ErgoType<>(RType.pairRType(tA._rtype, tB._rtype));
    }

    static public <A> ErgoType<Coll<A>> collType(ErgoType<A> tItem) {
        return new ErgoType<>(JavaHelpers.collRType(tItem._rtype));
    }

    static public <A> ErgoType<Option<A>> optionType(ErgoType<A> tItem) {
        return new ErgoType<>(RType.optionRType(tItem._rtype));
    }
}

package org.ergoplatform.appkit;

import scala.Option;
import scala.Tuple2;
import scala.Unit;
import scalan.RType;
import special.collection.Coll;
import special.sigma.*;

import java.util.Objects;

/**
 * Runtime representation of ErgoScript types. ErgoType is a Java friendly
 * wrapper around {@link RType} type descriptor.
 */
public class ErgoType<T> {
    private static ErgoType<Byte> _byte = new ErgoType(RType.ByteType());
    private static ErgoType<Short> _short = new ErgoType(RType.ShortType());
    private static ErgoType<Integer> _integer = new ErgoType(RType.IntType());
    private static ErgoType<Long> _long = new ErgoType(RType.LongType());
    private static ErgoType<Boolean> _boolean = new ErgoType(RType.BooleanType());
    private static ErgoType<BigInt> _bigInt = new ErgoType(JavaHelpers.BigIntRType());
    private static ErgoType<Unit> _unit = new ErgoType(RType.UnitType());
    private static ErgoType<GroupElement> _groupElement = new ErgoType(JavaHelpers.GroupElementRType());
    private static ErgoType<special.sigma.SigmaProp> _sigmaProp = new ErgoType(JavaHelpers.SigmaPropRType());
    private static ErgoType<AvlTree> _avlTree = new ErgoType(JavaHelpers.AvlTreeRType());
    private static ErgoType<Box> _box = new ErgoType(JavaHelpers.BoxRType());
    private static ErgoType<Header> _header = new ErgoType(JavaHelpers.HeaderRType());
    private static ErgoType<special.sigma.PreHeader> _preHeader = new ErgoType(JavaHelpers.PreHeaderRType());

    public RType<T> getRType() {
        return _rtype;
    }

    private final RType<T> _rtype;

    /**
     * Use static methods to create instances.
     */
    ErgoType(RType<T> rtype) {
        _rtype = rtype;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(_rtype);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ErgoType<?>) && Objects.equals(_rtype, ((ErgoType<?>)obj)._rtype);
    }

    @Override
    public String toString() {
        return "ErgoType(" + _rtype.name() + ")";
    }

    static public ErgoType<Byte> byteType() { return _byte; }

    static public ErgoType<Short> shortType() { return _short; }

    static public ErgoType<Integer> integerType() { return _integer; }

    static public ErgoType<Long> longType() { return _long; }

    static public ErgoType<Boolean> booleanType() { return _boolean; }

    static public ErgoType<BigInt> bigIntType() { return _bigInt; }

    static public ErgoType<Unit> unitType() { return _unit; }

    static public ErgoType<GroupElement> groupElementType() { return _groupElement; }

    static public ErgoType<special.sigma.SigmaProp> sigmaPropType() { return _sigmaProp; }

    static public ErgoType<AvlTree> avlTreeType() { return _avlTree; }

    static public ErgoType<Box> boxType() { return _box; }

    static public ErgoType<Header> headerType() { return _header; }

    static public ErgoType<special.sigma.PreHeader> preHeaderType() { return _preHeader; }

    static public <A, B> ErgoType<Tuple2<A, B>> pairType(ErgoType<A> tA, ErgoType<B> tB) {
        return new ErgoType<>(RType.pairRType(tA._rtype, tB._rtype));
    }

    static public <A> ErgoType<Coll<A>> collType(ErgoType<A> tItem) {
        return new ErgoType<>(JavaHelpers.collRType(tItem._rtype));
    }

    static public <A> ErgoType<A> ofRType(RType<A> rtype) {
        return new ErgoType<>(rtype);
    }
}

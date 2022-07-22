package org.ergoplatform.appkit;

import org.bouncycastle.math.ec.ECPoint;
import org.ergoplatform.ErgoBox;

import java.math.BigInteger;
import java.util.Objects;

import scala.Tuple2;
import scorex.util.encode.Base16$;
import sigmastate.AvlTreeData;
import sigmastate.SType;
import sigmastate.Values;
import sigmastate.serialization.ValueSerializer;
import sigmastate.serialization.ValueSerializer$;
import special.collection.Coll;
import special.sigma.AvlTree;
import special.sigma.BigInt;
import special.sigma.Box;
import special.sigma.GroupElement;
import special.sigma.SigmaProp;

/**
 * This class is used to represent any valid value of ErgoScript language.
 * Any such value comes equipped with {@link ErgoType} descriptor.
 */
public class ErgoValue<T> {
    private final T _value;
    private final ErgoType<T> _type;

    ErgoValue(T value, ErgoType<T> type) {
        _value = value;
        _type = type;
    }

    public T getValue() {
        return _value;
    }

    public ErgoType<T> getType() {
        return _type;
    }

    /**
     * Encode this value as Base16 hex string.
     * 1) it transforms this value into {@link Values.ConstantNode} of sigma.
     * 2) it serializes the constant into byte array using {@link sigmastate.serialization.ConstantSerializer}
     * 3) the bytes are encoded using Base16 encoder into string
     * @return hex string of serialized bytes
     */
    public String toHex() {
        Values.EvaluatedValue<SType> c = Iso.isoErgoValueToSValue().to(this);
        byte[] bytes = ValueSerializer$.MODULE$.serialize(c);
        return Base16$.MODULE$.encode(bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_type, _value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ErgoValue) {
           ErgoValue<?> other = (ErgoValue<?>)obj;
           return Objects.equals(_type, other._type) && Objects.equals(_value, other._value);
        }
        else
          return false;
    }

    @Override
    public String toString() {
        return "ErgoValue(" + _value.toString() + ", " + _type.toString() + ")";
    }

    static public ErgoValue<Byte> of(byte value) {
        return new ErgoValue(Iso.jbyteToByte().to(Byte.valueOf(value)), ErgoType.byteType());
    }

    static public ErgoValue<Short> of(short value) {
        return new ErgoValue(Iso.jshortToShort().to(Short.valueOf(value)), ErgoType.shortType());
    }

    static public ErgoValue<Integer> of(int value) {
        return new ErgoValue(Iso.jintToInt().to(value), ErgoType.integerType());
    }

    static public ErgoValue<Long> of(long value) {
        return new ErgoValue(Iso.jlongToLong().to(Long.valueOf(value)), ErgoType.longType());
    }

    static public ErgoValue<Boolean> of(boolean value) {
        return new ErgoValue(Iso.jboolToBool().to(Boolean.valueOf(value)), ErgoType.booleanType());
    }

    static public ErgoValue<?> unit() {
        return JavaHelpers.UnitErgoVal();
    }

    static public ErgoValue<BigInt> of(BigInteger value) {
        return new ErgoValue<>(JavaHelpers.SigmaDsl().BigInt(value), ErgoType.bigIntType());
    }

    static public ErgoValue<GroupElement> of(ECPoint value) {
        return new ErgoValue<>(JavaHelpers.SigmaDsl().GroupElement(value), ErgoType.groupElementType());
    }

    static public ErgoValue<GroupElement> of(GroupElement ge) {
        return new ErgoValue<>(ge, ErgoType.groupElementType());
    }

    static public ErgoValue<special.sigma.SigmaProp> of(Values.SigmaBoolean value) {
        return new ErgoValue<>(JavaHelpers.SigmaDsl().SigmaProp(value), ErgoType.sigmaPropType());
    }

    static public ErgoValue<AvlTree> of(AvlTreeData value) {
        return new ErgoValue<>(JavaHelpers.SigmaDsl().avlTree(value), ErgoType.avlTreeType());
    }

    static public ErgoValue<Box> of(ErgoBox value) {
        return of(JavaHelpers.SigmaDsl().Box(value));
    }

    static public ErgoValue<Box> of(Box value) {
        return new ErgoValue<>(value, ErgoType.boxType());
    }

    static public ErgoValue<Coll<Byte>> of(byte[] arr) {
        Coll value = JavaHelpers.collFrom(arr);
        ErgoType<Coll<Byte>> type = ErgoType.collType(ErgoType.byteType());
        return new ErgoValue<Coll<Byte>>(value, type);
    }

    static public <A, B> ErgoValue<Tuple2<A, B>> pairOf(ErgoValue<A> val1, ErgoValue<B> val2) {
        return new ErgoValue<>(new Tuple2<>(val1.getValue(), val2.getValue()),
            ErgoType.pairType(val1.getType(), val2.getType()));
    }
    static public <T> ErgoValue<Coll<T>> of(T[] arr, ErgoType<T> tT) {
        Coll<T> value = JavaHelpers.SigmaDsl().Colls().fromArray(arr, tT.getRType());
        return new ErgoValue<>(value, ErgoType.collType(tT));
    }

    static public <T> ErgoValue<Coll<T>> of(Coll<T> coll, ErgoType<T> tT) {
        return new ErgoValue<>(coll, ErgoType.collType(tT));
    }

    static public <T> ErgoValue<T> of(T value, ErgoType<T> tT) {
        return new ErgoValue<>(value, tT);
    }

    /**
     * Creates ErgoValue from hex encoded serialized bytes of Constant values.
     * <p>
     * In order to create ErgoValue you need to provide both value instance and
     * ErgoType descriptor. This is similar to how values are represented in sigma
     * ConstantNode. Each ConstantNode also have value instance and `tpe: SType`
     * descriptor.
     * Thus having ConstantNode we can use `Iso.isoErgoValueToSValue.from` method of to
     * convert ConstantNode to ErgoValue.
     *
     * @param hex the string is obtained as hex encoding of serialized ConstantNode.
     *            (The bytes obtained by ConstantSerializer in sigma)
     * @return new deserialized ErgoValue instance
     */
    static public ErgoValue<?> fromHex(String hex) {
        byte[] bytes = JavaHelpers.decodeStringToBytes(hex);
        Values.EvaluatedValue<SType> c = (Values.EvaluatedValue<SType>)ValueSerializer.deserialize(bytes, 0);
        ErgoValue<?> res = Iso.isoErgoValueToSValue().from(c);
        return res;
    }
}

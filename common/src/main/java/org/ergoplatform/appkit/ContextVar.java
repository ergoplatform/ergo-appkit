package org.ergoplatform.appkit;

import org.bouncycastle.math.ec.ECPoint;
import sigmastate.AvlTreeData;
import sigmastate.Values;
import special.sigma.GroupElement;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Represents one context variable binding (id -> value), where
 * id is in range [0 .. Byte.MaxValue].
 * Can be attached to each input box of the unsigned transaction.
 * @see sigmastate.interpreter.ContextExtension
 */
public class ContextVar {
    /** Minimal valid id of a context variable. */
    public static final byte MIN_ID = 0;

    /** Maximal valid id of a context variable. */
    public static final byte MAX_ID = Byte.MAX_VALUE;

    private final byte _id;
    private final ErgoValue<?> _value;

    /**
     * Construct a new instance
     *
     * @param id    identifier of the variable in range [0 .. Byte.MaxValue].
     * @param value value of the variable
     * @see sigmastate.interpreter.ContextExtension
     * @see ErgoValue
     */
    public ContextVar(byte id, ErgoValue<?> value) {
        if (id < MIN_ID)
            throw new IllegalArgumentException(String.format(
                "Context variable id should be in range [0 .. $d]: %d", MAX_ID, id));
        _id = id;
        _value = value;
    }

    /** Returns the id of this variable. */
    public byte getId() { return _id; }

    /** Returns the value of this variable. */
    public ErgoValue<?> getValue() { return _value; }

    @Override
    public int hashCode() {
        return _value.hashCode() + _id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true; // same instance
        if (obj instanceof ContextVar) {
            ContextVar other = (ContextVar)obj;
            return _id == other._id && Objects.equals(_value, other._value);
        }
        return false;
    }

    static public ContextVar of(byte id, byte value) {
        return new ContextVar(id, ErgoValue.of(value));
    }

    static public ContextVar of(byte id, short value) {
        return new ContextVar(id, ErgoValue.of(value));
    }

    static public ContextVar of(byte id, int value) {
        return new ContextVar(id, ErgoValue.of(value));
    }

    static public ContextVar of(byte id, long value) {
        return new ContextVar(id, ErgoValue.of(value));
    }

    static public ContextVar of(byte id, BigInteger value) {
        return new ContextVar(id, ErgoValue.of(value));
    }

    static public ContextVar of(byte id, ECPoint value) {
        return new ContextVar(id, ErgoValue.of(value));
    }

    static public ContextVar of(byte id, GroupElement value) {
        return new ContextVar(id, ErgoValue.of(value));
    }

    static public ContextVar of(byte id, Values.SigmaBoolean value) {
        return new ContextVar(id, ErgoValue.of(value));
    }

    static public ContextVar of(byte id, AvlTreeData value) {
        return new ContextVar(id, ErgoValue.of(value));
    }

    static public ContextVar of(byte id, byte[] arr) {
        return new ContextVar(id, ErgoValue.of(arr));
    }

    static public <T> ContextVar of(byte id, ErgoValue<T> v) {
        return new ContextVar(id, v);
    }
}

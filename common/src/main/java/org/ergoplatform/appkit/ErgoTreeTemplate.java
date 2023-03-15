package org.ergoplatform.appkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import scala.collection.IndexedSeq;
import scala.collection.immutable.ArraySeq;
import scorex.util.encode.Base16;
import sigmastate.SType;
import sigmastate.Values;
import sigmastate.Values.Constant;
import sigmastate.serialization.ErgoTreeSerializer;

/**
 * Represents ErgoTree template, which is an ErgoTree instance with placeholders.
 * Each placeholder have index and type and can be substituted with a constant of
 * the appropriate type.
 */
public class ErgoTreeTemplate {

    private static int[] _noParameters = new int[0]; // immutable and shared by all instances
    private final Values.ErgoTree _tree;
    private final byte[] _templateBytes;
    private int[] _parameterPositions = _noParameters;

    private ErgoTreeTemplate(Values.ErgoTree tree) {
        _tree = tree;
        _templateBytes = JavaHelpers.ergoTreeTemplateBytes(_tree);
    }

    /**
     * Specifies which ErgoTree constants will be used as template parameters.
     * Which tree constants to be used as parameters depends on the contract and use case.
     *
     * @param positions zero-based indexes in `ErgoTree.constants` array which can be
     *                  substituted as parameters using
     *                  {@link ErgoTreeTemplate#applyParameters(ErgoValue[])} method.
     * @see sigmastate.Values.ErgoTree
     */
    public ErgoTreeTemplate withParameterPositions(int[] positions) {
        HashSet<Integer> integerHashSet = new HashSet<>(positions.length);
        for (int position : positions) {
            integerHashSet.add(position);
        }

        if (integerHashSet.size() != positions.length)
            throw new IllegalArgumentException("Duplicate positions: " +
                ArraySeq.unsafeWrapArray(positions).mkString("[", ",", "]"));

        for (int p : positions)
            if (!_tree.constants().isDefinedAt(p))
                throw new IllegalArgumentException("Invalid parameter position " + p);
        _parameterPositions = positions;
        return this;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ErgoTreeTemplate template = (ErgoTreeTemplate) o;
        return Arrays.equals(this.getBytes(), template.getBytes());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_templateBytes);
    }

    /**
     * Returns serialized bytes of this template.
     *
     * @return template bytes at the tail of the serialized ErgoTree (i.e. exclusing header and segregated
     * constants)
     */
    public byte[] getBytes() {
        return _templateBytes;
    }

    /**
     * Returns template bytes encoded as Base16 string.
     *
     * @see ErgoTreeTemplate#getBytes
     */
    public String getEncodedBytes() {
        return Base16.encode(getBytes());
    }

    public String getTemplateHashHex() {
        return Base16.encode(scorex.crypto.hash.Sha256.hash(_templateBytes));
    }

    /**
     * A number of parameters in this template.
     * In general, there may be more constants of ErgoTree then template parameters because
     * not every constant make sense as a parameter.
     */
    public int getParameterCount() {
        return _parameterPositions.length;
    }

    /**
     * Returns types of all template parameters (i.e. specified constants in the ErgoTree).
     */
    public List<ErgoType<?>> getParameterTypes() {
        List<ErgoType<?>> types = new ArrayList<>();
        IndexedSeq<Constant<SType>> constants = _tree.constants();
        for (int position : _parameterPositions) {
            SType tpe = constants.apply(position).tpe();
            types.add(Iso.isoErgoTypeToSType().from(tpe));
        }
        return types;
    }

    /**
     * @param paramIndex 0-based index of parameter in [0 .. getParameterCount()) range
     * @return ErgoValue of the given parameter
     */
    public ErgoValue<?> getParameterValue(int paramIndex) {
        Constant<SType> c = _tree.constants().apply(_parameterPositions[paramIndex]);
        return Iso.isoErgoValueToSValue().from(c);
    }

    /**
     * Creates a new ErgoTree with new values for all parameters of this template.
     *
     * <br>Require:
     * <pre>
     * newValues.length == getParameterCount() &&
     * forall i = 0; i < getParameterCount() => newValue[i].getType().equals(getParameterTypes()[i])
     * </pre>
     *
     * @param newValues new values for all parameters
     * @return new ErgoTree with the same template as this but with all it's parameters
     * replaced with `newValues`
     */
    public Values.ErgoTree applyParameters(ErgoValue<?>... newValues) {
        if (newValues.length != _parameterPositions.length)
            throw new IllegalArgumentException(
                "Wrong number of newValues. Expected " + _parameterPositions.length +
                    " but was " + newValues.length);
        return JavaHelpers.substituteErgoTreeConstants(_tree.bytes(), _parameterPositions, newValues);
    }

    public static ErgoTreeTemplate fromErgoTree(Values.ErgoTree tree) {
        return new ErgoTreeTemplate(tree);
    }

    public static ErgoTreeTemplate fromErgoTreeBytes(byte[] treeBytes) {
        Values.ErgoTree ergoTree =
            ErgoTreeSerializer.DefaultSerializer().deserializeErgoTree(treeBytes);
        return fromErgoTree(ergoTree);
    }

    // TODO public static ErgoTreeTemplate fromTemplateBytes(byte[] templateBytes)
}

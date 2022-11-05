package org.ergoplatform.appkit;

import java.util.Arrays;

import scorex.util.encode.Base16;
import sigmastate.Values;
import sigmastate.serialization.ErgoTreeSerializer;

/**
 * Represents ErgoTree template, which is an ErgoTree instance with placeholders.
 * Each placeholder have index and type and can be substituted with a constant of
 * the appropriate type.
 */
public class ErgoTreeTemplate {

    private final Values.ErgoTree _tree;
    private final byte[] _templateBytes;

    private ErgoTreeTemplate(Values.ErgoTree tree) {
        _tree = tree;
        _templateBytes = JavaHelpers.ergoTreeTemplateBytes(_tree);
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
     * A number of placeholders in the template, which can be substituted (aka parameters).
     * This is immutable property of a {@link ErgoTreeTemplate}, which counts all the constants in the
     * {@link sigmastate.Values.ErgoTree} which can be replaced by new values using
     * {@link ErgoTreeTemplate#applyParameters} method.
     * In general, constants of ErgoTree cannot be replaced, but every placeholder can.
     */
    public int getParameterCount() {
        return _tree.constants().length();
    }

    /**
     * @param index 0-based
     * @return value object of paramter
     */
    public ErgoValue<?> getParameter(int index) {
        return Iso.isoErgoValueToSValue().from(_tree.constants().apply(index + 1));
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
        int[] positions = new int[newValues.length];
        for (int position : positions) {
            positions[position] = position + 1;
        }

        return JavaHelpers.substituteErgoTreeConstants(_tree.bytes(), positions, newValues);
    }

    public static ErgoTreeTemplate fromErgoTree(Values.ErgoTree tree) {
        return new ErgoTreeTemplate(tree);
    }

    public static ErgoTreeTemplate fromErgoTreeBytes(byte[] treeBytes) {
        return fromErgoTree(ErgoTreeSerializer.DefaultSerializer().deserializeErgoTree(treeBytes));
    }

    // TODO public static ErgoTreeTemplate fromTemplateBytes(byte[] templateBytes)
}

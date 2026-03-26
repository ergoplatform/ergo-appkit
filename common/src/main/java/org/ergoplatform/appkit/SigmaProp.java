package org.ergoplatform.appkit;

import sigma.data.SigmaBoolean;
import sigma.ast.ErgoTree;
import org.ergoplatform.sdk.JavaHelpers;

/**
 * Proposition which can be proven and verified by sigma protocol.
 */
public class SigmaProp {
    private final sigma.data.SigmaBoolean sigmaBoolean;

    public SigmaProp(sigma.data.SigmaBoolean sigmaBoolean) {
        this.sigmaBoolean = sigmaBoolean;
    }

    public SigmaProp(sigma.SigmaProp sigmaProp) {
        this(JavaHelpers.SigmaDsl().toSigmaBoolean(sigmaProp));
    }

    public sigma.data.SigmaBoolean getSigmaBoolean() {
        return sigmaBoolean;
    }

    /**
     * Serializes this SigmaProp via ErgoTree container.
     */
    public byte[] toBytes() {
        ErgoTree tree = ErgoTree.fromSigmaBoolean(sigmaBoolean);
        return tree.bytes();
    }

    public Address toAddress(NetworkType networkType) {
        return Address.fromSigmaBoolean(sigmaBoolean, networkType);
    }

    /**
     * @return SigmaProp equal to the one that was serialized with {@link #toBytes()}
     */
    public static SigmaProp parseFromBytes(byte[] serializedBytes) {
        ErgoTree tree = ErgoTree.fromBytes(serializedBytes);
        SigmaBoolean sb = tree.toSigmaBooleanOpt().get();
        return new SigmaProp(sb);
    }

    /**
     * @return SigmaProp from Address. Note that only SigmaBoolean addresses can be used
     */
    public static SigmaProp createFromAddress(Address address) {
        return new SigmaProp(address.getSigmaBoolean());
    }
}

package org.ergoplatform.appkit;

import org.ergoplatform.sdk.Iso;
import org.ergoplatform.sdk.JavaHelpers;
import sigmastate.Values;

/**
 * Proposition which can be proven and verified by sigma protocol.
 */
public class SigmaProp {
    private final Values.SigmaBoolean sigmaBoolean;

    public SigmaProp(Values.SigmaBoolean sigmaBoolean) {
        this.sigmaBoolean = sigmaBoolean;
    }

    public SigmaProp(special.sigma.SigmaProp sigmaProp) {
        this(JavaHelpers.SigmaDsl().toSigmaBoolean(sigmaProp));
    }

    public Values.SigmaBoolean getSigmaBoolean() {
        return sigmaBoolean;
    }

    /**
     * Serializes this SigmaProp.
     */
    public byte[] toBytes() {
        return Iso.isoSigmaBooleanToByteArray().to(sigmaBoolean);
    }

    public Address toAddress(NetworkType networkType) {
        return Address.fromSigmaBoolean(sigmaBoolean, networkType);
    }

    /**
     * @return SigmaProp equal to the one that was serialized with {@link #toBytes()}
     */
    public static SigmaProp parseFromBytes(byte[] serializedBytes) {
        return new SigmaProp(Iso.isoSigmaBooleanToByteArray().from(serializedBytes));
    }

    /**
     * @return SigmaProp from Address. Note that only SigmaBoolean addresses can be used
     */
    public static SigmaProp createFromAddress(Address address) {
        return new SigmaProp(address.getSigmaBoolean());
    }
}

package org.ergoplatform.appkit;

import org.ergoplatform.sdk.SdkIsos;
import org.ergoplatform.sdk.JavaHelpers;
import sigma.data.SigmaBoolean;

/**
 * Proposition which can be proven and verified by sigma protocol.
 */
public class SigmaProp {
    private final SigmaBoolean sigmaBoolean;

    public SigmaProp(SigmaBoolean sigmaBoolean) {
        this.sigmaBoolean = sigmaBoolean;
    }

    public SigmaProp(sigma.SigmaProp sigmaProp) {
        this(JavaHelpers.SigmaDsl().toSigmaBoolean(sigmaProp));
    }

    public SigmaBoolean getSigmaBoolean() {
        return sigmaBoolean;
    }

    /**
     * Serializes this SigmaProp.
     */
    public byte[] toBytes() {
        return SdkIsos.isoSigmaBooleanToByteArray().to(sigmaBoolean);
    }

    public Address toAddress(NetworkType networkType) {
        return Address.fromSigmaBoolean(sigmaBoolean, networkType);
    }

    /**
     * @return SigmaProp equal to the one that was serialized with {@link #toBytes()}
     */
    public static SigmaProp parseFromBytes(byte[] serializedBytes) {
        return new SigmaProp(SdkIsos.isoSigmaBooleanToByteArray().from(serializedBytes));
    }

    /**
     * @return SigmaProp from Address. Note that only SigmaBoolean addresses can be used
     */
    public static SigmaProp createFromAddress(Address address) {
        return new SigmaProp(address.getSigmaBoolean());
    }
}

package org.ergoplatform.appkit;

import java.nio.charset.StandardCharsets;

import sigmastate.Values.SigmaBoolean;

/**
 * Helper utilities for EIP-28 ErgoAuth
 */
public class ErgoAuthUtils {

    /**
     * Serializes the given SigmaBoolean to be sent in ErgoAuthRequest.
     */
    public static byte[] serializeSigmaBoolean(SigmaBoolean sigmaBoolean) {
        return Iso.isoSigmaBooleanToByteArray().to(sigmaBoolean);
    }

    /**
     * Converts given address to SigmaBoolean and serializes it  to be sent in ErgoAuthRequest.
     */
    public static byte[] serializeSigmaBoolean(Address address) {
        return serializeSigmaBoolean(address.getPublicKey());
    }

    /**
     * Deserializes a SigmaBoolean from byte array
     */
    public static SigmaBoolean deserializeSigmaBoolean(byte[] serializedSigmaBoolean) {
        return Iso.isoSigmaBooleanToByteArray().from(serializedSigmaBoolean);
    }

    /**
     * Verifies an ErgoAuthResponse
     *
     * @param sigmaBoolean    the SigmaBoolean needed to be fulfilled for signing the message
     * @param originalMessage the original message sent in ErgoAuthRequest, needs to be contained
     *                        in signedMessage
     * @param signedMessage   the message signed by client
     * @param signature       signature for signedMessage
     * @return whether verification is successful
     */
    public static boolean verifyResponse(SigmaBoolean sigmaBoolean, String originalMessage,
                                         String signedMessage, byte[] signature) {

        if (!signedMessage.contains(originalMessage))
            return false;

        return Signature.verifySignature(sigmaBoolean,
            signedMessage.getBytes(StandardCharsets.UTF_8),
            signature);
    }

    /**
     * Verifies an ErgoAuthResponnse for a given address.
     * See {@link #verifyResponse(SigmaBoolean, String, String, byte[])} for more information.
     */
    public static boolean verifyResponse(Address address, String originalMessage,
                                         String signedMessage, byte[] signature) {
        return verifyResponse(address.getPublicKey(), originalMessage, signedMessage, signature);
    }
}

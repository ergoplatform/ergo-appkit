package org.ergoplatform.appkit;

import java.nio.charset.StandardCharsets;

/**
 * Helper utilities for EIP-28 ErgoAuth
 */
public class ErgoAuthUtils {

    /**
     * Verifies an ErgoAuthResponse
     *
     * @param sigmaProp       the Sigma proposition needed to be fulfilled for signing the message
     * @param originalMessage the original message sent in ErgoAuthRequest, needs to be contained
     *                        in signedMessage
     * @param signedMessage   the message signed by client
     * @param signature       signature for signedMessage
     * @return whether verification is successful
     */
    public static boolean verifyResponse(SigmaProp sigmaProp, String originalMessage,
                                         String signedMessage, byte[] signature) {

        if (!signedMessage.contains(originalMessage))
            return false;

        return Signature.verifySignature(sigmaProp,
            signedMessage.getBytes(StandardCharsets.UTF_8),
            signature);
    }
}

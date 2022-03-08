package org.ergoplatform.appkit;

import org.ergoplatform.ErgoAddress;
import org.ergoplatform.ErgoAddressEncoder;

/**
 * Enumeration of network types as they are defined by Ergo specification of {@link ErgoAddress}.
 */
public enum NetworkType {
    /**
     * Mainnet network type.
     * @see ErgoAddressEncoder#MainnetNetworkPrefix()
     */
    MAINNET(ErgoAddressEncoder.MainnetNetworkPrefix(), "mainnet"),

    /**
     * Testnet network type.
     * @see ErgoAddressEncoder#TestnetNetworkPrefix()
     */
    TESTNET(ErgoAddressEncoder.TestnetNetworkPrefix(), "testnet");

    /**
     * The network prefix code used in Ergo addresses
     */
    public final byte networkPrefix;

    /**
     * verbose name for network type as reported by Node API
     */
    public final String verboseName;

    NetworkType(byte networkPrefix, String verboseName) {
        this.networkPrefix = networkPrefix;
        this.verboseName = verboseName;
    }

    public static NetworkType fromValue(String text) {
        for (NetworkType t : NetworkType.values()) {
            if (t.verboseName.equals(text)) {
                return t;
            }
        }
        return null;
    }
}

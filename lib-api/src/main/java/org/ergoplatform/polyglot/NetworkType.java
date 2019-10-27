package org.ergoplatform.polyglot;

import org.ergoplatform.ErgoAddressEncoder;

public enum NetworkType {
    MAINNET(ErgoAddressEncoder.MainnetNetworkPrefix()),
    TESTNET(ErgoAddressEncoder.TestnetNetworkPrefix());

    public final byte networkPrefix;

    NetworkType(byte networkPrefix) {
        this.networkPrefix = networkPrefix;
    }
}

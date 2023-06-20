package org.ergoplatform.appkit;

// TODO remove when org.ergoplatform.wallet.interface4j.SecretString is removed in ergo-wallet
public final class SecretStringConverter {
    public static org.ergoplatform.wallet.interface4j.SecretString toInterface4JSecretString(
        org.ergoplatform.sdk.SecretString sdkStr) {
        return org.ergoplatform.wallet.interface4j.SecretString.create(sdkStr.getData().clone());
    }
}

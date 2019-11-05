package org.ergoplatform.ergotool;

import org.ergoplatform.polyglot.NetworkType;

public class ErgoNodeConfig {
  private ApiConfig nodeApi;
  private WalletConfig wallet;
  private NetworkType networkType;

  public ApiConfig getNodeApi() {
    return nodeApi;
  }

  public WalletConfig getWallet() {
    return wallet;
  }

  public NetworkType getNetworkType() {
    return networkType;
  }
}

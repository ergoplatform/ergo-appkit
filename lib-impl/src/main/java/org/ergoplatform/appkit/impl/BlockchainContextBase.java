package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.BlockchainContext;
import org.ergoplatform.appkit.Constants;
import org.ergoplatform.appkit.ErgoContract;
import org.ergoplatform.appkit.NetworkType;
import org.ergoplatform.restapi.client.ApiClient;
import org.ergoplatform.restapi.client.NodeInfo;
import sigmastate.Values;

public abstract class BlockchainContextBase implements BlockchainContext {
    protected final NetworkType _networkType;

    public BlockchainContextBase(NetworkType networkType) {
        _networkType = networkType;
    }

    @Override
    public ErgoContract newContract(Values.ErgoTree ergoTree) {
        return new ErgoTreeContract(ergoTree);
    }

    @Override
    public ErgoContract compileContract(Constants constants, String ergoScript) {
        return ErgoScriptContract.create(constants, ergoScript, _networkType);
    }

    @Override
    public NetworkType getNetworkType() {
        return _networkType;
    }

    abstract ApiClient getApiClient();

    public abstract NodeInfo getNodeInfo();
}

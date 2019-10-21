package org.ergoplatform.polyglot;

import okhttp3.OkHttpClient;
import org.ergoplatform.api.client.ApiClient;
import org.ergoplatform.api.client.NodeInfo;
import retrofit2.Retrofit;

public class BlockchainContextBuilderImpl implements BlockchainContextBuilder {
    private final ApiClient _client;
    private final byte _networkPrefix;
    private OkHttpClient _ok;
    private Retrofit _retrofit;
    private NodeInfo _nodeInfo;

    public BlockchainContextBuilderImpl(ApiClient client, byte networkPrefix) {
        _client = client;
        _networkPrefix = networkPrefix;
    }

    @Override
    public BlockchainContext build() throws ErgoClientException {
        _ok = _client.getOkBuilder().build();
        _retrofit = _client.getAdapterBuilder()
                .client(_ok)
                .build();
        _nodeInfo  = ErgoNodeFacade.getNodeInfo(_retrofit);
        return new BlockchainContextImpl(_networkPrefix, _nodeInfo);
    }


}

package org.ergoplatform.polyglot;

import okhttp3.OkHttpClient;
import org.ergoplatform.api.client.ApiClient;
import org.ergoplatform.api.client.BlockHeader;
import org.ergoplatform.api.client.NodeInfo;
import retrofit2.Retrofit;
import scala.math.Ordering;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class BlockchainContextBuilderImpl implements BlockchainContextBuilder {
    private final ApiClient _client;
    private final byte _networkPrefix;
    private OkHttpClient _ok;
    private Retrofit _retrofit;
    private NodeInfo _nodeInfo;
    private List<BlockHeader> _headers;

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
        _headers  = ErgoNodeFacade.getLastHeaders(_retrofit, BigDecimal.valueOf(LastHeadersInContext));
        Collections.reverse(_headers);
        return new BlockchainContextImpl(_retrofit, _networkPrefix, _nodeInfo, _headers);
    }


}

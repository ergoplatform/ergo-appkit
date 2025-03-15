package org.ergoplatform.appkit;

import org.ergoplatform.appkit.impl.ColdBlockchainContext;
import org.ergoplatform.appkit.impl.NodeInfoParameters;
import org.ergoplatform.restapi.client.NodeInfo;
import org.ergoplatform.restapi.client.Parameters;

import java.util.function.Function;

public class ColdErgoClient implements ErgoClient {

    private final NetworkType networkType;
    private final BlockchainParameters params;

    public ColdErgoClient(NetworkType networkType, BlockchainParameters params) {
        this.networkType = networkType;
        this.params = params;
    }

    /**
     * Convenience constructor for giving maxBlockCost
     */
    public ColdErgoClient(NetworkType networkType, int maxBlockCost, byte blockVersion) {
        this(networkType, new NodeInfoParameters(
            new NodeInfo()
                .parameters(new Parameters()
                    .maxBlockCost(maxBlockCost)
                    .blockVersion((int) blockVersion))
        ));
    }

    public BlockchainParameters params() {
        return params;
    }

    @Override
    public <T> T execute(Function<BlockchainContext, T> action) {
        ColdBlockchainContext ctx = new ColdBlockchainContext(networkType, params);
        return action.apply(ctx);
    }

    @Override
    public BlockchainDataSource getDataSource() {
        throw new UnsupportedOperationException();
    }
}

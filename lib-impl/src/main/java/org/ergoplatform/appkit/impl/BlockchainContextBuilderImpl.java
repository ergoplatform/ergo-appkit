package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.BlockchainContext;
import org.ergoplatform.appkit.BlockchainContextBuilder;
import org.ergoplatform.appkit.BlockchainDataSource;
import org.ergoplatform.appkit.ErgoClientException;
import org.ergoplatform.appkit.NetworkType;
import org.ergoplatform.appkit.BlockHeader;
import org.ergoplatform.appkit.BlockchainParameters;

import java.util.Collections;
import java.util.List;

public class BlockchainContextBuilderImpl implements BlockchainContextBuilder {
    private final BlockchainDataSource _dataSource;
    private final NetworkType _networkType;

    public BlockchainContextBuilderImpl(BlockchainDataSource dataSource, NetworkType networkType) {
        _dataSource = dataSource;
        _networkType = networkType;
    }

    @Override
    public BlockchainContext build() throws ErgoClientException {

        BlockchainParameters blockchainParameters = _dataSource.getParameters();
        List<BlockHeader> _headers = _dataSource.getLastBlockHeaders(NUM_LAST_HEADERS);
        Collections.reverse(_headers);

        return new BlockchainContextImpl(_dataSource, _networkType, blockchainParameters, (List<BlockHeaderImpl>) (List)_headers);
    }


}

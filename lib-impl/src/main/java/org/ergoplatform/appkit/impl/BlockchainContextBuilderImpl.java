package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.BlockchainContext;
import org.ergoplatform.appkit.BlockchainContextBuilder;
import org.ergoplatform.appkit.BlockchainDataSource;
import org.ergoplatform.appkit.ErgoClientException;
import org.ergoplatform.appkit.NetworkType;

public class BlockchainContextBuilderImpl implements BlockchainContextBuilder {
    private final BlockchainDataSource _dataSource;
    private final NetworkType _networkType;

    public BlockchainContextBuilderImpl(BlockchainDataSource dataSource, NetworkType networkType) {
        _dataSource = dataSource;
        _networkType = networkType;
    }

    @Override
    public BlockchainContext build() throws ErgoClientException {

        return new BlockchainContextImpl(_dataSource, _networkType);
    }


}

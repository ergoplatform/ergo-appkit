package org.ergoplatform.appkit.impl;

import com.google.gson.Gson;

import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.BlockchainDataSource;
import org.ergoplatform.appkit.BoxOperations;
import org.ergoplatform.appkit.CoveringBoxes;
import org.ergoplatform.appkit.ErgoClientException;
import org.ergoplatform.appkit.ErgoProverBuilder;
import org.ergoplatform.appkit.ErgoToken;
import org.ergoplatform.appkit.ErgoWallet;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.appkit.NetworkType;
import org.ergoplatform.appkit.PreHeaderBuilder;
import org.ergoplatform.appkit.SignedTransaction;
import org.ergoplatform.appkit.UnsignedTransactionBuilder;
import org.ergoplatform.appkit.BlockchainParameters;
import org.ergoplatform.restapi.client.ErgoTransaction;
import org.ergoplatform.restapi.client.JSON;

import java.util.ArrayList;
import java.util.List;

public class BlockchainContextImpl extends BlockchainContextBase {
    private final BlockchainDataSource _dataSource;
    final PreHeaderImpl _preHeader;
    final BlockchainParameters _blockchainParameters;
    private final List<BlockHeaderImpl> _headers;
    private ErgoWalletImpl _wallet;

    public BlockchainContextImpl(
            BlockchainDataSource dataSource,
            NetworkType networkType,
            BlockchainParameters blockchainParameters,
            List<BlockHeaderImpl> headers) {
        super(networkType);

        if (blockchainParameters.getNetworkType() != networkType) {
            throw new IllegalArgumentException("Network type of NodeInfo does not match given networkType - "
                + blockchainParameters.getNetworkType() + "/" + networkType.verboseName);
        }
        _dataSource = dataSource;
        _blockchainParameters = blockchainParameters;
        _headers = headers;
        _preHeader = headers.get(0);
    }

    @Override
    public PreHeaderBuilder createPreHeader() {
        return new PreHeaderBuilderImpl(this);
    }

    @Override
    public SignedTransaction signedTxFromJson(String json) {
        Gson gson = JSON.createGson().create();
        ErgoTransaction txData = gson.fromJson(json, ErgoTransaction.class);
        ErgoLikeTransaction tx = ScalaBridge.isoErgoTransaction().to(txData);
        return new SignedTransactionImpl(this, tx, 0);
    }

    @Override
    public UnsignedTransactionBuilder newTxBuilder() {
        return new UnsignedTransactionBuilderImpl(this);
    }

    @Override
    public BlockchainDataSource getDataSource() {
        return _dataSource;
    }

    @Override
    public InputBox[] getBoxesById(String... boxIds) throws ErgoClientException {
        List<InputBox> list = new ArrayList<>();
        for (String id : boxIds) {
            list.add(_dataSource.getBoxById(id));
        }
        return list.toArray(new InputBox[0]);
    }

    @Override
    public ErgoProverBuilder newProverBuilder() {
        return new ErgoProverBuilderImpl(this);
    }

    @Override
    public int getHeight() { return _headers.get(0).getHeight(); }

    /*=====  Package-private methods accessible from other Impl classes. =====*/

    @Override
    public BlockchainParameters getParameters() {
        return _blockchainParameters;
    }

    public org.ergoplatform.appkit.PreHeader getPreHeader() {
        return _preHeader;
    }

    public List<BlockHeaderImpl> getHeaders() {
        return _headers;
    }

    @Override
    public String sendTransaction(SignedTransaction tx) {
        return _dataSource.sendTransaction(tx);
    }

    @Override
    public ErgoWallet getWallet() {
        if (_wallet == null) {
            List<InputBox> unspentBoxes = _dataSource.getWalletUnspentBoxes(0, 0);
            _wallet = new ErgoWalletImpl(unspentBoxes);
            _wallet.setContext(this);
        }
        return _wallet;
    }

    @Override
    public List<InputBox> getUnspentBoxesFor(Address address, int offset, int limit) {
        return _dataSource.getUnspentBoxesFor(address, offset, limit);
    }

    @Override
    public CoveringBoxes getCoveringBoxesFor(Address address, long amountToSpend, List<ErgoToken> tokensToSpend) {
        return BoxOperations.getCoveringBoxesFor(amountToSpend, tokensToSpend, false,
            page -> _dataSource.getUnspentBoxesFor(address, page * DEFAULT_LIMIT_FOR_API, DEFAULT_LIMIT_FOR_API));
    }
}


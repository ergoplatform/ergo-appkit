package org.ergoplatform.appkit.impl;

import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.BlockHeader;
import org.ergoplatform.appkit.BlockchainDataSource;
import org.ergoplatform.appkit.BlockchainParameters;
import org.ergoplatform.appkit.ErgoClientException;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.sdk.SdkIsos;
import org.ergoplatform.appkit.OutBox;
import org.ergoplatform.appkit.SignedTransaction;
import org.ergoplatform.appkit.Transaction;
import org.ergoplatform.restapi.client.ApiClient;
import org.ergoplatform.restapi.client.BlockchainApi;
import org.ergoplatform.restapi.client.BlocksApi;
import org.ergoplatform.restapi.client.ErgoTransaction;
import org.ergoplatform.restapi.client.ErgoTransactionDataInput;
import org.ergoplatform.restapi.client.ErgoTransactionInput;
import org.ergoplatform.restapi.client.ErgoTransactionOutput;
import org.ergoplatform.restapi.client.InfoApi;
import org.ergoplatform.restapi.client.NodeInfo;
import org.ergoplatform.restapi.client.Transactions;
import org.ergoplatform.restapi.client.TransactionsApi;
import org.ergoplatform.restapi.client.UtxoApi;
import org.ergoplatform.restapi.client.WalletApi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * BlockchainDataSource implementation using Node API only. Not all methods can be used when
 * blockchain methods are disabled. Consider using {@link NodeAndExplorerDataSourceImpl} instead.
 */
public class NodeDataSourceImpl implements BlockchainDataSource {
    // Node
    private final InfoApi nodeInfoApi;
    private final BlocksApi nodeBlocksApi;
    private final UtxoApi nodeUtxoApi;
    private final TransactionsApi nodeTransactionsApi;
    private final WalletApi nodeWalletApi;
    private final BlockchainApi blockchainApi;

    private Boolean blockchainApiEnabled = null;

    // cached to avoid multiple fetches, these values won't change while this class lives
    private BlockchainParameters blockchainParameters;

    /**
     * set to true to make {@link #sendTransaction(SignedTransaction)} call node's checkTransaction
     * endpoint before actually sending the transaction
     */
    public boolean performCheckBeforeSend = false;

    public NodeDataSourceImpl(ApiClient nodeClient) {

        OkHttpClient ok = nodeClient.getOkBuilder().build();
        Retrofit nodeRetrofit = nodeClient.getAdapterBuilder()
            .client(ok)
            .build();

        nodeInfoApi = nodeRetrofit.create(InfoApi.class);
        nodeBlocksApi = nodeRetrofit.create(BlocksApi.class);
        nodeUtxoApi = nodeRetrofit.create(UtxoApi.class);
        nodeTransactionsApi = nodeRetrofit.create(TransactionsApi.class);
        nodeWalletApi = nodeRetrofit.create(WalletApi.class);
        blockchainApi = nodeRetrofit.create(BlockchainApi.class);
    }

    @Override
    public BlockchainParameters getParameters() {
        if (blockchainParameters == null) {
            // getNodeInfo will load and set the parameters
            getNodeInfo();
        }
        return blockchainParameters;
    }

    protected boolean isBlockchainApiEnabled() {
        if (blockchainApiEnabled == null)
            getNodeInfo();

        return (blockchainApiEnabled != null) ? blockchainApiEnabled : false;
    }

    private NodeInfo getNodeInfo() {
        NodeInfo nodeInfo = executeCall(nodeInfoApi.getNodeInfo());
        // cache the parameters while we have them
        blockchainParameters = new NodeInfoParameters(nodeInfo);
        blockchainApiEnabled = nodeInfo.isExplorer();
        return nodeInfo;
    }

    @Override
    public List<BlockHeader> getLastBlockHeaders(int count, boolean onlyFullHeaders) {
        List<org.ergoplatform.restapi.client.BlockHeader> headers;
        if (onlyFullHeaders) {
            NodeInfo nodeInfo = getNodeInfo();
            int fullHeight = nodeInfo.getFullHeight();
            int headersHeight = nodeInfo.getHeadersHeight();
            int additionalCount = Math.max(0, headersHeight - fullHeight);

            List<org.ergoplatform.restapi.client.BlockHeader> headersFromNode =
                executeCall(nodeBlocksApi.getLastHeaders(BigDecimal.valueOf(count + additionalCount)));

            headers = new ArrayList<>();

            // only add the block headers that match the full height
            for (org.ergoplatform.restapi.client.BlockHeader blockHeader : headersFromNode) {
                if (blockHeader.getHeight() <= fullHeight) {
                    headers.add(blockHeader);
                }
            }

            if (headers.isEmpty()) {
                throw new IllegalStateException("onlyFullHeaders set, but all returned headers are not within range.");
            }

        } else {
            headers = executeCall(nodeBlocksApi.getLastHeaders(BigDecimal.valueOf(count)));
        }

        Collections.reverse(headers);
        List<BlockHeader> retVal = new ArrayList<>(headers.size());
        for (org.ergoplatform.restapi.client.BlockHeader header : headers) {
            retVal.add(BlockHeaderImpl.createFromRestApi(header));
        }

        return retVal;
    }

    @Override
    public InputBox getBoxById(String boxId, boolean findInPool, boolean findInSpent) {
        if (findInSpent && !findInPool) {
            return getBoxByIdIncludingSpent(boxId);
        } else if (!findInSpent) {
            return getUnspentBoxByIdNode(boxId, findInPool);
        } else {
            // find in spent and find in pool => try unspent first and fall back to spent
            try {
                return getUnspentBoxByIdNode(boxId, findInPool);
            } catch (Throwable t) {
                return getBoxByIdIncludingSpent(boxId);
            }
        }
    }

    protected InputBox getUnspentBoxByIdNode(String boxId, boolean findInPool) {
        ErgoTransactionOutput boxData = (findInPool) ? executeCall(nodeUtxoApi.getBoxWithPoolById(boxId))
            : executeCall(nodeUtxoApi.getBoxById(boxId));
        return new InputBoxImpl(boxData);
    }

    protected InputBox getBoxByIdIncludingSpent(String boxId) {
        return getBoxByIdNode(boxId);
    }

    public InputBox getBoxByIdNode(String boxId) {
        if (!isBlockchainApiEnabled()) {
            throw new UnsupportedOperationException("Request needs node with enabled blockchain API.");
        }

        ErgoTransactionOutput boxData = executeCall(blockchainApi.getBoxById(boxId));
        return new InputBoxImpl(boxData);
    }

    @Override
    public String sendTransaction(SignedTransaction tx) {
        ErgoLikeTransaction ergoTx = ((SignedTransactionImpl) tx).getTx();
        List<ErgoTransactionDataInput> dataInputsData =
            SdkIsos.JListToIndexedSeq(ScalaBridge.isoErgoTransactionDataInput()).from(ergoTx.dataInputs());
        List<ErgoTransactionInput> inputsData =
            SdkIsos.JListToIndexedSeq(ScalaBridge.isoErgoTransactionInput()).from(ergoTx.inputs());
        List<ErgoTransactionOutput> outputsData =
            SdkIsos.JListToIndexedSeq(ScalaBridge.isoErgoTransactionOutput()).from(ergoTx.outputs());
        ErgoTransaction txData = new ErgoTransaction()
            .id(ergoTx.id())
            .dataInputs(dataInputsData)
            .inputs(inputsData)
            .outputs(outputsData);

        if (performCheckBeforeSend) {
            String txId = executeCall(nodeTransactionsApi.checkTransaction(txData)).replace("\"", "");
            if (!txData.getId().equals(txId)) {
                throw new IllegalStateException("checkTransaction returned tx id " + txId +
                    ", expected was " + txData.getId());
            }
        }

        return executeCall(nodeTransactionsApi.sendTransaction(txData));
    }

    @Override
    public List<InputBox> getUnspentBoxesFor(Address address, int offset, int limit) {
        return getUnspentBoxesNodeApi(address, offset, limit);
    }

    public List<InputBox> getUnspentBoxesNodeApi(Address address, int offset, int limit) {
        if (isBlockchainApiEnabled()) {
            List<ErgoTransactionOutput> ergoTransactionOutput =
                executeCall(blockchainApi.getUnspentBoxesByAddress(address.toString(), limit, offset, BlockchainApi.sortDirectionOldestFirst));
            List<InputBox> inputs = new ArrayList<>(ergoTransactionOutput.size());
            for (ErgoTransactionOutput transactionOutput : ergoTransactionOutput) {
                inputs.add(new InputBoxImpl(transactionOutput));
            }
            return inputs;
        } else {
            throw new UnsupportedOperationException("Request needs node with enabled blockchain API.");
        }
    }

    @Override
    public List<InputBox> getUnconfirmedUnspentBoxesFor(Address address, int offset, int limit) {
        Transactions poolTransactions = executeCall(nodeTransactionsApi.getUnconfirmedTransactions(limit, offset));

        // first prepare a list of box ids that are spent so that we don't return output boxes that
        // are already spent in chained transactions
        List<String> spentBoxIds = new ArrayList<>();
        for (ErgoTransaction poolTransaction : poolTransactions) {
            for (ErgoTransactionInput input : poolTransaction.getInputs()) {
                spentBoxIds.add(input.getBoxId());
            }
        }

        String ergoTree = address.toErgoContract().getErgoTree().bytesHex();
        List<InputBox> inputs = new ArrayList<>();

        // now check the output boxes
        for (ErgoTransaction poolTransaction : poolTransactions) {
            for (ErgoTransactionOutput output : poolTransaction.getOutputs()) {
                if (output.getErgoTree().equals(ergoTree) && !spentBoxIds.contains(output.getBoxId()))
                    inputs.add(new InputBoxImpl(output));
            }
        }

        return inputs;
    }

    @Override
    public List<Transaction> getUnconfirmedTransactions(int offset, int limit) {
        Transactions mempoolTx = executeCall(getNodeTransactionsApi().getUnconfirmedTransactions(limit, offset));
        List<Transaction> returned = new ArrayList<>(mempoolTx.size());
        for (ErgoTransaction tx : mempoolTx) {
            returned.add(new MempoolTransaction(tx));
        }
        return returned;
    }

    protected <T> T executeCall(Call<T> apiCall) throws ErgoClientException {
        try {
            Response<T> response = apiCall.execute();

            if (!response.isSuccessful()) {
                throw new ErgoClientException(response.code() + ": " +
                    (response.errorBody() != null ? response.errorBody().string() : "Server returned error"), null);
            } else {
                return response.body();
            }
        } catch (Exception e) {
            throw new ErgoClientException(
                String.format("Error executing API request to %s: %s", apiCall.request().url(), e.getMessage()), e);
        }
    }

    // getters for the API clients - all public so that clients can utilize them for own calls
    public InfoApi getNodeInfoApi() {
        return nodeInfoApi;
    }

    public BlocksApi getNodeBlocksApi() {
        return nodeBlocksApi;
    }

    public UtxoApi getNodeUtxoApi() {
        return nodeUtxoApi;
    }

    public TransactionsApi getNodeTransactionsApi() {
        return nodeTransactionsApi;
    }

    public WalletApi getNodeWalletApi() {
        return nodeWalletApi;
    }

    private static class MempoolTransaction implements Transaction {
        private final ErgoTransaction tx;

        public MempoolTransaction(ErgoTransaction tx) {
            this.tx = tx;
        }

        @Override
        public String getId() {
            return tx.getId();
        }

        @Override
        public List<String> getInputBoxesIds() {
            List<String> returnVal = new ArrayList<>(tx.getInputs().size());
            for (ErgoTransactionInput input : tx.getInputs()) {
                returnVal.add(input.getBoxId());
            }
            return returnVal;
        }

        @Override
        public List<OutBox> getOutputs() {
            List<OutBox> returnVal = new ArrayList<>(tx.getOutputs().size());
            for (ErgoTransactionOutput output : tx.getOutputs()) {
                returnVal.add(new OutBoxImpl(ScalaBridge.isoErgoTransactionOutput().to(output)));
            }
            return returnVal;
        }
    }
}

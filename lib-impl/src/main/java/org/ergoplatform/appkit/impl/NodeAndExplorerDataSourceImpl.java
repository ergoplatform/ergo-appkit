package org.ergoplatform.appkit.impl;

import com.google.common.base.Preconditions;

import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.BlockHeader;
import org.ergoplatform.appkit.BlockchainDataSource;
import org.ergoplatform.appkit.BlockchainParameters;
import org.ergoplatform.appkit.ErgoClient;
import org.ergoplatform.appkit.ErgoClientException;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.appkit.Iso;
import org.ergoplatform.appkit.SignedTransaction;
import org.ergoplatform.explorer.client.DefaultApi;
import org.ergoplatform.explorer.client.ExplorerApiClient;
import org.ergoplatform.explorer.client.model.OutputInfo;
import org.ergoplatform.restapi.client.ApiClient;
import org.ergoplatform.restapi.client.BlocksApi;
import org.ergoplatform.restapi.client.ErgoTransaction;
import org.ergoplatform.restapi.client.ErgoTransactionDataInput;
import org.ergoplatform.restapi.client.ErgoTransactionInput;
import org.ergoplatform.restapi.client.ErgoTransactionOutput;
import org.ergoplatform.restapi.client.InfoApi;
import org.ergoplatform.restapi.client.NodeInfo;
import org.ergoplatform.restapi.client.TransactionsApi;
import org.ergoplatform.restapi.client.UtxoApi;
import org.ergoplatform.restapi.client.WalletApi;
import org.ergoplatform.restapi.client.WalletBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jline.internal.Nullable;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * BlockchainDataSource implementation using Node API and Explorer API. Node API is preferred,
 * Explorer is optional to use. Not all methods can be used without Explorer set up.
 */
public class NodeAndExplorerDataSourceImpl implements BlockchainDataSource {
    // Node
    private final InfoApi nodeInfoApi;
    private final BlocksApi nodeBlocksApi;
    private final UtxoApi nodeUtxoApi;
    private final TransactionsApi nodeTransactionsApi;
    private final WalletApi nodeWalletApi;

    // Explorer
    private final DefaultApi explorerApi;

    public NodeAndExplorerDataSourceImpl(ApiClient nodeClient, @Nullable ExplorerApiClient explorerClient) {

        OkHttpClient _ok = nodeClient.getOkBuilder().build();
        Retrofit nodeRetrofit = nodeClient.getAdapterBuilder()
            .client(_ok)
            .build();

        nodeInfoApi = nodeRetrofit.create(InfoApi.class);
        nodeBlocksApi = nodeRetrofit.create(BlocksApi.class);
        nodeUtxoApi = nodeRetrofit.create(UtxoApi.class);
        nodeTransactionsApi = nodeRetrofit.create(TransactionsApi.class);
        nodeWalletApi = nodeRetrofit.create(WalletApi.class);

        if (explorerClient != null) {
            OkHttpClient okExplorer = explorerClient.getOkBuilder().build();
            Retrofit _retrofitExplorer = explorerClient.getAdapterBuilder()
                .client(okExplorer)
                .build();
            explorerApi = _retrofitExplorer.create(DefaultApi.class);
        } else
            explorerApi = null;

    }

    @Override
    public BlockchainParameters getParameters() {
        NodeInfo nodeInfo = executeCall(nodeInfoApi.getNodeInfo());
        return new NodeInfoParameters(nodeInfo);
    }

    @Override
    public List<BlockHeader> getLastBlockHeaders(int count) {
        List<org.ergoplatform.restapi.client.BlockHeader> headers = executeCall(nodeBlocksApi.getLastHeaders(BigDecimal.valueOf(count)));
        Collections.reverse(headers);
        List<BlockHeader> retVal = new ArrayList<>(headers.size());
        for (org.ergoplatform.restapi.client.BlockHeader header : headers) {
            retVal.add(BlockHeaderImpl.createFromRestApi(header));
        }

        return retVal;
    }

    @Override
    public InputBox getBoxById(String boxId) {
        ErgoTransactionOutput boxData = executeCall(nodeUtxoApi.getBoxById(boxId));
        return new InputBoxImpl(boxData);
    }

    @Override
    public String sendTransaction(SignedTransaction tx) {
        ErgoLikeTransaction ergoTx = ((SignedTransactionImpl) tx).getTx();
        List<ErgoTransactionDataInput> dataInputsData =
            Iso.JListToIndexedSeq(ScalaBridge.isoErgoTransactionDataInput()).from(ergoTx.dataInputs());
        List<ErgoTransactionInput> inputsData =
            Iso.JListToIndexedSeq(ScalaBridge.isoErgoTransactionInput()).from(ergoTx.inputs());
        List<ErgoTransactionOutput> outputsData =
            Iso.JListToIndexedSeq(ScalaBridge.isoErgoTransactionOutput()).from(ergoTx.outputs());
        ErgoTransaction txData = new ErgoTransaction()
            .id(ergoTx.id())
            .dataInputs(dataInputsData)
            .inputs(inputsData)
            .outputs(outputsData);

        return executeCall(nodeTransactionsApi.sendTransaction(txData));
    }

    @Override
    public List<InputBox> getUnspentBoxesFor(Address address, int offset, int limit) {
        Preconditions.checkNotNull(explorerApi, ErgoClient.explorerUrlNotSpecifiedMessage);
        List<OutputInfo> boxes = executeCall(explorerApi.getApiV1BoxesUnspentByaddressP1(address.toString(), offset, limit, "asc")).getItems();
        return getInputBoxes(boxes);
    }

    private List<InputBox> getInputBoxes(List<OutputInfo> boxes) {
        ArrayList<InputBox> returnList = new ArrayList<>(boxes.size());

        for (OutputInfo box : boxes) {
            String boxId = box.getBoxId();
            try {
                InputBox boxInfo = getBoxById(boxId);
                // can be null if node does not know about the box (yet)
                // instead of throwing an error, we continue with the boxes actually known
                if (boxInfo != null) {
                    returnList.add(boxInfo);
                }
            } catch (ErgoClientException ignored) {
                // as stated above, we ignore exceptions getting the box information
            }
        }

        return returnList;
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

    public DefaultApi getExplorerApi() {
        return explorerApi;
    }
}

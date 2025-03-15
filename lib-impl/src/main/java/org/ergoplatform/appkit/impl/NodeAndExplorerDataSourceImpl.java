package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.ErgoClient;
import org.ergoplatform.appkit.ErgoClientException;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.explorer.client.DefaultApi;
import org.ergoplatform.explorer.client.ExplorerApiClient;
import org.ergoplatform.explorer.client.model.OutputInfo;
import org.ergoplatform.explorer.client.model.TransactionInfo;
import org.ergoplatform.restapi.client.ApiClient;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * BlockchainDataSource implementation using Node API and Explorer API. Node API is preferred,
 * Explorer is optional to use. Not all methods can be used without Explorer set up when Node's
 * blockchain API is disabled.
 */
public class NodeAndExplorerDataSourceImpl extends NodeDataSourceImpl {
    // Explorer
    private final DefaultApi explorerApi;

    public NodeAndExplorerDataSourceImpl(ApiClient nodeClient, ExplorerApiClient explorerClient) {
        super(nodeClient);

        if (explorerClient == null) {
            throw new IllegalArgumentException("For node-only use, use NodeDataSourceImpl");
        }

        OkHttpClient okExplorer = explorerClient.getOkBuilder().build();
        Retrofit retrofitExplorer = explorerClient.getAdapterBuilder()
            .client(okExplorer)
            .build();
        explorerApi = retrofitExplorer.create(DefaultApi.class);
    }

    @Override
    protected InputBox getBoxByIdIncludingSpent(String boxId) {
        if (isBlockchainApiEnabled())
            return getBoxByIdNode(boxId);
        else
            return getBoxByIdExplorer(boxId);
    }

    public InputBox getBoxByIdExplorer(String boxId) {
        OutputInfo outputInfo = executeCall(explorerApi.getApiV1BoxesP1(boxId));
        return new InputBoxImpl(outputInfo);
    }

    @Override
    public List<InputBox> getUnspentBoxesFor(Address address, int offset, int limit) {
        if (isBlockchainApiEnabled())
            return getUnspentBoxesNodeApi(address, offset, limit);
        else
            return getUnspentBoxesForExplorer(address, offset, limit);
    }

    public List<InputBox> getUnspentBoxesForExplorer(Address address, int offset, int limit) {
        List<OutputInfo> boxes = executeCall(explorerApi.getApiV1BoxesUnspentByaddressP1(address.toString(), offset, limit, "asc")).getItems();
        return getInputBoxes(boxes);
    }

    private List<InputBox> getInputBoxes(List<OutputInfo> boxes) {
        ArrayList<InputBox> returnList = new ArrayList<>(boxes.size());

        for (OutputInfo box : boxes) {
            String boxId = box.getBoxId();
            try {
                InputBox boxInfo = getBoxById(boxId, false, false);
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

    public DefaultApi getExplorerApi() {
        return explorerApi;
    }
}

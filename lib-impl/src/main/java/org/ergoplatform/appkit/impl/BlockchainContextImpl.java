package org.ergoplatform.appkit.impl;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.appkit.*;
import org.ergoplatform.explorer.client.ExplorerApiClient;
import org.ergoplatform.explorer.client.model.OutputInfo;
import org.ergoplatform.restapi.client.*;
import retrofit2.Retrofit;
import special.sigma.Header;

import java.util.ArrayList;
import java.util.List;

public class BlockchainContextImpl extends BlockchainContextBase {
    private final ApiClient _client;
    private final Retrofit _retrofit;
    final PreHeaderImpl _preHeader;
    private ExplorerApiClient _explorer;
    private Retrofit _retrofitExplorer;
    private final NodeInfo _nodeInfo;
    private final List<BlockHeader> _headers;
    private ErgoWalletImpl _wallet;

    public BlockchainContextImpl(
            ApiClient client, Retrofit retrofit,
            ExplorerApiClient explorer, Retrofit retrofitExplorer,
            NetworkType networkType,
            NodeInfo nodeInfo, List<BlockHeader> headers) {
        super(networkType);
        _client = client;
        _retrofit = retrofit;
        _explorer = explorer;
        _retrofitExplorer = retrofitExplorer;
        _nodeInfo = nodeInfo;
        _headers = headers;
        Header h = ScalaBridge.isoBlockHeader().to(_headers.get(0));
        _preHeader = new PreHeaderImpl(JavaHelpers.toPreHeader(h));
    }

    @Override
    public PreHeaderBuilder createPreHeader() {
        return new PreHeaderBuilderImpl(this);
    }

    @Override
    public SignedTransaction signedTxFromJson(String json) {
        Gson gson = getApiClient().getGson();
        ErgoTransaction txData = gson.fromJson(json, ErgoTransaction.class);
        ErgoLikeTransaction tx = ScalaBridge.isoErgoTransaction().to(txData);
        return new SignedTransactionImpl(this, tx, 0);
    }

    @Override
    public UnsignedTransactionBuilder newTxBuilder() {
        return new UnsignedTransactionBuilderImpl(this);
    }

    @Override
    public InputBox[] getBoxesById(String... boxIds) throws ErgoClientException {
        List<InputBox> list = new ArrayList<>();
        for (String id : boxIds) {
            ErgoTransactionOutput boxData = ErgoNodeFacade.getBoxById(_retrofit, id);
            if (boxData == null) {
                throw new ErgoClientException("Cannot load UTXO box " + id, null);
            }
            list.add(new InputBoxImpl(this, boxData));
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

    Retrofit getRetrofit() {
        return _retrofit;
    }

    @Override
    ApiClient getApiClient() {
        return _client;
    }

    /**
     * This method should be private. No classes of HTTP client should ever leak into interfaces.
     */
    private List<InputBox> getInputBoxes(List<OutputInfo> boxes) {
        ArrayList<InputBox> returnList = new ArrayList<>(boxes.size());

        for (OutputInfo box : boxes) {
            String boxId = box.getBoxId();
            ErgoTransactionOutput boxInfo = ErgoNodeFacade.getBoxById(_retrofit, boxId);
            // can be null if node does not know about the box (yet)
            // instead of throwing an error, we continue with the boxes actually known
            if (boxInfo != null) {
                returnList.add(new InputBoxImpl(this, boxInfo));
            }
        }

        return returnList;
    }

    @Override
    public NodeInfo getNodeInfo() {
        return _nodeInfo;
    }

    public org.ergoplatform.appkit.PreHeader getPreHeader() {
        return _preHeader;
    }

    public List<BlockHeader> getHeaders() {
        return _headers;
    }

    @Override
    public String sendTransaction(SignedTransaction tx) {
        ErgoLikeTransaction ergoTx = ((SignedTransactionImpl)tx).getTx();
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
        return ErgoNodeFacade.sendTransaction(_retrofit, txData);
    }

    @Override
    public ErgoWallet getWallet() {
        if (_wallet == null) {
            List<WalletBox> unspentBoxes = ErgoNodeFacade.getWalletUnspentBoxes(_retrofit, 0, 0);
            _wallet = new ErgoWalletImpl(unspentBoxes);
            _wallet.setContext(this);
        }
        return _wallet;
    }

    @Override
    public List<InputBox> getUnspentBoxesFor(Address address, int offset, int limit) {
        Preconditions.checkNotNull(_retrofitExplorer, ErgoClient.explorerUrlNotSpecifiedMessage);
        List<OutputInfo> boxes = ExplorerFacade
                .transactionsBoxesByAddressUnspentIdGet(
                    _retrofitExplorer, address.toString(), offset, limit);
        return getInputBoxes(boxes);
    }

    @Override
    public CoveringBoxes getCoveringBoxesFor(Address address, long amountToSpend, List<ErgoToken> tokensToSpend) {
        SelectTokensHelper tokensRemaining = new SelectTokensHelper(tokensToSpend);
        Preconditions.checkArgument(amountToSpend > 0 ||
            !tokensRemaining.areTokensCovered(), "amountToSpend or tokens to spend should be > 0");
        ArrayList<InputBox> selectedCoveringBoxes = new ArrayList<>();
        long remainingAmountToCover = amountToSpend;
        int offset = 0;
        while (true) {
            List<InputBox> chunk = getUnspentBoxesFor(address, offset, DEFAULT_LIMIT_FOR_API);
            for (InputBox boxCandidate : chunk) {
                // on rare occasions, chunk can include entries that we already had received on a
                // previous chunk page. We make sure we don't add any duplicate entries.
                if (!isAlreadyAdded(selectedCoveringBoxes, boxCandidate)) {
                    boolean usefulTokens = tokensRemaining.foundNewTokens(boxCandidate.getTokens());
                    if (usefulTokens || remainingAmountToCover > 0) {
                        selectedCoveringBoxes.add(boxCandidate);
                        remainingAmountToCover -= boxCandidate.getValue();
                    }
                    if (remainingAmountToCover <= 0 && tokensRemaining.areTokensCovered())
                        return new CoveringBoxes(amountToSpend, selectedCoveringBoxes);
                }
            }
            // this chunk is not enough, go to the next (if any)
            if (chunk.size() == 0) {
                // this was the last chunk, but still remain to collect
                assert remainingAmountToCover > 0 || !tokensRemaining.areTokensCovered();
                // cannot satisfy the request, but still return cb, with cb.isCovered == false
                return new CoveringBoxes(amountToSpend, selectedCoveringBoxes);
            }
            // step to next chunk
            offset += DEFAULT_LIMIT_FOR_API;
        }
    }

    /**
     * @return true when boxCandidate is already added to selectedBoxes list
     */
    private boolean isAlreadyAdded(ArrayList<InputBox> selectedBoxes, InputBox boxCandidate) {
        boolean alreadyAdded = false;
        for (InputBox coveringBox : selectedBoxes) {
            if (coveringBox.getId().equals(boxCandidate.getId())) {
                alreadyAdded = true;
                break;
            }
        }
        return alreadyAdded;
    }
}


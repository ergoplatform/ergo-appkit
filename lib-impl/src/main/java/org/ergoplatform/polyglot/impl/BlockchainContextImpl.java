package org.ergoplatform.polyglot.impl;

import org.ergoplatform.DataInput;
import org.ergoplatform.ErgoBox;
import org.ergoplatform.ErgoLikeTransaction;
import org.ergoplatform.Input;
import org.ergoplatform.polyglot.*;
import org.ergoplatform.restapi.client.*;
import org.ergoplatform.settings.ErgoAlgos;
import retrofit2.Retrofit;
import scala.Tuple2;
import sigmastate.Values;
import sigmastate.interpreter.ContextExtension;
import sigmastate.interpreter.ProverResult;

import java.util.ArrayList;
import java.util.List;

public class BlockchainContextImpl implements BlockchainContext {

    private final ApiClient _client;
    private final Retrofit _retrofit;
    private final NetworkType _networkType;
    private final NodeInfo _nodeInfo;
    private final List<BlockHeader> _headers;
    private ErgoWalletImpl _wallet;

    public BlockchainContextImpl(
            ApiClient client, Retrofit retrofit, NetworkType networkType,
            NodeInfo nodeInfo, List<BlockHeader> headers,
            ErgoWalletImpl wallet) {
        _client = client;
        _retrofit = retrofit;
        _networkType = networkType;
        _nodeInfo = nodeInfo;
        _headers = headers;
        _wallet = wallet;
        _wallet.setContext(this);
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
        InputBox[] inputs = list.toArray(new InputBox[0]);
        return inputs;
    }

    @Override
    public ErgoProverBuilder newProverBuilder() {
        return new ErgoProverBuilderImpl(this);
    }

    @Override
    public NetworkType getNetworkType() {
        return _networkType;
    }

    @Override
    public int getHeight() { return _headers.get(0).getHeight(); }

    /*=====  Package-private methods accessible from other Impl classes. =====*/

    Retrofit getRetrofit() {
        return _retrofit;
    }

    ApiClient getApiClient() {
        return _client;
    }

    public NodeInfo getNodeInfo() {
        return _nodeInfo;
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
        String txId = ErgoNodeFacade.sendTransaction(_retrofit, txData);
        return txId;
    }

    @Override
    public ErgoWallet getWallet() {
        return _wallet;
    }

    @Override
    public ErgoContract newContract(Values.ErgoTree ergoTree) {
        return new ErgoTreeContract(ergoTree);
    }

    @Override
    public ErgoContract compileContract(Constants constants, String ergoScript) {
        return ErgoScriptContract.create(constants, ergoScript, _networkType);
    }
}


package org.ergoplatform.appkit.impl;

import org.ergoplatform.ApiTestBase;
import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.BlockHeader;
import org.ergoplatform.appkit.BlockchainDataSource;
import org.ergoplatform.appkit.CoveringBoxes;
import org.ergoplatform.appkit.ErgoToken;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.appkit.NetworkType;
import org.ergoplatform.appkit.Parameters;
import org.ergoplatform.appkit.SignedTransaction;
import org.ergoplatform.appkit.BlockchainParameters;
import org.ergoplatform.appkit.Transaction;
import org.ergoplatform.restapi.client.ErgoTransactionOutput;
import org.ergoplatform.restapi.client.PowSolutions;
import org.ergoplatform.restapi.client.Registers;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockchainContextImplTest extends ApiTestBase {
    @Test
    public void getCoveringBoxesForTest() {
        MockedBlockChainContextImpl bci = new MockedBlockChainContextImpl();

        CoveringBoxes coveringBoxesFor = bci.getCoveringBoxesFor(Address.create(address), Parameters.OneErg, new ArrayList<>());
        Assert.assertFalse(coveringBoxesFor.isCovered());

        // add a covering box and try again
        InputBoxImpl box = new InputBoxImpl(getMockBox(Parameters.OneErg));
        bci.ds.unspentBoxesMock.add(box);
        coveringBoxesFor = bci.getCoveringBoxesFor(Address.create(address), Parameters.OneErg, new ArrayList<>());
        Assert.assertTrue(coveringBoxesFor.isCovered());
        coveringBoxesFor = bci.getCoveringBoxesFor(Address.create(address), 2 * Parameters.OneErg, new ArrayList<>());
        Assert.assertFalse(coveringBoxesFor.isCovered());

        // add the box again - it should be ignored (isAlreadyAdded() kicks in)
        bci.ds.unspentBoxesMock.add(box);
        coveringBoxesFor = bci.getCoveringBoxesFor(Address.create(address), 2 * Parameters.OneErg, new ArrayList<>());
        Assert.assertFalse(coveringBoxesFor.isCovered());
    }

    private ErgoTransactionOutput getMockBox(long nanoErgs) {
        ErgoTransactionOutput output = new ErgoTransactionOutput();
        output.boxId(boxId).ergoTree(ergoTree).assets(new ArrayList<>())
            .additionalRegisters(new Registers()).index(10).value(nanoErgs).creationHeight(667);
        return output;
    }

    private List<BlockHeaderImpl> getMockHeader() {
        ArrayList<BlockHeaderImpl> headers = new ArrayList<>();
        PowSolutions powSolutions = new PowSolutions();
        powSolutions.setPk("03224c2f2388ae0741be2c50727caa49bd62654dc1f36ee72392b187b78da2c717");
        powSolutions.w("0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
        powSolutions.n("20d68047ea27a031");
        powSolutions.d(BigInteger.ZERO);
        BlockHeaderImpl firstHeader = BlockHeaderImpl.createFromRestApi(
            new org.ergoplatform.restapi.client.BlockHeader().height(667).nBits(19857408L).difficulty(BigInteger.TEN)
            .id(blockId).parentId(blockId).adProofsRoot(blockId).stateRoot(blockId).transactionsRoot(blockId)
            .version(2).extensionHash(blockId).powSolutions(powSolutions)
            .timestamp(0L)
            .votes("0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798"));
        headers.add(firstHeader);
        return headers;
    }

    class MockedBlockChainContextImpl extends BlockchainContextImpl {
        final MockedDataSource ds;

        public MockedBlockChainContextImpl() {
            super(new MockedDataSource(), NetworkType.MAINNET);
            ds = (MockedDataSource) getDataSource();
        }
    }

    static class MockedDataSource implements BlockchainDataSource {
        List<InputBox> unspentBoxesMock = new ArrayList<>();

        @Override
        public List<InputBox> getUnspentBoxesFor(Address address, int offset, int limit) {
            if (offset >= limit)
                return Collections.emptyList();
            else
                return unspentBoxesMock;
        }

        @Override
        public List<InputBox> getUnspentBoxesFor(ErgoToken token, int offset, int limit) {
            return null;
        }

        @Override
        public BlockchainParameters getParameters() {
            return new MockedParameters();
        }

        @Override
        public List<BlockHeader> getLastBlockHeaders(int count) {
            return null;
        }

        @Override
        public InputBox getBoxById(String boxId) {
            return null;
        }

        @Override
        public List<InputBox> getUnconfirmedUnspentBoxesFor(Address address, int offset, int limit) {
            return null;
        }

        @Override
        public InputBox getBoxByIdWithMemPool(String boxId) {
            return null;
        }

        @Override
        public List<Transaction> getUnconfirmedTransactions(int offset, int limit) {
            return null;
        }

        @Override
        public String sendTransaction(SignedTransaction tx) {
            return null;
        }
    }

    static class MockedParameters implements BlockchainParameters {
        @Override
        public NetworkType getNetworkType() {
            return NetworkType.MAINNET;
        }

        @Override
        public int getStorageFeeFactor() {
            return 0;
        }

        @Override
        public int getMinValuePerByte() {
            return 0;
        }

        @Override
        public int getMaxBlockSize() {
            return 0;
        }

        @Override
        public int getTokenAccessCost() {
            return 0;
        }

        @Override
        public int getInputCost() {
            return 0;
        }

        @Override
        public int getDataInputCost() {
            return 0;
        }

        @Override
        public int getOutputCost() {
            return 0;
        }

        @Override
        public long getMaxBlockCost() {
            return 0;
        }

        @Override
        public byte getBlockVersion() {
            return 0;
        }
    }
}

package org.ergoplatform.appkit.impl;

import org.ergoplatform.ApiTestBase;
import org.ergoplatform.appkit.Address;
import org.ergoplatform.appkit.CoveringBoxes;
import org.ergoplatform.appkit.InputBox;
import org.ergoplatform.appkit.NetworkType;
import org.ergoplatform.appkit.Parameters;
import org.ergoplatform.restapi.client.BlockHeader;
import org.ergoplatform.restapi.client.ErgoTransactionOutput;
import org.ergoplatform.restapi.client.PowSolutions;
import org.ergoplatform.restapi.client.Registers;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BlockchainContextImplTest extends ApiTestBase {
    @Test
    public void getCoveringBoxesForTest() {
        MockedBlockChainContextImpl bci = new MockedBlockChainContextImpl();

        CoveringBoxes coveringBoxesFor = bci.getCoveringBoxesFor(Address.create(address), Parameters.OneErg, new ArrayList<>());
        Assert.assertFalse(coveringBoxesFor.isCovered());

        // add a covering box and try again
        InputBoxImpl box = new InputBoxImpl(bci, getMockBox(Parameters.OneErg));
        bci.unspentBoxesMock.add(box);
        coveringBoxesFor = bci.getCoveringBoxesFor(Address.create(address), Parameters.OneErg, new ArrayList<>());
        Assert.assertTrue(coveringBoxesFor.isCovered());
        coveringBoxesFor = bci.getCoveringBoxesFor(Address.create(address), 2 * Parameters.OneErg, new ArrayList<>());
        Assert.assertFalse(coveringBoxesFor.isCovered());

        // add the box again - it should be ignored (isAlreadyAdded() kicks in)
        bci.unspentBoxesMock.add(box);
        coveringBoxesFor = bci.getCoveringBoxesFor(Address.create(address), 2 * Parameters.OneErg, new ArrayList<>());
        Assert.assertFalse(coveringBoxesFor.isCovered());
    }

    private ErgoTransactionOutput getMockBox(long nanoErgs) {
        ErgoTransactionOutput output = new ErgoTransactionOutput();
        output.boxId(boxId).ergoTree(ergoTree).assets(new ArrayList<>()).additionalRegisters(new Registers()).index(10).value(nanoErgs);
        return output;
    }

    private List<BlockHeader> getMockHeader() {
        ArrayList<BlockHeader> headers = new ArrayList<>();
        PowSolutions powSolutions = new PowSolutions();
        powSolutions.setPk("03224c2f2388ae0741be2c50727caa49bd62654dc1f36ee72392b187b78da2c717");
        powSolutions.w("0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
        powSolutions.n("20d68047ea27a031");
        powSolutions.d(BigInteger.ZERO);
        BlockHeader firstHeader = new BlockHeader().height(667).nBits(19857408L).difficulty(BigInteger.TEN)
            .id(blockId).parentId(blockId).adProofsRoot(blockId).stateRoot(blockId).transactionsRoot(blockId)
            .extensionHash(blockId).powSolutions(powSolutions).votes("0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
        headers.add(firstHeader);
        return headers;
    }

    class MockedBlockChainContextImpl extends BlockchainContextImpl {
        List<InputBox> unspentBoxesMock = new ArrayList<>();

        public MockedBlockChainContextImpl() {
            super(null, null, null, null, NetworkType.MAINNET, null, getMockHeader());
        }

        @Override
        public List<InputBox> getUnspentBoxesFor(Address address, int offset, int limit) {
            return unspentBoxesMock;
        }
    }
}

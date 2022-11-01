package org.ergoplatform.appkit.babelfee;

import org.ergoplatform.appkit.ErgoContract;
import org.ergoplatform.appkit.ErgoId;
import org.ergoplatform.appkit.NetworkType;
import org.ergoplatform.appkit.impl.ErgoTreeContract;

import java.nio.ByteBuffer;
import java.util.Arrays;

import scorex.util.encode.Base16;
import sigmastate.Values;
import sigmastate.serialization.ErgoTreeSerializer;

public class BabelFeeBoxContract {
    static final String contractTemplateHexPref = "100604000e20";
    static final String contractTemplateHexSuf = "0400040005000500d803d601e30004d602e4c6a70408d603e4c6a7050595e67201d804d604b2a5e4720100d605b2db63087204730000d606db6308a7d60799c1a7c17204d1968302019683050193c27204c2a7938c720501730193e4c672040408720293e4c672040505720393e4c67204060ec5a796830201929c998c7205029591b1720673028cb272067303000273047203720792720773057202";

    private static byte[] contractPref;
    private static byte[] contractSuf;

    private static void prepare() {
        if (contractPref == null || contractSuf == null) {
            contractPref = Base16.decode(contractTemplateHexPref).get();
            contractSuf = Base16.decode(contractTemplateHexSuf).get();
        }
    }

    public ErgoContract getContractForToken(ErgoId tokenId, NetworkType networkType) {
        prepare();

        byte[] idBytes = tokenId.getBytes();

        byte[] completeContract = ByteBuffer.allocate(contractPref.length + contractSuf.length + idBytes.length)
            .put(contractPref)
            .put(idBytes)
            .put(contractSuf)
            .array();

        return new ErgoTreeContract(ErgoTreeSerializer.DefaultSerializer().deserializeErgoTree(completeContract), networkType);
    }

    public ErgoId getTokenIdFromErgoTree(Values.ErgoTree ergoTree) {
        prepare();

        byte[] treeBytes = ergoTree.bytes();

        ByteBuffer bb = ByteBuffer.wrap(treeBytes);

        byte[] thisPref = new byte[contractPref.length];
        byte[] thisSuf = new byte[contractSuf.length];
        byte[] tokenId = new byte[treeBytes.length - contractPref.length - contractSuf.length];
        bb.get(thisPref, 0, thisPref.length);
        bb.get(tokenId, 0, tokenId.length);
        bb.get(thisSuf, 0, thisSuf.length);

        if (!Arrays.equals(thisPref, contractPref) || !Arrays.equals(thisSuf, contractSuf))
            throw new IllegalArgumentException("Contract does not fit template");

        return new ErgoId(tokenId);
    }
}

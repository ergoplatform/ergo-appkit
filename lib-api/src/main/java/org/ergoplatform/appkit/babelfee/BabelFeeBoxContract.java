package org.ergoplatform.appkit.babelfee;

import org.ergoplatform.sdk.ErgoId;
import org.ergoplatform.appkit.ErgoTreeTemplate;
import org.ergoplatform.appkit.ErgoValue;
import org.ergoplatform.appkit.ScalaHelpers;

import scorex.util.encode.Base16;
import sigmastate.Values;
import sigma.Coll;

public class BabelFeeBoxContract {

    /**
     * babel fee box ErgoTreeTemplateHash to be used for Explorer requests
     */
    public static final String templateHash = "4e83fa68ef3ed9794bbab5d8799998a9e09fb10a0afe8d1bf928936dfd11c465";
    // this was calced by ErgoTreeTemplate.fromTemplateBytes(contractTemplate).getTemplateHashHex()

    private static final String contractTemplateHex = "100604000e000400040005000500d803d601e30004d602e4c6a70408d603e4c6a7050595e67201d804d604b2a5e4720100d605b2db63087204730000d606db6308a7d60799c1a7c17204d1968302019683050193c27204c2a7938c720501730193e4c672040408720293e4c672040505720393e4c67204060ec5a796830201929c998c7205029591b1720673028cb272067303000273047203720792720773057202";
    private static final byte[] contractTemplate;
    private static final int[] contractParameterPositions = new int[] {1};

    static {
        contractTemplate = Base16.decode(contractTemplateHex).get();
    }

    private final ErgoId tokenId;
    private final Values.ErgoTree ergoTree;

    public BabelFeeBoxContract(ErgoId tokenId) {
        this.tokenId = tokenId;
        byte[] idBytes = tokenId.getBytes();
        ergoTree = ErgoTreeTemplate.fromErgoTreeBytes(contractTemplate)
            .withParameterPositions(contractParameterPositions)
            .applyParameters(new ErgoValue[]{ErgoValue.of(idBytes)});
    }

    public BabelFeeBoxContract(Values.ErgoTree ergoTree) {
        this.ergoTree = ergoTree;
        tokenId = new ErgoId(ScalaHelpers.collByteToByteArray(
            (Coll<Byte>) ErgoTreeTemplate.fromErgoTree(ergoTree)
                .withParameterPositions(contractParameterPositions)
                .getParameterValue(0).getValue()
        ));
    }

    public Values.ErgoTree getErgoTree() {
        return ergoTree;
    }

    public ErgoId getTokenId() {
        return tokenId;
    }
}

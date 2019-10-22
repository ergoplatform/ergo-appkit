package org.ergoplatform.polyglot.ni;

import org.ergoplatform.ErgoAddressEncoder;
import org.ergoplatform.api.client.ApiClient;
import org.ergoplatform.polyglot.BlockchainContext;
import org.ergoplatform.polyglot.BlockchainContextBuilderImpl;
import org.ergoplatform.polyglot.ErgoClientException;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.UnsignedWord;
import kotlin.Unit;

public class Prove {

    static String nodeUrl = "http://localhost:9052";
//    static String nodeUrl = "http://209.97.134.210:9052";
    public static void main(String[] args) {
        Runner r = new Runner();
        r.request(nodeUrl);
        try {
            ApiClient client = new ApiClient(nodeUrl);
            BlockchainContext ctx =
                    new BlockchainContextBuilderImpl(client, ErgoAddressEncoder.TestnetNetworkPrefix()).build();
            String res = r.sign(ctx, "83b94f2df7e97586a9fe8fe43fa84d252aa74ecee5fe0871f85a45663927cd9a");
            System.out.println(res);
        } catch (ErgoClientException e) {
            e.printStackTrace();
        }
    }

    @CEntryPoint(name = "sign")
    public static void sign(IsolateThread thread, CCharPointer cBoxId, CCharPointer resBuffer, UnsignedWord bufferSize) {
        Runner r = new Runner();
        r.request(nodeUrl);

        /* Convert the C string to the target Java string. */
        String boxId = CTypeConversion.toJavaString(cBoxId);
        try {
            ApiClient client = new ApiClient(nodeUrl);
            BlockchainContext ctx =
                    new BlockchainContextBuilderImpl(client, ErgoAddressEncoder.TestnetNetworkPrefix()).build();
            String res = r.sign(ctx, boxId);
            CTypeConversion.toCString(res, resBuffer, bufferSize);
        } catch (ErgoClientException e) {
            e.printStackTrace();
        }
    }
}
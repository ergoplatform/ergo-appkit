package org.ergoplatform.appkit.examples.util;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.ergoplatform.appkit.BlockchainContext;
import org.ergoplatform.appkit.ErgoClientException;
import org.ergoplatform.appkit.NetworkType;
import org.ergoplatform.appkit.impl.BlockchainContextBuilderImpl;
import org.ergoplatform.restapi.client.ApiClient;
import scalan.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

/**
 * MockedRunner using given files to provide BlockchainContext information.
 */
public class FileMockedErgoClient implements MockedErgoClient {
    private final String _nodeInfoFile;
    private final String _lastHeadersFile;
    private final String _boxFile;

    public FileMockedErgoClient(String nodeInfoFile, String lastHeadersFile, String boxFile) {
        _nodeInfoFile = nodeInfoFile;
        _lastHeadersFile = lastHeadersFile;
        _boxFile = boxFile;
    }

    @Override
    public String getNodeInfoResp() {
        return FileUtil.read(new File(_nodeInfoFile));
    }

    @Override
    public String getLastHeadersResp() {
        return FileUtil.read(new File(_lastHeadersFile));
    }

    @Override
    public String getBoxResp() {
        return FileUtil.read(new File(_boxFile));
    }

    @Override
    public <T> T execute(Function<BlockchainContext, T> action) {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(getNodeInfoResp()));

        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(getLastHeadersResp()));

        server.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(getBoxResp()));
        try {
            server.start();
        } catch (IOException e) {
            throw new ErgoClientException("Cannot start server " + server.toString(), e);
        }

        HttpUrl baseUrl = server.url("/");
        ApiClient client = new ApiClient(baseUrl.toString());
        BlockchainContext ctx =
         new BlockchainContextBuilderImpl(client, NetworkType.MAINNET).build();

        T res = action.apply(ctx);

        try {
            server.shutdown();
        } catch (IOException e) {
            throw new ErgoClientException("Cannot shutdown server " + server.toString(), e);
        }
        return res;
    }
}


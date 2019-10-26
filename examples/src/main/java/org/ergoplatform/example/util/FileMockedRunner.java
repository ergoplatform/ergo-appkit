package org.ergoplatform.example.util;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.ergoplatform.ErgoAddressEncoder;
import org.ergoplatform.polyglot.BlockchainContext;
import org.ergoplatform.polyglot.ErgoClientException;
import org.ergoplatform.polyglot.impl.BlockchainContextBuilderImpl;
import org.ergoplatform.restapi.client.ApiClient;
import scalan.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

/**
 * MockedRunner using given files to provide BlockchainContext information.
 */
public class FileMockedRunner implements MockedRunner {
    private final String _nodeInfoFile;
    private final String _lastHeadersFile;
    private final String _boxFile;

    public FileMockedRunner(String nodeInfoFile, String lastHeadersFile, String boxFile) {
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
    public <T> T run(Function<BlockchainContext, T> action) {
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
         new BlockchainContextBuilderImpl(client, ErgoAddressEncoder.MainnetNetworkPrefix()).build();

        T res = action.apply(ctx);

        try {
            server.shutdown();
        } catch (IOException e) {
            throw new ErgoClientException("Cannot shutdown server " + server.toString(), e);
        }
        return res;
    }
}


package org.ergoplatform.appkit;

import com.google.common.base.Strings;
import org.ergoplatform.appkit.config.ErgoNodeConfig;
import org.ergoplatform.appkit.impl.BlockchainContextBuilderImpl;
import org.ergoplatform.appkit.impl.NodeAndExplorerDataSourceImpl;
import org.ergoplatform.explorer.client.ExplorerApiClient;
import org.ergoplatform.restapi.client.ApiClient;

import java.net.Proxy;
import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * This implementation of {@link ErgoClient} uses REST API of Ergo node for communication.
 */
public class RestApiErgoClient implements ErgoClient {
    private final String _nodeUrl;
    private final NetworkType _networkType;
    private final NodeAndExplorerDataSourceImpl apiClient;
    private final String _explorerUrl;

    public final static String defaultMainnetExplorerUrl = "https://api.ergoplatform.com";
    public final static String defaultTestnetExplorerUrl = "https://api-testnet.ergoplatform.com";

    /**
     * Create and initialize a new instance.
     *
     * @param nodeUrl     http url to Ergo node REST API endpoint of the form
     *                    `https://host[:port]` where port is optional.
     * @param networkType type of network (mainnet, testnet) the Ergo node is part of
     * @param apiKey      api key to authenticate this client
     * @param explorerUrl Optional http url to Ergo Explorer REST API endpoint of the
     *                    form `https://host[:port]` where port is optional.
     *                    If `null` or empty string passed then the Explorer client is not
     *                    initialized and the client works in the `node only` mode.
     * @param proxy       Requests are passed through this proxy (if non-null).
     */
    RestApiErgoClient(String nodeUrl, NetworkType networkType, String apiKey, String explorerUrl, @Nullable Proxy proxy) {
        _nodeUrl = nodeUrl;
        _networkType = networkType;
        ApiClient nodeClient = new ApiClient(_nodeUrl, "ApiKeyAuth", apiKey);
        if (proxy != null) {
            nodeClient.createDefaultAdapter(proxy);
        }
        _explorerUrl = explorerUrl;
        ExplorerApiClient explorerClient;
        if (!Strings.isNullOrEmpty(_explorerUrl)) {
            if (proxy != null) {
                explorerClient = new ExplorerApiClient(_explorerUrl, proxy);
            }
            else {
             explorerClient = new ExplorerApiClient(_explorerUrl);
            }
        } else {
            explorerClient = null;
        }

        apiClient = new NodeAndExplorerDataSourceImpl(nodeClient, explorerClient);
    }

    @Override
    public <T> T execute(Function<BlockchainContext, T> action) {
        BlockchainContext ctx = new BlockchainContextBuilderImpl(apiClient, _networkType).build();
        T res = action.apply(ctx);
        return res;
    }

    /**
     * Returns the default URL for the given network type.
     */
    public static String getDefaultExplorerUrl(NetworkType networkType) {
        switch (networkType) {
        case MAINNET:
            return defaultMainnetExplorerUrl;
        default:
            return defaultTestnetExplorerUrl;
        }
    }

    /**
     * Creates a new {@link ErgoClient} instance in the `node-only` mode, i.e. connected
     * to a given node of the given network type and not connected to explorer.
     *
     * @param nodeUrl     http url to Ergo node REST API endpoint of the form `https://host:port/`
     * @param networkType type of network (mainnet, testnet) the Ergo node is part of
     * @param apiKey      api key to authenticate this client
     * @return a new instance of {@link ErgoClient} connected to a given node
     */
    public static ErgoClient createWithoutExplorer(String nodeUrl, NetworkType networkType, String apiKey) {
        return new RestApiErgoClient(nodeUrl, networkType, apiKey, null, null);
    }

    /**
     * Creates a new {@link ErgoClient} instance connected to a given node of the given
     *  network type.
     *
     * @param nodeUrl     http url to Ergo node REST API endpoint of the form
     * `https://host:port/`
     * @param networkType type of network (mainnet, testnet) the Ergo node is part of
     * @param apiKey      api key to authenticate this client
     * @param explorerUrl optional http url to Explorer REST API endpoint of the form
     *                    `https://host:port/`. If null or empty, then explorer connection
     *                    is not initialized so that the resulting {@link ErgoClient} can
     *                    work in `node-only` mode.
     * @return a new instance of {@link ErgoClient} connected to a given node
     */
    public static ErgoClient create(String nodeUrl, NetworkType networkType, String apiKey, String explorerUrl) {
        return new RestApiErgoClient(nodeUrl, networkType, apiKey, explorerUrl, null);
    }

    /**
     * Creates a new {@link ErgoClient} instance connected to a given node of the given
     *  network type.
     *
     * @param nodeUrl     http url to Ergo node REST API endpoint of the form
     * `https://host:port/`
     * @param networkType type of network (mainnet, testnet) the Ergo node is part of
     * @param apiKey      api key to authenticate this client
     * @param explorerUrl optional http url to Explorer REST API endpoint of the form
     *                    `https://host:port/`. If null or empty, then explorer connection
     *                    is not initialized so that the resulting {@link ErgoClient} can
     *                    work in `node-only` mode.
     * @param proxy       Requests are passed through this proxy (if non-null).
     * @return a new instance of {@link ErgoClient} connected to a given node
     */
    public static ErgoClient createWithProxy(String nodeUrl, NetworkType networkType, String apiKey, String explorerUrl, @Nullable Proxy proxy) {
        return new RestApiErgoClient(nodeUrl, networkType, apiKey, explorerUrl, proxy);
    }

    /**
     * Create a new {@link ErgoClient} instance using node configuration parameters and
     * optional explorerUrl.
     *
     * @param nodeConf    parameters of Ergo node used by ErgoClient.
     * @param explorerUrl optional http url to Explorer REST API endpoint of the form
     *                    `https://host:port/`. If null or empty, then explorer connection
     *                    is not initialized so that the resulting {@link ErgoClient} can
     *                    work in `node-only` mode.
     */
    public static ErgoClient create(ErgoNodeConfig nodeConf, String explorerUrl) {
        return RestApiErgoClient.create(
                nodeConf.getNodeApi().getApiUrl(),
                nodeConf.getNetworkType(),
                nodeConf.getNodeApi().getApiKey(),
                explorerUrl);
    }

    /**
     * Create a new {@link ErgoClient} instance using node configuration parameters,
     * optional explorerUrl and optional proxy.
     *
     * @param nodeConf    parameters of Ergo node used by ErgoClient.
     * @param explorerUrl optional http url to Explorer REST API endpoint of the form
     *                    `https://host:port/`. If null or empty, then explorer connection
     *                    is not initialized so that the resulting {@link ErgoClient} can
     *                    work in `node-only` mode.
     * @param proxy       Requests are passed through this proxy (if non-null).
     */
    public static ErgoClient createWithProxy(ErgoNodeConfig nodeConf, String explorerUrl, @Nullable Proxy proxy) {
        return RestApiErgoClient.createWithProxy(
                nodeConf.getNodeApi().getApiUrl(),
                nodeConf.getNetworkType(),
                nodeConf.getNodeApi().getApiKey(),
                explorerUrl,
                proxy
            );
    }

    @Override
    public BlockchainDataSource getDataSource() {
        return apiClient;
    }
}

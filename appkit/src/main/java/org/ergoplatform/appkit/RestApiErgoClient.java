package org.ergoplatform.appkit;

import org.ergoplatform.appkit.config.ErgoNodeConfig;
import org.ergoplatform.appkit.impl.BlockchainContextBuilderImpl;
import org.ergoplatform.appkit.impl.NodeAndExplorerDataSourceImpl;
import org.ergoplatform.explorer.client.ExplorerApiClient;
import org.ergoplatform.restapi.client.ApiClient;

import java.util.function.Function;

import javax.annotation.Nullable;

import okhttp3.OkHttpClient;

/**
 * This implementation of {@link ErgoClient} uses REST API of Ergo node for communication.
 */
public class RestApiErgoClient implements ErgoClient {
    private final NetworkType _networkType;
    private final NodeAndExplorerDataSourceImpl apiClient;

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
     * @param httpClientBuilder Builder used to construct http client instances. If null, a new
     *                          OkHttpClient with default parameters is used.
     */
    RestApiErgoClient(String nodeUrl, NetworkType networkType, String apiKey, String explorerUrl, @Nullable OkHttpClient.Builder httpClientBuilder) {
        _networkType = networkType;

        // if no httpClientBuilder is set, we use a single one for both api clients
        if (httpClientBuilder == null) {
            // just using the same builder is not enough - we have to derive the builder from an
            // actual OkHttpClient instance to share the thread pools.
            httpClientBuilder = new OkHttpClient().newBuilder();
        }

        ApiClient nodeClient = new ApiClient(nodeUrl, "ApiKeyAuth", apiKey);
        nodeClient.configureFromOkClientBuilder(httpClientBuilder);

        ExplorerApiClient explorerClient;
        if (explorerUrl != null && !explorerUrl.isEmpty()) {
            explorerClient = new ExplorerApiClient(explorerUrl);
            explorerClient.configureFromOkClientBuilder(httpClientBuilder);
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
     * @param httpClientBuilder Builder used to construct http client instances. If null, a new
     *                          OkHttpClient with default parameters is used.
     * @return a new instance of {@link ErgoClient} connected to a given node
     */
    public static ErgoClient createWithHttpClientBuilder(String nodeUrl, NetworkType networkType, String apiKey, String explorerUrl, @Nullable OkHttpClient.Builder httpClientBuilder) {
        return new RestApiErgoClient(nodeUrl, networkType, apiKey, explorerUrl, httpClientBuilder);
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
     * @param httpClientBuilder Builder used to construct http client instances. If null, a new
     *                          OkHttpClient with default parameters is used.
     */
    public static ErgoClient createWithHttpClientBuilder(ErgoNodeConfig nodeConf, String explorerUrl, @Nullable OkHttpClient.Builder httpClientBuilder) {
        return RestApiErgoClient.createWithHttpClientBuilder(
                nodeConf.getNodeApi().getApiUrl(),
                nodeConf.getNetworkType(),
                nodeConf.getNodeApi().getApiKey(),
                explorerUrl,
                httpClientBuilder
            );
    }

    @Override
    public BlockchainDataSource getDataSource() {
        return apiClient;
    }
}

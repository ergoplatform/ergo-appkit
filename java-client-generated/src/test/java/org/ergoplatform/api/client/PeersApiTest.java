package org.ergoplatform.api.client;

import org.ergoplatform.api.client.ApiClient;
import org.ergoplatform.api.client.ApiError;
import org.ergoplatform.api.client.Peer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for PeersApi
 */
public class PeersApiTest {

    private PeersApi api;

    @Before
    public void setup() {
        api = new ApiClient("http://localhost:9052/").createService(PeersApi.class);
    }


    /**
     * Add address to peers list
     *
     * 
     */
    @Test
    public void connectToPeerTest() {
        String body = null;
        // Void response = api.connectToPeer(body);

        // TODO: test validations
    }

    /**
     * Get all known peers
     *
     * 
     */
    @Test
    public void getAllPeersTest() {
        // List<Peer> response = api.getAllPeers();

        // TODO: test validations
    }

    /**
     * Get blacklisted peers
     *
     * 
     */
    @Test
    public void getBlacklistedPeersTest() {
        // List<String> response = api.getBlacklistedPeers();

        // TODO: test validations
    }

    /**
     * Get current connected peers
     *
     * 
     */
    @Test
    public void getConnectedPeersTest() {
        // List<Peer> response = api.getConnectedPeers();

        // TODO: test validations
    }
}

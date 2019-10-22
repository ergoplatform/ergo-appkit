package org.ergoplatform.api.client;

import org.ergoplatform.api.client.ApiClient;
import org.ergoplatform.api.client.AddressHolder;
import org.ergoplatform.api.client.ApiError;
import org.ergoplatform.api.client.InlineResponse2005;
import org.ergoplatform.api.client.SourceHolder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for ScriptApi
 */
public class ScriptApiTest {

    private ScriptApi api;

    @Before
    public void setup() {
        api = new ApiClient("http://localhost:9052/").createService(ScriptApi.class);
    }


    /**
     * Convert an address to hex-encoded serialized ErgoTree (script)
     *
     * 
     */
    @Test
    public void addressToTreeTest() {
        String address = null;
        // InlineResponse2005 response = api.addressToTree(address);

        // TODO: test validations
    }

    /**
     * Create P2SAddress from Sigma source
     *
     * 
     */
    @Test
    public void scriptP2SAddressTest() {
        SourceHolder body = null;
        // AddressHolder response = api.scriptP2SAddress(body);

        // TODO: test validations
    }

    /**
     * Create P2SHAddress from Sigma source
     *
     * 
     */
    @Test
    public void scriptP2SHAddressTest() {
        SourceHolder body = null;
        // AddressHolder response = api.scriptP2SHAddress(body);

        // TODO: test validations
    }
}

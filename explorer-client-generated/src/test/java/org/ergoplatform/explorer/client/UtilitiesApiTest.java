package org.ergoplatform.explorer.client;

import org.ergoplatform.explorer.client.ApiClient;
import org.ergoplatform.explorer.client.model.InlineResponse2002;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for UtilitiesApi
 */
public class UtilitiesApiTest {

    private UtilitiesApi api;

    @Before
    public void setup() {
        api = new ApiClient("http://localhost:9052").createService(UtilitiesApi.class);
    }


    /**
     * Search block, transactions, adresses
     *
     * 
     */
    @Test
    public void searchGetTest() {
        String query = null;
        // InlineResponse2002 response = api.searchGet(query);

        // TODO: test validations
    }
}

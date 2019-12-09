package org.ergoplatform.explorer.client;

import org.ergoplatform.explorer.client.ApiClient;
import org.ergoplatform.explorer.client.model.InlineResponse200;
import org.ergoplatform.explorer.client.model.InlineResponse2001;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for BlocksApi
 */
public class BlocksApiTest {

    private BlocksApi api;

    @Before
    public void setup() {
        api = new ApiClient("http://localhost:9052").createService(BlocksApi.class);
    }


    /**
     * Get block by d
     *
     * 
     */
    @Test
    public void blocksByDDGetTest() {
        Integer d = null;
        // InlineResponse2001 response = api.blocksByDDGet(d);

        // TODO: test validations
    }

    /**
     * Get block by id
     *
     * 
     */
    @Test
    public void blocksIdGetTest() {
        String id = null;
        // InlineResponse2001 response = api.blocksIdGet(id);

        // TODO: test validations
    }

    /**
     * Get list of blocks
     *
     * Get list of blocks sorted by height
     */
    @Test
    public void listBlocksTest() {
        Integer offset = null;
        Integer limit = null;
        String sortBy = null;
        String sortDirection = null;
        Integer startDate = null;
        Integer endDate = null;
        // InlineResponse200 response = api.listBlocks(offset, limit, sortBy, sortDirection, startDate, endDate);

        // TODO: test validations
    }
}

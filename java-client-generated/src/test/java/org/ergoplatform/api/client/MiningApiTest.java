package org.ergoplatform.api.client;

import org.ergoplatform.api.client.ApiClient;
import org.ergoplatform.api.client.ApiError;
import org.ergoplatform.api.client.ExternalCandidateBlock;
import org.ergoplatform.api.client.InlineResponse2004;
import org.ergoplatform.api.client.PowSolutions;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for MiningApi
 */
public class MiningApiTest {

    private MiningApi api;

    @Before
    public void setup() {
        api = new ApiClient("http://localhost:9052/").createService(MiningApi.class);
    }


    /**
     * Read miner reward address
     *
     * 
     */
    @Test
    public void miningReadMinerRewardAddressTest() {
        // InlineResponse2004 response = api.miningReadMinerRewardAddress();

        // TODO: test validations
    }

    /**
     * Request block candidate
     *
     * 
     */
    @Test
    public void miningRequestBlockCandidateTest() {
        // ExternalCandidateBlock response = api.miningRequestBlockCandidate();

        // TODO: test validations
    }

    /**
     * Submit solution for current candidate
     *
     * 
     */
    @Test
    public void miningSubmitSolutionTest() {
        PowSolutions body = null;
        // Void response = api.miningSubmitSolution(body);

        // TODO: test validations
    }
}

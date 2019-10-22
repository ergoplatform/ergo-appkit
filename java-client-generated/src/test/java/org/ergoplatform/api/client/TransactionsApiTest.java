package org.ergoplatform.api.client;

import org.ergoplatform.api.client.ApiClient;
import org.ergoplatform.api.client.ApiError;
import org.ergoplatform.api.client.ErgoTransaction;
import org.ergoplatform.api.client.Transactions;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for TransactionsApi
 */
public class TransactionsApiTest {

    private TransactionsApi api;

    @Before
    public void setup() {
        api = new ApiClient("http://localhost:9052/").createService(TransactionsApi.class);
    }


    /**
     * Get current pool of the unconfirmed transactions pool
     *
     * 
     */
    @Test
    public void getUnconfirmedTransactionsTest() {
        Integer limit = null;
        Integer offset = null;
        // Transactions response = api.getUnconfirmedTransactions(limit, offset);

        // TODO: test validations
    }

    /**
     * Send an Ergo transaction
     *
     * 
     */
    @Test
    public void sendTransactionTest() {
        ErgoTransaction body = null;
        // String response = api.sendTransaction(body);

        // TODO: test validations
    }
}

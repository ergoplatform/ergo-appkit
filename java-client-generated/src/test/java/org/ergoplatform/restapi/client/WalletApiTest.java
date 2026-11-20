package org.ergoplatform.restapi.client;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * API tests for WalletApi
 */
public class WalletApiTest {

    private WalletApi api;

    @Before
    public void setup() {
        api = new ApiClient("http://localhost:9052/").createService(WalletApi.class);
    }


    /**
     * Get wallet status
     *
     * 
     */
    @Test
    public void getWalletStatusTest() {
        // InlineResponse2001 response = api.getWalletStatus();

        // TODO: test validations
    }

    /**
     * Get wallet addresses
     *
     * 
     */
    @Test
    public void walletAddressesTest() {
        // List<String> response = api.walletAddresses();

        // TODO: test validations
    }

    /**
     * Get total amount of confirmed Ergo tokens and assets
     *
     * 
     */
    @Test
    public void walletBalancesTest() {
        // BalancesSnapshot response = api.walletBalances();

        // TODO: test validations
    }

    /**
     * Get summary amount of confirmed plus unconfirmed Ergo tokens and assets
     *
     * 
     */
    @Test
    public void walletBalancesUnconfirmedTest() {
        // BalancesSnapshot response = api.walletBalancesUnconfirmed();

        // TODO: test validations
    }

    /**
     * Get a list of all wallet-related boxes
     *
     * 
     */
    @Test
    public void walletBoxesTest() {
        Integer minConfirmations = null;
        Integer minInclusionHeight = null;
        // List<WalletBox> response = api.walletBoxes(minConfirmations, minInclusionHeight);

        // TODO: test validations
    }

    /**
     * Derive new key according to a provided path
     *
     * 
     */
    @Test
    public void walletDeriveKeyTest() {
        Body4 body = null;
        // InlineResponse2002 response = api.walletDeriveKey(body);

        // TODO: test validations
    }

    /**
     * Derive next key
     *
     * 
     */
    @Test
    public void walletDeriveNextKeyTest() {
        // InlineResponse2003 response = api.walletDeriveNextKey();

        // TODO: test validations
    }

    /**
     * Get wallet-related transaction by id
     *
     * 
     */
    @Test
    public void walletGetTransactionTest() {
        String id = null;
        // List<WalletTransaction> response = api.walletGetTransaction(id);

        // TODO: test validations
    }

    /**
     * Initialize new wallet with randomly generated seed
     *
     * 
     */
    @Test
    public void walletInitTest() {
        Body body = null;
        // InlineResponse200 response = api.walletInit(body);

        // TODO: test validations
    }

    /**
     * Lock wallet
     *
     * 
     */
    @Test
    public void walletLockTest() {
        // Void response = api.walletLock();

        // TODO: test validations
    }

    /**
     * Generate and send payment transaction (default fee of 0.001 Erg is used)
     *
     * 
     */
    @Test
    public void walletPaymentTransactionGenerateAndSendTest() {
        List<PaymentRequest> body = null;
        // String response = api.walletPaymentTransactionGenerateAndSend(body);

        // TODO: test validations
    }

    /**
     * Create new wallet from existing mnemonic seed
     *
     * 
     */
    @Test
    public void walletRestoreTest() {
        Body1 body = null;
        // Void response = api.walletRestore(body);

        // TODO: test validations
    }

    /**
     * Generate arbitrary transaction from array of requests.
     *
     * 
     */
    @Test
    public void walletTransactionGenerateTest() {
        RequestsHolder body = null;
        // ErgoTransaction response = api.walletTransactionGenerate(body);

        // TODO: test validations
    }

    /**
     * Generate and send arbitrary transaction
     *
     * 
     */
    @Test
    public void walletTransactionGenerateAndSendTest() {
        RequestsHolder body = null;
        // String response = api.walletTransactionGenerateAndSend(body);

        // TODO: test validations
    }

    /**
     * Get a list of all wallet-related transactions
     *
     * 
     */
    @Test
    public void walletTransactionsTest() {
        Integer minInclusionHeight = null;
        Integer maxInclusionHeight = null;
        Integer minConfirmations = null;
        Integer maxConfirmations = null;
        // List<WalletTransaction> response = api.walletTransactions(minInclusionHeight, maxInclusionHeight, minConfirmations, maxConfirmations);

        // TODO: test validations
    }

    /**
     * Unlock wallet
     *
     * 
     */
    @Test
    public void walletUnlockTest() {
        Body2 body = null;
        // Void response = api.walletUnlock(body);

        // TODO: test validations
    }

    /**
     * Get a list of unspent boxes
     *
     * 
     */
    @Test
    public void walletUnspentBoxesTest() {
        Integer minConfirmations = null;
        Integer minInclusionHeight = null;
        // List<WalletBox> response = api.walletUnspentBoxes(minConfirmations, minInclusionHeight);

        // TODO: test validations
    }

    @Test
    public void walletBoxesCollectModelAndCallTest() {
        BoxesRequestHolder defaultRequest = new BoxesRequestHolder().targetBalance(1000000000L);
        org.junit.Assert.assertNotNull(defaultRequest.getTargetAssets());
        org.junit.Assert.assertTrue(defaultRequest.getTargetAssets().isEmpty());

        JSON json = new JSON();
        String defaultSerialized = json.getGson().toJson(defaultRequest);
        org.junit.Assert.assertTrue(defaultSerialized.contains("\"targetAssets\":{}"));
        org.junit.Assert.assertTrue(defaultSerialized.contains("\"targetBalance\":1000000000"));

        String tokenId = "d47f958b201dc7162f641f7eb055e9fa7a9cb65cc24d4447a10f86675fc58328";
        BoxesRequestHolder assetRequest = new BoxesRequestHolder()
            .targetBalance(2000000000L)
            .putTargetAssetsItem(tokenId, 50L);
        String assetSerialized = json.getGson().toJson(assetRequest);
        org.junit.Assert.assertTrue(assetSerialized.contains("\"" + tokenId + "\":50"));

        // Verify Retrofit builds POST /wallet/boxes/collect with @Body without throwing IllegalArgumentException
        retrofit2.Call<CollectedBoxes> call = api.walletBoxesCollect(assetRequest);
        org.junit.Assert.assertEquals("POST", call.request().method());
        org.junit.Assert.assertTrue(call.request().url().encodedPath().endsWith("/wallet/boxes/collect"));

        String nodeResponseJson = "{"
            + "\"boxes\":[{"
            + "\"boxId\":\"6ee4d5d0dc518d52386cef251efa732680a8600ecac87ee3ad95c7aa1d8282b3\","
            + "\"value\":2500000000,"
            + "\"ergoTree\":\"0008cd02d3a9410ac758ad45dfc85af8626efdacf398439c73977b13064aa8e6c8f2ac88\","
            + "\"creationHeight\":100,"
            + "\"assets\":[{\"tokenId\":\"" + tokenId + "\",\"amount\":75}],"
            + "\"additionalRegisters\":{},"
            + "\"transactionId\":\"5eb715015c6d894303f3ba9c1796791c9a2af61aab659b8a63d890fbbc92756b\","
            + "\"index\":0"
            + "}],"
            + "\"changeBoxes\":[{"
            + "\"value\":500000000,"
            + "\"tokens\":{\"" + tokenId + "\":25}"
            + "}]"
            + "}";
        CollectedBoxes parsed = json.getGson().fromJson(nodeResponseJson, CollectedBoxes.class);
        org.junit.Assert.assertEquals(1, parsed.getBoxes().size());
        org.junit.Assert.assertEquals(Long.valueOf(2500000000L), parsed.getBoxes().get(0).getValue());
        org.junit.Assert.assertEquals(1, parsed.getChangeBoxes().size());
        org.junit.Assert.assertEquals(Long.valueOf(500000000L), parsed.getChangeBoxes().get(0).getValue());
        org.junit.Assert.assertEquals(Long.valueOf(25L), parsed.getChangeBoxes().get(0).getTokens().get(tokenId));
    }

    /**
     * Update address to be used to send change to
     *
     * 
     */
    @Test
    public void walletUpdateChangeAddressTest() {
        Body3 body = null;
        // Void response = api.walletUpdateChangeAddress(body);

        // TODO: test validations
    }
}

package org.ergoplatform.api.client;

import org.ergoplatform.api.client.ApiClient;
import org.ergoplatform.api.client.ApiError;
import org.ergoplatform.api.client.BalancesSnapshot;
import org.ergoplatform.api.client.Body;
import org.ergoplatform.api.client.Body1;
import org.ergoplatform.api.client.Body2;
import org.ergoplatform.api.client.Body3;
import org.ergoplatform.api.client.Body4;
import org.ergoplatform.api.client.ErgoTransaction;
import org.ergoplatform.api.client.InlineResponse200;
import org.ergoplatform.api.client.InlineResponse2001;
import org.ergoplatform.api.client.InlineResponse2002;
import org.ergoplatform.api.client.InlineResponse2003;
import org.ergoplatform.api.client.PaymentRequest;
import org.ergoplatform.api.client.RequestsHolder;
import org.ergoplatform.api.client.WalletBox;
import org.ergoplatform.api.client.WalletTransaction;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

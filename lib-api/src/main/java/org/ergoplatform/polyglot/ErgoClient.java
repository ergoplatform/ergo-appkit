package org.ergoplatform.polyglot;

import java.util.function.Function;

/**
 * Ergo blockchain client object.
 * Used as a runner of some action in a blockchain context.
 * The blockchain context is created by specific implementation
 * and passed to the action.
 * Some implementations may connect to network nodes, while others
 * may use mock web server to simulate connection in tests.
 */
public interface ErgoClient {
    /**
     * Execute the given action and return action's result.
     */
    <T> T execute(Function<BlockchainContext, T> action);
}


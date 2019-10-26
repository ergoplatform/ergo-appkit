package org.ergoplatform.example.util;

import org.ergoplatform.polyglot.BlockchainContext;

import java.util.function.Function;

/**
 * Runner of some action in a blockchain context.
 * The blockchain context is created by specific implementation
 * and passed to the action.
 * Some implementations may connect to network nodes, while others
 * may use mock web server to simulate real connection in tests.
 */
public interface Runner {
    /**
     * Creates context, execute the given action and return action's result.
     */
    <T> T run(Function<BlockchainContext, T> action);
}


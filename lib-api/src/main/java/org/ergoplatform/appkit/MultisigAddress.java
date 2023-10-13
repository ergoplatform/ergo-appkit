package org.ergoplatform.appkit;

import org.ergoplatform.appkit.impl.MultisigAddressImpl;

import java.util.List;

/**
 * An EIP-42 compliant multisig address
 */
public abstract class MultisigAddress {

    /**
     * @return address for this multisig address
     */
    public abstract Address getAddress();

    /**
     * @return list of participating p2pk addresses
     */
    public abstract List<Address> getParticipants();

    /**
     * @return number of signers required to sign a transaction for this address
     */
    public abstract int getSignersRequiredCount();

    /** Default value for tree header flags (0 means no flags). */
    public static byte DEFAULT_TREE_HEADER_FLAGS = (byte)0;

    /**
     * constructs a k-out-of-n (threshold signature) address from the list of participants
     * and the number of required signers
     *
     * @param signersRequired number k, signers required to sign a transaction for this address
     * @param particpants list of p2pk addresses of possible signers
     * @return MultisigAddress interface
     */
    public static MultisigAddress buildFromParticipants(int signersRequired, List<Address> particpants, NetworkType networkType) {
        return MultisigAddressImpl.fromParticipants(signersRequired, particpants, networkType, DEFAULT_TREE_HEADER_FLAGS);
    }

    /**
     *
     * @param address multisig address to construct class for
     * @return MultisigAddress if the given address is an EIP-42 compliant multisig address
     * @throws IllegalArgumentException if given address is NOT an EIP-42 compliant multisig address
     */
    public static MultisigAddress buildFromAddress(Address address) {
        return MultisigAddressImpl.fromAddress(address);
    }
}

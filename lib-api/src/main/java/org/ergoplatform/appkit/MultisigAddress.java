package org.ergoplatform.appkit;

import java.util.List;

/**
 * An EIP-41 compliant multisig address
 */
public class MultisigAddress {

    /**
     * @return address for this multisig address
     */
    public Address getAddress() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return list of participating p2pk addresses
     */
    public List<Address> getParticipants() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return number of signers required to sign a transaction for this address
     */
    public int getSignersRequiredCount() {
        throw new UnsupportedOperationException();
    }

    /**
     * constructs an N out of M address from the list of particpants and the number of required
     * signers
     *
     * @param signersRequired number N, signers required to sign a transaction for this addres
     * @param particpants list of p2pk addresses of possible signers
     * @return MultisigAddress class
     */
    public static MultisigAddress buildFromParticipants(int signersRequired, List<Address> particpants) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param address multisig address to construct class for
     * @return MultisigAddress if the given address is an EIP-41 compliant multisig address
     * @throws IllegalArgumentException if given address is not an EIP-41 compliant multisig address
     */
    public static MultisigAddress buildFromAddress(Address address) {
        throw new UnsupportedOperationException();
    }
}

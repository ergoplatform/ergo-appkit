package org.ergoplatform.appkit;

import java.util.List;

/**
 * Participants able to sign for a {@link ReducedTransaction}
 */
abstract public class SigningParticipants {

    /**
     * @return true if multiple signers are needed
     */
    abstract public boolean isMultisig();

    /**
     * Multisig requirement, k-out-of-n
     */
    public static class MultisigRequirement extends SigningParticipants {
        public final int hintsNeeded;
        public final List<SigningParticipants> hintbag;

        public MultisigRequirement(int hintsNeeded, List<SigningParticipants> hintbag) {
            this.hintsNeeded = hintsNeeded;
            this.hintbag = hintbag;
        }

        @Override
        public boolean isMultisig() {
            return hintsNeeded > 1;
        }
    }

    public static class SingleSigner extends SigningParticipants {
        public final Address address;

        public SingleSigner(Address address) {
            this.address = address;
        }

        @Override
        public boolean isMultisig() {
            return false;
        }
    }
}

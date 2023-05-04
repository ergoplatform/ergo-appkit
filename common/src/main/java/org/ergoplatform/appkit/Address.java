package org.ergoplatform.appkit;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Objects;

import org.bouncycastle.math.ec.custom.sec.SecP256K1Point;
import org.ergoplatform.ErgoAddress;
import org.ergoplatform.ErgoAddressEncoder;
import org.ergoplatform.P2PKAddress;
import org.ergoplatform.Pay2SAddress;
import org.ergoplatform.appkit.impl.ErgoTreeContract;
import org.ergoplatform.wallet.secrets.DerivationPath;
import org.ergoplatform.wallet.secrets.ExtendedPublicKey;
import org.ergoplatform.wallet.secrets.ExtendedSecretKey;

import scala.MatchError;
import scala.util.Try;
import scorex.util.encode.Base58;
import sigmastate.Values;
import sigmastate.basics.DLogProtocol;
import sigmastate.crypto.Platform;
import sigmastate.eval.CostingSigmaDslBuilder$;
import sigmastate.serialization.ErgoTreeSerializer;
import sigmastate.utils.Helpers;
import special.sigma.GroupElement;

public class Address {
    private final String _base58String;
    private final byte[] _addrBytes;

    ErgoAddress _address;

    public Address(ErgoAddress ergoAddress) {
        _address = ergoAddress;
        ErgoAddressEncoder encoder =
            ErgoAddressEncoder.apply(ergoAddress.networkPrefix());
        _base58String = encoder.toString(_address);
        _addrBytes = Base58.decode(_base58String).get();
    }

    /**
     * First byte is used to encode network type and address type.
     *
     * @see ErgoAddressEncoder
     */
    private byte headByte() { return _addrBytes[0]; }

    private Address(String base58String) {
        _base58String = base58String;
        Try<byte[]> res = Base58.decode(base58String);
        if (res.isFailure())
            throw new RuntimeException(
                "Invalid address encoding, expected base58 string: " + base58String,
                (Throwable) new Helpers.TryOps(res).toEither().left().get());
        _addrBytes = res.get();
        ErgoAddressEncoder encoder =
            ErgoAddressEncoder.apply(getNetworkType().networkPrefix);
        Try<ErgoAddress> addrTry = encoder.fromString(base58String);
        if (addrTry.isFailure())
            throw new RuntimeException(
                "Invalid address encoding, expected base58 string: " + base58String,
                (Throwable) new Helpers.TryOps(addrTry).toEither().left().get());
        _address = addrTry.get();
    }

    /**
     * @return NetworkType of this address.
     */
    public NetworkType getNetworkType() {
        return isMainnet() ? NetworkType.MAINNET :
            NetworkType.TESTNET;
    }

    /**
     * @return true if this address from Ergo mainnet.
     */
    public boolean isMainnet() { return headByte() < NetworkType.TESTNET.networkPrefix; }

    /**
     * @return true if this address has Pay-To-Public-Key type.
     */
    public boolean isP2PK() { return _address instanceof P2PKAddress; }

    /**
     * @return underlying {@link P2PKAddress}.
     * @throws IllegalArgumentException if this instance is not P2PK address
     */
    public P2PKAddress asP2PK() {
        checkArgument(isP2PK(), "This instance %s is not P2PKAddress", this);
        return (P2PKAddress) _address;
    }

    /**
     * @return true if this address has Pay-To-Script type.
     */
    public boolean isP2S() { return _address instanceof Pay2SAddress; }

    /**
     * @return underlying {@link Pay2SAddress}.
     * @throws IllegalArgumentException if this instance is not P2S address
     */
    public Pay2SAddress asP2S() {
        checkArgument(isP2S(), "This instance %s is not Pay2SAddress", this);
        return (Pay2SAddress) _address;
    }

    /**
     * Obtain an instance of {@link ErgoAddress} related to this Address instance.
     *
     * @return {@link ErgoAddress} instance associated with this address
     */
    public ErgoAddress getErgoAddress() {
        return _address;
    }

    /**
     * Extract public key from P2PKAddress.
     */
    public DLogProtocol.ProveDlog getPublicKey() { return asP2PK().pubkey(); }

    /**
     * Extract public key from P2PKAddress and return its group element
     */
    public GroupElement getPublicKeyGE() {
        Platform.Ecp point = getPublicKey().value();
        return CostingSigmaDslBuilder$.MODULE$.GroupElement(point);
    }

    /**
     * @return ErgoContract representing this address
     */
    public ErgoContract toErgoContract() {
        return new ErgoTreeContract(getErgoAddress().script(), getNetworkType());
    }

    /**
     * @return this addresses ErgoTree's proposition bytes. Use this to store this address
     * on Box registers.
     */
    public byte[] toPropositionBytes() {
        return getErgoAddress().script().bytes();
    }

    /**
     * @return true if this address is a SigmaBoolean
     */
    public boolean isSigmaBoolean() {
        try {
            getSigmaBoolean();
            return true;
        } catch (MatchError me) {
            return false;
        }
    }

    /**
     * @return SigmaBoolean value of this address. Throws an error if
     * {@link #isSigmaBoolean()} is false
     */
    public Values.SigmaBoolean getSigmaBoolean() {
        Values.ErgoTree ergoTree = getErgoAddress().script();
        return JavaHelpers$.MODULE$.toSigmaBoolean(ergoTree);
    }

    /**
     * Create Ergo Address from base58 string.
     *
     * @param base58Str base58 string representation of address bytes.
     * @return Address instance decoded from string
     */
    public static Address create(String base58Str) { return new Address(base58Str); }

    /**
     * Creates address from given ergovalue containing an ErgoTree proposition bytes.
     * Use this to convert a box register containing an ErgoTree into its address.
     *
     * @param networkType      mainnet or testnet network
     * @param propositionBytes ErgoTree proposition bytes
     */
    public static Address fromPropositionBytes(NetworkType networkType, byte[] propositionBytes) {
        return new ErgoTreeContract(
            ErgoTreeSerializer.DefaultSerializer().deserializeErgoTree(propositionBytes),
            networkType
        ).toAddress();
    }

    public static Address fromMnemonic(NetworkType networkType, Mnemonic mnemonic, Boolean usePre1627KeyDerivation) {
        return fromMnemonic(networkType, mnemonic.getPhrase(), mnemonic.getPassword(), usePre1627KeyDerivation);
    }

    /**
     * Creates an {@link Address} from the given mnemonic using `m / 0` derivation path
     * (corresponds to master key). The returned address is not compliant with
     * <a href="https://github.com/ergoplatform/eips/blob/master/eip-0003.md">EIP-3</a>.
     *
     * @param networkType  mainnet or testnet network
     * @param mnemonic     mnemonic (e.g. 15 words) phrase
     * @param mnemonicPass optional (i.e. it can be empty) mnemonic password which is
     *                     necessary to know in order to restore the secrets
     * @param usePre1627KeyDerivation use incorrect(previous) BIP32 derivation, expected to be false for new 
     * wallets, and true for old pre-1627 wallets (see https://github.com/ergoplatform/ergo/issues/1627 for details)
     */
    public static Address fromMnemonic(
            NetworkType networkType, SecretString mnemonic, SecretString mnemonicPass, Boolean usePre1627KeyDerivation) {
        ExtendedSecretKey masterKey = JavaHelpers.seedToMasterKey(mnemonic, mnemonicPass, usePre1627KeyDerivation);
        DLogProtocol.ProveDlog pk = masterKey.publicImage();
        P2PKAddress p2pkAddress = JavaHelpers.createP2PKAddress(pk,
            networkType.networkPrefix);
        return new Address(p2pkAddress);
    }

    /**
     * Creates an {@link Address} from the given mnemonic using <a
     * href="https://github.com/ergoplatform/eips/blob/master/eip-0003.md">EIP-3</a>
     * derivation path (`m / 44' / 429' / 0' / 0 / 0`)
     * The returned address is compliant with EIP-3.
     *
     * @param index        the last index in the path (zero based)
     * @param networkType  mainnet or testnet network
     * @param mnemonic     mnemonic (e.g. 15 words) phrase
     * @param mnemonicPass optional (i.e. it can be empty) mnemonic password which is
     *                     necessary to know in order to restore the secrets
     * @param usePre1627KeyDerivation use incorrect(previous) BIP32 derivation, expected to be false for new 
     * wallets, and true for old pre-1627 wallets (see https://github.com/ergoplatform/ergo/issues/1627 for details)
     */
    public static Address createEip3Address(
        int index,
        NetworkType networkType,
        SecretString mnemonic,
        SecretString mnemonicPass, 
        Boolean usePre1627KeyDerivation) {
        ExtendedSecretKey rootSecret = JavaHelpers.seedToMasterKey(mnemonic, mnemonicPass, usePre1627KeyDerivation);

        // Let's use "m/44'/429'/0'/0/index" path (this path is compliant with EIP-3 which
        // is BIP-44 for Ergo)
        DerivationPath path = JavaHelpers.eip3DerivationParent();
        ExtendedSecretKey secretKey = (ExtendedSecretKey) rootSecret.derive(path);
        ExtendedPublicKey pubkey = secretKey.publicKey();

        return createEip3Address(index, networkType, pubkey);
    }

    /**
     * Creates an {@link Address} from the given extended public key using <a
     * href="https://github.com/ergoplatform/eips/blob/master/eip-0003.md">EIP-3</a>
     * derivation path (`m / 44' / 429' / 0' / 0 / 0`)
     * The returned address is compliant with EIP-3.
     * <p>
     * Use this with a key deserialized by {@link Bip32Serialization#parseExtendedPublicKey(byte[], NetworkType)}
     */
    public static Address createEip3Address(
        int index,
        NetworkType networkType,
        ExtendedPublicKey extendedPublicKey) {

        DerivationPath path = JavaHelpers.eip3DerivationWithLastIndex(index).toPublicBranch();
        ExtendedPublicKey pubkey = (ExtendedPublicKey) extendedPublicKey.derive(path);
        P2PKAddress p2pkAddress = JavaHelpers.createP2PKAddress(
            pubkey.key(),
            networkType.networkPrefix);
        return new Address(p2pkAddress);
    }

    public static Address fromErgoTree(Values.ErgoTree ergoTree, NetworkType networkType) {
        ErgoAddressEncoder encoder =
            ErgoAddressEncoder.apply(networkType.networkPrefix);
        ErgoAddress ergoAddress = encoder.fromProposition(ergoTree).get();
        return new Address(ergoAddress);
    }

    public static Address fromSigmaBoolean(Values.SigmaBoolean sigmaBoolean, NetworkType networkType) {
        Values.ErgoTree ergoTree = JavaHelpers$.MODULE$.toErgoTree(sigmaBoolean);
        return fromErgoTree(ergoTree, networkType);
    }

    @Override
    public String toString() {
        return _address.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(_address.hashCode(), _address.networkPrefix());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Address) {
            return _address.networkPrefix() == ((Address) obj)._address.networkPrefix()
                && _address.equals(((Address) obj)._address);
        }
        return false;
    }
}

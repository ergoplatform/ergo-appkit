package org.ergoplatform.appkit;

import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.ergoplatform.appkit.NetworkType;
import org.ergoplatform.sdk.wallet.secrets.DerivationPath;
import org.ergoplatform.sdk.wallet.secrets.ExtendedPublicKey;
import org.ergoplatform.sdk.wallet.secrets.ExtendedSecretKey;
import org.ergoplatform.sdk.wallet.Constants;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import scala.collection.immutable.Seq;
import scorex.crypto.hash.Sha256;
import scorex.util.encode.Base16$;

/**
 * Methods to serialize and deserialize key according to bip32 standard
 * https://en.bitcoin.it/wiki/BIP_0032
 */
public class Bip32Serialization {
    /**
     * serializes the given master key for a seed phrase to a BIP-32 compliant byte array and
     * converts it into a hex-encoded String
     */
    public static String serializeExtendedPublicKeyToHex(ExtendedSecretKey masterKey, NetworkType networkType) {
        byte[] serializedKey = serializeExtendedPublicKeyBip32(masterKey, networkType);
        return Base16$.MODULE$.encode(serializedKey);
    }

    /**
     * serializes the given master key for a seed phrase to a BIP-32 compliant byte array
     */
    public static byte[] serializeExtendedPublicKeyBip32(ExtendedSecretKey masterKey,
                                                         NetworkType networkType) {
        if (masterKey.path().depth() > 1) {
            throw new IllegalArgumentException("Master key expected for serialization");
        }

        DerivationPath eip3ParentPath = org.ergoplatform.sdk.JavaHelpers.eip3DerivationParent();
        ExtendedPublicKey eip3ParentKey = ((ExtendedSecretKey) masterKey.derive(eip3ParentPath)).publicKey();
        // we need the parent's parent for its fingerprint
        ExtendedPublicKey eip3ParentParent = ((ExtendedSecretKey) masterKey.derive(
            new DerivationPath((Seq) eip3ParentPath.decodedPath().dropRight(1),
                masterKey.path().publicBranch()))).publicKey();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(getPublicHeader(networkType), 0, 4);
        out.write((byte) (eip3ParentKey.path().depth() - 1));
        out.write(calculateFingerPrint(eip3ParentParent.keyBytes()), 0, 4);
        out.write(new byte[]{0, 0, 0, 0}, 0, 4);
        out.write(eip3ParentKey.chainCode(), 0, 32);
        out.write(eip3ParentKey.keyBytes(), 0, 33);
        return out.toByteArray();
    }

    private static byte[] calculateFingerPrint(byte[] key) {
        byte[] hashedKey = Sha256.hash(key);
        return Arrays.copyOfRange(new RIPEMD160.Digest().digest(hashedKey), 0, 4);
    }

    private static byte[] getPublicHeader(NetworkType networkType) {
        if (networkType == NetworkType.MAINNET) {
            return new byte[]{0x04, (byte) 0x88, (byte) 0xB2, 0x1E};
        } else {
            return new byte[]{0x04, 0x35, (byte) 0x87, (byte) 0xCF};
        }
    }

    /**
     * parses a hex-encoded serialized public key and returns it to use for address derivation
     */
    public static ExtendedPublicKey parseExtendedPublicKeyFromHex(String hexEncodedKey, NetworkType networkType) {
        byte[] xpub = Base16$.MODULE$.decode(hexEncodedKey).get();
        return parseExtendedPublicKey(xpub, networkType);
    }

    /**
     * parses a serialized public key and returns it to use for address derivation
     */
    public static ExtendedPublicKey parseExtendedPublicKey(byte[] serializedKey, NetworkType networkType) {
        if (serializedKey.length != 78) {
            throw new IllegalArgumentException("xpubkey should have length of 78 bytes.");
        }
        if (!Arrays.equals(getPublicHeader(networkType), Arrays.copyOfRange(serializedKey, 0, 4))) {
            throw new IllegalArgumentException("Given xpubkey did not start with expected version bytes.");
        }

        return new ExtendedPublicKey(Arrays.copyOfRange(serializedKey, 45, 78),
            Arrays.copyOfRange(serializedKey, 13, 32 + 13),
            new DerivationPath((Seq) Constants.eip3DerivationPath().decodedPath().dropRight(1),
                true));
    }
}

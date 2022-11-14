package org.ergoplatform.appkit;

import org.ergoplatform.P2PKAddress;
import org.ergoplatform.wallet.secrets.ExtendedSecretKey;
import org.ergoplatform.wallet.secrets.JsonSecretStorage;
import org.ergoplatform.wallet.settings.EncryptionSettings;
import org.ergoplatform.wallet.settings.SecretStorageSettings;
import scala.Option;
import scala.runtime.BoxedUnit;
import scala.util.Failure;
import scala.util.Try;
import sigmastate.basics.DLogProtocol;

import java.io.File;

/**
 * Encrypted storage of mnemonic phrase in a file which can be accessed using password.
 */
public class SecretStorage {
    public static EncryptionSettings DEFAULT_SETTINGS = new EncryptionSettings(
            "HmacSHA256", 128000, 256);

    private final JsonSecretStorage _jsonStorage;

    SecretStorage(JsonSecretStorage jsonStorage) {
        _jsonStorage = jsonStorage;
    }

    /**
     * @return true if this storage is locked (call {@link #unlock(String pass)} to unlock this storage).
     */
    public boolean isLocked() { return _jsonStorage.isLocked(); }

    /**
     * @return underlying storage file
     */
    public File getFile() { return _jsonStorage.secretFile(); }

    public ExtendedSecretKey getSecret() {
        Option<ExtendedSecretKey> secretOpt = _jsonStorage.secret();
        if (secretOpt.isEmpty()) return null;
        return secretOpt.get();
    }

    public Address getAddressFor(NetworkType networkType) {
        DLogProtocol.ProveDlog pk = _jsonStorage.secret().get().publicImage();
        P2PKAddress p2pk = JavaHelpers.createP2PKAddress(pk, networkType.networkPrefix);
        return new Address(p2pk);
    }

    public void unlock(SecretString encryptionPass) {
        Try<BoxedUnit> resTry = _jsonStorage.unlock(encryptionPass.toInterface4JSecretString());
        if (resTry.isFailure()) {
            Throwable cause = ((Failure)resTry).exception();
            throw new RuntimeException("Cannot unlock secrete storage.", cause);
        }
    }

    public void unlock(String encryptionPass) {
        unlock(SecretString.create(encryptionPass));
    }

    /**
     * Initializes storage with the seed derived from an existing mnemonic phrase.
     * @param mnemonic - mnemonic phase
     * @param encryptionPass - encryption password
     * @param usePre1627KeyDerivation use incorrect(previous) BIP32 derivation, expected to be false for new 
     * wallets, and true for old pre-1627 wallets (see https://github.com/ergoplatform/ergo/issues/1627 for details)
    */
    public static SecretStorage createFromMnemonicIn(
            String secretDir, Mnemonic mnemonic, SecretString encryptionPassword, Boolean usePre1627KeyDerivation) {
        SecretStorageSettings settings = new SecretStorageSettings(secretDir, DEFAULT_SETTINGS);

        SecretString password = mnemonic.getPassword();

        JsonSecretStorage jsonStorage = JsonSecretStorage
            .restore(mnemonic.getPhrase().toInterface4JSecretString(),
                JavaHelpers.secretStringToOption(password != null ?
                    password.toInterface4JSecretString() : null),
                encryptionPassword.toInterface4JSecretString(),
                settings, usePre1627KeyDerivation);

        return new SecretStorage(jsonStorage);
    }

    public static SecretStorage createFromMnemonicIn(
            String secretDir, Mnemonic mnemonic, String encryptionPassword, Boolean usePre1627KeyDerivation) {
        return createFromMnemonicIn(secretDir, mnemonic, SecretString.create(encryptionPassword), usePre1627KeyDerivation);
    }

    public static SecretStorage loadFrom(String storageFileName) {
        File file = new File(storageFileName);
        return loadFrom(file);
    }

    public static SecretStorage loadFrom(File storageFile) {
        if (!storageFile.exists())
            throw new RuntimeException("SecreteStorage file not found: " + storageFile.getPath());
        return new SecretStorage(new JsonSecretStorage(storageFile, DEFAULT_SETTINGS));
    }
}

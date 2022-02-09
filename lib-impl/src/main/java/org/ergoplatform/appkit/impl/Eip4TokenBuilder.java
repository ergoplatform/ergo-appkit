package org.ergoplatform.appkit.impl;

import org.ergoplatform.appkit.Eip4Token;
import org.ergoplatform.appkit.ErgoValue;
import org.ergoplatform.explorer.client.DefaultApi;
import org.ergoplatform.explorer.client.model.AdditionalRegister;
import org.ergoplatform.explorer.client.model.AdditionalRegisters;
import org.ergoplatform.explorer.client.model.OutputInfo;
import org.ergoplatform.explorer.client.model.TokenInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import retrofit2.Response;
import special.collection.Coll;

public class Eip4TokenBuilder {
    /**
     * @param id                  token id
     * @param amount              token amount
     * @param additionalRegisters list of registers R4-R9, deserialized to byte[]. Must at least contain R4-R6.
     */
    public static Eip4Token buildFromRegisters(@Nonnull String id, long amount,
                                               @Nonnull List<String> additionalRegisters) {
        if (additionalRegisters.size() < 3) {
            throw new IllegalArgumentException("EIP-4 compliant minting box additionalRegisters must contain at least R4 to R6");
        }

        ErgoValue<Coll<Object>> r4 = getCollByteValue(Objects.requireNonNull(additionalRegisters.get(0)));
        ErgoValue<Coll<Object>> r5 = getCollByteValue(Objects.requireNonNull(additionalRegisters.get(1)));
        ErgoValue<Coll<Object>> r6 = getCollByteValue(Objects.requireNonNull(additionalRegisters.get(2)));

        ErgoValue<?> r7 = (additionalRegisters.size() > 3 && additionalRegisters.get(3) != null) ?
            ErgoValue.fromHex(additionalRegisters.get(3)) : null;
        ErgoValue<?> r8 = (additionalRegisters.size() > 4 && additionalRegisters.get(4) != null) ?
            ErgoValue.fromHex(additionalRegisters.get(4)) : null;
        ErgoValue<?> r9 = (additionalRegisters.size() > 5 && additionalRegisters.get(5) != null) ?
            ErgoValue.fromHex(additionalRegisters.get(5)) : null;

        return new Eip4Token(id, amount, r4, r5, r6, r7, r8, r9);
    }

    private static ErgoValue<Coll<Object>> getCollByteValue(String serializedValue) {
        return (ErgoValue<Coll<Object>>) ErgoValue.fromHex(serializedValue);
    }

    /**
     * @param id        token id
     * @param amount    token amount
     * @param registers list of registers R4-R9 as returned by Explorer Transactions API
     */
    public static Eip4Token buildFromRegisters(@Nonnull String id, long amount,
                                               @Nonnull AdditionalRegisters registers) {

        ArrayList<String> additionalRegisters = new ArrayList<>();
        // required by EIP4
        additionalRegisters.add(Objects.requireNonNull(getSerializedErgoValueForRegister(registers, "R4")));
        additionalRegisters.add(Objects.requireNonNull(getSerializedErgoValueForRegister(registers, "R5")));
        additionalRegisters.add(Objects.requireNonNull(getSerializedErgoValueForRegister(registers, "R6")));

        // optional by EIP4
        additionalRegisters.add(getSerializedErgoValueForRegister(registers, "R7"));
        additionalRegisters.add(getSerializedErgoValueForRegister(registers, "R8"));
        additionalRegisters.add(getSerializedErgoValueForRegister(registers, "R9"));

        return buildFromRegisters(id, amount, additionalRegisters);
    }

    /**
     *
     * @return Eip4Token for given tokenId, or null of not available
     */
    public static Eip4Token buildFromExplorerByTokenId(DefaultApi explorerApiClient, String tokenId) throws IOException {
        Response<TokenInfo> tokenInfo = explorerApiClient.getApiV1TokensP1(tokenId).execute();

        if (!tokenInfo.isSuccessful()) {
            return null;
        }

        OutputInfo boxInfo = explorerApiClient.getApiV1BoxesP1(tokenInfo.body().getBoxId()).execute().body();

        return buildFromRegisters(tokenId, boxInfo.getAssets().get(0).getAmount(),
            boxInfo.getAdditionalRegisters());

    }

    private static String getSerializedErgoValueForRegister(AdditionalRegisters registers, String registerId) {
        AdditionalRegister register = registers.get(registerId);
        if (register == null)
            return null;
        else {
            return register.serializedValue;
        }
    }

}

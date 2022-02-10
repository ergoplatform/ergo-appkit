package org.ergoplatform.appkit.impl;

import org.ergoplatform.ErgoBoxCandidate;
import org.ergoplatform.appkit.Eip4Token;
import org.ergoplatform.appkit.ErgoToken;
import org.ergoplatform.appkit.ErgoValue;
import org.ergoplatform.appkit.Iso;
import org.ergoplatform.appkit.JavaHelpers;
import org.ergoplatform.explorer.client.DefaultApi;
import org.ergoplatform.explorer.client.model.AdditionalRegister;
import org.ergoplatform.explorer.client.model.AdditionalRegisters;
import org.ergoplatform.explorer.client.model.AssetInstanceInfo;
import org.ergoplatform.explorer.client.model.OutputInfo;
import org.ergoplatform.explorer.client.model.TokenInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import retrofit2.Response;
import special.collection.Coll;

public class Eip4TokenBuilder {
    /**
     * @param id                  token id
     * @param amount              token amount
     * @param additionalRegisters list of registers R4-R9, deserialized to byte[]. Must at least contain R4-R6.
     */
    @Nonnull
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
    @Nonnull
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
     * @return Eip4Token for given tokenId, or null of not available
     */
    @Nullable
    public static Eip4Token buildFromExplorerByTokenId(DefaultApi explorerApiClient, String tokenId) throws IOException {
        Response<TokenInfo> tokenInfo = explorerApiClient.getApiV1TokensP1(tokenId).execute();

        if (!tokenInfo.isSuccessful()) {
            return null;
        }

        return buildFromExplorerByIssuingBox(explorerApiClient, tokenId, tokenInfo.body().getBoxId());

    }

    @Nullable
    public static Eip4Token buildFromExplorerByIssuingBox(DefaultApi explorerApiClient,
                                                          String tokenId, String issuingBoxId) throws IOException {
        OutputInfo boxInfo = explorerApiClient.getApiV1BoxesP1(issuingBoxId).execute().body();

        if (boxInfo == null) {
            return null;
        }

        AssetInstanceInfo tokenInfo = null;
        for (AssetInstanceInfo assetInstanceInfo : boxInfo.getAssets()) {
            if (assetInstanceInfo.getTokenId().equals(tokenId))
                tokenInfo = assetInstanceInfo;
        }

        if (tokenInfo == null) {
            throw new IllegalArgumentException("Token with id " + tokenId + " not found in box " + issuingBoxId);
        }

        return buildFromRegisters(tokenId, tokenInfo.getAmount(), boxInfo.getAdditionalRegisters());
    }

    @Nullable
    public static Eip4Token buildFromErgoBoxCandidate(@Nonnull String tokenId, @Nonnull ErgoBoxCandidate boxCandidate) {
        ErgoToken foundToken = null;
        for (ErgoToken token : Iso.isoTokensListToPairsColl().from(boxCandidate.additionalTokens())) {
            if (token.getId().toString().equals(tokenId))
                foundToken = token;
        }

        if (foundToken == null) {
            throw new IllegalArgumentException("Token with id " + tokenId + " not found in box");
        }

        List<ErgoValue<?>> boxRegisters = JavaHelpers.getBoxRegisters(boxCandidate);

        return new Eip4Token(tokenId, foundToken.getValue(),
            (ErgoValue<Coll<Object>>) boxRegisters.get(0),
            (ErgoValue<Coll<Object>>) boxRegisters.get(1),
            (ErgoValue<Coll<Object>>) boxRegisters.get(2),
            boxRegisters.size() > 3 ? boxRegisters.get(3) : null,
            boxRegisters.size() > 4 ? boxRegisters.get(4) : null,
            boxRegisters.size() > 5 ? boxRegisters.get(5) : null);
    }

    private static String getSerializedErgoValueForRegister(AdditionalRegisters registers, String registerId) {
        AdditionalRegister register = registers.get(registerId);
        if (register == null)
            return null;
        else {
            return register.serializedValue;
        }
    }

    /**
     * Use this method to mint a picture NFT token with
     * {@link org.ergoplatform.appkit.OutBoxBuilder#mintToken(Eip4Token)}
     *
     * @param id                token id
     * @param amount            amount to issue
     * @param name              display name
     * @param description       description for new token
     * @param decimals          decimals to format the amount with
     * @param sha256ContentHash sha-256 hash of content
     * @param linkToImage       link to the content
     * @return Eip4Token representation
     */
    public static Eip4Token buildNftPictureToken(@Nonnull String id, long amount, @Nonnull String name,
                                                 @Nonnull String description, int decimals,
                                                 @Nonnull byte[] sha256ContentHash,
                                                 @Nullable String linkToImage) {

        return buildNftToken(id, amount, name, description, decimals,
            Eip4Token.AssetType.NFT_PICTURE, sha256ContentHash, linkToImage, null);
    }

    /**
     * Use this method to mint a video NFT token with
     * {@link org.ergoplatform.appkit.OutBoxBuilder#mintToken(Eip4Token)}
     *
     * @param id                token id
     * @param amount            amount to issue
     * @param name              display name
     * @param description       description for new token
     * @param decimals          decimals to format the amount with
     * @param sha256ContentHash sha-256 hash of content
     * @param linkToVideo       link to the content
     * @return Eip4Token representation
     */
    public static Eip4Token buildNftVideoToken(@Nonnull String id, long amount, @Nonnull String name,
                                               @Nonnull String description, int decimals,
                                               @Nonnull byte[] sha256ContentHash,
                                               @Nullable String linkToVideo) {

        return buildNftToken(id, amount, name, description, decimals,
            Eip4Token.AssetType.NFT_VIDEO, sha256ContentHash, linkToVideo, null);
    }

    /**
     * Use this method to mint an audio NFT token with
     * {@link org.ergoplatform.appkit.OutBoxBuilder#mintToken(Eip4Token)}
     *
     * @param id                token id
     * @param amount            amount to issue
     * @param name              display name
     * @param description       description for new token
     * @param decimals          decimals to format the amount with
     * @param sha256ContentHash sha-256 hash of content
     * @param linkToAudio       link to the content
     * @param linkToCover       link to a cover image
     * @return Eip4Token representation
     */
    public static Eip4Token buildNftAudioToken(@Nonnull String id, long amount, @Nonnull String name,
                                               @Nonnull String description, int decimals,
                                               @Nonnull byte[] sha256ContentHash,
                                               @Nullable String linkToAudio,
                                               @Nullable String linkToCover) {

        return buildNftToken(id, amount, name, description, decimals,
            Eip4Token.AssetType.NFT_AUDIO, sha256ContentHash, linkToAudio, linkToCover);
    }

    private static Eip4Token buildNftToken(@Nonnull String id, long amount, @Nonnull String name,
                                           @Nonnull String description, int decimals,
                                           Eip4Token.AssetType type,
                                           @Nonnull byte[] sha256ContentHash,
                                           @Nullable String linkToContent,
                                           @Nullable String linkToCoverImage) {


        ErgoValue<Coll<scala.Byte>> r7 = ErgoValue.of(type.getR7ByteArrayForType());
        ErgoValue<Coll<scala.Byte>> r8 = ErgoValue.of(sha256ContentHash);
        ErgoValue<?> r9;

        if (linkToContent != null && linkToCoverImage == null)
            r9 = ErgoValue.of(linkToContent.getBytes(StandardCharsets.UTF_8));
        else if (linkToContent != null && linkToCoverImage != null)
            r9 = ErgoValue.pairOf(ErgoValue.of(linkToContent.getBytes(StandardCharsets.UTF_8)),
                ErgoValue.of(linkToCoverImage.getBytes(StandardCharsets.UTF_8)));
        else
            r9 = null;

        Eip4Token eip4Token = new Eip4Token(id, amount, name, description, decimals, r7, r8, r9);

        if (!eip4Token.isNftAssetType()) {
            throw new IllegalArgumentException("Constructed token is no NFT type");
        }

        return eip4Token;
    }
}

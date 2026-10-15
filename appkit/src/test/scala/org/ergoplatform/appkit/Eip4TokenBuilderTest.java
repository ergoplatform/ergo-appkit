package org.ergoplatform.appkit;

import org.ergoplatform.appkit.impl.Eip4TokenBuilder;
import org.ergoplatform.explorer.client.DefaultApi;
import org.ergoplatform.sdk.ErgoToken;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sigma.ast.ErgoTree;

public class Eip4TokenBuilderTest {

    @Test
    public void buildFromExplorerTest() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(RestApiErgoClient.defaultMainnetExplorerUrl + "/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        DefaultApi defaultApi = retrofit.create(DefaultApi.class);

        Eip4Token sigUsd = Eip4TokenBuilder.buildFromExplorerByTokenId(defaultApi,
            "03faf2cb329f2e90d6d23b58d91bbb6c046aa143261cc21f52fbe2824bfcbf04");
        Eip4Token nftPicture = Eip4TokenBuilder.buildFromExplorerByTokenId(defaultApi,
            "87f5a614e40399c40ad9b2ffe521f9576e34824ddfd29d246f019bb4599866b4");
        Eip4Token audioWithCover = Eip4TokenBuilder.buildFromExplorerByTokenId(defaultApi,
            "6d35874826256ba6f41d896663b5ac461a04264142e46192dd3cd42538c5cee4");
        Eip4Token notAvailable = Eip4TokenBuilder.buildFromExplorerByTokenId(defaultApi,
            "unusedtokenid");

        Assert.assertNull(notAvailable);
        Assert.assertNotNull(sigUsd);
        Assert.assertNotNull(nftPicture);
        Assert.assertNotNull(audioWithCover);

        Assert.assertEquals(Eip4Token.AssetType.NONE, sigUsd.getAssetType());
        Assert.assertEquals(Eip4Token.AssetType.NFT_PICTURE, nftPicture.getAssetType());
        Assert.assertEquals(Eip4Token.AssetType.NFT_AUDIO, audioWithCover.getAssetType());

        Assert.assertEquals(2, sigUsd.getDecimals());
        Assert.assertEquals(0, nftPicture.getDecimals());

        Assert.assertTrue(nftPicture.isNftAssetType());
        Assert.assertTrue(audioWithCover.isNftAssetType());
        Assert.assertFalse(sigUsd.isNftAssetType());

        Assert.assertNotNull(nftPicture.getNftContentHash());
        Assert.assertNotNull(audioWithCover.getNftContentHash());
        Assert.assertNull(sigUsd.getNftContentHash());

        Assert.assertNull(sigUsd.getNftContentLink());
        Assert.assertEquals("ipfs://bafybeieswxwp6hzu36pu7pznwybcnopl4wwrfsny4ag4b6rwkcm7rpjeyq",
            nftPicture.getNftContentLink());
        Assert.assertEquals("ipfs://bafybeih5aigkrqiopylhvv24utehjb3optd3xhaxlheh7n6de4kloulfma",
            audioWithCover.getNftContentLink());

        Assert.assertNull(sigUsd.getNftCoverImageLink());
        Assert.assertNull(nftPicture.getNftCoverImageLink());
        Assert.assertEquals("ipfs://bafkreigzktykbpkbpknofphvmae2z2bwzqdg3artn26nnxr7akal7lnm2m",
            audioWithCover.getNftCoverImageLink());
    }

    @Test
    public void buildNftPictureTest() {
        Eip4Token nftPicture = Eip4TokenBuilder.buildNftPictureToken(
            "87f5a614e40399c40ad9b2ffe521f9576e34824ddfd29d246f019bb4599866b4",
            1,
            "Test NFT",
            "Description for Pic NFT",
            0,
            new byte[256],
            null);

        String linkToImage = "https://test.link";
        Eip4Token nftPictureWithLink = Eip4TokenBuilder.buildNftPictureToken(
            "87f5a614e40399c40ad9b2ffe521f9576e34824ddfd29d246f019bb4599866b4",
            1,
            "Test NFT",
            "",
            0,
            new byte[256],
            linkToImage);

        Assert.assertNotNull(nftPicture);
        Assert.assertNotNull(nftPictureWithLink);

        Assert.assertEquals(Eip4Token.AssetType.NFT_PICTURE, nftPicture.getAssetType());
        Assert.assertEquals(Eip4Token.AssetType.NFT_PICTURE, nftPictureWithLink.getAssetType());

        Assert.assertEquals(0, nftPicture.getDecimals());

        Assert.assertTrue(nftPicture.isNftAssetType());
        Assert.assertTrue(nftPictureWithLink.isNftAssetType());

        Assert.assertNotNull(nftPicture.getNftContentHash());
        Assert.assertNotNull(nftPictureWithLink.getNftContentHash());

        Assert.assertNull(nftPicture.getNftContentLink());
        Assert.assertEquals(linkToImage, nftPictureWithLink.getNftContentLink());

        Assert.assertNull(nftPicture.getNftCoverImageLink());
        Assert.assertNull(nftPictureWithLink.getNftCoverImageLink());

        Assert.assertTrue(nftPictureWithLink.getTokenDescription().isEmpty());
    }

    @Test
    public void buildNftAudioTest() {
        Eip4Token nftWithCover = Eip4TokenBuilder.buildNftAudioToken(
            "6d35874826256ba6f41d896663b5ac461a04264142e46192dd3cd42538c5cee4",
            1,
            "Test NFT",
            "Description for Audio NFT",
            0,
            new byte[256],
            "linkToAudio",
            "linkToCover");

        Eip4Token nftAudio = Eip4TokenBuilder.buildNftAudioToken(
            "6d35874826256ba6f41d896663b5ac461a04264142e46192dd3cd42538c5cee4",
            1,
            "Test NFT",
            "Description for Audio NFT",
            0,
            new byte[256],
            "linkToAudio",
            null);


        Assert.assertNotNull(nftWithCover);
        Assert.assertNotNull(nftAudio);

        Assert.assertEquals(Eip4Token.AssetType.NFT_AUDIO, nftWithCover.getAssetType());
        Assert.assertEquals(Eip4Token.AssetType.NFT_AUDIO, nftAudio.getAssetType());

        Assert.assertEquals(0, nftWithCover.getDecimals());

        Assert.assertTrue(nftWithCover.isNftAssetType());
        Assert.assertTrue(nftAudio.isNftAssetType());

        Assert.assertNotNull(nftWithCover.getNftContentHash());
        Assert.assertNotNull(nftAudio.getNftContentHash());

        Assert.assertNotNull(nftWithCover.getNftContentLink());
        Assert.assertNotNull(nftAudio.getNftContentLink());

        Assert.assertNotNull(nftWithCover.getNftCoverImageLink());
        Assert.assertNull(nftAudio.getNftCoverImageLink());
    }

    @Test
    public void buildNftArtworkCollectionTokenTest() {
        Eip4Token collectionToken = Eip4TokenBuilder.buildNftArtworkCollectionToken(
            "6d35874826256ba6f41d896663b5ac461a04264142e46192dd3cd42538c5cee4",
            1,
            "Collection Test Token",
            "Description for Collection Test Token",
            0
        );


        Assert.assertNotNull(collectionToken);

        Assert.assertEquals(Eip4Token.AssetType.ARTWORK_COLLECTION, collectionToken.getAssetType());

        Assert.assertEquals(0, collectionToken.getDecimals());

        Assert.assertTrue(collectionToken.isNftAssetType());

        Assert.assertNull(collectionToken.getNftContentLink());

        Assert.assertNull(collectionToken.getNftCoverImageLink());
    }

    private static void expectIllegalArgument(Runnable action, String messagePart) {
        try {
            action.run();
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            if (messagePart != null) {
                Assert.assertTrue("exception message should mention " + messagePart +
                        ", actual: " + e.getMessage(),
                    e.getMessage() != null && e.getMessage().contains(messagePart));
            }
        }
    }

    private static TransactionBox stubBox(String tokenId, List<ErgoValue<?>> registers) {
        return new TransactionBox() {
            @Override
            public long getValue() {
                return 1000000L;
            }

            @Override
            public int getCreationHeight() {
                return 1;
            }

            @Override
            public List<ErgoToken> getTokens() {
                return Collections.singletonList(new ErgoToken(tokenId, 1));
            }

            @Override
            public List<ErgoValue<?>> getRegisters() {
                return registers;
            }

            @Override
            public ErgoTree getErgoTree() {
                return null;
            }

            @Override
            public BoxAttachment getAttachment() {
                return null;
            }
        };
    }

    @Test
    public void buildFromMalformedRegistersTest() {
        String tokenId = "03faf2cb329f2e90d6d23b58d91bbb6c046aa143261cc21f52fbe2824bfcbf04";
        String validR4 = ErgoValue.of("TokenName".getBytes(StandardCharsets.UTF_8)).toHex();
        String validR5 = ErgoValue.of("Description".getBytes(StandardCharsets.UTF_8)).toHex();
        String validR6 = ErgoValue.of("2".getBytes(StandardCharsets.UTF_8)).toHex();

        // valid registers build a token as before
        Eip4Token valid = Eip4TokenBuilder.buildFromHexEncodedRegisters(tokenId, 1,
            Arrays.asList(validR4, validR5, validR6));
        Assert.assertEquals("TokenName", valid.getTokenName());
        Assert.assertEquals("Description", valid.getTokenDescription());
        Assert.assertEquals(2, valid.getDecimals());

        // R4 is not a Coll at all (a single Long constant)
        expectIllegalArgument(() -> Eip4TokenBuilder.buildFromHexEncodedRegisters(tokenId, 1,
            Arrays.asList(ErgoValue.of(42L).toHex(), validR5, validR6)), "R4");

        // R5 is a Coll[Long], not a Coll[Byte]
        expectIllegalArgument(() -> Eip4TokenBuilder.buildFromHexEncodedRegisters(tokenId, 1,
            Arrays.asList(validR4, ErgoValue.of(new long[]{1, 2}).toHex(), validR6)), "R5");

        // R6 does not contain a number string
        expectIllegalArgument(() -> Eip4TokenBuilder.buildFromHexEncodedRegisters(tokenId, 1,
            Arrays.asList(validR4, validR5,
                ErgoValue.of("abc".getBytes(StandardCharsets.UTF_8)).toHex())), "R6");

        // R6 is empty
        expectIllegalArgument(() -> Eip4TokenBuilder.buildFromHexEncodedRegisters(tokenId, 1,
            Arrays.asList(validR4, validR5, ErgoValue.of(new byte[0]).toHex())), "R6");

        // R6 contains a number string out of int range
        expectIllegalArgument(() -> Eip4TokenBuilder.buildFromHexEncodedRegisters(tokenId, 1,
            Arrays.asList(validR4, validR5,
                ErgoValue.of("99999999999999999999".getBytes(StandardCharsets.UTF_8)).toHex())), "R6");

        // a box holding the token but only R4 and R5 (R6 missing)
        TransactionBox boxMissingR6 = stubBox(tokenId, Arrays.asList(
            ErgoValue.of("TokenName".getBytes(StandardCharsets.UTF_8)),
            ErgoValue.of("Description".getBytes(StandardCharsets.UTF_8))));
        expectIllegalArgument(() -> Eip4TokenBuilder.buildFromErgoBox(tokenId, boxMissingR6), null);

        // a box whose R4 is a Long, not a Coll[Byte]
        TransactionBox boxWithLongR4 = stubBox(tokenId, Arrays.asList(
            ErgoValue.of(42L),
            ErgoValue.of("Description".getBytes(StandardCharsets.UTF_8)),
            ErgoValue.of("2".getBytes(StandardCharsets.UTF_8))));
        expectIllegalArgument(() -> Eip4TokenBuilder.buildFromErgoBox(tokenId, boxWithLongR4), "R4");
    }

}

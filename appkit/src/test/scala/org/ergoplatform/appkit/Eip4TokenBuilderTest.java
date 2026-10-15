package org.ergoplatform.appkit;

import org.ergoplatform.appkit.impl.Eip4TokenBuilder;
import org.ergoplatform.explorer.client.DefaultApi;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    public void nftLinkWithMalformedR9Test() {
        String tokenId = "87f5a614e40399c40ad9b2ffe521f9576e34824ddfd29d246f019bb4599866b4";
        ErgoValue<?> r7 = ErgoValue.of(Eip4Token.AssetType.NFT_PICTURE.getR7ByteArrayForType());
        ErgoValue<?> r8 = ErgoValue.of(new byte[32]);
        String contentLink = "https://content.link";
        String coverLink = "https://cover.link";

        // valid shapes keep working exactly as before (pins, pass pre-fix)
        Eip4Token directLink = new Eip4Token(tokenId, 1, "Test NFT", "desc", 0, r7, r8,
            ErgoValue.of(contentLink.getBytes(StandardCharsets.UTF_8)));
        Assert.assertEquals(contentLink, directLink.getNftContentLink());
        Assert.assertNull(directLink.getNftCoverImageLink());

        Eip4Token pairLink = new Eip4Token(tokenId, 1, "Test NFT", "desc", 0, r7, r8,
            ErgoValue.pairOf(ErgoValue.of(contentLink.getBytes(StandardCharsets.UTF_8)),
                ErgoValue.of(coverLink.getBytes(StandardCharsets.UTF_8))));
        Assert.assertEquals(contentLink, pairLink.getNftContentLink());
        Assert.assertEquals(coverLink, pairLink.getNftCoverImageLink());

        // R9 as Coll[Long]: not a byte link, both getters must return null
        Eip4Token collLongR9 = new Eip4Token(tokenId, 1, "Test NFT", "desc", 0, r7, r8,
            ErgoValue.of(new long[]{1, 2, 3}));
        Assert.assertNull(collLongR9.getNftContentLink());
        Assert.assertNull(collLongR9.getNftCoverImageLink());

        // R9 as (Long, Coll[Byte]): content element is not a byte collection,
        // cover element still is and must still be returned
        Eip4Token longContentPair = new Eip4Token(tokenId, 1, "Test NFT", "desc", 0, r7, r8,
            ErgoValue.pairOf(ErgoValue.of(42L),
                ErgoValue.of(coverLink.getBytes(StandardCharsets.UTF_8))));
        Assert.assertNull(longContentPair.getNftContentLink());
        Assert.assertEquals(coverLink, longContentPair.getNftCoverImageLink());

        // R9 as (Coll[Byte], Long): cover element is not a byte collection,
        // content element still is and must still be returned
        Eip4Token longCoverPair = new Eip4Token(tokenId, 1, "Test NFT", "desc", 0, r7, r8,
            ErgoValue.pairOf(ErgoValue.of(contentLink.getBytes(StandardCharsets.UTF_8)),
                ErgoValue.of(42L)));
        Assert.assertEquals(contentLink, longCoverPair.getNftContentLink());
        Assert.assertNull(longCoverPair.getNftCoverImageLink());

        // R9 as (Coll[Long], Coll[Long]): neither element is a byte collection
        Eip4Token collLongPair = new Eip4Token(tokenId, 1, "Test NFT", "desc", 0, r7, r8,
            ErgoValue.pairOf(ErgoValue.of(new long[]{1}),
                ErgoValue.of(new long[]{2})));
        Assert.assertNull(collLongPair.getNftContentLink());
        Assert.assertNull(collLongPair.getNftCoverImageLink());
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

}

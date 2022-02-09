package org.ergoplatform.appkit;

import org.ergoplatform.appkit.impl.Eip4TokenBuilder;
import org.ergoplatform.explorer.client.DefaultApi;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

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

        Assert.assertEquals(Eip4Token.AssetType.NONE, sigUsd.getAssetType());
        Assert.assertEquals(Eip4Token.AssetType.NFT_PICTURE, nftPicture.getAssetType());
        Assert.assertEquals(Eip4Token.AssetType.NFT_AUDIO, audioWithCover.getAssetType());
        Assert.assertNull(notAvailable);

        Assert.assertEquals(2, sigUsd.getDecimals());
        Assert.assertEquals(0, nftPicture.getDecimals());

        Assert.assertTrue(nftPicture.isNftAssetType());
        Assert.assertTrue(audioWithCover.isNftAssetType());
        Assert.assertFalse(sigUsd.isNftAssetType());

        Assert.assertNotNull(nftPicture.getNftContentHash());
        Assert.assertNotNull(audioWithCover.getNftContentHash());
        Assert.assertNull(sigUsd.getNftContentHash());
    }
}

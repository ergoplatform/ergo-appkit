package org.ergoplatform.restapi.client;

import com.google.gson.annotations.SerializedName;

public class BlockchainIndexHeight {
    @SerializedName("fullHeight")
    private Long fullHeight = null;

    @SerializedName("indexedHeight")
    private Long indexedHeight = null;

    public Long getFullHeight() {
        return fullHeight;
    }

    public void setFullHeight(Long fullHeight) {
        this.fullHeight = fullHeight;
    }

    public Long getIndexedHeight() {
        return indexedHeight;
    }

    public void setIndexedHeight(Long indexedHeight) {
        this.indexedHeight = indexedHeight;
    }
}

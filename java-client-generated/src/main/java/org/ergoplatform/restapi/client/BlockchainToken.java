package org.ergoplatform.restapi.client;

import com.google.gson.annotations.SerializedName;

public class BlockchainToken {
    @SerializedName("id")
    private String id;

    @SerializedName("boxId")
    private String boxId;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("decimals")
    private int decimals;

    @SerializedName("emissionAmount")
    private Long emissionAmount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBoxId() {
        return boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public Long getEmissionAmount() {
        return emissionAmount;
    }

    public void setEmissionAmount(Long emissionAmount) {
        this.emissionAmount = emissionAmount;
    }
}

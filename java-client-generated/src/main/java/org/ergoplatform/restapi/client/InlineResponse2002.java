/*
 * Ergo Node API
 * API docs for Ergo Node. Models are shared between all Ergo products
 *
 * OpenAPI spec version: 4.0.12
 * Contact: ergoplatform@protonmail.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package org.ergoplatform.restapi.client;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
/**
 * InlineResponse2002
 */


public class InlineResponse2002 {
  @SerializedName("isInitialized")
  private Boolean isInitialized = null;

  @SerializedName("isUnlocked")
  private Boolean isUnlocked = null;

  @SerializedName("changeAddress")
  private String changeAddress = null;

  @SerializedName("walletHeight")
  private Integer walletHeight = null;

  public InlineResponse2002 isInitialized(Boolean isInitialized) {
    this.isInitialized = isInitialized;
    return this;
  }

   /**
   * true if wallet is initialized, false otherwise
   * @return isInitialized
  **/
  @Schema(required = true, description = "true if wallet is initialized, false otherwise")
  public Boolean isIsInitialized() {
    return isInitialized;
  }

  public void setIsInitialized(Boolean isInitialized) {
    this.isInitialized = isInitialized;
  }

  public InlineResponse2002 isUnlocked(Boolean isUnlocked) {
    this.isUnlocked = isUnlocked;
    return this;
  }

   /**
   * true if wallet is unlocked, false otherwise
   * @return isUnlocked
  **/
  @Schema(required = true, description = "true if wallet is unlocked, false otherwise")
  public Boolean isIsUnlocked() {
    return isUnlocked;
  }

  public void setIsUnlocked(Boolean isUnlocked) {
    this.isUnlocked = isUnlocked;
  }

  public InlineResponse2002 changeAddress(String changeAddress) {
    this.changeAddress = changeAddress;
    return this;
  }

   /**
   * address to send change to. Empty when wallet is not initialized or locked. By default change address correponds to root key address, could be set via /wallet/updateChangeAddress method.
   * @return changeAddress
  **/
  @Schema(example = "3WzCFq7mkykKqi4Ykdk8BK814tkh6EsPmA42pQZxU2NRwSDgd6yB", required = true, description = "address to send change to. Empty when wallet is not initialized or locked. By default change address correponds to root key address, could be set via /wallet/updateChangeAddress method.")
  public String getChangeAddress() {
    return changeAddress;
  }

  public void setChangeAddress(String changeAddress) {
    this.changeAddress = changeAddress;
  }

  public InlineResponse2002 walletHeight(Integer walletHeight) {
    this.walletHeight = walletHeight;
    return this;
  }

   /**
   * last scanned height for the wallet (and external scans)
   * @return walletHeight
  **/
  @Schema(required = true, description = "last scanned height for the wallet (and external scans)")
  public Integer getWalletHeight() {
    return walletHeight;
  }

  public void setWalletHeight(Integer walletHeight) {
    this.walletHeight = walletHeight;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InlineResponse2002 inlineResponse2002 = (InlineResponse2002) o;
    return Objects.equals(this.isInitialized, inlineResponse2002.isInitialized) &&
        Objects.equals(this.isUnlocked, inlineResponse2002.isUnlocked) &&
        Objects.equals(this.changeAddress, inlineResponse2002.changeAddress) &&
        Objects.equals(this.walletHeight, inlineResponse2002.walletHeight);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isInitialized, isUnlocked, changeAddress, walletHeight);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse2002 {\n");
    
    sb.append("    isInitialized: ").append(toIndentedString(isInitialized)).append("\n");
    sb.append("    isUnlocked: ").append(toIndentedString(isUnlocked)).append("\n");
    sb.append("    changeAddress: ").append(toIndentedString(changeAddress)).append("\n");
    sb.append("    walletHeight: ").append(toIndentedString(walletHeight)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

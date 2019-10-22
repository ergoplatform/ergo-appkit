/*
 * Ergo Node API
 * API docs for Ergo Node. Models are shared between all Ergo products
 *
 * OpenAPI spec version: 0.1
 * Contact: ergoplatform@protonmail.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package org.ergoplatform.api.client;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Spending proof for transaction input
 */
@Schema(description = "Spending proof for transaction input")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2019-10-19T14:53:04.559Z[GMT]")
public class SpendingProof {
  @SerializedName("proofBytes")
  private String proofBytes = null;

  @SerializedName("extension")
  private Map<String, String> extension = new HashMap<>();

  public SpendingProof proofBytes(String proofBytes) {
    this.proofBytes = proofBytes;
    return this;
  }

   /**
   * Get proofBytes
   * @return proofBytes
  **/
  @Schema(required = true, description = "")
  public String getProofBytes() {
    return proofBytes;
  }

  public void setProofBytes(String proofBytes) {
    this.proofBytes = proofBytes;
  }

  public SpendingProof extension(Map<String, String> extension) {
    this.extension = extension;
    return this;
  }

  public SpendingProof putExtensionItem(String key, String extensionItem) {
    this.extension.put(key, extensionItem);
    return this;
  }

   /**
   * Variables to be put into context
   * @return extension
  **/
  @Schema(example = "{\"1\":\"a2aed72ff1b139f35d1ad2938cb44c9848a34d4dcfd6d8ab717ebde40a7304f2541cf628ffc8b5c496e6161eba3f169c6dd440704b1719e0\"}", required = true, description = "Variables to be put into context")
  public Map<String, String> getExtension() {
    return extension;
  }

  public void setExtension(Map<String, String> extension) {
    this.extension = extension;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SpendingProof spendingProof = (SpendingProof) o;
    return Objects.equals(this.proofBytes, spendingProof.proofBytes) &&
        Objects.equals(this.extension, spendingProof.extension);
  }

  @Override
  public int hashCode() {
    return Objects.hash(proofBytes, extension);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SpendingProof {\n");
    
    sb.append("    proofBytes: ").append(toIndentedString(proofBytes)).append("\n");
    sb.append("    extension: ").append(toIndentedString(extension)).append("\n");
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

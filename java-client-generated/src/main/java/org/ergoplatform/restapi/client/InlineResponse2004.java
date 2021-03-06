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
 * InlineResponse2004
 */


public class InlineResponse2004 {
  @SerializedName("derivationPath")
  private String derivationPath = null;

  @SerializedName("address")
  private String address = null;

  public InlineResponse2004 derivationPath(String derivationPath) {
    this.derivationPath = derivationPath;
    return this;
  }

   /**
   * Derivation path of the resulted secret
   * @return derivationPath
  **/
  @Schema(example = "m/1/2", required = true, description = "Derivation path of the resulted secret")
  public String getDerivationPath() {
    return derivationPath;
  }

  public void setDerivationPath(String derivationPath) {
    this.derivationPath = derivationPath;
  }

  public InlineResponse2004 address(String address) {
    this.address = address;
    return this;
  }

   /**
   * Get address
   * @return address
  **/
  @Schema(required = true, description = "")
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InlineResponse2004 inlineResponse2004 = (InlineResponse2004) o;
    return Objects.equals(this.derivationPath, inlineResponse2004.derivationPath) &&
        Objects.equals(this.address, inlineResponse2004.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(derivationPath, address);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InlineResponse2004 {\n");
    
    sb.append("    derivationPath: ").append(toIndentedString(derivationPath)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
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

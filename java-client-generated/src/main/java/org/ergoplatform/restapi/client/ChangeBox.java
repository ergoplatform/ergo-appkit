/*
 * Ergo Node API
 * API docs for Ergo Node. Models are shared between all Ergo products
 *
 * OpenAPI spec version: 4.0.12
 * Contact: ergoplatform@protonmail.com
 */

package org.ergoplatform.restapi.client;

import java.util.Objects;
import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Box model for Wallet API change output
 */
@Schema(description = "Box model for Wallet API change output")
public class ChangeBox {
  @SerializedName("value")
  private Long value = null;

  @SerializedName("tokens")
  private java.util.Map<String, Long> tokens = new java.util.HashMap<String, Long>();

  public ChangeBox value(Long value) {
    this.value = value;
    return this;
  }

   /**
   * Amount of Ergs
   * @return value
  **/
  @Schema(required = true, description = "Amount of Ergs")
  public Long getValue() {
    return value;
  }

  public void setValue(Long value) {
    this.value = value;
  }

  public ChangeBox tokens(java.util.Map<String, Long> tokens) {
    this.tokens = tokens;
    return this;
  }

  public ChangeBox putTokensItem(String key, Long tokensItem) {
    if (this.tokens == null) {
      this.tokens = new java.util.HashMap<String, Long>();
    }
    this.tokens.put(key, tokensItem);
    return this;
  }

   /**
   * IDs and amounts of other tokens
   * @return tokens
  **/
  @Schema(required = true, description = "IDs and amounts of other tokens")
  public java.util.Map<String, Long> getTokens() {
    return tokens;
  }

  public void setTokens(java.util.Map<String, Long> tokens) {
    this.tokens = tokens;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChangeBox changeBox = (ChangeBox) o;
    return Objects.equals(this.value, changeBox.value) &&
        Objects.equals(this.tokens, changeBox.tokens);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, tokens);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ChangeBox {\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    tokens: ").append(toIndentedString(tokens)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

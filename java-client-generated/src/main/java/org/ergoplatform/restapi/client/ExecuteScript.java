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
import org.ergoplatform.restapi.client.ErgoLikeContext;
/**
 * ExecuteScript
 */


public class ExecuteScript {
  @SerializedName("script")
  private String script = null;

  @SerializedName("namedConstants")
  private Object namedConstants = null;

  @SerializedName("context")
  private ErgoLikeContext context = null;

  public ExecuteScript script(String script) {
    this.script = script;
    return this;
  }

   /**
   * Sigma script to be executed
   * @return script
  **/
  @Schema(required = true, description = "Sigma script to be executed")
  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public ExecuteScript namedConstants(Object namedConstants) {
    this.namedConstants = namedConstants;
    return this;
  }

   /**
   * Environment for compiler
   * @return namedConstants
  **/
  @Schema(required = true, description = "Environment for compiler")
  public Object getNamedConstants() {
    return namedConstants;
  }

  public void setNamedConstants(Object namedConstants) {
    this.namedConstants = namedConstants;
  }

  public ExecuteScript context(ErgoLikeContext context) {
    this.context = context;
    return this;
  }

   /**
   * Get context
   * @return context
  **/
  @Schema(required = true, description = "")
  public ErgoLikeContext getContext() {
    return context;
  }

  public void setContext(ErgoLikeContext context) {
    this.context = context;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExecuteScript executeScript = (ExecuteScript) o;
    return Objects.equals(this.script, executeScript.script) &&
        Objects.equals(this.namedConstants, executeScript.namedConstants) &&
        Objects.equals(this.context, executeScript.context);
  }

  @Override
  public int hashCode() {
    return Objects.hash(script, namedConstants, context);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExecuteScript {\n");
    
    sb.append("    script: ").append(toIndentedString(script)).append("\n");
    sb.append("    namedConstants: ").append(toIndentedString(namedConstants)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
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

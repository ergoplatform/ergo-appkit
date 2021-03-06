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
import org.ergoplatform.restapi.client.ScanningPredicate;
/**
 * ScanRequest
 */


public class ScanRequest {
  @SerializedName("scanName")
  private String scanName = null;

  @SerializedName("trackingRule")
  private ScanningPredicate trackingRule = null;

  public ScanRequest scanName(String scanName) {
    this.scanName = scanName;
    return this;
  }

   /**
   * Get scanName
   * @return scanName
  **/
  @Schema(description = "")
  public String getScanName() {
    return scanName;
  }

  public void setScanName(String scanName) {
    this.scanName = scanName;
  }

  public ScanRequest trackingRule(ScanningPredicate trackingRule) {
    this.trackingRule = trackingRule;
    return this;
  }

   /**
   * Get trackingRule
   * @return trackingRule
  **/
  @Schema(description = "")
  public ScanningPredicate getTrackingRule() {
    return trackingRule;
  }

  public void setTrackingRule(ScanningPredicate trackingRule) {
    this.trackingRule = trackingRule;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScanRequest scanRequest = (ScanRequest) o;
    return Objects.equals(this.scanName, scanRequest.scanName) &&
        Objects.equals(this.trackingRule, scanRequest.trackingRule);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scanName, trackingRule);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScanRequest {\n");
    
    sb.append("    scanName: ").append(toIndentedString(scanName)).append("\n");
    sb.append("    trackingRule: ").append(toIndentedString(trackingRule)).append("\n");
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

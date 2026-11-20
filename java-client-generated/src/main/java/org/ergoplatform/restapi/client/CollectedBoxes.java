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
 * Response for requested boxes that contains ErgoBoxes and ChangeBoxes
 */
@Schema(description = "Response for requested boxes that contains ErgoBoxes and ChangeBoxes")
public class CollectedBoxes {
  @SerializedName("boxes")
  private java.util.List<ErgoTransactionOutput> boxes = new java.util.ArrayList<ErgoTransactionOutput>();

  @SerializedName("changeBoxes")
  private java.util.List<ChangeBox> changeBoxes = new java.util.ArrayList<ChangeBox>();

  public CollectedBoxes boxes(java.util.List<ErgoTransactionOutput> boxes) {
    this.boxes = boxes;
    return this;
  }

  public CollectedBoxes addBoxesItem(ErgoTransactionOutput boxesItem) {
    if (this.boxes == null) {
      this.boxes = new java.util.ArrayList<ErgoTransactionOutput>();
    }
    this.boxes.add(boxesItem);
    return this;
  }

   /**
   * ErgoBoxes that satisfy user&#x27;s request
   * @return boxes
  **/
  @Schema(required = true, description = "ErgoBoxes that satisfy user's request")
  public java.util.List<ErgoTransactionOutput> getBoxes() {
    return boxes;
  }

  public void setBoxes(java.util.List<ErgoTransactionOutput> boxes) {
    this.boxes = boxes;
  }

  public CollectedBoxes changeBoxes(java.util.List<ChangeBox> changeBoxes) {
    this.changeBoxes = changeBoxes;
    return this;
  }

  public CollectedBoxes addChangeBoxesItem(ChangeBox changeBoxesItem) {
    if (this.changeBoxes == null) {
      this.changeBoxes = new java.util.ArrayList<ChangeBox>();
    }
    this.changeBoxes.add(changeBoxesItem);
    return this;
  }

   /**
   * Boxes with excessive tokens and ergs
   * @return changeBoxes
  **/
  @Schema(required = true, description = "Boxes with excessive tokens and ergs")
  public java.util.List<ChangeBox> getChangeBoxes() {
    return changeBoxes;
  }

  public void setChangeBoxes(java.util.List<ChangeBox> changeBoxes) {
    this.changeBoxes = changeBoxes;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CollectedBoxes collectedBoxes = (CollectedBoxes) o;
    return Objects.equals(this.boxes, collectedBoxes.boxes) &&
        Objects.equals(this.changeBoxes, collectedBoxes.changeBoxes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(boxes, changeBoxes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CollectedBoxes {\n");
    sb.append("    boxes: ").append(toIndentedString(boxes)).append("\n");
    sb.append("    changeBoxes: ").append(toIndentedString(changeBoxes)).append("\n");
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

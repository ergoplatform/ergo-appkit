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
import java.util.ArrayList;
import java.util.List;
import org.ergoplatform.api.client.ErgoTransactionDataInput;
import org.ergoplatform.api.client.ErgoTransactionInput;
import org.ergoplatform.api.client.ErgoTransactionOutput;
/**
 * Ergo transaction
 */
@Schema(description = "Ergo transaction")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2019-10-19T14:53:04.559Z[GMT]")
public class ErgoTransaction {
  @SerializedName("id")
  private String id = null;

  @SerializedName("inputs")
  private List<ErgoTransactionInput> inputs = new ArrayList<>();

  @SerializedName("dataInputs")
  private List<ErgoTransactionDataInput> dataInputs = new ArrayList<>();

  @SerializedName("outputs")
  private List<ErgoTransactionOutput> outputs = new ArrayList<>();

  @SerializedName("size")
  private Integer size = null;

  public ErgoTransaction id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @Schema(description = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ErgoTransaction inputs(List<ErgoTransactionInput> inputs) {
    this.inputs = inputs;
    return this;
  }

  public ErgoTransaction addInputsItem(ErgoTransactionInput inputsItem) {
    this.inputs.add(inputsItem);
    return this;
  }

   /**
   * Many transaction inputs
   * @return inputs
  **/
  @Schema(required = true, description = "Many transaction inputs")
  public List<ErgoTransactionInput> getInputs() {
    return inputs;
  }

  public void setInputs(List<ErgoTransactionInput> inputs) {
    this.inputs = inputs;
  }

  public ErgoTransaction dataInputs(List<ErgoTransactionDataInput> dataInputs) {
    this.dataInputs = dataInputs;
    return this;
  }

  public ErgoTransaction addDataInputsItem(ErgoTransactionDataInput dataInputsItem) {
    this.dataInputs.add(dataInputsItem);
    return this;
  }

   /**
   * Many transaction data inputs
   * @return dataInputs
  **/
  @Schema(required = true, description = "Many transaction data inputs")
  public List<ErgoTransactionDataInput> getDataInputs() {
    return dataInputs;
  }

  public void setDataInputs(List<ErgoTransactionDataInput> dataInputs) {
    this.dataInputs = dataInputs;
  }

  public ErgoTransaction outputs(List<ErgoTransactionOutput> outputs) {
    this.outputs = outputs;
    return this;
  }

  public ErgoTransaction addOutputsItem(ErgoTransactionOutput outputsItem) {
    this.outputs.add(outputsItem);
    return this;
  }

   /**
   * Many transaction outputs
   * @return outputs
  **/
  @Schema(required = true, description = "Many transaction outputs")
  public List<ErgoTransactionOutput> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<ErgoTransactionOutput> outputs) {
    this.outputs = outputs;
  }

  public ErgoTransaction size(Integer size) {
    this.size = size;
    return this;
  }

   /**
   * Size in bytes
   * @return size
  **/
  @Schema(description = "Size in bytes")
  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ErgoTransaction ergoTransaction = (ErgoTransaction) o;
    return Objects.equals(this.id, ergoTransaction.id) &&
        Objects.equals(this.inputs, ergoTransaction.inputs) &&
        Objects.equals(this.dataInputs, ergoTransaction.dataInputs) &&
        Objects.equals(this.outputs, ergoTransaction.outputs) &&
        Objects.equals(this.size, ergoTransaction.size);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, inputs, dataInputs, outputs, size);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErgoTransaction {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
    sb.append("    dataInputs: ").append(toIndentedString(dataInputs)).append("\n");
    sb.append("    outputs: ").append(toIndentedString(outputs)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
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

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
import org.ergoplatform.restapi.client.MerkleProof;
/**
 * Proof that a block corresponding to given header without PoW contains some transactions
 */
@Schema(description = "Proof that a block corresponding to given header without PoW contains some transactions")

public class ProofOfUpcomingTransactions {
  @SerializedName("msgPreimage")
  private String msgPreimage = null;

  @SerializedName("txProofs")
  private java.util.List<MerkleProof> txProofs = new java.util.ArrayList<MerkleProof>();

  public ProofOfUpcomingTransactions msgPreimage(String msgPreimage) {
    this.msgPreimage = msgPreimage;
    return this;
  }

   /**
   * Base16-encoded serialized header without Proof-of-Work
   * @return msgPreimage
  **/
  @Schema(example = "0112e03c6d39d32509855be7cee9b62ff921f7a0cf6883e232474bd5b54d816dd056f846980d34c3b23098bdcf41222f8cdee5219224aa67750055926c3a2310a483accc4f9153e7a760615ea972ac67911cff111f8c17f563d6147205f58f85133ae695d1d4157e4aecdbbb29952cfa42b75129db55bddfce3bc53b8fd5b5465f10d8be8ddda62ed3b86afb0497ff2d381ed884bdae5287d20667def224a28d2b6e3ebfc78709780702c70bd8df0e000000", required = true, description = "Base16-encoded serialized header without Proof-of-Work")
  public String getMsgPreimage() {
    return msgPreimage;
  }

  public void setMsgPreimage(String msgPreimage) {
    this.msgPreimage = msgPreimage;
  }

  public ProofOfUpcomingTransactions txProofs(java.util.List<MerkleProof> txProofs) {
    this.txProofs = txProofs;
    return this;
  }

  public ProofOfUpcomingTransactions addTxProofsItem(MerkleProof txProofsItem) {
    this.txProofs.add(txProofsItem);
    return this;
  }

   /**
   * Merkle proofs of transactions included into blocks (not necessarily all the block transactions)
   * @return txProofs
  **/
  @Schema(required = true, description = "Merkle proofs of transactions included into blocks (not necessarily all the block transactions)")
  public java.util.List<MerkleProof> getTxProofs() {
    return txProofs;
  }

  public void setTxProofs(java.util.List<MerkleProof> txProofs) {
    this.txProofs = txProofs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProofOfUpcomingTransactions proofOfUpcomingTransactions = (ProofOfUpcomingTransactions) o;
    return Objects.equals(this.msgPreimage, proofOfUpcomingTransactions.msgPreimage) &&
        Objects.equals(this.txProofs, proofOfUpcomingTransactions.txProofs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(msgPreimage, txProofs);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProofOfUpcomingTransactions {\n");
    
    sb.append("    msgPreimage: ").append(toIndentedString(msgPreimage)).append("\n");
    sb.append("    txProofs: ").append(toIndentedString(txProofs)).append("\n");
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

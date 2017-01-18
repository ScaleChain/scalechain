package io.scalechain.blockchain.proto

import io.scalechain.blockchain.oap.transaction.OapTransactionOutput
import io.scalechain.blockchain.oap.wallet.AssetId

/**
  * Created by shannon on 16. 12. 13.
  */
/**
  * Class for strore Colored information of a Transaction Output
  *
  * @param output
  * @param assetId
  * @param quantity
  */
case class OutputCacheItem(output : TransactionOutput, assetId : String, quantity : Int) extends ProtocolMessage

object OutputCacheItem {
  /**
    *  Create CacheItem from assetId, quantity
    * @param assetId
    * @param quantity
    * @return
    */
  def from(output : TransactionOutput, assetId : AssetId, quantity : Int)  : OutputCacheItem = {
    OutputCacheItem(output, assetId.base58(), quantity);
  }
  def from(output: OapTransactionOutput) : OutputCacheItem = {
    OutputCacheItem(output, output.getAssetId.base58(), output.getQuantity)
  }
}
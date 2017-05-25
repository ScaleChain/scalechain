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
data class OutputCacheItem(val output : TransactionOutput, val assetId : String, val quantity : Int) : ProtocolMessage {
  companion object {
    /**
     *  Create CacheItem from assetId, quantity
     * @param assetId
     * @param quantity
     * @return
     */
    fun from(output : TransactionOutput, assetId : AssetId, quantity : Int)  : OutputCacheItem {
      return OutputCacheItem(output, assetId.base58(), quantity);
    }
    fun from(output: OapTransactionOutput) : OutputCacheItem {
      return OutputCacheItem(output.getTransactionOutput(), output.assetId.base58(), output.quantity)
    }
  }
}


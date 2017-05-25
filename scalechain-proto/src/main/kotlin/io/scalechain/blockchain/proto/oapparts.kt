package io.scalechain.blockchain.proto

import io.scalechain.util.Bytes


/**
  * Created by shannon on 16. 12. 27.
  */
//case class OapOutputCacheItem(output : TransactionOutput, assetId : String, quantity : Int) extends ProtocolMessage
//
//object OapOutputCacheItem {
//  /**
//    *  Create CacheItem from assetId, quantity
//    * @param assetId
//    * @param quantity
//    * @return
//    */
//  def from(output : TransactionOutput, assetId : AssetId, quantity : Int)  : OapOutputCacheItem = {
//    OapOutputCacheItem(output, assetId.base58(), quantity);
//  }
//}
data class FixedByteArrayMessage(val value : Bytes) : ProtocolMessage

data class StringMessage(val value:String) : ProtocolMessage
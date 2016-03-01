package io.scalechain.blockchain.api.command

import io.scalechain.blockchain.api.command.blockchain.GetBlockResult
import io.scalechain.blockchain.api.command.rawtx.{DecodedRawTransaction, SignRawTransactionResult, RawTransaction}
import io.scalechain.blockchain.api.command.wallet.{TransactionItem}
import io.scalechain.blockchain.proto._
import spray.json.{JsObject, JsValue}


// [API layer] Convert a block to a specific block format.
class BlockFormatter {
  /** Get the GetBlockResult case class instance from a block.
    *
    * Used by : getblockhash RPC.
    * @param block The block to format.
    * @return The GetBlockResult instance.
    */
  def getBlockResult(block : Block) : GetBlockResult = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Get a serialized block data.
    *
    * Used by : getblockhash RPC.
    *
    * @param block The block to format.
    * @return The serialized string value.
    */
  def getSerializedBlock(block : Block) : String = {
    // TODO : Implement
    assert(false)
    null
  }
}

// [API Layer] decode a transaction.
class TransactionDecoder {
  /** Decodes a serialized transaction hex string into a DecodedRawTransaction.
    *
    * Used by : decoderawtransaction RPC.
    *
    * @param serializedTransction The serialized transaction.
    * @return The decoded transaction, DecodedRawTransaction instance.
    */
  def decodeTransaction(serializedTransction : String) : Transaction = {
    // TODO : Implement
    assert(false)
    null
  }
}


// [API layer] Convert a transaction into a specific transaction format.
class TransactionFormatter {

  /** Get a serialized version of a transaction.
    *
    * Used by : sign raw transaction
    */
  def getSerializedTranasction(transaction : Transaction) : String = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Convert a transaction to a RawTransaction instance.
    *
    * Used by getrawtransaction RPC.
    *
    * @param transaction The transaction to convert.
    * @return The converted RawTransaction instance.
    */
  def getRawTransaction(transaction : Transaction) : RawTransaction = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Convert a transaction to DecodedRawTransaction.
    *
    * Used by : decoderawtransaction RPC.
    *
    * @param transaction The transaction to convert.
    * @return The converted DecodedRawTransaction instance.
    */
  def getDecodedRawTransaction(transaction : Transaction) : DecodedRawTransaction = {
    // TODO : Implement
    assert(false)
    null
  }



  /** Convert a transaction to a TransactionItem, which is an element of array to respond for listtransactions RPC.
    *
    * @param transaction The transaction to convert.
    * @return The converted transaction item.
    */
  def getTransactionItem( transaction : Transaction ) : TransactionItem =  {
    // TODO : Implement
    assert(false)
    null
  }
}

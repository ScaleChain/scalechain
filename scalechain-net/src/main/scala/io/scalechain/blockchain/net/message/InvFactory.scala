package io.scalechain.blockchain.net.message

import io.scalechain.blockchain.proto.{Inv, Hash}

/**
  * The factory that creates Inv messages.
  */
object InvFactory {
  /** Create an Inv message containing block inventories.
    *
    * @param blockHashes The hash of blocks to put into the block inventories.
    * @return The created Inv message.
    */
  def createBlockInventories(blockHashes : List[Hash]) : Inv = {
    // TODO : Implement
    assert(false)
    null
  }

  /** Create an Inv message containing transaction inventories.
    *
    * @param transactionHashes The hash of transactions to put into the transation inventories.
    * @return The created Inv message.
    */
  def createTransactionInventories(transactionHashes : List[Hash]) : Inv = {
    // TODO : Implement
    assert(false)
    null
  }
}

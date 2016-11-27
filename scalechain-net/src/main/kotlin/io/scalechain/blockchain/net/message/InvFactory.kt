package io.scalechain.blockchain.net.message

import io.scalechain.blockchain.proto.InvType
import io.scalechain.blockchain.proto.InvVector
import io.scalechain.blockchain.proto.Inv
import io.scalechain.blockchain.proto.Hash

/**
  * The factory that creates Inv messages.
  */
object InvFactory {
  /** Create an Inv message containing block inventories.
    *
    * @param blockHashes The hash of blocks to put into the block inventories.
    * @return The created Inv message.
    */
  fun createBlockInventories(blockHashes : List<Hash>) : Inv {
    Inv(blockHashes.map{ hash : Hash =>
      InvVector(
        InvType.MSG_BLOCK,
        hash
      )
    })
  }

  /** Create an Inv message containing transaction inventories.
    *
    * @param transactionHashes The hash of transactions to put into the transation inventories.
    * @return The created Inv message.
    */
  fun createTransactionInventories(transactionHashes : List<Hash>) : Inv {
    Inv(transactionHashes.map{ hash : Hash =>
      InvVector(
        InvType.MSG_TX,
        hash
      )
    })
  }
}

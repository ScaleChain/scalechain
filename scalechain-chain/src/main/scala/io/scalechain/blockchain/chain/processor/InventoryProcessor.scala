package io.scalechain.blockchain.chain.processor

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.{InvType, InvVector}

/**
  * Process a received Inv message.
  */
object InventoryProcessor {
  val chain = Blockchain.get

  def alreadyHas(inventory : InvVector) : Boolean = {
    inventory.invType match {
      case InvType.MSG_TX => {
        // Transaction : Check the transaction database, orphan transactions, and diskpool

      }
      case InvType.MSG_BLOCK => {
        // Block : Check the block database, orphan blocks
      }
    }
    // TODO : Implement.
    assert(false)
    false
  }
}

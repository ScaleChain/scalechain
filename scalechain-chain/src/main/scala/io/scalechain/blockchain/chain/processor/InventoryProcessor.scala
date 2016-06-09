package io.scalechain.blockchain.chain.processor

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.{InvType, InvVector}
import org.slf4j.LoggerFactory

/**
  * Process a received Inv message.
  */
object InventoryProcessor {
  private lazy val logger = LoggerFactory.getLogger(InventoryProcessor.getClass)

  val chain = Blockchain.get

  def alreadyHas(inventory : InvVector) : Boolean = {
    inventory.invType match {
      case InvType.MSG_TX => {
        // Transaction : Check the transaction database, transaction pool, and orphan transactions
        TransactionProcessor.exists(inventory.hash)
      }
      case InvType.MSG_BLOCK => {
        // Block : Check the block database, orphan blocks
        BlockProcessor.exists(inventory.hash)
      }
      case _ => {
        logger.warn(s"Unknown inventory type : ${inventory}")
        false
      }
    }
  }
}

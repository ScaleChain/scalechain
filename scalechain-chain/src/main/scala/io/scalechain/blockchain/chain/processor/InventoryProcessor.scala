package io.scalechain.blockchain.chain.processor

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.{InvType, InvVector}
import org.slf4j.LoggerFactory

object InventoryProcessor extends InventoryProcessor( Blockchain.get )

/**
  * Process a received Inv message.
  */
class InventoryProcessor(val chain : Blockchain) {
  private lazy val logger = LoggerFactory.getLogger(classOf[InventoryProcessor])

  def alreadyHas(inventory : InvVector) : Boolean = {
    inventory.invType match {
      case InvType.MSG_TX => {
        // Transaction : Check the transaction database, transaction pool, and orphan transactions
        new TransactionProcessor(chain).exists(inventory.hash)
      }
      case InvType.MSG_BLOCK => {
        // Block : Check the block database, orphan blocks
        new BlockProcessor(chain).exists(inventory.hash)
      }
      case _ => {
        logger.warn(s"Unknown inventory type : ${inventory}")
        false
      }
    }
  }
}

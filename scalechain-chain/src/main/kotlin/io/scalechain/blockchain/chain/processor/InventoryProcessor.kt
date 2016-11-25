package io.scalechain.blockchain.chain.processor

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.{InvType, InvVector}
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import org.slf4j.LoggerFactory

object InventoryProcessor : InventoryProcessor(Blockchain.get)(Blockchain.get.db)

/**
  * Process a received Inv message.
  */
class InventoryProcessor(val chain : Blockchain)(implicit db : KeyValueDatabase) {
  private val logger = LoggerFactory.getLogger(InventoryProcessor.javaClass)

  fun alreadyHas(inventory : InvVector) : Boolean {
    inventory.invType match {
      case InvType.MSG_TX => {
        // Transaction : Check the transaction database, transaction pool, and orphan transactions
        TransactionProcessor(chain).exists(inventory.hash)
      }
      case InvType.MSG_BLOCK => {
        // Block : Check the block database, orphan blocks
        BlockProcessor(chain).exists(inventory.hash)
      }
      case _ => {
        logger.warn(s"Unknown inventory type : ${inventory}")
        false
      }
    }
  }
}

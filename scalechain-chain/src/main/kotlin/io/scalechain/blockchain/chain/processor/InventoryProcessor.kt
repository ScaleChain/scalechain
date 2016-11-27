package io.scalechain.blockchain.chain.processor

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.InvType
import io.scalechain.blockchain.proto.InvVector
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import org.slf4j.LoggerFactory

/**
  * Process a received Inv message.
  */
open class InventoryProcessor(private val db : KeyValueDatabase, val chain : Blockchain) {
  private val logger = LoggerFactory.getLogger(InventoryProcessor::class.java)

  fun alreadyHas(inventory : InvVector) : Boolean {
    return when(inventory.invType) {
      InvType.MSG_TX -> {
        // Transaction : Check the transaction database, transaction pool, and orphan transactions
        TransactionProcessor(chain).exists(db, inventory.hash)
      }
      InvType.MSG_BLOCK -> {
        // Block : Check the block database, orphan blocks
        BlockProcessor(db, chain).exists(inventory.hash)
      }
      else -> {
        logger.warn("Unknown inventory type : ${inventory}")
        false
      }
    }
  }

  companion object : InventoryProcessor(Blockchain.get().db, Blockchain.get()) {
  }
}

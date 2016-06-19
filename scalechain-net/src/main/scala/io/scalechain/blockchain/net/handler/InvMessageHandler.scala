package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.chain.processor.{BlockProcessor, InventoryProcessor}
import io.scalechain.blockchain.net.MessageSummarizer
import io.scalechain.blockchain.net.message.{GetDataFactory, GetBlocksFactory}
import io.scalechain.blockchain.proto.{InvType, InvVector, Inv, ProtocolMessage}
import org.slf4j.LoggerFactory

/**
  * The message handler for Inv message.
  */
object InvMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(InvMessageHandler.getClass)

  /** Handle Inv message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param inv The Inv message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, inv : Inv ) : Unit = {

    // TODO : Step 1 : Return an error if the number of inventories is more than 50,000

    // TODO : Step 2 : Add the inventory as a known inventory to the node that sent the "inv" message.

    // Step 3 : Get a list of inventories to request data with GetData message.
    val inventoriesToGetData =
    inv.inventories.map { inventory : InvVector  =>
      // Step 3 : Check if we already have it
      if (InventoryProcessor.alreadyHas(inventory)) { // The inventory already exists.
          if (inventory.invType == InvType.MSG_BLOCK && BlockProcessor.hasOrphan(inventory.hash)) {
            // Step 3.A : If it is an orphan block, Request to get the root parent of the orphan to the peer that sent the inventory.
            val orphanRoot = BlockProcessor.getOrphanRoot(inventory.hash)
            val getBlocksMessage = GetBlocksFactory.create(inventory.hash)
            context.peer.send(getBlocksMessage)
            logger.info(s"Requesting getblocks of orphan parents in response to inv. Message : ${MessageSummarizer.summarize(getBlocksMessage)}")
          }
          None
      } else {
        // Step 3.B : If we don't have it yet, send "getdata" message to the peer that sent the "inv" message
        Some(inventory)
      }
    }.filter(_.isDefined).map(_.get) // filter out None values.

    // Step 4 : Send the GetData message to get data for the missing inventories in this node.
    val getDataMessage = GetDataFactory.create(inventoriesToGetData)
    context.peer.send(getDataMessage)
    logger.info(s"Requesting getdata in response to inv. Message : ${MessageSummarizer.summarize(getDataMessage)}")
  }
}

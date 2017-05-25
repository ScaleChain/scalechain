package io.scalechain.blockchain.net.handler

import java.util.TimerTask
import java.util.Timer

import io.scalechain.blockchain.chain.processor.InventoryProcessor
import io.scalechain.blockchain.net.Node
import io.scalechain.blockchain.net.MessageSummarizer
import io.scalechain.blockchain.net.message.GetDataFactory
import io.scalechain.blockchain.net.message.GetBlocksFactory
import io.scalechain.blockchain.proto.InvType
import io.scalechain.blockchain.proto.InvVector
import io.scalechain.blockchain.proto.Inv
import org.slf4j.LoggerFactory

/**
  * The message handler for Inv message.
  */
object InvMessageHandler {
  private val logger = LoggerFactory.getLogger(InvMessageHandler.javaClass)

  /** Handle Inv message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param inv The Inv message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  fun handle( context : MessageHandlerContext, inv : Inv ) : Unit {

    // TODO : Step 1 : Return an error if the number of inventories is more than 50,000

    // TODO : Step 2 : Add the inventory as a known inventory to the node that sent the "inv" message.
    logger.trace("Handling Inventories receieved.")

    var blockInventories = 0
    val inventoriesToGetData =
      // Step 3 : Get a list of inventories to request data with GetData message.
      inv.inventories.map { inventory : InvVector ->
        if (inventory.invType == InvType.MSG_BLOCK) {
          blockInventories += 1
        }
        // Step 3 : Check if we already have it
        if (InventoryProcessor.alreadyHas(inventory)) {
          // The inventory already exists.
          /*
          if (inventory.invType == InvType.MSG_BLOCK && BlockProcessor.hasOrphan(inventory.hash)) {
            assert(!BlockProcessor.hasNonOrphan(inventory.hash))
            // Step 3.A : If it is an orphan block, Request to get the root parent of the orphan to the peer that sent the inventory.
            val orphanRoot = BlockProcessor.getOrphanRoot(inventory.hash)
            val getBlocksMessage = GetBlocksFactory.create(inventory.hash)
            context.peer.send(getBlocksMessage)
            logger.info(s"Requesting getblocks of orphan parents in response to inv. Orphan Block: ${inventory.hash}, Message : ${MessageSummarizer.summarize(getBlocksMessage)}")
          }
        */
          null
        } else {
          // Step 3.B : If we don't have it yet, send "getdata" message to the peer that sent the "inv" message
          inventory
        }
      }.filterNotNull() // filter out None values.

    // Step 4 : Send the GetData message to get data for the missing inventories in this node.
    if (inventoriesToGetData.isEmpty()) {
      // Nothing to request.
    } else {
      val getDataMessage = GetDataFactory.create(inventoriesToGetData)
      context.peer.send(getDataMessage)

      logger.trace("Requesting getdata in response to inv. Message : ${MessageSummarizer.summarize(getDataMessage)}")
    }

//    if (blockInventories == GetBlocksMessageHandler.MAX_HASH_PER_REQUEST) {
      val node = Node.get()
      if (node.isInitialBlockDownload()) {
        if (node.bestPeerForIBD == context.peer) {
          // After a second, summarize local blockchain and send getblocks again.
          val timer = Timer(true)
          timer.schedule( object : TimerTask() {
            override fun run() : Unit  {
              // TODO : BUGBUG - Use the snapshot block hash for the hash stop.
              context.peer.send( GetBlocksFactory.create() )
              logger.trace("Requesting the next batch of hashes to get the blocks.")
            }
          }, 1000);

//          context.peer.send( GetBlocksFactory.create( node.getLastBlockHashForIBD ) )
        }
      }
//    }
  }
}

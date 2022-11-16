package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.net.Node
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.net.message.GetBlocksFactory
import io.scalechain.blockchain.net.message.InvFactory
import io.scalechain.blockchain.proto.*
import io.scalechain.util.Config
import org.slf4j.LoggerFactory
import io.scalechain.blockchain.script.hash

/**
  * The message handler for Block message.
  */
object BlockMessageHandler {
  private val logger = LoggerFactory.getLogger(BlockMessageHandler.javaClass)

  // More than half of the peers should sign the block.
  val RequiredSigningTransactions = Config.get().peerAddresses().size / 2 + 1


  /** Handle Block message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param block The Block message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  fun handle( context : MessageHandlerContext, block : Block ) : Unit {

    // TODO : Revert the adoption of PBFT. The change to PBFT was by this commit: https://github.com/ScaleChain/scalechain/commit/5e58696f337042067cb895196dc388ad94251321

    // TODO : Need to apply patch for synchronization : https://github.com/ScaleChain/scalechain/commit/6ba24918e50f5ef8a1293d75b812032211a46824
    // TODO : BUGBUG : Need to think about RocksDB transactions.

    val blockHash = block.header.hash()

    logger.trace("<P2P> Received a block. Hash : ${blockHash}, Header : ${block.header}")

    // BUGBUG ; We need to check if the block is an orphan
    // BUGBUG ; We need to ask the peer to send necessary parents if the block is an orphan.
    BlockProcessor.get().validateBlock(block)
    BlockProcessor.get().acceptBlock(blockHash, block)

    /*
    // Doring block reorganization, blocks can be attached to/detached from the best blockchain,
    // But it does not affect whether a block exists, whether a block is an orphan or not.
    // So, it is safe not to synchronize the Blockchain object within the block message.

    if (BlockProcessor.get().hasNonOrphan(blockHash)) {
      logger.trace("[P2P] Duplicate block was received. Hash : ${blockHash}")
    } else if (BlockProcessor.get().hasOrphan(blockHash)) {
      logger.trace("[P2P] Duplicate orphan block was received. Hash : ${blockHash}")
    } else {

      // TODO : Add the block as a known inventory of the node that sent it.
      // pfrom->AddInventoryKnown(inv);
      BlockProcessor.get().validateBlock(block)
      // TODO : Increase DoS score of the peer if the block was invalid.
      // TODO : Make sure we do not hit any issue even though an exception is thrown by validateBlock.

      if (BlockProcessor.get().hasNonOrphan(block.header.hashPrevBlock)) { // Case 1 : Non-Orphan block.
        // Step 1.2 : Recursively bring any orphan blocks to blockchain, if the new block is the previous block of any orphan block.
        val newlyBestBlockHashes: List<Hash> = BlockProcessor.get().acceptChildren(blockHash)

        val newBlockHashes = if (acceptedAsNewBestBlock) {
          blockHash :: newlyBestBlockHashes
        } else {
          newlyBestBlockHashes
        }

        if (newBlockHashes.isEmpty) {
          // Do nothing. Nothing to send.
          logger.info("A block was ignored, as there is an orphan block being requested. ${blockHash}")
        } else {
          // Step 1.3 : Relay the newly added blocks to peers as an inventory
          val invMessage = InvFactory.createBlockInventories(newBlockHashes)
          context.communicator.sendToAll(invMessage)
          logger.trace("Propagating newly accepted blocks : ${invMessage} ")
        }
      } else { // Case 2 : Orphan block
        if (context.peer.requestedBlock().isDefined) {
          // Do not process orphan blocks. We already requested parents of an orphan root.
          logger.info("A block was ignored, as there is an orphan block being requested. ${blockHash}")
        } else {
          // Step 2.1 : Put the orphan block
          // BUGBUG : An attacker can fill up my disk with lots of orphan blocks.
          BlockProcessor.get().putOrphan(block)

          // Step 2.2 : Request the peer to send the root parent of the orphan block.
          val orphanRootHash: Hash = BlockProcessor.get().getOrphanRoot(blockHash)

          val getBlocksMessage = GetBlocksFactory.create(orphanRootHash)
          context.peer.send(getBlocksMessage)
          context.peer.blockRequested(orphanRootHash)
          logger.info("An orphan block was found. Block Hash : ${blockHash}, Previous Hash : ${block.header.hashPrevBlock}, Inventories requested : ${getBlocksMessage} ")
          logger.trace("Requesting inventories of parents of the orphan. Orphan root: ${orphanRootHash} ")
        }
      }
    }*/
  }
}

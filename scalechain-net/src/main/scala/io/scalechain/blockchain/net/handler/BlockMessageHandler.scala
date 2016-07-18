package io.scalechain.blockchain.net.handler

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.net.{IncompleteBlock, IncompleteBlockCache, BlockSigner, BlockSigningHistory}
import io.scalechain.blockchain.net.message.{InvFactory, GetBlocksFactory}
import io.scalechain.blockchain.proto.{Hash, Block, ProtocolMessage, Addr}
import io.scalechain.util.Config
import org.slf4j.LoggerFactory
import io.scalechain.blockchain.script.HashSupported._

/**
  * The message handler for Block message.
  */
object BlockMessageHandler {
  private lazy val logger = Logger( LoggerFactory.getLogger(BlockMessageHandler.getClass) )

  // More than half of the peers should sign the block.
  val RequiredSigningTransactions = Config.peerAddresses().length / 2 + 1


  /** Handle Block message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param block The Block message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, block : Block ) : Unit = {
    // TODO : BUGBUG : Need to think about RocksDB transactions.

    val blockHash = block.header.hash

    logger.trace(s"[P2P] Received a block. Hash : ${blockHash}, Header : ${block.header}")

    // Doring block reorganization, blocks can be attached to/detached from the best blockchain,
    // But it does not affect whether a block exists, whether a block is an orphan or not.
    // So, it is safe not to synchronize the Blockchain object within the block message.

    if (BlockProcessor.hasNonOrphan(blockHash)) {
      logger.trace(s"[P2P] Duplicate block was received. Hash : ${blockHash}")
    } else if (BlockProcessor.hasOrphan(blockHash)) {
      logger.trace(s"[P2P] Duplicate orphan block was received. Hash : ${blockHash}")
    } else {

      val shouldProcessBlock =
        if (Config.isPrivate && (Blockchain.get.getBestBlockHeight >= Config.InitialSetupBlocks) ) {
          val incompleteBlockOption : Option[IncompleteBlock] =
            if (BlockSigningHistory.didSignOn(blockHash)) {
              // Already signed.
              logger.trace(s"[Block Handler] An already signed block. Block Hash : ${blockHash}")
              IncompleteBlockCache.getBlock(blockHash)
            } else {
              logger.trace(s"[Block Handler] Creating and propagating a signing transaction. Block Hash : ${blockHash}")
              val signingTx = BlockSigner.get.signBlock(Blockchain.get, blockHash)
              context.communicator.sendToAll( signingTx )
              BlockSigningHistory.signedOn(blockHash)
              IncompleteBlockCache.addBlock(blockHash, block)
              val incompleteBlock = IncompleteBlockCache.addSigningTransaction(blockHash, signingTx)
              Some(incompleteBlock)
            }
          if (incompleteBlockOption.isDefined) {
            val enough = incompleteBlockOption.get.hasEnoughSigningTransactions( RequiredSigningTransactions )
            if (enough) {
              logger.trace(s"[Block Handler] A block with enough signing transactions found. Delegating to BlockMessageHandler. Block Hash : ${blockHash}")
            } else {
              logger.trace(s"[Block Handler] A block with signing transactions found. But need more signing transactions. Current Transactions : ${incompleteBlockOption.get.signingTxs.size}, Required : ${BlockMessageHandler.RequiredSigningTransactions}, Block Hash : ${blockHash}")
            }
            enough
          } else {
            false
          }
        } else {
          true
        }

      if (shouldProcessBlock) {
        // TODO : Add the block as a known inventory of the node that sent it.
        // pfrom->AddInventoryKnown(inv);
        BlockProcessor.validateBlock(block)
        // TODO : Increase DoS score of the peer if the block was invalid.
        // TODO : Make sure we do not hit any issue even though an exception is thrown by validateBlock.

        if (BlockProcessor.hasNonOrphan(block.header.hashPrevBlock)) { // Case 1 : Non-Orphan block.
        // Step 1.1 : Store the block to the blockchain. Do block reorganization if necessary.
        val acceptedAsNewBestBlock = BlockProcessor.acceptBlock(blockHash, block)

          // Step 1.2 : Recursively bring any orphan blocks to blockchain, if the new block is the previous block of any orphan block.
          val newlyBestBlockHashes : List[Hash] = BlockProcessor.acceptChildren(blockHash)

          val newBlockHashes = if (acceptedAsNewBestBlock) {
            blockHash :: newlyBestBlockHashes
          } else {
            newlyBestBlockHashes
          }

          if (newBlockHashes.isEmpty) {
            // Do nothing. Nothing to send.
            logger.info(s"A block was ignored, as there is an orphan block being requested. ${blockHash}")
          } else {
            // Step 1.3 : Relay the newly added blocks to peers as an inventory
            val invMessage = InvFactory.createBlockInventories(newBlockHashes)
            context.communicator.sendToAll(invMessage)
            logger.trace(s"Propagating newly accepted blocks : ${invMessage} ")
          }
        } else { // Case 2 : Orphan block
          if (context.peer.requestedBlock().isDefined ) {
            // Do not process orphan blocks. We already requested parents of an orphan root.
            logger.info(s"A block was ignored, as there is an orphan block being requested. ${blockHash}")
          } else {
            // Step 2.1 : Put the orphan block
            // BUGBUG : An attacker can fill up my disk with lots of orphan blocks.
            BlockProcessor.putOrphan(block)

            // Step 2.2 : Request the peer to send the root parent of the orphan block.
            val orphanRootHash : Hash = BlockProcessor.getOrphanRoot(blockHash)

            val getBlocksMessage = GetBlocksFactory.create(orphanRootHash)
            context.peer.send(getBlocksMessage)
            context.peer.blockRequested(orphanRootHash)
            logger.info(s"An orphan block was found. Block Hash : ${blockHash}, Previous Hash : ${block.header.hashPrevBlock}, Inventories requested : ${getBlocksMessage} ")
            logger.trace(s"Requesting inventories of parents of the orphan. Orphan root: ${orphanRootHash} ")
          }
        }
      }
    }

    if ( context.peer.requestedBlock().isDefined ) { // If the block hash matches the requested block, clear the requested block for getting parents of an orphan root.
      if ( blockHash == context.peer.requestedBlock().get ) {
        logger.trace(s"The requested block received. Clearing the requested block. ${blockHash}")
        context.peer.clearRequestedBlock()
      }
    }
  }
}

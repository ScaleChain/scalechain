package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.{Block, ProtocolMessage, Addr}
import org.slf4j.LoggerFactory
import io.scalechain.blockchain.script.HashSupported._

/**
  * The message handler for Block message.
  */
object BlockMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(BlockMessageHandler.getClass)

  val chain = Blockchain.get

  /** Handle Block message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param block The Block message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, block : Block ) : Option[ProtocolMessage] = {
    val blockHash = block.header.hash
    if (chain.getBlock(blockHash).isEmpty) { // Process the transaction only if we don't have it yet.
      logger.info(s"[P2P] Received a block. Hash : ${blockHash}")
      chain.putBlock(blockHash, block)

      // Propagate the block only if the block was not found.
      //peerCommunication.sendToAll(block)

/*
    // Step 1 : Add the block as a known inventory of the node that sent it.
    pfrom->AddInventoryKnown(inv);

    // Step 2 : Process the block
    ProcessBlock(pfrom, &block)
        - Step 1 : check if we already have the block by looking up mapBlockIndex and mapOrphanBlocks
        - Step 2 : Do preliminary checks for a block
        pblock->CheckBlock()
            - 1. check the serialized block size.
            - 2. check the proof of work - block hash vs target hash
            - 3. check the block timestamp.
            - 4. check the first transaction is coinbase, and others are not.
            - 5. check each transaction in a block.
            - 6. check the number of script operations on the locking/unlocking script.
            - 7. Calculate the merkle root hash, compare it with the one in the block header.

        - Step 3 : Keep the orphan block in memory if the previous block does not exist.
        if (!mapBlockIndex.count(pblock->hashPrevBlock))
            - Insert the block into mapOrphanBlocks, mapOrphanBlocksByPrev
            - pfrom->PushGetBlocks(pindexBest, GetOrphanRoot(pblock2)); // Ask for the missing parent of the orphan block.

        - Step 4 : Store the block to the blockchain if the previous block exists.
        pblock->AcceptBlock()
            - 1. Need to check if the same blockheader hash exists by looking up mapBlockIndex
            - 2. Need to increase DoS score if an orphan block was received.
            - 3. Need to increase DoS score if the block hash does not meet the required difficulty.
            - 4. Need to get the median timestamp for the past N blocks.
            - 5. Need to check the lock time of all transactions.
            - 6. Need to check block hashes for checkpoint blocks.
            - 7. Write the block on the block database as well as in-memory index for blocks.
            WriteToDisk(nFile, nBlockPos)
            AddToBlockIndex(nFile, nBlockPos) // Block reorganization happens here.
            - 8. relay the new block to peers as an inventory if it is the tip of the longest block chain.

        - Step 5 : Recursively bring any orphan blocks to blockchain, if the new block is the previous block of any orphan block.
        newly_added_blocks = List(block hash)
        LOOP newBlock := For each newly_added_blocks
            LOOP orphanBlock := For each orphan block which depends on the new Block as the parent of it
                // Store the block into the blockchain database.
                if (orphanBlock->AcceptBlock())
                    newly_added_blocks += orphanBlock.hash
                remove the orphanBlock from mapOrphanBlocks
            remove all orphan blocks depending on newBlock from mapOrphanBlocksByPrev
*/
    }
    None
  }
}

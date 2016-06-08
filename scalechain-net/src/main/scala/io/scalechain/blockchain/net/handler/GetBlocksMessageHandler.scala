package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.chain.{BlockLocatorHashes, Blockchain, BlockLocator}
import io.scalechain.blockchain.net.message.InvFactory
import io.scalechain.blockchain.proto.{GetBlocks, ProtocolMessage, GetData}
import org.slf4j.LoggerFactory

/**
  * The message handler for GetBlocks message.
  */
object GetBlocksMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(GetDataMessageHandler.getClass)

  /** Handle GetBlocks message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param getBlocks The GetBlocks message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle(context: MessageHandlerContext, getBlocks: GetBlocks): Unit = {
    // TODO :   Investigate : Need to understand : GetDistanceBack returns the depth(in terms of the sender's blockchain) of the block that is in our main chain. It returns 0 if the tip of sender's branch is in our main chain. We will send up to 500 more blocks from the tip height of the sender's chain.

    // Step 1 : Get the list of block hashes to send.
    val locator = new BlockLocator(Blockchain.get)
    // Step 2 : Skip the common block, start building the list of block hashes from the next block of the common block.
    //          Stop constructing the block hashes if we hit the count limit, 500. GetBlocks sends up to 500 block hashes.
    val blockHashes = locator.getHashes(BlockLocatorHashes(getBlocks.blockLocatorHashes), getBlocks.hashStop, maxHashCount = 500)

    val filteredBlockHashes =
    // Step 3 : Remove the hashStop if it is the last element of the list. GetBlocks does not send the hashStop block as an Inv.
    if (blockHashes.lastOption.isDefined && blockHashes.lastOption.get == getBlocks.hashStop) {
      blockHashes.dropRight(1)
    } else {
      blockHashes
    }

    // Step 4 : Pack the block hashes into an Inv message, and reply it to the requester.
    if (filteredBlockHashes.isEmpty) {
      // Do nothing. Nothing to send.
    } else {
      val invMessage = InvFactory.createBlockInventories(filteredBlockHashes)
      context.peer.send(invMessage)
    }
  }
}
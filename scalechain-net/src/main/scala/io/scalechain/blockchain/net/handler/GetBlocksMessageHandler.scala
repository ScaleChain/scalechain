package io.scalechain.blockchain.net.handler

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
    // TODO : Implement

/*
    // Step 1 : Get the latest common block with the caller in the best blockchain.
    // Step 2 : Skip the common block, start sending Inv from the next block of the common block.
    // Step 3 : Send Inv until we hit the hashStop. The hashStop is not sent as an Inv.
    //          Stop sending Inv if we hit the count limit.
    //          Investigate : Need to understand : GetDistanceBack returns the depth(in terms of the sender's blockchain) of the block that is in our main chain. It returns 0 if the tip of sender's branch is in our main chain. We will send up to 500 more blocks from the tip height of the sender's chain.
*/
  }
}
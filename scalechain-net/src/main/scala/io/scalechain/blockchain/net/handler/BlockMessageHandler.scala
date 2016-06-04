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

    }
    None
  }
}

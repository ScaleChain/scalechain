package io.scalechain.blockchain.net

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashCalculator
import org.slf4j.LoggerFactory


class ProtocolMessageHandler  {

  private val logger = LoggerFactory.getLogger(classOf[ProtocolMessageHandler])

  /** Handle a message coming from the TCP stream.
    *
    * @param message The messages to handle.
    * @return The list of responses we created after handling each message in messages.
    */
  def handle(message : ProtocolMessage): Option[ProtocolMessage] = {
    val chain = Blockchain.get

    // Return Some[ProtocolMessage] if we need to reply a message. Return None otherwise.
    message match {
      case version: Version => {
        // TODO : Implement - Update peerInfo.version.
        Some(Verack())
      }
      case Ping(nonce) => {
        // TODO : Implement - Update peerInfo.lastPingReceivedTime.
        Some(Pong(nonce))
      }
      case Pong(nonce) => {
        // TODO : Implement - Update peerInfo.lastPongReceivedTime.
        None
      }
      case verack: Verack => {
        // TODO : Implement - Handler for a verack.
        None
      }
      case addr: Addr => {
        // TODO : Implement - Handler for an addr.
        None
      }
      case inv: Inv => {
        // TODO : Implement - Handler for an inv.
        None
      }
      case headers: Headers => {
        // TODO : Implement - Handler for headers.
        None
      }
      case transaction: Transaction => {
        val transactionHash = Hash( HashCalculator.transactionHash(transaction) )
        if (chain.getTransaction(transactionHash).isEmpty) { // Process the transaction only if we don't have it yet.
          println(s"[P2P] Received a transaction. ${transaction}")
          chain.putTransaction(transaction)
        }
        None
      }
      case block: Block => {
        val blockHash = Hash( HashCalculator.blockHeaderHash(block.header) )
        if (chain.getBlock(blockHash).isEmpty) { // Process the transaction only if we don't have it yet.
          println(s"[P2P] Received a block. ${block}")
          chain.putBlock(BlockHash(blockHash.value), block)
        }
        None
      }
      case m: ProtocolMessage => {
        logger.info("Received a message, but done nothing : " + m.getClass.getName)
        None
      }
    }
  }
}

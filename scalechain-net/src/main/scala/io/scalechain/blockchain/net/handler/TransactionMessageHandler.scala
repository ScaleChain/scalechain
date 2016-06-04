package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.{ProtocolMessage, Transaction}
import org.slf4j.LoggerFactory
import io.scalechain.blockchain.script.HashSupported._

/**
  * The message handler for Transaction message.
  */
object TransactionMessageHandler {
  private lazy val logger = LoggerFactory.getLogger(TransactionMessageHandler.getClass)

  val chain = Blockchain.get

  /** Handle Transaction message.
    *
    * @param context The context where handlers handling different messages for a peer can use to store state data.
    * @param transaction The Transaction message to handle.
    * @return Some(message) if we need to respond to the peer with the message.
    */
  def handle( context : MessageHandlerContext, transaction : Transaction ) : Option[ProtocolMessage] = {
    val transactionHash = transaction.hash
    if (chain.getTransaction(transactionHash).isEmpty) { // Process the transaction only if we don't have it yet.
      logger.info(s"[P2P] Received a transaction. Hash : ${transactionHash}")
      chain.putTransaction(transaction)

      // Propagate the transaction only if the block transaction was not found.
      //peerCommunication.sendToAll(transaction)
    }
    None
  }

}

package io.scalechain.blockchain.chain.processor

import io.scalechain.blockchain.proto.ProtocolMessage

/**
  * Provides method for a peer such as sending message to the peer.
  * Implemented by net layer, and passed to chain layer.
  *
  * Because the net layer is above the chain layer, we can't directly use classes in the net layer from the chain layer,
  * but use dependency injection.
  */
/*
trait PeerCommunicationTrait {
  /**
    * Reply a message back to the peer that sent a message.
    *
    * @param message The message to reply.
    */
  def reply(message : ProtocolMessage) : Unit

  /**
    * Send a message to all connected peers.
    * @param message The message to send.
    */
  def sendToAll(message : ProtocolMessage) : Unit
}
*/
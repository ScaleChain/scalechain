package io.scalechain.blockchain.net

import akka.actor.ActorRef
import akka.stream.scaladsl.Flow
import akka.util.ByteString


/** Represents a connected peer.
  * @param requester The actor that can send protocol messages to the connected peer.
  * @param messageTransformer The message transformer which converts incoming request messages to response messages.
  */
case class Peer(requester : ActorRef, messageTransformer : ProtocolMessageTransformer) {
}

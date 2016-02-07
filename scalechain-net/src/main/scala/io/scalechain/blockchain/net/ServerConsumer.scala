package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props}
import akka.camel.{CamelMessage, Consumer}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BitcoinProtocolDecoder, BitcoinProtocolEncoder}
import org.apache.camel.impl.SimpleRegistry

import scala.collection.mutable

class ServerConsumer extends Consumer {
  def endpointUri = "netty4:tcp://0.0.0.0:8778?decoders=#bitcoin-protocol-decoder&encoders=#bitcoin-protocol-encoder"

  val peerByAddress = mutable.HashMap[InetSocketAddress, ActorRef]()
  override def preStart(): Unit = {
    val registry = new SimpleRegistry()

    registry.put("bitcoin-protocol-decoder", new BitcoinProtocolEncoder())
    registry.put("bitcoin-protocol-encoder", new BitcoinProtocolDecoder())

    camelContext.setRegistry( registry )

    super.preStart()
  }

  def receive = {
    case msg : CamelMessage => {
      // Step 1 : Get the address of the peer.
      val remotePeerAddress = msg.headers("CamelNettyRemoteAddress").asInstanceOf[InetSocketAddress]
      println("Get camel message from remote address : " + remotePeerAddress)
      val protocolMessage = msg.bodyAs[ProtocolMessage]
      println("Got from the client : " + protocolMessage )

      // Step 2 : Get an actor reference to communicates with the peer.
      val peer = peerByAddress.get(remotePeerAddress) match {
        case Some(node : ActorRef) => node
        case None => {
          val node = context.actorOf(Props[PeerNode])
          peerByAddress.put(remotePeerAddress, node)
          node
        }
      }

      // Step 3 : Forward the message to the peer.
      peer forward protocolMessage
    }
  }
}

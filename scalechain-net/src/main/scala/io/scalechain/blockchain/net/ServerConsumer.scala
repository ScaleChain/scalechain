package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props}
import akka.camel.{CamelMessage, Consumer}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BitcoinProtocolDecoder, BitcoinProtocolEncoder}
import org.apache.camel.impl.SimpleRegistry

import scala.collection.mutable

object ServerConsumer {
  def apply(port : Int, peerBroker : ActorRef) : Props = Props(new ServerConsumer(port, peerBroker))
}

class ServerConsumer(port : Int, peerBroker : ActorRef) extends Consumer {
  // options to consider
  // option.child.keepAlive=true
  def endpointUri = s"netty4:tcp://0.0.0.0:$port?decoders=#bitcoin-protocol-decoder&encoders=#bitcoin-protocol-encoder"

  override def preStart(): Unit = {
    val registry = new SimpleRegistry()

    registry.put("bitcoin-protocol-decoder", new BitcoinProtocolEncoder())
    registry.put("bitcoin-protocol-encoder", new BitcoinProtocolDecoder())

    camelContext.setRegistry( registry )

    super.preStart()
  }

  def getRemoteAddress(message : CamelMessage) = message.headers("CamelNettyRemoteAddress").asInstanceOf[InetSocketAddress]

  def receive = {
    case camelMessage : CamelMessage => {
      // Step 1 : Get the address of the peer.
      val remotePeerAddress = getRemoteAddress(camelMessage)
      println("Get camel message from remote address : " + remotePeerAddress)
      //println("Got camel message : " + camelMessage)

      val protocolMessage = camelMessage.bodyAs[ProtocolMessage]
      println("Got from the client : " + protocolMessage )

      // Step 2 : Forward the message to the peer broker.
      peerBroker forward (remotePeerAddress, protocolMessage)
    }
  }
}

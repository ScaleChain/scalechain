package io.scalechain.blockchain.net

import akka.camel.{CamelMessage, Consumer}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BitcoinProtocolDecoder, BitcoinProtocolEncoder}
import org.apache.camel.impl.SimpleRegistry

class ServerConsumer extends Consumer {
  def endpointUri = "netty4:tcp://0.0.0.0:8778?decoders=#bitcoin-protocol-decoder&encoders=#bitcoin-protocol-encoder"

  override def preStart(): Unit = {
    val registry = new SimpleRegistry()

    registry.put("bitcoin-protocol-decoder", new BitcoinProtocolEncoder())
    registry.put("bitcoin-protocol-encoder", new BitcoinProtocolDecoder())

    camelContext.setRegistry( registry )

    super.preStart()
  }

  def receive = {
    case msg : CamelMessage => {
      val message = msg.bodyAs[ProtocolMessage]
      println("Got from the client : " + message )
      message match {
        case Ping(nonce) => {
          sender ! Pong(nonce)
        }
        case v : Version => {
          // Need to send version first. Just a testing purpose.
          sender ! Verack()
        }
      }
    }
  }
}

package io.scalechain.blockchain.net

import akka.camel.{CamelMessage, Consumer}
import io.scalechain.blockchain.proto.codec.{BitcoinProtocolDecoder, BitcoinProtocolEncoder}
import org.apache.camel.impl.SimpleRegistry

/**
  * Created by kangmo on 1/8/16.
  */
class ServerConsumer extends Consumer {
  def endpointUri = "netty4:tcp://0.0.0.0:8778?decoders=#bitcoin-protocol-decoder&encoders=bitcoin-protocol-encoder"

  override def preStart(): Unit = {
    super.preStart()

    val registry = new SimpleRegistry()
    registry.put("bitcoin-protocol-encoder", new BitcoinProtocolEncoder())
    registry.put("bitcoin-protocol-decoder", new BitcoinProtocolDecoder())
    camelContext.setRegistry( registry )
  }

  def receive = {
    case msg : CamelMessage => {
      val bodyString = msg.bodyAs[String]
      println("Got %s from the client" format bodyString )
      sender ! ("Got %s from the client" format bodyString )
    }
  }
}

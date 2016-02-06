package io.scalechain.blockchain.net

import akka.actor.Actor
import akka.camel.{CamelMessage, Producer}
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.codec.{BitcoinProtocolDecoder, BitcoinProtocolEncoder}
import org.apache.camel.impl.{SimpleRegistry, JndiRegistry}
import org.apache.camel.spi.Registry

/**
  * Created by kangmo on 1/8/16.
  */
class ClientProducer extends Actor with Producer {
  def endpointUri = "netty4:tcp://localhost:8778?decoders=#bitcoin-protocol-decoder&encoders=#bitcoin-protocol-encoder"

  override def preStart(): Unit = {
    val registry = new SimpleRegistry()
    registry.put("bitcoin-protocol-encoder", new BitcoinProtocolEncoder())
    registry.put("bitcoin-protocol-decoder", new BitcoinProtocolDecoder())
    camelContext.setRegistry( registry )

    super.preStart()
  }

  override protected def transformResponse(msg:Any) = {
    msg match {
      case cm : CamelMessage => {
        println("Transforming message: ")
        cm.bodyAs[ProtocolMessage]
      }
      case _ => {
        super.transformResponse(msg)
      }
    }
  }
}

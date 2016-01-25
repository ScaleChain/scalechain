package io.scalechain.blockchain.net

import akka.actor.Actor
import akka.camel.{CamelMessage, Producer}
import org.apache.camel.impl.{SimpleRegistry, JndiRegistry}
import org.apache.camel.spi.Registry
import io.scalechain.blockchain.proto.codec._

/**
  * Created by kangmo on 1/8/16.
  */
class ClientProducer extends Actor with Producer {
  def endpointUri = "netty4:tcp://localhost:8778?decoders=#bitcoin-protocol-decoder&encoders=bitcoin-protocol-encoder"

  override def preStart(): Unit = {
    super.preStart()

    val registry = new SimpleRegistry()
    if ( registry.get("bitcoin-protocol-encoder") == null ) {
      registry.put("bitcoin-protocol-encoder", new BitcoinProtocolEncoder())
    }
    if ( registry.get("bitcoin-protocol-decoder") == null ) {
      registry.put("bitcoin-protocol-decoder", new BitcoinProtocolDecoder())
    }
    camelContext.setRegistry( registry )
  }

  override protected def transformResponse(msg:Any) = {
    msg match {
      case cm : CamelMessage => {
        cm.bodyAs[String]
      }
      case _ => {
        super.transformResponse(msg)
      }
    }
  }
}

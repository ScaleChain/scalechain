package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.{Props, Actor}
import akka.camel.{CamelMessage, Producer}
import io.scalechain.blockchain.proto.ProtocolMessage
import io.scalechain.blockchain.proto.codec.{BitcoinProtocolDecoder, BitcoinProtocolEncoder}
import org.apache.camel.impl.{SimpleRegistry, JndiRegistry}
import org.apache.camel.spi.Registry


object ClientProducer {
  def apply(inetSocketAddress: InetSocketAddress) = Props(new ClientProducer(inetSocketAddress))
}

/**
  * Created by kangmo on 1/8/16.
  */
class ClientProducer(socketAddress: InetSocketAddress) extends Actor with Producer {
  // options to consider :
  // synchronous=true ;
  // - If this is set to false(default), a new connection is created for a request sent while the response of the previous request has not arrived yet.
  // - By setting this to true, we can reuse the connection and the new request is sent to the endpoint after the response of the previous request arrives.
  //
  // Whether Asynchronous Routing Engine is not in use. false then the Asynchronous Routing Engine is used, true to force processing synchronous.
  def endpointUri = s"netty4:tcp://${socketAddress.getAddress.getHostAddress}:${socketAddress.getPort}?decoders=#bitcoin-protocol-decoder&encoders=#bitcoin-protocol-encoder&synchronous=true"

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

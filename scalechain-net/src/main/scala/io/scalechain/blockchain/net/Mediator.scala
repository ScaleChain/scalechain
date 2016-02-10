package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.{Props, ActorRef, Actor}
import akka.camel.CamelMessage
import io.scalechain.blockchain.proto.ProtocolMessage


object Mediator {
  case class Request(message : ProtocolMessage)
  def apply(address:InetSocketAddress, peerBroker : ActorRef) : Props = Props(new Mediator(address, peerBroker))
}

/**
  * Created by kangmo on 1/8/16.
  */
class Mediator(address:InetSocketAddress, peerBroker: ActorRef) extends Actor {
  val clientProducer = context.actorOf( ClientProducer(address))

  override def preStart() = {
    super.preStart()

    peerBroker forward (clientProducer /*connected peer*/, address, StartPeer)
  }

  def receive = {

    /* Request to send the message to the client producer */
    case Mediator.Request(message : ProtocolMessage) => {
      clientProducer ! message
    }

    /* client producer sent back a reply */
    case protocolMessage : ProtocolMessage => {
      println("Got camel message: %s" format protocolMessage.toString )
      peerBroker forward (clientProducer /*connected peer*/, address, protocolMessage)
    }
    case obj : Any=> {
      println("Got something else:" + obj)
    }
  }
}

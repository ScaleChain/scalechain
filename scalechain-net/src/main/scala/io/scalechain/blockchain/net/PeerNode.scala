package io.scalechain.blockchain.net

import akka.actor.Actor
import akka.camel.CamelMessage
import io.scalechain.blockchain.proto._

/**
  * Created by kangmo on 2/7/16.
  */
class PeerNode extends Actor {

  def receive = {
    case message : ProtocolMessage => {
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

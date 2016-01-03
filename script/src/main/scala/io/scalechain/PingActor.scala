package io.scalechain

import java.math.BigInteger

import io.scalechain.blockchain.proto._
import akka.actor.{Actor, ActorLogging, Props}

class PingActor extends Actor with ActorLogging {
  import PingActor._
  
  var count : Long = 0
  val pongActor = context.actorOf(PongActor.props, "pongActor")

  def receive = {
  	case PongMessage(nonce:BigInteger) => {
      log.info(s"In PingActor - received nonce: $nonce")
      count += 1
      // TODO : This is just a sample implementation. Make sure what kind of value we need to use.
      sender() ! PingMessage(nonce.add(BigInteger.ONE))
    }
  }
}

object PingActor {
  val props = Props[PingActor]
}
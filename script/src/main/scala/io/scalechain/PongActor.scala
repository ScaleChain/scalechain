package io.scalechain

import java.math.BigInteger

import io.scalechain.blockchain.proto._
import akka.actor.{Actor, ActorLogging, Props}

class PongActor extends Actor with ActorLogging {
  def receive = {
  	case PingMessage(nonce) =>
  	  log.info(s"In PongActor - received message: $nonce")
  	  sender() ! PongMessage(nonce)
  }	
}

object PongActor {
  val props = Props[PongActor]
}

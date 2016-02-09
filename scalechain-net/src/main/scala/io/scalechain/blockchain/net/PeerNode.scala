package io.scalechain.blockchain.net

import akka.actor.{Props, ActorRef, Cancellable, Actor}
import akka.camel.CamelMessage
import io.scalechain.blockchain.proto._
import scala.concurrent.duration._


object PeerNode {
  object SendPing
  def apply() = Props( new PeerNode())
}
/**
  * Created by kangmo on 2/7/16.
  */
class PeerNode extends Actor {
  var pingSchedule : Cancellable = null
  var originalSender : ActorRef = null
  def receive = {
    case version : Version => {
      // Need to send version first. Just a testing purpose.
      sender ! Verack()
    }
    case verack : Verack => {
      import scala.concurrent.ExecutionContext.Implicits.global
      sender ! Ping(123)
      sender ! Ping(124)

      originalSender = sender
      pingSchedule = context.system.scheduler.schedule( initialDelay = 1 second, interval = 1 second, self, PeerNode.SendPing)
//      context.become(receivingMessages)
    }

    case PeerNode.SendPing => {
      originalSender ! Ping( System.currentTimeMillis() )
    }
    case Ping(nonce) => {
      println("received ping with nonce : " + nonce)
      sender ! Pong(nonce)
    }
    case Pong(nonce) => {
      println("received pong with nonce : " + nonce)
    }

  }
/*
  def receivingMessages : Receive = {
    case PeerNode.SendPing => {
      originalSender ! Ping( System.currentTimeMillis() )
    }
    case Ping(nonce) => {
      println("received ping with nonce : " + nonce)
      sender ! Pong(nonce)
    }
    case Pong(nonce) => {
      println("received pong with nonce : " + nonce)
    }
  }
*/
}

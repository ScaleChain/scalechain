package io.scalechain.blockchain.net

import akka.actor.{Props, ActorRef, Cancellable, Actor}
import akka.camel.CamelMessage
import io.scalechain.blockchain.proto._
import scala.concurrent.duration._
import io.scalechain.util.HexUtil._


object StartPeer extends ProtocolMessage

object PeerNode {
  object SendPing
  def apply(remotePeer:ActorRef) = Props( new PeerNode(remotePeer) )
}
/**
  * Created by kangmo on 2/7/16.
  */
class PeerNode(remotePeer:ActorRef) extends Actor {
  var pingSchedule : Cancellable = null

  var versionSent = false

  def receive = {
    case StartPeer => {
      println("PeerNode.StartPeer. remote : " + remotePeer + ", sender :" + sender)

      val version = Version(70002, BigInt("1"), 1454059080L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 0), NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 8333), BigInt("5306546289391447548"), "/Satoshi:0.11.2/", 395585, true)
      remotePeer ! version

      versionSent = true
    }

    case version : Version => {
      // Need to send version first. Just a testing purpose.
      println("PeerNode.version. remote : " + remotePeer + ", sender :" + sender)
/*
      if (!versionSent) {
        val version = Version(70002, BigInt("1"), 1454059080L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 0), NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 8333), BigInt("5306546289391447548"), "/Satoshi:0.11.2/", 395585, true)
        sender ! version
      }
      sender ! Ping(1004)
*/
      sender ! Verack()
    }
    case verack : Verack => {
      println("PeerNode.verack. remote : " + remotePeer + ", sender :" + sender)

      import scala.concurrent.ExecutionContext.Implicits.global
      sender ! Ping(123)
      sender ! Ping(124)

      // BUGBUG : Need to cancel the schedule when the actor stops.
      // See : http://doc.akka.io/docs/akka/current/scala/howto.html#Scheduling_Periodic_Messages
      pingSchedule = context.system.scheduler.schedule( initialDelay = 1 second, interval = 1 second, self, PeerNode.SendPing)
//      context.become(receivingMessages)
    }
    case PeerNode.SendPing => {
      println("PeerNode.SendPing. remote : " + remotePeer + ", sender :" + sender)
      remotePeer ! Ping( System.currentTimeMillis() )
    }
    case Ping(nonce) => {
      println("PeerNode.Ping. remote : " + remotePeer + ", sender :" + sender)
      println("received ping with nonce : " + nonce)
      sender ! Pong(nonce)
    }
    case Pong(nonce) => {
      println("PeerNode.Pong. remote : " + remotePeer + ", sender :" + sender)
      println("received pong with nonce : " + nonce)
    }

  }
/*
  def receivingMessages : Receive = {
    case PeerNode.SendPing => {
      remotePeer ! Ping( System.currentTimeMillis() )
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

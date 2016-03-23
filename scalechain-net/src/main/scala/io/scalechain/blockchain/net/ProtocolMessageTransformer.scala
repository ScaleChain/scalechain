package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.ActorRef
import akka.stream.stage.{SyncDirective, PushStage, Context}
import io.scalechain.blockchain.net.DomainMessageRouter.{VersionFrom, InventoriesFrom}
import io.scalechain.blockchain.proto._
import io.scalechain.util.HexUtil

/**
  * Created by kangmo on 2/14/16.
  */
class ProtocolMessageTransformer(domainMessageRouter : ActorRef, remoteAddress: InetSocketAddress) extends PushStage[List[ProtocolMessage], List[ProtocolMessage]] {
  var pongReceivedAt : Long = 0L

  def isLive(): Boolean = {
    true
    // BUGBUG : Need to make sure the remote peer is alive by checking the ping receival time.
    //System.currentTimeMillis() - pongReceivedAt < (ServerRequester.PING_INTERVAL_SECONDS + 1) * 1000
  }

  override def onPush(elems: List[ProtocolMessage], ctx: Context[List[ProtocolMessage]]): SyncDirective = {

    var messages = List[ProtocolMessage]()
    elems foreach { elem =>
      elem match {
        case version     : Version         => {
          messages :+= Verack()
          domainMessageRouter ! VersionFrom(remoteAddress, version)
        }
        case Ping(nonce)                   => messages :+= Pong(nonce)
        case Pong(nonce)                   => {
          pongReceivedAt = System.currentTimeMillis()
        }
        case verack      : Verack          => domainMessageRouter ! verack
        case addr        : Addr            => domainMessageRouter ! addr
        case inv         : Inv             => domainMessageRouter ! InventoriesFrom(remoteAddress, inv.inventories)
        case headers     : Headers         => domainMessageRouter ! headers
        case transaction : Transaction     => domainMessageRouter ! transaction
        case block       : Block           => domainMessageRouter ! block
        case m           : ProtocolMessage => println("Received a message : " + m.getClass.getName);
      }
    }
    ctx.push(messages)
  }
}

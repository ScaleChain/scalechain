package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.ActorRef
import akka.stream.stage.{SyncDirective, PushStage, Context}
import io.scalechain.blockchain.proto._

/**
  * Created by kangmo on 2/14/16.
  */
class ProtocolMessageTransformer(domainMessageRouter : ActorRef, remoteAddress: InetSocketAddress) extends PushStage[List[ProtocolMessage], List[ProtocolMessage]] {
  var pongReceivedAt : Long = 0L

  def isLive(): Boolean = {
    System.currentTimeMillis() - pongReceivedAt < (ServerRequester.PING_INTERVAL_SECONDS + 1) * 1000
  }

  override def onPush(elems: List[ProtocolMessage], ctx: Context[List[ProtocolMessage]]): SyncDirective = {

    var messages = List[ProtocolMessage]()
    elems foreach { elem =>
      elem match {
        case version     : Version         => messages :+= Verack()
        case Ping(nonce)                   => messages :+= Pong(nonce)
        case Pong(nonce)                   => {
          pongReceivedAt = System.currentTimeMillis()
        }
        case addr        : Addr            => domainMessageRouter ! addr
        case inv         : Inv             => domainMessageRouter ! (remoteAddress, inv)
        case headers     : Headers         => domainMessageRouter ! headers
        case transaction : Transaction     => domainMessageRouter ! transaction
        case block       : Block           => domainMessageRouter ! block
        case m           : ProtocolMessage => println("Received a message : " + m.toString);
      }
    }
    ctx.push(messages)
  }
}

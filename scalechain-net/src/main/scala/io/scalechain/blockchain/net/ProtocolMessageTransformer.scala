package io.scalechain.blockchain.net

import akka.stream.stage.{SyncDirective, PushStage, Context}
import io.scalechain.blockchain.proto._

/**
  * Created by kangmo on 2/14/16.
  */
class ProtocolMessageTransformer extends PushStage[List[ProtocolMessage], List[ProtocolMessage]] {
  override def onPush(elems: List[ProtocolMessage], ctx: Context[List[ProtocolMessage]]): SyncDirective = {
    var messages = List[ProtocolMessage]()
    elems foreach { elem =>
      elem match {
        case v : Version => messages :+= Verack()
        case Ping(nonce) => messages :+= Pong(nonce)
        case m: ProtocolMessage => println("Received a message : " + m.toString);
      }
    }
    ctx.push(messages)
  }
}

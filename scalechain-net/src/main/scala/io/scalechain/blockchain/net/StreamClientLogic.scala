package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.io.Framing
import akka.stream.scaladsl._
import akka.stream.stage.{SyncDirective, Context, PushStage}
import akka.util.ByteString


object StreamClientLogic {
  def apply(system : ActorSystem, materializer : Materializer, address : InetSocketAddress) = new StreamClientLogic(system, materializer, address)
}

/** Connect to a stream server.
  *
  * Source code copied from http://doc.akka.io/docs/akka-stream-and-http-experimental/2.0.3/scala/stream-io.html#streaming-tcp
  */
class StreamClientLogic(system : ActorSystem, materializer : Materializer, address : InetSocketAddress) {
  implicit val s = system
  implicit val m = materializer

  val connection = Tcp().outgoingConnection(address.getAddress.getHostAddress, address.getPort)

  val replParser = new PushStage[String, ByteString] {
    override def onPush( elem: String, ctx : Context[ByteString]) : SyncDirective = {
      elem match {
        case "q" => ctx.pushAndFinish(ByteString("BYE\n"))
        case _ => ctx.push(ByteString(s"$elem\n"))
      }
    }
  }

  val repl = Flow[ByteString]
    .via(Framing.delimiter(
      ByteString("\n"),
      maximumFrameLength = 256,
      allowTruncation = true))
    .map(_.utf8String)
    .map(text => println("Server : " + text))
    .map(_ => readLine("> "))
    .transform(() => replParser)

  connection.join(repl).run()
}

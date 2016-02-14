package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.stream.{Materializer, FlowShape}
import akka.stream.io.Framing
import akka.stream.scaladsl.Tcp.{ServerBinding, IncomingConnection}
import akka.stream.scaladsl._
import akka.stream.stage.{Context, SyncDirective, PushStage}
import akka.util.ByteString

import scala.concurrent.Future

object StreamServerLogic {
  def apply(system : ActorSystem, materializer : Materializer, address : InetSocketAddress) = new StreamServerLogic(system, materializer, address)
}


/** Open a TCP port to accept clients' connections.
  *
  * Source code copied from http://doc.akka.io/docs/akka-stream-and-http-experimental/2.0.3/scala/stream-io.html#streaming-tcp
  */
class StreamServerLogic(system : ActorSystem, materializer : Materializer, address : InetSocketAddress) {
  implicit val s = system
  implicit val m = materializer

  val connections : Source[IncomingConnection, Future[ServerBinding]] = Tcp().bind(address.getAddress.getHostAddress, address.getPort)

  connections runForeach { connection : IncomingConnection =>
    val serverLogic = Flow.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // server logic, parses incoming commands
      val commandParser = new PushStage[String, String] {
        override def onPush(elem : String, ctx : Context[String]) : SyncDirective = {
          elem match {
            case "BYE" => ctx.finish()
            case _ => ctx.push(elem + "!")
          }
        }
      }

      import connection._
      val welcomeMsg = s"Welcome to : $localAddress, you are : $remoteAddress!\n"

      val welcome = Source.single(ByteString(welcomeMsg))
      val echo = builder.add( Flow[ByteString]
        .via(Framing.delimiter(
          ByteString("\n"),
          maximumFrameLength = 256,
          allowTruncation = true))
        .map(_.utf8String)
        .transform(() => commandParser)
        .map(_ + "\n")
        .map(ByteString(_))
      )

      val concat = builder.add(Concat[ByteString]())

      // first we emit the welcome message,
      welcome ~> concat.in(0)

      // then we continute using the echo-logic Flow
      echo.outlet ~> concat.in(1)

      FlowShape(echo.in, concat.out)
    })

    connection.handleWith(serverLogic)
  }
}

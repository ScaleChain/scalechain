package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.stream.actor.ActorPublisher
import akka.stream.{Materializer, FlowShape}
import akka.stream.io.Framing
import akka.stream.scaladsl.Tcp.{ServerBinding, IncomingConnection}
import akka.stream.scaladsl._
import akka.stream.stage.{Context, SyncDirective, PushStage}
import akka.util.ByteString
import io.scalechain.blockchain.net.ServerRequester.RequestDenied
import io.scalechain.blockchain.proto.ProtocolMessage

import scala.annotation.tailrec
import scala.concurrent.Future

object StreamServerLogic {
  def apply(system : ActorSystem, materializer : Materializer, address : InetSocketAddress) = new StreamServerLogic(system, materializer, address)
}

case class TestMessage(message : String) extends ProtocolMessage {
  override def toString = message
}

object ServerRequester {
  case object RequestDenied
  case object RequestAccepted

  val props = Props[ServerRequester](new ServerRequester())
}

/** A actor publisher that sends requests to clients from the server for full duplex communication.
  *
  * Code copied from :
  * http://doc.akka.io/docs/akka-stream-and-http-experimental/2.0.3/scala/stream-integrations.html#integrating-with-actors
  */
class ServerRequester extends ActorPublisher[ByteString] {
  import akka.stream.actor.ActorPublisherMessage._
  import ServerRequester._


  val MaxBufferSize = 1024
  var buffer = Vector.empty[ProtocolMessage]

  def receive = {
    case message : ProtocolMessage if buffer.size == MaxBufferSize =>
      //sender ! RequestDenied

    case message : ProtocolMessage =>
      //sender ! RequestAccepted
      if (buffer.isEmpty && totalDemand > 0)
        onNext(ByteString(message.toString))
      else {
        buffer :+= message
        deliverBuffer()
      }
    case Request(_) => deliverBuffer()
    case Cancel => context.stop(self)
  }

  @tailrec final def deliverBuffer() : Unit = {
    if (totalDemand > 0) {
      /*
       * totalDemand is a Long and could be larger than what buffer.splitAt can accept.
       */
      if (totalDemand <= Int.MaxValue) {
        val (use, keep) = buffer.splitAt(totalDemand.toInt)
        buffer = keep
        use foreach{ message : ProtocolMessage =>
          onNext(ByteString(message.toString))
        }
      } else {
        val (use, keep) = buffer.splitAt(Int.MaxValue)
        buffer = keep

        use foreach{ message : ProtocolMessage =>
          onNext(ByteString(message.toString))
        }

        deliverBuffer()
      }
    }
  }
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
    println(s"Accepting connection from client : ${connection.remoteAddress}")

/*
Source.actorPublisher :
  def actorPublisher[T](props : akka.actor.Props) : akka.stream.scaladsl.Source[T, akka.actor.ActorRef]


 */

/*
  final class Source[+Out, +Mat](private[stream] override val module : akka.stream.impl.StreamLayout.Module)
    extends scala.AnyRef with akka.stream.scaladsl.FlowOpsMat[Out, Mat]
    with akka.stream.Graph[akka.stream.SourceShape[Out], Mat]
 */
    val requester : Source[ByteString, akka.actor.ActorRef] = Source.actorPublisher[ByteString](ServerRequester.props)

    println("Accepting : 1")

    /*
    GraphDSL.create :
      def create[S <: akka.stream.Shape, Mat](g1 : akka.stream.Graph[akka.stream.Shape, Mat])
      (buildBlock : scala.Function1[akka.stream.scaladsl.GraphDSL.Builder[Mat], scala.Function1[g1.Shape, S]]) : akka.stream.Graph[S, Mat] = { /* compiled code */ }
    */

    /*
        val g : akka.stream.Graph[ByteString, ActorRef] =
        GraphDSL.create(requester) { builder : akka.stream.scaladsl.GraphDSL.Builder[ActorRef] => {
            requesterShape : requester.Shape =>
            import GraphDSL.Implicits._
            requesterShape
          }
        }
    */


/*
RunnableGraph.fromGraph :
  def fromGraph[Mat](
    g : akka.stream.Graph[akka.stream.ClosedShape, Mat]) :
      akka.stream.scaladsl.RunnableGraph[Mat] = { /* compiled code */ }


Flow.fromGraph :
  def fromGraph[I, O, M](
    g : akka.stream.Graph[akka.stream.FlowShape[I, O], M]) :
      akka.stream.scaladsl.Flow[I, O, M] = { /* compiled code */ }
*/

    val serverLogic = Flow.fromGraph(GraphDSL.create(requester) { implicit builder =>
      requesterShape : requester.Shape => {
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


        val merge = builder.add(Merge[ByteString](2))

        // first we emit the welcome message,
        requesterShape ~> merge.in(0)

        // then we continute using the echo-logic Flow
        echo.outlet ~> merge.in(1)

        FlowShape(echo.in, merge.out)
      }
    })

    println("Accepting : 2")

    val ref : ActorRef = connection.handleWith(serverLogic)

    println("Accepting : 3")

  {
/*
      val ref = Flow[ByteString]
      .map{byteString => print(byteString); byteString}
      .to(Sink.ignore)
      .runWith(requester)
*/
      val welcomeMsg = s"Welcome to : ${connection.localAddress}, you are : ${connection.remoteAddress}!\n"

      println("Sending messages to client : ")
      ref ! TestMessage("1:"+welcomeMsg)
      ref ! TestMessage("2:"+welcomeMsg)
      ref ! TestMessage("3:"+welcomeMsg)
      ref ! TestMessage("4:"+welcomeMsg)
    }
  }
}

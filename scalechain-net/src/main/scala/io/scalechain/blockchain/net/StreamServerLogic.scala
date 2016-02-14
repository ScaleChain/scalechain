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
import io.scalechain.blockchain.proto._
import io.scalechain.util.HexUtil._

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
class ServerRequester extends ActorPublisher[List[ProtocolMessage]] {
  import akka.stream.actor.ActorPublisherMessage._
  import ServerRequester._


  val MaxBufferSize = 1024
  var buffer = List.empty[ProtocolMessage]

  def receive = {
    case message : ProtocolMessage if buffer.size == MaxBufferSize =>
      //sender ! RequestDenied

    case message : ProtocolMessage =>
      //sender ! RequestAccepted
      if (buffer.isEmpty && totalDemand > 0)
        onNext(List(message))
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
        onNext(use)
      } else {
        val (use, keep) = buffer.splitAt(Int.MaxValue)
        buffer = keep

        onNext(use)

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

    val ref : ActorRef = connection.handleWith( PeerLogic.flow() )

    val versionMessage = Version(70002, BigInt("1"), 1454059080L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 0), NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 8333), BigInt("5306546289391447548"), "/Satoshi:0.11.2/", 395585, true)

    println("Sending version message to the client : ")
    ref ! versionMessage
  }
}

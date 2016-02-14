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
import scala.concurrent.duration._

object StreamServerLogic {
  def apply(system : ActorSystem, materializer : Materializer, peerBroker : ActorRef, address : InetSocketAddress) = new StreamServerLogic(system, materializer, peerBroker, address)
}

case class TestMessage(message : String) extends ProtocolMessage {
  override def toString = message
}


/** Open a TCP port to accept clients' connections.
  *
  * Source code copied from http://doc.akka.io/docs/akka-stream-and-http-experimental/2.0.3/scala/stream-io.html#streaming-tcp
  */
class StreamServerLogic(system : ActorSystem, materializer : Materializer, peerBroker : ActorRef, address : InetSocketAddress) {
  implicit val s = system
  implicit val m = materializer

  val connections : Source[IncomingConnection, Future[ServerBinding]] = Tcp().bind(address.getAddress.getHostAddress, address.getPort)

  connections runForeach { connection : IncomingConnection =>
    println(s"Accepting connection from client : ${connection.remoteAddress}")

    val (peerLogicFlow, messageTransformer) = PeerLogic.flow()

    val requester : ActorRef = connection.handleWith( peerLogicFlow )

    val connectedPeer = Peer(requester, messageTransformer)
    // Register the connected peer to the peer broker.
    peerBroker ! (connectedPeer, connection.remoteAddress, null /* Nothing to send */ )

    val versionMessage = Version(70002, BigInt("1"), 1454059080L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 0), NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 8333), BigInt("5306546289391447548"), "/Satoshi:0.11.2/", 395585, true)

    println("Sending version message to the client : ")
    requester ! versionMessage
  }
}

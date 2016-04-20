package io.scalechain.blockchain.net

import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingQueue

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Tcp.{ServerBinding, IncomingConnection}
import akka.stream.scaladsl._
import io.scalechain.blockchain.proto._
import io.scalechain.util.HexUtil._

import scala.concurrent.Future

object StreamServerLogic {
  def apply(system : ActorSystem, materializer : Materializer, peerSet : PeerSet, address : InetSocketAddress) =
    new StreamServerLogic(system, materializer, peerSet, address)
}

case class TestMessage(message : String) extends ProtocolMessage {
  override def toString = message
}


/** Open a TCP port to accept clients' connections.
  *
  * Source code copied from http://doc.akka.io/docs/akka-stream-and-http-experimental/2.0.3/scala/stream-io.html#streaming-tcp
  */
class StreamServerLogic(system : ActorSystem, materializer : Materializer, peerSet : PeerSet, address : InetSocketAddress) {
  implicit val s = system
  implicit val m = materializer

  val connections : Source[IncomingConnection, Future[ServerBinding]] = Tcp().bind(address.getAddress.getHostAddress, address.getPort)

  connections runForeach { connection : IncomingConnection =>
    println(s"Accepting connection from client : ${connection.remoteAddress}")

    val peerLogicFlow = PeerLogic.flow( connection.remoteAddress )

    val sendQueue:LinkedBlockingQueue[ProtocolMessage] = connection.handleWith( peerLogicFlow )

    // Register the connected peer to the peer set.
    peerSet.add(connection.remoteAddress, sendQueue)

    val versionMessage = Version(70002, BigInt("1"), 1454059080L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 0), NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 8333), BigInt("5306546289391447548"), "/Satoshi:0.11.2/", 395585, true)

    println("Sending version message to the client : ")
    sendQueue.add( versionMessage )
  }
}

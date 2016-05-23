package io.scalechain.blockchain.net

import java.net.InetSocketAddress
import java.util.concurrent.LinkedBlockingQueue

import akka.actor.{ActorSystem}
import akka.stream.Materializer
import akka.stream.scaladsl._
import io.scalechain.blockchain.proto._
import io.scalechain.util.HexUtil._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success}


object StreamClientLogic {
  def apply(system : ActorSystem, materializer : Materializer, peerSet : PeerSet, address : InetSocketAddress) =
    new StreamClientLogic(system, materializer, peerSet, address)
}

/** Connect to a stream server.
  *
  * Source code copied from http://doc.akka.io/docs/akka-stream-and-http-experimental/2.0.3/scala/stream-io.html#streaming-tcp
  */
class StreamClientLogic(system : ActorSystem, materializer : Materializer, peerSet : PeerSet, remoteAddress : InetSocketAddress) {
  private val logger = LoggerFactory.getLogger(classOf[StreamClientLogic])

  implicit val s = system
  implicit val m = materializer

  val connection = Tcp().outgoingConnection(remoteAddress.getAddress.getHostAddress, remoteAddress.getPort)

  val peerAddress = s"${remoteAddress.getAddress.getHostAddress}:${remoteAddress.getPort}"

  logger.info(s"Connecting to server : $peerAddress")

  val peerLogicFlow = PeerLogic.flow(remoteAddress)
  val (outgoingConnection : Future[Tcp.OutgoingConnection], sendQueue:LinkedBlockingQueue[ProtocolMessage]) = connection.joinMat(peerLogicFlow)(Keep.both).run()

  import scala.concurrent.ExecutionContext.Implicits.global
  outgoingConnection.onComplete {
    case Success(outgoingConnection) => {

      // Register the connected peer to the peer set.
      peerSet.add(outgoingConnection.remoteAddress, sendQueue)

      val versionMessage = Version(70002, BigInt("1"), 1454059080L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 0), NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 8333), BigInt("5306546289391447548"), "/Satoshi:0.11.2/", 395585, true)

      sendQueue.add( versionMessage )

      logger.info(s"Connected to the server : peerAddress")
    }

    case Failure(throwable) => {
      logger.info(s"Failed to connect to the peer : $peerAddress. Will retry in 1 seconds.")
      val PEER_CONNECTION_RETRY_MILLIS = 1000

//      if ( !peerSet.hasPeer(remoteAddress.getAddress) ) {
        Thread.sleep(PEER_CONNECTION_RETRY_MILLIS)
        StreamClientLogic(system, materializer, peerSet, remoteAddress)
//      }
    }
  }
}

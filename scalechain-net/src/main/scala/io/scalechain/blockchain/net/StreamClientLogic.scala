package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import akka.stream.io.Framing
import akka.stream.scaladsl._
import akka.stream.stage.{SyncDirective, Context, PushStage}
import akka.util.ByteString
import io.scalechain.blockchain.proto._
import io.scalechain.util.HexUtil._


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

  val protocolDecoder = new ProtocolDecoder()
  val messageTransformer = new ProtocolMessageTransformer()

  println(s"Connecting to server : ${address.getAddress.getHostAddress}:${address.getPort}")
  val requester = connection.joinMat(PeerLogic.flow())(Keep.right).run()


  val versionMessage = Version(70002, BigInt("1"), 1454059080L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 0), NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 8333), BigInt("5306546289391447548"), "/Satoshi:0.11.2/", 395585, true)

  println("Sending version message to the server : ")
  requester ! versionMessage
}

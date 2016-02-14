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

import scala.concurrent.Future
import scala.util.{Failure, Success}


object StreamClientLogic {
  def apply(system : ActorSystem, materializer : Materializer, peerBroker : ActorRef, address : InetSocketAddress) = new StreamClientLogic(system, materializer, peerBroker, address)
}

/** Connect to a stream server.
  *
  * Source code copied from http://doc.akka.io/docs/akka-stream-and-http-experimental/2.0.3/scala/stream-io.html#streaming-tcp
  */
class StreamClientLogic(system : ActorSystem, materializer : Materializer, peerBroker : ActorRef, address : InetSocketAddress) {
  implicit val s = system
  implicit val m = materializer

  val connection = Tcp().outgoingConnection(address.getAddress.getHostAddress, address.getPort)

  val peerAddress = s"${address.getAddress.getHostAddress}:${address.getPort}"

  println(s"Connecting to server : $peerAddress")

  val (peerLogicFlow, messageTransformer) = PeerLogic.flow()
  val (outgoingConnection : Future[Tcp.OutgoingConnection], requester:ActorRef) = connection.joinMat(peerLogicFlow)(Keep.both).run()

  import scala.concurrent.ExecutionContext.Implicits.global
  outgoingConnection.onComplete {
    case Success(outgoingConnection) => {
      println(s"Connected!")

      val connectedPeer = Peer(requester, messageTransformer)
      // Register the connected peer to the peer broker.
      peerBroker ! (connectedPeer, outgoingConnection.remoteAddress, null /* Nothing to send */ )

      val versionMessage = Version(70002, BigInt("1"), 1454059080L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 0), NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 8333), BigInt("5306546289391447548"), "/Satoshi:0.11.2/", 395585, true)

      println("Sending version message to the server : ")
      requester ! versionMessage

      try {
        Thread.sleep(5000)
        requester ! GetHeaders(70002L, List(Hash(bytes("000000000000000003f8342444bbcdf461825e1a9a9b45475c25ec0f85c9b900")),Hash(bytes("000000000000000004885affd352524cbb9f478be5666f45e63fcb9b62bdb829")),Hash(bytes("000000000000000005f4f8734cce932160230ec78fae523e150bf7753132fd37")),Hash(bytes("0000000000000000009960dd44dd990240878a8d49ee9047db12a962d21c399c")),Hash(bytes("00000000000000000455a116bb190ba1547ac825789e75b2ffacfc0719d278af")),Hash(bytes("000000000000000000776b13db69f8773021d8acc75729d25cc8a8e9da20962e")),Hash(bytes("0000000000000000019fd17e035a2b6b1fbdd883135e1d6ba8338032a43b9002")),Hash(bytes("00000000000000000759fea0a10923b48bde6c5a0f116a3a0befc96980aa0f81")),Hash(bytes("00000000000000000006b14477512fb4de486b98f3f56ecde077a8a9edba77ae")),Hash(bytes("0000000000000000077f754f22f21629a7975cf7fdb0e5336e5d1f210973e546")),Hash(bytes("00000000000000000623f7955cdcb1a6eb211e229e39e0a0a3c3ebe169459fdb")),Hash(bytes("00000000000000000724d55829572221d9feeb92a663c59d87357af6eca7bbf0")),Hash(bytes("000000000000000003b925e0ea031a8e3af2015079f04b6beed7f8376c97efbd")),Hash(bytes("0000000000000000024c9359a8e29175996e2697dd21590b441dc96e9fdc0a58")),Hash(bytes("0000000000000000001c5eaf1a2093707e08dd05dd1e0dd012d15fddb5c13b76")),Hash(bytes("0000000000000000045c2f67e368f7e8a4da8ee363b6d30d4355e9882177c49a")),Hash(bytes("00000000000000000060837b4ece2d91c012d2470027748bc6c004794f7aa7a4")),Hash(bytes("0000000000000000053b0cfdf6df34ceb485493d53f38ef93eb275aaecdd4f75")),Hash(bytes("000000000000000006c41b3003bf0ed9b56d88e8ddf3f2911793adfa8e8aae45")),Hash(bytes("000000000000000006bf01ce29aefa7f72b78a706db7c76d0f201886441a361f")),Hash(bytes("000000000000000005d9e8faf746d440cb15fd59a7f49d18beafc3747d862ad5")),Hash(bytes("000000000000000006eb997575186ebc1058fd9a266d6990ec579088738369ef")),Hash(bytes("0000000000000000020b4115182e865e3806587b4e4d3a458b92633fabc1c644")),Hash(bytes("000000000000000004970304cf4fde1934489a78fe030f36b40df6affd3f2471")),Hash(bytes("00000000000000000601848f7eb277d351865eeddfdb1b6f562d92dc89a5177e")),Hash(bytes("00000000000000001113cd7699988701661e57d83b6ae2b7e75bb3667dcecae7")),Hash(bytes("00000000000000000877063f88ebd20e4a2561ec370af44e48c8a022e209ee6e")),Hash(bytes("000000000000000078301a3e53a4b148d5b213d7817e8fa7f77a1e6292e32c21")),Hash(bytes("00000000000003d7263eaafe3e57c2ea4b5e4e9603645b9c8ab16a61f276ff0a")),Hash(bytes("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"))), Hash(bytes("0000000000000000000000000000000000000000000000000000000000000000")))
      } catch {
        case e: Exception =>
      }
    }
    case Failure(throwable) => {
      println(s"Failed to connect to the peer : $peerAddress")
    }
  }
}

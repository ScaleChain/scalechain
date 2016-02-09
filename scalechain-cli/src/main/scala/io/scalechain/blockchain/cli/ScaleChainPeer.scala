package io.scalechain.blockchain.cli

import java.net.InetSocketAddress

import akka.actor.{Props, ActorSystem}
import akka.util
import com.typesafe.config.{ConfigFactory, Config}
import io.scalechain.blockchain.cli.api.{RpcInvoker, Parameters}
import io.scalechain.blockchain.net.{Mediator, ServerConsumer, PeerBroker}
import io.scalechain.blockchain.proto.{IPv6Address, NetworkAddress, Version}
import io.scalechain.util.HexUtil._
import scala.collection.JavaConverters._

/**
  * Created by kangmo on 2/9/16.
  */
object ScaleChainPeer {
  case class Parameters(
    inboundPort : Int = io.scalechain.util.Config.scalechain.getInt("scalechain.p2p.inbound_port")
  )

  def main(args: Array[String]) = {
    val parser = new scopt.OptionParser[Parameters]("scalechain") {
      head("scalechain", "1.0")
      opt[Int]('p', "port") action { (x, c) =>
        c.copy(inboundPort = x) } text("The inbound port to use to accept connection from other peers.")
    }

    // parser.parse returns Option[C]
    parser.parse(args, Parameters()) match {
      case Some(params) => {
        startPeer(params.inboundPort)
      }

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }


  def startPeer(inboundPort : Int): Unit = {
    case class PeerAddress(address : String, port : Int) {
      def isMyself() = (address == "localhost" || address == "127.0.0.1") && (port == inboundPort )
    }

    /**
      * Read list of peers from scalechain.conf
      * It contains list of peer address and port.
      *
      * scalechain {
      *   p2p {
      *     peers = [
      *       { address:"127.0.0.1", port:"7643" },
      *       { address:"127.0.0.1", port:"7644" },
      *       { address:"127.0.0.1", port:"7645" }
      *     ]
      *   }
      * }
      */
    val peers = io.scalechain.util.Config.scalechain.getConfigList("scalechain.p2p.peers").asScala.toList.map { peer =>
      PeerAddress( peer.getString("address"), peer.getInt("port") )
    }

    /** Create the actor system.
      */
    val system = ActorSystem("ScaleChainPeer", ConfigFactory.load.getConfig("server"))

    /** The peer broker that keeps multiple PeerNode(s) and routes messages based on the origin address of the message.
      */
    val peerBroker = system.actorOf(Props[PeerBroker], "peerBroker")

    /** The consumer that opens an inbound port, and waits for connections from other peers.
      */
    val consumer = system.actorOf(ServerConsumer(inboundPort, peerBroker), "consumer")

    /** The mediator that creates outbound connections to other peers listed in the scalechain.p2p.peers configuration.
      */
    peers.map { peer =>
      if (!peer.isMyself()) {
        val mediator = system.actorOf(Mediator(new InetSocketAddress(peer.address, peer.port), peerBroker))

        // Send Version message
        //val version = Version(70012, BigInt("5"), 1454764863L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff42807623")), 36128), NetworkAddress(BigInt("5"), IPv6Address(bytes("00000000000000000000ffff00000000")), 36128), BigInt("11546941380556780170"), "/Kimtoshi:0.12.99/", 396491, true)
        val version = Version(70002, BigInt("1"), 1454059080L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 0), NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 8333), BigInt("5306546289391447548"), "/Satoshi:0.11.2/", 395585, true)

        mediator ! Mediator.Request(version)
      }
    }
  }
}

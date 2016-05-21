package io.scalechain.blockchain.cli

import java.io.File
import java.net.InetSocketAddress

import akka.actor.{Props, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.directives.{LoggingMagnet, DebuggingDirectives}
import akka.stream.ActorMaterializer
import akka.util
import com.typesafe.config.{ConfigFactory, Config}
import io.scalechain.blockchain.cli.api.{RpcInvoker, Parameters}
import io.scalechain.blockchain.net._
import io.scalechain.blockchain.proto.{ProtocolMessage, IPv6Address, NetworkAddress, Version}
import io.scalechain.blockchain.script.BlockPrinterSetter
import io.scalechain.blockchain.storage.{DiskBlockStorage, Storage}
import io.scalechain.blockchain.transaction.ChainEnvironment
import io.scalechain.util.Config
import io.scalechain.util.HexUtil._
import scala.collection.JavaConverters._
import io.scalechain.blockchain.api.{JsonRpcMicroservice, JsonRpc}

/** A ScaleChainPeer that connects to other peers and accepts connection from other peers.
  */
object ScaleChainPeer extends JsonRpc {
  case class Parameters(
    peerAddress : Option[String] = None, // The address of the peer we want to connect. If this is set, scalechain.p2p.peers is ignored.
    peerPort : Option[Int] = None, // The port of the peer we want to connect. If this is set, scalechain.p2p.peers is ignored.
    inboundPort : Int = io.scalechain.util.Config.scalechain.getInt("scalechain.p2p.inbound_port")
  )

  def main(args: Array[String]) = {
    val parser = new scopt.OptionParser[Parameters]("scalechain") {
      head("scalechain", "1.0")
      opt[Int]('p', "port") action { (x, c) =>
        c.copy(inboundPort = x) } text("The inbound port to use to accept connection from other peers.")
      opt[String]('a', "peerAddress") action { (x, c) =>
        c.copy(peerAddress = Some(x)) } text("The address of the peer we want to connect.")
      opt[Int]('x', "peerPort") action { (x, c) =>
        c.copy(peerPort = Some(x)) } text("The port of the peer we want to connect.")
    }

    // Create the testnet enviornment.
    // TODO : Need to get an argument from the command line to decide which network to use. By default use testnet for now.
    ChainEnvironment.create("testnet")

    Storage.initialize()
    DiskBlockStorage.create(new File("./target/blockstorage"))

    // parser.parse returns Option[C]
    parser.parse(args, Parameters()) match {
      case Some(params) => {
        startPeer(params)
      }

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }


  def startPeer(params : Parameters): Unit = {
    case class PeerAddress(address : String, port : Int)

    def isMyself(addr : PeerAddress) =
      (addr.address == "localhost" || addr.address == "127.0.0.1") && (addr.port == params.inboundPort )

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
    val peers =
      // If the command parameter has -peerAddress and -peerPort, connect to the given peer.
      if (params.peerAddress.isDefined && params.peerPort.isDefined) {
        List(PeerAddress(params.peerAddress.get, params.peerPort.get))
      } else { // Otherwise, connect to peers listed in the configuration file.
        io.scalechain.util.Config.scalechain.getConfigList("scalechain.p2p.peers").asScala.toList.map { peer =>
          PeerAddress( peer.getString("address"), peer.getInt("port") )
        }
      }

    // Initialize the storage sub-system.
    Storage.initialize()

    // Initialize the printer setter, to print script operations on transactions.
    BlockPrinterSetter.initialize

    /** Create the actor system.
      */
    implicit val system = ActorSystem("ScaleChainPeer", ConfigFactory.load.getConfig("server"))

    implicit val materializer = ActorMaterializer()

    /** The peer set that keeps multiple PeerNode(s).
      */
    val peerSet = PeerSet.create

    /** The consumer that opens an inbound port, and waits for connections from other peers.
      */
    val server = StreamServerLogic(system, materializer, peerSet, new InetSocketAddress("127.0.0.1", params.inboundPort))

/*  // Currently the we use RocksDB, so commented out the code to sleep 30 seconds to wait for the cassandra to start up.
    // Wait for the cassandra starts up.
    try {
      Thread.sleep(30000)
    } catch {
      case e : InterruptedException => println("ScaleChainPEer Sleep interrupted.")
    }
*/
    /** The mediator that creates outbound connections to other peers listed in the scalechain.p2p.peers configuration.
      */
    peers.map { peer =>
      if (!isMyself(peer)) {
        val peerAddress = new InetSocketAddress(peer.address, peer.port)
        val client = StreamClientLogic(system, materializer, peerSet, peerAddress)

        // Send StartPeer message to the peer, so that it can initiate the node start-up process.
        //peerBroker ! (clientProducer /*connected peer*/, peerAddress, Some(StartPeer) /* No message to send to the peer node */  )
      }
    }

    JsonRpcMicroservice.runService(system, materializer, system.dispatcher)

    /** Start RPC server.
      *
      */
    /*
    implicit val ec = system.dispatcher

    def printRequestMethod(req: HttpRequest): Unit = {
      println(s"request : ${req}")
    }
    val logRequestPrintln = DebuggingDirectives.logRequest(LoggingMagnet(_ => printRequestMethod))

    val port = io.scalechain.util.Config.scalechain.getInt("scalechain.api.port")
    val bindingFuture = Http().bindAndHandle( logRequestPrintln(routes), "localhost", port)

    println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")
    */
  }
}

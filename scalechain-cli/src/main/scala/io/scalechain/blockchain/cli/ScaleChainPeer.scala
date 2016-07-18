package io.scalechain.blockchain.cli

import java.io.File
import java.net.{InetAddress, NetworkInterface, InetSocketAddress}
import java.util

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.cli.api.{RpcInvoker, Parameters}
import io.scalechain.blockchain.net._
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.script.{BlockPrinterSetter}
import io.scalechain.blockchain.storage._
import io.scalechain.blockchain.storage.index.{RocksDatabase, KeyValueDatabase}
import io.scalechain.blockchain.transaction.{CoinAddress, SigHash, PrivateKey, ChainEnvironment}
import io.scalechain.util.{PeerAddress, NetUtil, HexUtil, Config}
import io.scalechain.util.HexUtil._
import io.scalechain.wallet.Wallet
import org.apache.log4j.PropertyConfigurator
import io.scalechain.blockchain.api.{RpcSubSystem, JsonRpcMicroservice}

import scala.collection.mutable.ArrayBuffer

/** A ScaleChainPeer that connects to other peers and accepts connection from other peers.
  */
object ScaleChainPeer {

  case class Parameters(
                         peerAddress: Option[String] = None, // The address of the peer we want to connect. If this is set, scalechain.p2p.peers is ignored.
                         peerPort: Option[Int] = None, // The port of the peer we want to connect. If this is set, scalechain.p2p.peers is ignored.
                         p2pInboundPort: Int = io.scalechain.util.Config.getInt("scalechain.p2p.port"),
                         apiInboundPort: Int = io.scalechain.util.Config.getInt("scalechain.api.port"),
                         miningAccount: String = io.scalechain.util.Config.getString("scalechain.mining.account"),
                         network: String = io.scalechain.util.Config.getString("scalechain.network.name"),
                         maxBlockSize: Int = io.scalechain.util.Config.getInt("scalechain.mining.max_block_size"),
                         minerInitialDelayMS: Int = 20000,
                         minerHashDelayMS : Int = 200
                       )

  def main(args: Array[String]) = {
    val parser = new scopt.OptionParser[Parameters]("scalechain") {
      head("scalechain", "1.0")
      opt[Int]('p', "p2pPort") action { (x, c) =>
        c.copy(p2pInboundPort = x)
      } text ("The P2P inbound port to use to accept connection from other peers.")
      // TODO : Bitcoin Compatibility - Check the parameter of bitcoind.
      opt[Int]('c', "apiPort") action { (x, c) =>
        c.copy(apiInboundPort = x)
      } text ("The API inbound port to use to accept connection from RPC clients.")
      opt[String]('a', "peerAddress") action { (x, c) =>
        c.copy(peerAddress = Some(x))
      } text ("The address of the peer we want to connect.")
      opt[Int]('x', "peerPort") action { (x, c) =>
        c.copy(peerPort = Some(x))
      } text ("The port of the peer we want to connect.")
      // TODO : Bitcoin Compatibility - Check the parameter of bitcoind.
      opt[String]('m', "miningAccount") action { (x, c) =>
        c.copy(miningAccount = x)
      } text ("The account to get the coins mined. The receiving address of the account will get the coins mined.")
      // TODO : Bitcoin Compatibility - Check the parameter of bitcoind.
      opt[String]('n', "network") action { (x, c) =>
        c.copy(network = x)
      } text ("The network to use. currently 'testnet' is supported. Will support 'mainnet' as well as 'regtest' soon.")
      opt[Int]("minerInitialDelayMS") action { (x, c) =>
        c.copy(minerInitialDelayMS = x) } validate { x =>
        if (x > 0) success else failure("Value <minerInitialDelayMS> must be >0")
      } text ("Seconds to sleep before starting the miner.")
      opt[Int]("minerHashDelayMS") action { (x, c) =>
        c.copy(minerHashDelayMS = x) } validate { x =>
          if (x > 0) success else failure("Value <minerHashDelayMS> must be >0")
        } text ("Maximum seconds to sleep for each hash calculation.")
      }

    // parser.parse returns Option[C]
    parser.parse(args, Parameters()) match {
      case Some(params) => {
        initializeSystem(params)
      }

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }

  protected[cli] def initializeNetLayer(params: Parameters, indexDb : RocksDatabase, wallet: Wallet)(implicit db : KeyValueDatabase): PeerCommunicator = {

    def isMyself(addr: PeerAddress) = {
      NetUtil.getLocalAddresses().contains(addr.address) && addr.port == params.p2pInboundPort
    }
//      (addr.address == "localhost" || addr.address == "127.0.0.1") && (addr.port == params.p2pInboundPort)


    /**
      * Read list of peers from scalechain.conf
      * It contains list of peer address and port.
      *
      * scalechain {
      * p2p {
      * peers = [
      * { address:"127.0.0.1", port:"7643" },
      * { address:"127.0.0.1", port:"7644" },
      * { address:"127.0.0.1", port:"7645" }
      * ]
      * }
      * }
      */
    val peerAddresses: List[PeerAddress] =
    // If the command parameter has -peerAddress and -peerPort, connect to the given peer.
      if (params.peerAddress.isDefined && params.peerPort.isDefined) {
        List(PeerAddress(params.peerAddress.get, params.peerPort.get))
      } else {
        // Otherwise, connect to peers listed in the configuration file.
        Config.peerAddresses
      }

    val nodeIndex = BlockGateway.getPeerIndex(params.p2pInboundPort).get
    BlockGateway.create(nodeIndex)

    PeerToPeerNetworking.getPeerCommunicator(
      params.p2pInboundPort,
      peerAddresses.filter(! isMyself(_) ))
  }

  /** Initialize sub-moudles from the lower layer to the upper layer.
    *
    * @param params The command line parameter of ScaleChain.
    */
  def initializeSystem(params: Parameters) {

    PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));

    // Step 2 : Initialize the printer setter, to print script operations on transactions.
    BlockPrinterSetter.initialize

    // Step 3 : Create the testnet enviornment.
    val env : ChainEnvironment = ChainEnvironment.create(params.network).getOrElse {
      println(s"Invalid p2p network : ${params.network}")
      System.exit(-1)
      null
    }

    // Step 4 : Storage Layer : Initialize block storage.
    val blockStoragePath = new File(s"./target/blockstorage-${params.p2pInboundPort}")
    Storage.initialize()

    val indexDb : RocksDatabase = new RocksDatabase(blockStoragePath)
    implicit val db : KeyValueDatabase = indexDb

    val storage: BlockStorage =
      // Initialize the block storage.
      // TODO : Investigate when to call storage.close.
      DiskBlockStorage.create(blockStoragePath, indexDb)


    // Step 5 : Chain Layer : Initialize blockchain.
    // BUGBUG : Need to change the folder name according to the production env.
    val chain = Blockchain.create(indexDb, storage)

    // See if we have genesis block. If not, put one.
    if ( ! chain.hasBlock(env.GenesisBlockHash) ) {
      chain.putBlock(env.GenesisBlockHash, env.GenesisBlock)
    }

    assert( chain.getBestBlockHash().isDefined )

    // Step 6 : Wallet Layer : set the wallet as an event listener of the blockchain.
    // Currently Wallet is a singleton, no need to initialize it.
    val walletPath = new File(s"./target/wallet-${params.p2pInboundPort}")
    val wallet = Wallet.create()
    chain.setEventListener(wallet)

    // Step 7 : Net Layer : Initialize peer to peer communication system, and
    // return the peer communicator that knows how to propagate blocks and transactions to peers.

    val peerCommunicator: PeerCommunicator = initializeNetLayer(params, indexDb, wallet)

    // Step 8 : API Layer : Initialize RpcSubSystem Sub-system and start the RPC service.
    // TODO : Pass Wallet as a parameter.
    RpcSubSystem.create(chain, peerCommunicator)
    JsonRpcMicroservice.runService(params.apiInboundPort)

    // Step 9 : CLI Layer : Create a miner that gets list of transactions from the Blockchain and create blocks to submmit to the Blockchain.
    val minerParams = CoinMinerParams(P2PPort = params.p2pInboundPort, InitialDelayMS = params.minerInitialDelayMS, HashDelayMS = params.minerHashDelayMS, MaxBlockSize = params.maxBlockSize)

    CoinMiner.create(indexDb, params.miningAccount, wallet, chain, peerCommunicator, minerParams)
  }
}

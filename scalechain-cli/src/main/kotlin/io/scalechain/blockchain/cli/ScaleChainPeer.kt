package io.scalechain.blockchain.cli

import java.io.File

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.net.*
import io.scalechain.blockchain.script.BlockPrinterSetter
import io.scalechain.blockchain.storage.*
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.ChainEnvironment
import io.scalechain.util.PeerAddress
import io.scalechain.util.NetUtil
import io.scalechain.wallet.Wallet
import org.apache.log4j.PropertyConfigurator
import io.scalechain.blockchain.api.RpcSubSystem
import io.scalechain.blockchain.api.JsonRpcMicroservice
import io.scalechain.blockchain.storage.index.DatabaseFactory
import io.scalechain.util.Config
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import kotlin.system.exitProcess


/** A ScaleChainPeer that connects to other peers and accepts connection from other peers.
  */
object ScaleChainPeer {

  data class Parameters(
                         val peerAddress: String? = null, // The address of the peer we want to connect. If this is set, scalechain.p2p.peers is ignored.
                         val peerPort: Int? = null, // The port of the peer we want to connect. If this is set, scalechain.p2p.peers is ignored.
                         val cassandraAddress: String? = if (Config.get().hasPath("scalechain.storage.cassandra.address")) Config.get().getString("scalechain.storage.cassandra.address") else null,
                         val cassandraPort: Int? = if (Config.get().hasPath("scalechain.storage.cassandra.port")) Config.get().getInt("scalechain.storage.cassandra.port") else null,
                         val p2pInboundPort: Int = Config.get().getInt("scalechain.p2p.port"),
                         val apiInboundPort: Int = Config.get().getInt("scalechain.api.port"),
                         val miningAccount: String = Config.get().getString("scalechain.mining.account"),
                         val network: String = Config.get().getString("scalechain.network.name"),
                         val maxBlockSize: Int = Config.MAX_BLOCK_SIZE,
                         val disableMiner : Boolean = false
                       )


    val parser = DefaultParser()
    val options = Options()

    init {
        options.addOption(Option.builder("p")
            .longOpt("p2pPort")
            .hasArg()
            .desc("The P2P inbound port to use to accept connection from other peers.")
            .build())


        options.addOption(Option.builder("c")
            .longOpt("apiPort")
            .hasArg()
            .desc("The API inbound port to use to accept connection from RPC clients.")
            .build())

        options.addOption(Option.builder("a")
            .longOpt("peerAddress")
            .hasArg()
            .desc("The address of the peer we want to connect.")
            .build())


        options.addOption(Option.builder("x")
            .longOpt("peerPort")
            .hasArg()
            .desc("The port of the peer we want to connect.")
            .build())


        options.addOption(Option.builder("m")
            .longOpt("miningAccount")
            .hasArg()
            .desc("The account to get the coins mined. The receiving address of the account will get the coins mined.")
            .build())


        options.addOption(Option.builder("n")
            .longOpt("network")
            .hasArg()
            .desc("The network to use. currently 'testnet' is supported. Will support 'mainnet' as well as 'regtest' soon.")
            .build())


        options.addOption(Option.builder()
          .longOpt("disableMiner")
          .desc("Disable coin miner.")
          .build())

    }

  @JvmStatic
  fun main(args: Array<String>) {
    val parser = DefaultParser()

    val line = parser.parse(options, args)

      val params = Parameters(
          peerAddress = line.getOptionValue("peerAddress", null), // The address of the peer we want to connect. If this is set, scalechain.p2p.peers is ignored.
          peerPort = CommandArgumentConverter.toInt( "peerPort", line.getOptionValue("peerPort", null)), // The port of the peer we want to connect. If this is set, scalechain.p2p.peers is ignored.
          cassandraAddress = line.getOptionValue("cassandraAddress", null) ?: if (Config.get().hasPath("scalechain.storage.cassandra.address")) Config.get().getString("scalechain.storage.cassandra.address") else null,
          cassandraPort = CommandArgumentConverter.toInt( "cassandraPort", line.getOptionValue("cassandraPort", null) ) ?: if (Config.get().hasPath("scalechain.storage.cassandra.port")) Config.get().getInt("scalechain.storage.cassandra.port") else null,
          p2pInboundPort = CommandArgumentConverter.toInt( "p2pPort", line.getOptionValue("p2pPort", null) ) ?: Config.get().getInt("scalechain.p2p.port"),
          apiInboundPort = CommandArgumentConverter.toInt( "apiPort", line.getOptionValue("apiPort", null) ) ?: Config.get().getInt("scalechain.api.port"),
          miningAccount = line.getOptionValue("miningAccount", null) ?: Config.get().getString("scalechain.mining.account"),
          network = line.getOptionValue("network", null) ?: Config.get().getString("scalechain.network.name"),
          maxBlockSize = Config.MAX_BLOCK_SIZE,
          disableMiner = line.hasOption("disableMiner")
      )

      initializeSystem(params)
  }

  fun initializeNetLayer(params: Parameters, chain : Blockchain) : PeerCommunicator {

    fun isMyself(addr: PeerAddress) : Boolean {
      return NetUtil.getLocalAddresses().contains(addr.address) && addr.port == params.p2pInboundPort
    }
//      (addr.address == "localhost" || addr.address == "127.0.0.1") && (addr.port == params.p2pInboundPort)


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
    val peerAddresses: List<PeerAddress> =
    // If the command parameter has -peerAddress and -peerPort, connect to the given peer.
      if (params.peerAddress != null && params.peerPort != null) {
        listOf(PeerAddress(params.peerAddress, params.peerPort))
      } else {
        // Otherwise, connect to peers listed in the configuration file.
        Config.get().peerAddresses()
      }

    val peerCommunicator = PeerToPeerNetworking.createPeerCommunicator(
      params.p2pInboundPort,
      peerAddresses.filterNot{ isMyself( it) })

    Node.create(peerCommunicator, chain)

    return peerCommunicator
  }

  fun blockStoragePath(p2pInboundPort : Int = Config.get().getInt("scalechain.p2p.port")) = File("./build/blockstorage-${p2pInboundPort}")

  /** Initialize sub-moudles from the lower layer to the upper layer.
    *
    * @param params The command line parameter of ScaleChain.
    */
  fun initializeSystem(params: Parameters) {

    PropertyConfigurator.configure(this.javaClass.classLoader.getResource("log4j.properties"));

    // Step 2 : Initialize the printer setter, to print script operations on transactions.
    BlockPrinterSetter.initialize()

    // Step 3 : Create the testnet enviornment.
    val env : ChainEnvironment? = ChainEnvironment.create(params.network)
    if (env == null) {
      println("Invalid p2p network : ${params.network}")
      exitProcess(-1)
    }

    // Step 4 : Storage Layer : Initialize block storage.
    val blockStoragePath = blockStoragePath(params.p2pInboundPort)
    Storage.initialize()

    val db : KeyValueDatabase = DatabaseFactory.create(blockStoragePath)


    val storage: BlockStorage =
      if (params.cassandraAddress != null && params.cassandraPort != null) {
        // Cassandra is not supported.
        throw UnsupportedOperationException()
//        CassandraBlockStorage.create(blockStoragePath, params.cassandraAddress.get, params.cassandraPort.get)
      } else {
        // Initialize the block storage.
        // TODO : Investigate when to call storage.close.
        DiskBlockStorage.create(blockStoragePath, db)
//        SpannerBlockStorage.create(db, "scalechain", "blockchain")
      }


    // Step 5 : Chain Layer : Initialize blockchain.
    // BUGBUG : Need to change the folder name according to the production env.
    val chain = Blockchain.create(db, storage)
    BlockProcessor.create(chain)

    // See if we have genesis block. If not, put one.
    if ( ! chain.hasBlock(db, env.GenesisBlockHash) ) {
      chain.putBlock(db, env.GenesisBlockHash, env.GenesisBlock)
    }

    assert( chain.getBestBlockHash(db) != null )

    // Step 6 : Wallet Layer : set the wallet as an event listener of the blockchain.
    // Currently Wallet is a singleton, no need to initialize it.
    //val walletPath = File("./build/wallet-${params.p2pInboundPort}")
    val wallet = Wallet.create()
    chain.setEventListener(wallet)

    // Step 7 : Net Layer : Initialize peer to peer communication system, and
    // return the peer communicator that knows how to propagate blocks and transactions to peers.

    val peerCommunicator: PeerCommunicator = initializeNetLayer(params, chain)

    // Step 8 : API Layer : Initialize RpcSubSystem Sub-system and start the RPC service.
    // TODO : Pass Wallet as a parameter.
    RpcSubSystem.create(chain, peerCommunicator)
    JsonRpcMicroservice.runService(params.apiInboundPort)

    // Step 9 : CLI Layer : Create a miner that gets list of transactions from the Blockchain and create blocks to submmit to the Blockchain.
    val minerParams = CoinMinerParams(P2PPort = params.p2pInboundPort, MaxBlockSize = params.maxBlockSize)

    val miner = CoinMiner.create(db, params.miningAccount, wallet, chain, peerCommunicator, minerParams)
    if (!params.disableMiner) {
      miner.start()
    }
  }

}

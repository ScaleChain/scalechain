package io.scalechain.blockchain.oap.helper

import java.io.File

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.oap.blockchain.scalechain.{ScalechainBlockchainInterface, ScalechainWalletInterface}
import io.scalechain.blockchain.oap.{IOapConstants, OpenAssetsProtocol}
import io.scalechain.blockchain.script.BlockPrinterSetter
import io.scalechain.blockchain.storage.{BlockStorage, DiskBlockStorage, Storage}
import io.scalechain.blockchain.storage.index.{CachedRocksDatabase, KeyValueDatabase, RocksDatabase}
import io.scalechain.blockchain.transaction.ChainEnvironment
import io.scalechain.util.{Config, NetUtil, PeerAddress}
import io.scalechain.wallet.Wallet
import org.apache.log4j.PropertyConfigurator

/**
  * Created by shannon on 16. 12. 26.
  */
//class RunScaleChainPeer {
//
//}

object RunScaleChainPeer {

  case class Parameters(
                         peerAddress: Option[String] = None, // The address of the peer we want to connect. If this is set, scalechain.p2p.peers is ignored.
                         peerPort: Option[Int] = None, // The port of the peer we want to connect. If this is set, scalechain.p2p.peers is ignored.
                         cassandraAddress: Option[String] = if (io.scalechain.util.Config.hasPath("scalechain.storage.cassandra.address")) Some(io.scalechain.util.Config.getString("scalechain.storage.cassandra.address")) else None,
                         cassandraPort: Option[Int] = if (io.scalechain.util.Config.hasPath("scalechain.storage.cassandra.port")) Some(io.scalechain.util.Config.getInt("scalechain.storage.cassandra.port")) else None,
                         p2pInboundPort: Int = io.scalechain.util.Config.getInt("scalechain.p2p.port"),
                         apiInboundPort: Int = io.scalechain.util.Config.getInt("scalechain.api.port"),
                         miningAccount: String = io.scalechain.util.Config.getString("scalechain.mining.account"),
                         network: String = io.scalechain.util.Config.getString("scalechain.network.name"),
                         maxBlockSize: Int = io.scalechain.util.Config.getInt("scalechain.mining.max_block_size"),
                         minerInitialDelayMS: Int = 20000,
                         minerHashDelayMS : Int = 200
                       )

  def main(args: Array[String]) = {
    val params = Parameters();
    initializeSystem(params)
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

    val indexDb : RocksDatabase = new CachedRocksDatabase(blockStoragePath)
    implicit val db : KeyValueDatabase = indexDb


    val storage: BlockStorage =
      if (params.cassandraAddress.isDefined && params.cassandraPort.isDefined) {
        // Cassandra is not supported.
        throw new UnsupportedOperationException
        //        CassandraBlockStorage.create(blockStoragePath, params.cassandraAddress.get, params.cassandraPort.get)
      } else {
        // Initialize the block storage.
        // TODO : Investigate when to call storage.close.
        DiskBlockStorage.create(blockStoragePath, indexDb)
      }


    // Step 5 : Chain Layer : Initialize blockchain.
    // BUGBUG : Need to change the folder name according to the production env.
    val chain = Blockchain.create(indexDb, storage)
    BlockProcessor.create(chain)

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

    // OAP Layer :
    val cachePath = new File(s"./target/oap-cache-${params.p2pInboundPort}");
    val chainInterface = new ScalechainBlockchainInterface(chain);
    val walletInterface = new ScalechainWalletInterface(chain, wallet);
    OpenAssetsProtocol.create(chainInterface, walletInterface, cachePath);

    System.out.println("ScaleChain Initialized...");
  }
}

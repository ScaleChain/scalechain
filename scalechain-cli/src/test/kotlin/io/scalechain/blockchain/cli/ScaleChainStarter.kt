package io.scalechain.blockchain.cli

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.TransactionSampleData
import io.scalechain.blockchain.transaction.TransactionGeneratorBlockchainView
import io.scalechain.util.GlobalEnvironemnt

/**
 * Created by kangmo on 20/01/2017.
 */
// Start scalechain to execute smoke tests
object ScaleChainStarter {
  /**
   * Start a scalechain thread to listen to API port.
   * @return true only if the scalechain thread was not running and it was started by calling start() method. false otherwise.
   */
  @JvmStatic
  fun start() : Boolean {
    // This class is used by many test suites in cli layer.
    // Not to start scalechain node more than twice, check if it was already started.
    if (CoinMiner.theCoinMiner == null) { // If the coin miner is not null, it is already started.
      val BITCOIND_PORT = 8333

      /*
      // Unit test runs under scalechain/scalechain-cli/, and the configuration files are in scalechain/scalechain-cli/unittest/config
      // So we need to set the ScaleChainHome variable to ./unittest/ to make sure config/scalechain.conf and configuration files for bftsmart in config folder are loaded correctly.
      */
      //println("ABSPATH:${File("").getAbsoluteFile().absolutePath}")
      GlobalEnvironemnt.ScaleChainHome = "./unittest/"
      // Remove previous files
      for (path in listOf(ScaleChainPeer.blockStoragePath(), ScaleChainPeer.oapCacheStoragePath())) {
        path.deleteRecursively()
        path.mkdir()
      }

      // Mine two blocks per second with initial delay 1 second.
      ScaleChainPeer.main( arrayOf("-a", "localhost", "-x", "$BITCOIND_PORT", "--network", "testnet", "--minerInitialDelayMS", "1000","--minerHashDelayMS", "500") )

      // Disable miner, to make the test result deterministic. Miner mines blocks randomly resulting in different test results whenever we run test cases.
      //ScaleChainPeer.main( arrayOf("-a", "localhost", "-x", "$BITCOIND_PORT", "-disableMiner") )
      return true
    }
    return false
  }
}
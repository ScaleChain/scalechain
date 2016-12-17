package io.scalechain.blockchain.cli

import com.google.gson.JsonElement
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.command.blockchain.GetBestBlockHash
import io.scalechain.blockchain.proto.Hash
import io.scalechain.test.TestMethods.filledString
import io.scalechain.util.Either

import io.scalechain.blockchain.api.domain.RpcError
import io.scalechain.blockchain.api.domain.RpcParams
import io.scalechain.blockchain.api.domain.RpcRequest
import io.scalechain.blockchain.api.domain.RpcResult
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.TransactionSampleData
import io.scalechain.blockchain.cli.command.RpcInvoker
import io.scalechain.blockchain.transaction.ChainTestTrait
import io.scalechain.util.Bytes
import io.scalechain.util.GlobalEnvironemnt
import io.scalechain.util.HexUtil
import io.scalechain.util.HexUtil.bytes
import jdk.nashorn.internal.objects.Global

/** Start the peer and connect to the local bitcoind node.
  */
/*
object RunnablePeer : Runnable {
  val BITCOIND_PORT = 8333

  override fun run() : Unit {
    ScaleChainPeer.main( arrayOf("-a", "localhost", "-x", "$BITCOIND_PORT") )
  }
  var peerThread : Thread? = null
  fun startPeer() {
    if (peerThread == null) {
      peerThread = Thread( RunnablePeer )
      peerThread!!.start()
      // Wait until the peer connects to the bitcoin node.
      Thread.sleep(5);
    }
  }
  fun stopPeer() {
    peerThread
  }
}
*/

/**
  * Created by kangmo on 3/15/16.
  */
abstract class APITestSuite : FlatSpec(), Matchers, ChainTestTrait {
  val GENESIS_BLOCK_HEIGHT = 0
  val GENESIS_BLOCK_HASH = HexUtil.hex(env().GenesisBlockHash.value.array)
  val ALL_ZERO_HASH = Hash(Bytes.from(filledString(64, '0'.toByte())))

  val RPC_USER = "user"
  val RPC_PASSWORD = "pleasechangethispassword123@.@"

  override fun beforeAll() {
    super.beforeAll()

//    RunnablePeer.startPeer()

    // This class is used by many test suites in cli layer.
    // Not to start scalechain node more than twice, check if it was already started.
    if (CoinMiner.theCoinMiner == null) { // If the coin miner is not null, it is already started.
      val BITCOIND_PORT = 8333

      // Unit test runs under scalechain/scalechain-cli, and the configuration files are in scalechain/scalechain-cli/unittest/config
      // So we need to set the ScaleChainHome variable to ./unittest/ to make sure config/scalechain.conf and configuration files for bftsmart in config folder are loaded correctly.
      GlobalEnvironemnt.ScaleChainHome = "./unittest/"
      // Disable miner, to make the test result deterministic. Miner mines blocks randomly resulting in different test results whenever we run test cases.
      ScaleChainPeer.main( arrayOf("-a", "localhost", "-x", "$BITCOIND_PORT", "-disableMiner") )

      // Create test data.
      Data = TransactionSampleData(Blockchain.get().db)
    }
  }
  override fun afterAll() {
    super.afterAll()
  }

  fun invoke(command : RpcCommand, args : List<JsonElement> = listOf()) : Either<RpcError, RpcResult?> {
    val request = RpcRequest("1.0", 1L, "command-unused", RpcParams(args))
    return command.invoke(request)
  }

  companion object {
    lateinit var Data : TransactionSampleData
  }
}

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
import io.scalechain.blockchain.cli.command.RpcInvoker
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil.bytes

/** Start the peer and connect to the local bitcoind node.
  */
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
}

/**
  * Created by kangmo on 3/15/16.
  */
abstract class APITestSuite : FlatSpec(), Matchers {
  val GENESIS_BLOCK_HEIGHT = 0
  val GENESIS_BLOCK_HASH = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"
  val ALL_ZERO_HASH = Hash(Bytes.from(filledString(64, '0'.toByte())))

  val RPC_USER = "user"
  val RPC_PASSWORD = "pleasechangethispassword123@.@"

  override fun beforeAll() {
    super.beforeAll()

    RunnablePeer.startPeer()
  }
  override fun afterAll() {
    super.afterAll()
  }

  fun invoke(command : RpcCommand, args : List<JsonElement> = listOf()) : Either<RpcError, RpcResult?> {
    val request = RpcRequest("1.0", 1L, "command-unused", RpcParams(args))
    return command.invoke(request)
  }
}

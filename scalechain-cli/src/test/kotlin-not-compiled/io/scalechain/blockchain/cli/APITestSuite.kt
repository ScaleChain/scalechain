package io.scalechain.blockchain.cli

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.command.blockchain.GetBestBlockHash
import io.scalechain.blockchain.api.domain.{RpcError, RpcParams, RpcRequest, RpcResult}
import io.scalechain.blockchain.cli.command.RpcInvoker
import io.scalechain.blockchain.proto.Hash
import org.scalatest.Matchers
import spray.json.JsValue

/** Start the peer and connect to the local bitcoind node.
  */
object StartPeer : Runnable {
  val BITCOIND_PORT = 8333

  val peerThread = Thread( StartPeer )
  peerThread.start()
  // Wait until the peer connects to the bitcoin node.
  Thread.sleep(5);

  fun apply() {
    // do nothing
  }

  fun run() : Unit {
    ScaleChainPeer.main( Array("-a", "localhost", "-x", s"$BITCOIND_PORT") )
  }
}

/**
  * Created by kangmo on 3/15/16.
  */
trait APITestSuite : Matchers {
  val GENESIS_BLOCK_HEIGHT = 0
  val GENESIS_BLOCK_HASH = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"
  val ALL_ZERO_HASH = Hash("0" * 64)

  val RPC_USER = "user"
  val RPC_PASSWORD = "pleasechangethispassword123@.@"
  StartPeer()

  fun invoke(command : RpcCommand, args : List<JsValue> = List()) : Either<RpcError, Option<RpcResult>> {
    val request = RpcRequest(Some("1.0"), 1L, "command-unused", RpcParams(args))
    command.invoke(request)
  }
}

package io.scalechain.blockchain.cli

import io.scalechain.blockchain.api.command.RpcCommand
import io.scalechain.blockchain.api.command.blockchain.GetBestBlockHash
import io.scalechain.blockchain.api.domain.{RpcError, RpcParams, RpcRequest, RpcResult}
import io.scalechain.blockchain.cli.api.RpcInvoker
import io.scalechain.blockchain.proto.Hash
import org.scalatest.ShouldMatchers
import spray.json.JsValue

/** Start the peer and connect to the local bitcoind node.
  */
object StartPeer extends Runnable {
  val BITCOIND_PORT = 8333

  val peerThread = new Thread( StartPeer )
  peerThread.start()
  // Wait until the peer connects to the bitcoin node.
  Thread.sleep(5);

  def apply() = {
    // do nothing
  }

  def run() : Unit = {
    ScaleChainPeer.main( Array("-a", "localhost", "-x", s"$BITCOIND_PORT") )
  }
}

/**
  * Created by kangmo on 3/15/16.
  */
trait APITestSuite extends ShouldMatchers {
  val GENESIS_BLOCK_HEIGHT = 0
  val GENESIS_BLOCK_HASH = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"
  val ALL_ZERO_HASH = Hash("0" * 64)

  val RPC_USER = "user"
  val RPC_PASSWORD = "pleasechangethispassword123@.@"
  StartPeer()

  def invoke(command : RpcCommand, args : List[JsValue] = List()) : Either[RpcError, Option[RpcResult]] = {
    val request = RpcRequest("1.0", "id", "command-unused", RpcParams(args))
    command.invoke(request)
  }
}

package io.scalechain.blockchain.cli.command.stresstests

import io.scalechain.blockchain.cli.command.stresstests.MultiThreadTransactionTester.RawTransactionWithGroupListener
import io.scalechain.blockchain.cli.command.{RpcInvoker, RpcParameters, Command}

object MultiThreadTestRPC extends Command {

  def invoke(command : String, args : Array[String], rpcParams : RpcParameters) = {
    val nodeCount = Integer.parseInt(args(1))
    val transactionGroupCount = Integer.parseInt(args(2))
    val nodeFilterIndexOption = if ( args.length >= 4) Some(Integer.parseInt(args(3))) else None

    val sendRawTransaction : RawTransactionWithGroupListener =
      (txGroupIndex : Int, rawTransaction : String) => {
        // txGroupIndex can be from 0 to 39 in case there are 40 groups.
        // Distribute each group to different nodes.
        // The port of a node is rpcParams.port, rpcParams.port+1, rpcParams.port+2, ...,  rpcParams.port + (node count - 1)
        val port = rpcParams.port + txGroupIndex % nodeCount
        RpcInvoker.invoke("sendrawtransaction", Array(rawTransaction), rpcParams.host, port, rpcParams.user, rpcParams.password)
      }

    val threadGroupIndexFilter =
      nodeFilterIndexOption.map{ nodeFilterIndex =>
        (txGroupIndex : Int) => (txGroupIndex % nodeCount) == nodeFilterIndex
      }
    new MultiThreadTransactionTester(threadGroupIndexFilter).testRawTransaction(transactionGroupCount, sendRawTransaction)
  }
}

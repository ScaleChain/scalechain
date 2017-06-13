package io.scalechain.blockchain.cli.command.stresstests

import io.scalechain.blockchain.cli.command.RpcInvoker
import io.scalechain.blockchain.cli.command.RpcParameters
import io.scalechain.blockchain.cli.command.Command
import io.scalechain.blockchain.cli.command.CommandDescriptor
import io.scalechain.util.ListExt

object MultiThreadTestRPC : Command {
  override val descriptor = CommandDescriptor( "multithreadtestrpc", 3, "multithreadtestrpc <node count> <transaction group count> <filter node index>. multithreadtestrpc calls sendrawtransaction RPC using multi-threads. You need to run generaterawtransaction to generate transaction files used as inputs of this RPC.")


    override fun invoke(command : String, args : Array<String>, rpcParams : RpcParameters) {
    val nodeCount = Integer.parseInt(args[1])
    val transactionGroupCount = Integer.parseInt(args[2])
    val nodeFilterIndexOption = if ( args[3] == "x") null else Integer.parseInt(args[3])

 //   println(s"nodeFilterIndexOption = ${nodeFilterIndexOption}")

    val sendSplitTransaction = object : RawTransactionWithGroupListener {
        override fun onRawTransaction(txGroupIndex: Int, rawTransaction: String): Unit {
            // We need to send the initial split transaction to the node which matches the nodeFilterIndex.

            // We use port 8080 only for launching N scalechain deamons in a local machine.
            val port =
                if (rpcParams.port==8080) rpcParams.port + txGroupIndex % nodeCount
                else rpcParams.port

//        println(s"sendSplitTransaction - Using port ${port}")
            RpcInvoker.invoke("sendrawtransaction", arrayOf(rawTransaction), rpcParams.host, port, rpcParams.user, rpcParams.password)
        }
    }


    val sendThreadTransaction = object : RawTransactionWithGroupListener {
      override fun onRawTransaction(txGroupIndex : Int, rawTransaction : String) : Unit {
          // txGroupIndex can be from 0 to 39 in case there are 40 groups.
          // Distribute each group to different nodes.
          // The port of a node is rpcParams.port, rpcParams.port+1, rpcParams.port+2, ...,  rpcParams.port + (node count - 1)
          val port =
              // We use port 8080 only for launching N scalechain deamons in a local machine.
              if (rpcParams.port==8080) rpcParams.port + txGroupIndex % nodeCount
              else rpcParams.port

//        println(s"sendThreadTransaction - Using port ${port}, txGroupIndex=${txGroupIndex}, nodeCount=${nodeCount}")

          RpcInvoker.invoke("sendrawtransaction", arrayOf(rawTransaction), rpcParams.host, port, rpcParams.user, rpcParams.password)
      }
    }

    val threadGroupIndexFilter : ThreadGroupIndexFilter? =
    if (nodeFilterIndexOption == null) null
      else object : ThreadGroupIndexFilter {
        override fun filterByGroupIndex(txGroupIndex: Int): Boolean {
          return (txGroupIndex % nodeCount) == nodeFilterIndexOption
        }
      }


    val startTimeMillis = System.currentTimeMillis()

    MultiThreadTransactionTester(threadGroupIndexFilter).testRawTransaction(
      sendSplitTransaction, ListExt.fill(transactionGroupCount, sendThreadTransaction))
    val elapsedMillis = System.currentTimeMillis() - startTimeMillis

    println("Elapsed Time(ms) : ${elapsedMillis}")
  }
}

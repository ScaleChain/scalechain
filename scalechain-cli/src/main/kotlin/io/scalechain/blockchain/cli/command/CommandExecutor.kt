package io.scalechain.blockchain.cli.command

import io.scalechain.blockchain.transaction.ChainEnvironment
import io.scalechain.util.Config

data class RpcParameters(
                          host : String = "localhost",
                          port : Int = Config.getInt("scalechain.api.port"),
                          user : String = Config.getString("scalechain.api.user"),
                          password : String = Config.getString("scalechain.api.password")
                        )

data class Parameters(
  rpcParameters: RpcParameters = RpcParameters(),
  network : String = "testnet",
  command : String = null,
  args : Array<String> = Array())

/**
  * Created by kangmo on 1/24/16.
  */
object CommandExecutor {

  fun main(args: Array<String>) {
    val parser = scopt.OptionParser<Parameters>("scalechain-cli") {
      head("scalechain-cli", "1.0")
      opt<String>('h', "host") action { (x, c) =>
        c.copy(rpcParameters = c.rpcParameters.copy(host = x)) } text("host of the ScaleChain Json-RPC service.")
      opt<Int>('p', "port") action { (x, c) =>
        c.copy(rpcParameters = c.rpcParameters.copy(port = x)) } text("port of the ScaleChain Json-RPC service.")
      opt<String>('u', "user") action { (x, c) =>
        c.copy(rpcParameters = c.rpcParameters.copy(user = x)) } text("The user name for RPC authentication")
      opt<String>('p', "password") action { (x, c) =>
        c.copy(rpcParameters = c.rpcParameters.copy(password = x)) } text("The password for RPC authentication")
      opt<String>('n', "network") action { (x, c) =>
        c.copy(network = x)
      } text ("The network to use. currently 'testnet' is supported. Will support 'mainnet' as well as 'regtest' soon.")
      cmd("getinfo") required() action { (_, c) =>
        c.copy(command = "getinfo") } text("getinfo shows current status of the ScaleChain node.")
      cmd("gettxout") required() action { (_, c) =>
        c.copy(command = "gettxout") } text("gettxout shows a transaction output.") children {
        arg<String>("<transaction id> <output index>") minOccurs(2) maxOccurs(2) required() action { (x, c) =>
          c.copy(args = args :+ x) } text("provide the transaction ID and the index of the output you want to see.")
      }
      cmd("generateaddress") required() action { (_, c) =>
        c.copy(command = "generateaddress") } text("generateaddress generates a private key, public key, and an address.")
      cmd("generaterawtransactions") required() action { (_, c) =>
        c.copy(command = "generaterawtransactions") } text("generaterawtransactions generates transactions.") children {
        arg<String>("<private key> <output split count> <transaction group count> <transaction count for each group>") minOccurs(4) maxOccurs(4) required() action { (x, c) =>
          c.copy(args = args :+ x) } text("provide the private key to get coins to test, the number of outputs for split transactions to use, transaction group count for the parallelism in your test, the number of transactions for each group.")
      }
      cmd("multithreadtestlayers") required() action { (_, c) =>
        c.copy(command = "multithreadtestlayers") } text("multithreadtestlayers tests each layer using multi-threads. You need to run generaterawtransaction to generate transaction files used as inputs of this RPC.") children {
        arg<String>("<transaction group count>") minOccurs(1) maxOccurs(1) required() action { (x, c) =>
          c.copy(args = args :+ x) } text("provide transaction group count for the parallelism in your test.")
      }
      cmd("multithreadtestrpc") required() action { (_, c) =>
        c.copy(command = "multithreadtestrpc") } text("multithreadtestrpc calls sendrawtransaction RPC using multi-threads. You need to run generaterawtransaction to generate transaction files used as inputs of this RPC.") children {
        arg<String>("<node count> <transaction group count> <filter node index>") minOccurs(3) maxOccurs(3) required() action { (x, c) =>
          c.copy(args = args :+ x) } text("provide total node count, transaction group count, the node index to send data(x for all nodes, or 0,1,2,.. or node count-1) for the parallelism in your test.")
      }
    }

    // parser.parse returns Option<C>
    parser.parse(args, Parameters()) match {
      case Some(params) =>
        val env : ChainEnvironment = ChainEnvironment.create(params.network).getOrElse {
          println(s"Invalid p2p network : ${params.network}")
          System.exit(-1)
          null
        }

        val commandOption = Commands.commandMap.get(params.command)
        if (commandOption.isDefined) { // If we have the command in the command map, execute it.
          commandOption.get.invoke(params.command, params.args, params.rpcParameters)
        } else { // Otherwise, send the command as a RPC.
          val response = RpcInvoker.invoke(params.command, params.args, params.rpcParameters.host, params.rpcParameters.port, params.rpcParameters.user, params.rpcParameters.password)
        }

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }
}




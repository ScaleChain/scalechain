package io.scalechain.blockchain.cli.command

/**
  * Created by kangmo on 7/28/16.
  */
trait Command {
  fun invoke(command : String, args : Array<String>, rpcParams : RpcParameters)
}

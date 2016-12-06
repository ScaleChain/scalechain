package io.scalechain.blockchain.cli.command

data class CommandDescriptor(
    val command : String,
    val argumentCount : Int,
    val description : String
)


/**
  * Created by kangmo on 7/28/16.
  */
interface Command {
  val descriptor : CommandDescriptor
  fun invoke(command : String, args : Array<String>, rpcParams : RpcParameters)
}

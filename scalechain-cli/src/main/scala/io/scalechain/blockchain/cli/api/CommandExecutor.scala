package io.scalechain.blockchain.cli.api
import io.scalechain.util.Config

case class Parameters(
  host : String = "localhost",
  port : Int = Config.scalechain.getInt("scalechain.api.port"),
  user : String = Config.scalechain.getString("scalechain.api.user"),
  password : String = Config.scalechain.getString("scalechain.api.password"),
  command : String = null,
  args : Array[String] = Array())

/**
  * Created by kangmo on 1/24/16.
  */
object CommandExecutor {

  def main(args: Array[String]) = {
    val parser = new scopt.OptionParser[Parameters]("scopt") {
      head("scalechain", "1.0")
      opt[String]('h', "host") action { (x, c) =>
        c.copy(host = x) } text("host of the ScaleChain Json-RPC service.")
      opt[Int]('p', "port") action { (x, c) =>
        c.copy(port = x) } text("port of the ScaleChain Json-RPC service.")
      opt[String]('u', "user") action { (x, c) =>
        c.copy(host = x) } text("The user name for RPC authentication")
      opt[String]('p', "password") action { (x, c) =>
        c.copy(host = x) } text("The password for RPC authentication")
      cmd("getinfo") required() action { (_, c) =>
        c.copy(command = "getinfo") } text("getinfo shows current status of the ScaleChain node.")
      cmd("gettxout") required() action { (_, c) =>
        c.copy(command = "gettxout") } text("gettxout shows a transaction output.") children {
        arg[String]("<transaction_id> <output_index>") minOccurs(2) maxOccurs(2) required() action { (x, c) =>
          c.copy(args = args :+ x) } text("provide the transaction ID and the index of the output you want to see.")
      }
    }

    // parser.parse returns Option[C]
    parser.parse(args, Parameters()) match {
      case Some(params) =>
        val response = RpcInvoker.invoke(params.command, params.args, params.host, params.port, params.user, params.password)
        println("params:" + params)
        println("response:" + response)

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }
}




package io.scalechain.blockchain.cli.api
import io.scalechain.util.Config

case class Parameters(
  host: String = "localhost",
  port : Int = Config.scalechain.getInt("scalechain.api.port"),
  command : String = null )

/**
  * Created by kangmo on 1/24/16.
  */
object CommandExecutor {

  def main(args: Array[String]) = {
    val parser = new scopt.OptionParser[Parameters]("scopt") {
      head("scalechain", "1.0")
      opt[String]('h', "host") action { (x, c) =>
        c.copy(host = x) } text("host is a string property")
      opt[Int]('p', "port") action { (x, c) =>
        c.copy(port = x) } text("port is an integer property")
      cmd("getinfo") required() action { (_, c) =>
        c.copy(command = "getinfo") } text("getinfo shows current status of the ScaleChain node.")
    }

    // parser.parse returns Option[C]
    parser.parse(args, Parameters()) match {
      case Some(params) =>
        println("params:" + params)

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }
}




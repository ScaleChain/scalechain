package io.scalechain.blockchain.cli.command

/**
  * Created by kangmo on 7/28/16.
  */
trait Command {
  def invoke(command : String, args : Array[String])
}

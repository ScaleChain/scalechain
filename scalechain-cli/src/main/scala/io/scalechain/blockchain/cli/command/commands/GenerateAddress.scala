package io.scalechain.blockchain.cli.command.commands

import io.scalechain.blockchain.transaction.{CoinAddress, PublicKey, PrivateKey}


/**
  * Created by kangmo on 7/28/16.
  */
object GenerateAddress extends Command {
  def invoke(command : String, args : Array[String]) = {
    val privateKey = PrivateKey.generate()
    val publicKey = PublicKey.from(privateKey)
    val address = CoinAddress.from(privateKey)

    println(
      s"""
        |private key : ${privateKey.base58}
        |address : ${address.base58()}
      """.stripMargin
    )
  }
}

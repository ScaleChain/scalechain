package io.scalechain.blockchain.cli.command.stresstests

import io.scalechain.blockchain.cli.command.Command
import io.scalechain.blockchain.transaction.{CoinAddress, PublicKey, PrivateKey}
import io.scalechain.util.HexUtil


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
        |public key hash : ${HexUtil.hex(publicKey.getHash().bytes)}
        |address : ${address.base58()}
      """.stripMargin
    )
  }
}

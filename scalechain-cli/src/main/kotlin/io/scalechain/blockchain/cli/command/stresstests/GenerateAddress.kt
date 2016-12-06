package io.scalechain.blockchain.cli.command.stresstests

import io.scalechain.blockchain.cli.command.RpcParameters
import io.scalechain.blockchain.cli.command.Command
import io.scalechain.blockchain.cli.command.CommandDescriptor
import io.scalechain.blockchain.transaction.CoinAddress
import io.scalechain.blockchain.transaction.PublicKey
import io.scalechain.blockchain.transaction.PrivateKey
import io.scalechain.util.HexUtil


/**
  * Created by kangmo on 7/28/16.
  */
object GenerateAddress : Command {
  override val descriptor = CommandDescriptor( "generateaddress", 0, "generateaddress generates a private key, public key, and an address.")

  override fun invoke(command : String, args : Array<String>, rpcParams : RpcParameters) {
    val privateKey = PrivateKey.generate()
    val publicKey = PublicKey.from(privateKey)
    val address = CoinAddress.from(privateKey)

    println(
      """
        |private key : ${privateKey.base58()}
        |public key hash : ${HexUtil.hex(publicKey.getHash().value)}
        |address : ${address.base58()}
      """.trimMargin()
    )
  }
}

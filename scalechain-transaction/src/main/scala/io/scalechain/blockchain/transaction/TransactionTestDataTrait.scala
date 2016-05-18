package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.blockchain.proto.{OutPoint, Hash}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.util.HexUtil

/**
  * Created by kangmo on 5/18/16.
  */
trait TransactionTestDataTrait extends ProtoTestData {
  case class AddressData(privateKey : PrivateKey, publicKey : PublicKey, pubKeyScript : ParsedPubKeyScript, address : CoinAddress)

  def generateAddress() : AddressData = {
    val privateKey   = PrivateKey.generate
    val publicKey = PublicKey.from(privateKey)
    val publicKeyScript = ParsedPubKeyScript.from(privateKey)
    val address       = CoinAddress.from(privateKey)

    AddressData(
      privateKey,
      publicKey,
      publicKeyScript,
      address
    )
  }

  val TXHASH1 = Hash( HashCalculator.transactionHash(transaction1) )
  val TXHASH2 = Hash( HashCalculator.transactionHash(transaction2) )
  val TXHASH3 = Hash( HashCalculator.transactionHash(transaction3) )

  val OUTPOINT1 = OutPoint(TXHASH1, 0)
  val OUTPOINT2 = OutPoint(TXHASH2, 0)
  val OUTPOINT3 = OutPoint(TXHASH3, 0)

  val ADDR1 = generateAddress()
  val ADDR2 = generateAddress()
  val ADDR3 = generateAddress()
}


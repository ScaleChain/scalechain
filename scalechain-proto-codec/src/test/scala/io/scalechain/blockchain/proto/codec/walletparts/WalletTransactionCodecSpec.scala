package io.scalechain.blockchain.proto.codec.walletparts

import io.scalechain.blockchain.proto.codec.WalletTransactionCodec
import io.scalechain.blockchain.proto.codec.primitive.{CString, VarInt, CodecSuite}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.test.ProtoTestData

class WalletTransactionCodecSpec extends CodecSuite with ProtoTestData {

  implicit val walletTxCodec = WalletTransactionCodec.codec

  val walletTx1 = WalletTransaction(
    involvesWatchonly = true,
    account          = "someone else's address2",
    outputOwnership  = Some("n3GNqMveyvaPvUbH469vDRadqpJMPc84JA"),
    attributes       = WalletTransactionAttribute.RECEIVE,
    amount           = scala.math.BigDecimal( 0.0005),
    fee              = Some(scala.math.BigDecimal(0.0002)),
    confirmations    = Some(34714),
    generated        = None,
    blockhash        = Some(Hash("00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929")),
    blockindex       = Some(11),
    blocktime        = Some(1411051649),
    txid             = Some(Hash("99845fd840ad2cc4d6f93fafb8b072d188821f55d9298772415175c456f3077d")),
    time             = 1418695703,
    timereceived = Some(1418925580),
    transaction = transaction1
  )

  "WalletTransactionCodec" should {

    implicit val countCodec = CString.codec

    "roundtrip" in {
      roundtrip(walletTx1)
    }
  }
}




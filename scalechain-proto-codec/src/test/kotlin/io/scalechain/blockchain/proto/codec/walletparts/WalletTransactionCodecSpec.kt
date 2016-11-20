package io.scalechain.blockchain.proto.codec.walletparts

import io.scalechain.blockchain.proto.codec.WalletTransactionCodec
import io.scalechain.blockchain.proto.codec.primitive.{CString, VarInt, CodecSuite}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.test.ProtoTestData

class WalletTransactionCodecSpec extends CodecSuite with ProtoTestData {

  implicit val walletTxCodec = WalletTransactionCodec.codec

  val walletTx1 = WalletTransaction(
    blockHash        = Some(Hash("00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929")),
    blockIndex       = Some(11),
    blockTime        = Some(1411051649),
    transactionId    = Some(Hash("99845fd840ad2cc4d6f93fafb8b072d188821f55d9298772415175c456f3077d")),
    addedTime     = 1418695703,
    transactionIndex = Some(1),
    transaction = transaction1
  )

  "WalletTransactionCodec" should {

    "roundtrip" in {
      roundtrip(walletTx1)
    }
  }
}




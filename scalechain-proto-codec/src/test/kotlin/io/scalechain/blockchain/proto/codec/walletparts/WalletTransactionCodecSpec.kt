package io.scalechain.blockchain.proto.codec.walletparts

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.codec.CodecTestUtil
import io.scalechain.blockchain.proto.codec.WalletTransactionCodec
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class WalletTransactionCodecSpec : FlatSpec(), Matchers, CodecTestUtil, ProtoTestData {

  val walletTx1 = WalletTransaction(
    blockHash        = Hash(bytes("00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929")),
    blockIndex       = 11,
    blockTime        = 1411051649,
    transactionId    = Hash(bytes("99845fd840ad2cc4d6f93fafb8b072d188821f55d9298772415175c456f3077d")),
    addedTime     = 1418695703,
    transactionIndex = 1,
    transaction = transaction1()
  )

  init {
    "WalletTransactionCodec" should "roundtrip" {
      roundTrip(WalletTransactionCodec, walletTx1) shouldBe true
    }
  }
}




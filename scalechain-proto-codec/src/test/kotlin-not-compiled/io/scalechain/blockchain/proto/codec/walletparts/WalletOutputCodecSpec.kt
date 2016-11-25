package io.scalechain.blockchain.proto.codec.walletparts

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.codec.WalletOutputCodec
import io.scalechain.blockchain.proto.codec.CodecSuite
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.util.HexUtil

class WalletOutputCodecSpec : CodecSuite with ProtoTestData {

  implicit val outputDescriptorCodec = WalletOutputCodec.codec

  val walletOutput1 =
    WalletOutput(
      blockindex    = Some(100L),
      coinbase      = true,
      spent         = false,
      transactionOutput = transaction1.outputs(0)
    )

  "WalletOutputCodec" should {
    "roundtrip" in {
      roundtrip(walletOutput1)
    }

    "roundtrip with parse/serialize" in {
      val serialized = WalletOutputCodec.serialize( walletOutput1 )
      val parsed : WalletOutput = WalletOutputCodec.parse(serialized)
      parsed shouldBe walletOutput1
    }
  }

}




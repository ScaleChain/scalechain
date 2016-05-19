package io.scalechain.blockchain.proto.codec.walletparts

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.codec.WalletOutputCodec
import io.scalechain.blockchain.proto.codec.primitive.CodecSuite
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.util.HexUtil

class WalletOutputCodecSpec extends CodecSuite with ProtoTestData {

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

    "scodec bool codec bug should be fixed." in {
      // scodec 1.8.3 had an issue not fully consuming a Boolean value using bool codec
      // when I tested with scodec 1.9.0, the issue was fixed.
      // leaving the test case to make sure it works well.
      val output = WalletOutput(
        blockindex    = Some(100L),
        coinbase      = true,
        spent         = true,
        transactionOutput = transaction1.outputs(0)
      )

      val serialized = WalletOutputCodec.serialize( output )
      val parsed : WalletOutput = WalletOutputCodec.parse(serialized)
      parsed shouldBe output
    }
  }

}




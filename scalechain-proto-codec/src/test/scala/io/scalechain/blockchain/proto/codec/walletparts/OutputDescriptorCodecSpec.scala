package io.scalechain.blockchain.proto.codec.walletparts

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.codec.WalletOutputCodec
import io.scalechain.blockchain.proto.codec.primitive.CodecSuite
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.util.HexUtil

class OutputDescriptorCodecSpec extends CodecSuite with ProtoTestData {

  implicit val outputDescriptorCodec = WalletOutputCodec.codec

  val outputDescriptor1 =
    WalletOutput(
      spent         = false,
      transactionOutput = transaction1.outputs(0)
    )

  "WalletTransactionCodec" should {

    "roundtrip" in {
      roundtrip(outputDescriptor1)
    }
  }
}




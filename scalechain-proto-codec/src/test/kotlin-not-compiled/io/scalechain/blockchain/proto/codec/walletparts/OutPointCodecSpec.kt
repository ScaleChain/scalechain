package io.scalechain.blockchain.proto.codec.walletparts

import io.scalechain.blockchain.proto.codec.OutPointCodec
import io.scalechain.blockchain.proto.{Hash, OutPoint}
import io.scalechain.blockchain.proto.codec.primitive.CodecSuite
import io.scalechain.blockchain.proto.test.ProtoTestData

class OutPointCodecSpec : CodecSuite with ProtoTestData {

  implicit val outPointCodec = OutPointCodec.codec

  val outPoint1 = OutPoint(
    transactionHash  = Hash("00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929"),
    outputIndex      = 1
  )

  "OutPointCodec" should {

    "roundtrip" in {
      roundtrip(outPoint1)
    }
  }
}


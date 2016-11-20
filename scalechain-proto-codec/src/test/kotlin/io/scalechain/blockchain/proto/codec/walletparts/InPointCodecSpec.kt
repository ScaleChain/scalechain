package io.scalechain.blockchain.proto.codec.walletparts

import io.scalechain.blockchain.proto.{Hash, InPoint}
import io.scalechain.blockchain.proto.codec.{InPointCodec}
import io.scalechain.blockchain.proto.codec.primitive.CodecSuite
import io.scalechain.blockchain.proto.test.ProtoTestData

class InPointCodecSpec : CodecSuite with ProtoTestData {

  implicit val inPointCodec = InPointCodec.codec

  val inPoint1 = InPoint(
    transactionHash  = Hash("00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929"),
    inputIndex       = 1
  )

  "InPointCodec" should {

    "roundtrip" in {
      roundtrip(inPoint1)
    }
  }
}

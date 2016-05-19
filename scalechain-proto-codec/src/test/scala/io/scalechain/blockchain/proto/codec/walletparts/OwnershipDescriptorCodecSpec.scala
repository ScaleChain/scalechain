package io.scalechain.blockchain.proto.codec.walletparts

import io.scalechain.blockchain.proto.{WalletOutput, Hash, OwnershipDescriptor}
import io.scalechain.blockchain.proto.codec._
import io.scalechain.blockchain.proto.codec.primitive.CodecSuite
import io.scalechain.blockchain.proto.test.ProtoTestData

class OwnershipDescriptorCodecSpec extends CodecSuite with ProtoTestData {

  implicit val ownershipDescriptorCodec = OwnershipDescriptorCodec.codec

  val ownershipDescriptor = OwnershipDescriptor(
    account  = "kangmo",
    privateKeys = List( "00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929" )
  )

  "OwnershipDescriptorCodec" should {

    "roundtrip" in {
      roundtrip(ownershipDescriptor)
    }
/*
    "test" in {

      val serialized = OwnershipDescriptorCodec.serialize( ownershipDescriptor )
      val parsed : OwnershipDescriptor = OwnershipDescriptorCodec.parse(serialized)
      parsed shouldBe ownershipDescriptor
    }
    */
  }
}


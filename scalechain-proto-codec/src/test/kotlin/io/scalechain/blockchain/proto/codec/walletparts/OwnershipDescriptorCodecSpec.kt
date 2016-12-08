package io.scalechain.blockchain.proto.codec.walletparts

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.CodecTestUtil
import io.scalechain.blockchain.proto.codec.*
import org.junit.runner.RunWith


@RunWith(KTestJUnitRunner::class)
class OwnershipDescriptorCodecSpec : FlatSpec(), Matchers, CodecTestUtil {


  val ownershipDescriptor = OwnershipDescriptor(
    account  = "kangmo",
    privateKeys = listOf( "00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929" )
  )

  init {

    "OwnershipDescriptorCodec" should "roundtrip" {
      roundTrip(OwnershipDescriptorCodec, ownershipDescriptor) shouldBe true
    }
  }
/*
    "test" {

      val serialized = OwnershipDescriptorCodec.serialize( ownershipDescriptor )
      val parsed : OwnershipDescriptor = OwnershipDescriptorCodec.parse(serialized)
      parsed shouldBe ownershipDescriptor
    }
    */
}


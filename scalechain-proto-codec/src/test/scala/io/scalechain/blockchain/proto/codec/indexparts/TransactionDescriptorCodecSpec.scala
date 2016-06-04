package io.scalechain.blockchain.proto.codec.indexparts

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.messages.TransactionSpec
import io.scalechain.blockchain.proto.codec.{TransactionDescriptorCodec, InPointCodec}
import io.scalechain.blockchain.proto.codec.primitive.CodecSuite
import io.scalechain.blockchain.proto.test.ProtoTestData

class TransactionDescriptorCodecSpec extends CodecSuite with ProtoTestData {
  implicit val inPointCodec = TransactionDescriptorCodec.codec

  val DUMMY_HASH1 = Hash ( "1" * 64 )
  val DUMMY_HASH2 = Hash ( "2" * 64 )

  val txDesc1 = TransactionDescriptor(
    transaction = Left( FileRecordLocator( 1, RecordLocator(2,3)) ),
    outputsSpentBy = List( None, Some(InPoint(DUMMY_HASH1, 1)), None )
  )

  val txDesc2 = TransactionDescriptor(
    transaction = Right( TransactionSpec.SampleTransaction ),
    outputsSpentBy = List( None, None, Some(InPoint(DUMMY_HASH2, 2)) )
  )


  "TransactionDescriptorCodecSpec" should {
    "roundtrip" in {
      roundtrip(txDesc1)
      roundtrip(txDesc2)
    }
  }
}

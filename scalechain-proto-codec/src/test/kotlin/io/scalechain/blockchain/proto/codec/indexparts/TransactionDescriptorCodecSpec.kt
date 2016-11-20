package io.scalechain.blockchain.proto.codec.indexparts

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.messages.TransactionSpec
import io.scalechain.blockchain.proto.codec.{TransactionDescriptorCodec, InPointCodec}
import io.scalechain.blockchain.proto.codec.primitive.CodecSuite
import io.scalechain.blockchain.proto.test.ProtoTestData

class TransactionDescriptorCodecSpec : CodecSuite with ProtoTestData {
  implicit val inPointCodec = TransactionDescriptorCodec.codec

  val DUMMY_HASH1 = Hash ( "1" * 64 )

  val txDesc1 = TransactionDescriptor(
    transactionLocator = FileRecordLocator( 1, RecordLocator(2,3)  ),
    1234,
    outputsSpentBy = List( None, Some(InPoint(DUMMY_HASH1, 1)), None )
  )

  "TransactionDescriptorCodecSpec" should {
    "roundtrip" in {
      roundtrip(txDesc1)
    }
  }
}

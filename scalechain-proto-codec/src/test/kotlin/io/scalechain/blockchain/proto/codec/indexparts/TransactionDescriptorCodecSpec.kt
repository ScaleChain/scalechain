package io.scalechain.blockchain.proto.codec.indexparts

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.TransactionDescriptorCodec
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.test.TestMethods.filledString
import io.scalechain.util.HexUtil
import io.scalechain.util.HexUtil.bytes
import io.scalechain.util.ListExt
import org.junit.runner.RunWith
import kotlin.text.String

@RunWith(KTestJUnitRunner::class)
class TransactionDescriptorCodecSpec : PayloadTestSuite<TransactionDescriptor>()  {

  override val codec = TransactionDescriptorCodec

  override val payload = bytes(
      // BUGBUG : The test will fail.
      """0100000002000000000000000300000000000000000004d203000111111111111111111111111111111111111111111111111111111111111111110100000000
      """)

  val DUMMY_HASH1 = Hash ( HexUtil.bytes( filledString(64, '1'.toByte() )) )

  override val message = TransactionDescriptor(
      transactionLocator = FileRecordLocator( 1, RecordLocator(2,3)  ),
      blockHeight = 1234L,
      outputsSpentBy = listOf( null, InPoint(DUMMY_HASH1, 1), null )
  )
}


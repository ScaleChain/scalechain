package io.scalechain.blockchain.proto.codec.indexparts


import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

import io.scalechain.blockchain.proto.RecordLocator
import io.scalechain.blockchain.proto.codec.RecordLocatorCodec

@RunWith(KTestJUnitRunner::class)
class RecordLocatorCodecSpec : PayloadTestSuite<RecordLocator>()  {

  override val codec = RecordLocatorCodec

  override val payload = bytes(
    """
      0a 00 00 00 00 00 00 00   c8 00 00 00
    """)

  override val message = RecordLocator(
    offset = 10,
    size = 200
  )
}

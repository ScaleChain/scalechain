package io.scalechain.blockchain.proto.codec.indexparts

import io.scalechain.blockchain.proto.RecordLocator
import io.scalechain.blockchain.proto.codec.{PayloadTestSuite, RecordLocatorCodec}
import io.scalechain.util.HexUtil._

class RecordLocatorCodecSpec extends PayloadTestSuite[RecordLocator]  {

  val codec = RecordLocatorCodec.codec

  val payload = bytes(
    """
      0a 00 00 00 00 00 00 00   c8 00 00 00
    """)

  val message = RecordLocator(
    offset = 10,
    size = 200
  )
}

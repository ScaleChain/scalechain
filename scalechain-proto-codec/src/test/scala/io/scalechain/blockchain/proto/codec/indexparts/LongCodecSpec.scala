package io.scalechain.blockchain.proto.codec.indexparts

import io.scalechain.blockchain.proto.LongValue
import io.scalechain.blockchain.proto.codec.{LongValueCodec, OneByteCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

class LongValueCodecSpec extends PayloadTestSuite[LongValue]  {

  val codec = LongValueCodec.codec

  val payload = bytes(
    """ 7f ff ff ff ff ff ff ff
    """)

  val message = LongValue(Long.MaxValue)
}

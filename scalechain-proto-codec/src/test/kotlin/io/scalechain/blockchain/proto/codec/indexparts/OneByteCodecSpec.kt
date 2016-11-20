package io.scalechain.blockchain.proto.codec.indexparts

import io.scalechain.blockchain.proto.OneByte
import io.scalechain.blockchain.proto.codec.{OneByteCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

class OneByteCodecSpec extends PayloadTestSuite[OneByte]  {

  val codec = OneByteCodec.codec

  val payload = bytes(
    """
       61
    """)

  val message = OneByte('a')
}

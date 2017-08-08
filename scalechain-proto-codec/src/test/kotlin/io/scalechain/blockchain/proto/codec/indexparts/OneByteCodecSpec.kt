package io.scalechain.blockchain.proto.codec.indexparts

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.OneByte
import io.scalechain.blockchain.proto.codec.OneByteCodec
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class OneByteCodecSpec : PayloadTestSuite<OneByte>()  {

  override val codec = OneByteCodec

  override val payload = bytes(
    """
       61
    """)

  override val message = OneByte('a'.toByte())
}

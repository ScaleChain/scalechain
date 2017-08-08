package io.scalechain.blockchain.proto.codec.indexparts

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.LongValue
import io.scalechain.blockchain.proto.codec.LongValueCodec
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class LongValueCodecSpec : PayloadTestSuite<LongValue>()  {

  override val codec = LongValueCodec

  override val payload = bytes(
    """ 7f ff ff ff ff ff ff ff
    """)

  override val message = LongValue(Long.MAX_VALUE)
}

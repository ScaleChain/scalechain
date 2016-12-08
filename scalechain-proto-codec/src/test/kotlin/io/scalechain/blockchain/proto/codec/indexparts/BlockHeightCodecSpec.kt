package io.scalechain.blockchain.proto.codec.indexparts

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.BlockHeight
import io.scalechain.blockchain.proto.codec.BlockHeightCodec
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class BlockHeightCodecSpec : PayloadTestSuite<BlockHeight>()  {

  override val codec = BlockHeightCodec

  override val payload = bytes(
    """
       00 00 00 00 00 00 00 01
    """)

  override val message = BlockHeight(1)
}

package io.scalechain.blockchain.proto.codec.indexparts

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.BlockFileInfo
import io.scalechain.blockchain.proto.codec.BlockFileInfoCodec
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class BlockFileInfoCodecSpec : PayloadTestSuite<BlockFileInfo>()  {

  override val codec = BlockFileInfoCodec

  override val payload = bytes(
    """
       01 00 00 00 64 00 00 00  00 00 00 00 00 00 00 00
       00 00 00 00 05 00 00 00  00 00 00 00 d2 02 96 49
       00 00 00 00 ea 16 b0 4c  02 00 00 00
    """)

  override val message = BlockFileInfo(
    blockCount =1,
    fileSize =100,
    firstBlockHeight = 0,
    lastBlockHeight = 5,
    firstBlockTimestamp = 1234567890L,
    lastBlockTimestamp = 9876543210L
  )
}

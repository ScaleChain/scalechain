package io.scalechain.blockchain.proto.codec.indexparts

import io.scalechain.blockchain.proto.BlockFileInfo
import io.scalechain.blockchain.proto.codec.{BlockFileInfoCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

class BlockFileInfoCodecSpec : PayloadTestSuite<BlockFileInfo>  {

  val codec = BlockFileInfoCodec.codec

  val payload = bytes(
    """
       01 00 00 00 64 00 00 00  00 00 00 00 00 00 00 00
       00 00 00 00 05 00 00 00  00 00 00 00 d2 02 96 49
       00 00 00 00 ea 16 b0 4c  02 00 00 00
    """)

  val message = BlockFileInfo(
    blockCount =1,
    fileSize =100,
    firstBlockHeight = 0,
    lastBlockHeight = 5,
    firstBlockTimestamp = 1234567890L,
    lastBlockTimestamp = 9876543210L
  )
}

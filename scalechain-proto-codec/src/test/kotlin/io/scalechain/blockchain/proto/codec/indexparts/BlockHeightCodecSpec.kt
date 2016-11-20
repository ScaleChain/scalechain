package io.scalechain.blockchain.proto.codec.indexparts

import io.scalechain.blockchain.proto.BlockHeight
import io.scalechain.blockchain.proto.codec.{BlockHeightCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

class BlockHeightCodecSpec : PayloadTestSuite<BlockHeight>  {

  val codec = BlockHeightCodec.codec

  val payload = bytes(
    """
       00 00 00 00 00 00 00 01
    """)

  val message = BlockHeight(1)
}

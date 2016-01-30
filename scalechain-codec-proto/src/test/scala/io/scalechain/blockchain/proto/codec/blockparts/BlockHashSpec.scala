package io.scalechain.blockchain.proto.codec.blockparts

import io.scalechain.blockchain.proto.BlockHash
import io.scalechain.blockchain.proto.codec.{BlockHashCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
[Bitcoin Core Packets Captured]
  */
class BlockHashSpec extends PayloadTestSuite[BlockHash]  {

  val codec = BlockHashCodec.codec

  val payload = bytes("")

  val message = null//BlockHash()

}



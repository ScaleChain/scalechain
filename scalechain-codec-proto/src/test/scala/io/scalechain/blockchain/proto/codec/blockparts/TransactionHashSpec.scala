package io.scalechain.blockchain.proto.codec.blockparts

import io.scalechain.blockchain.proto.TransactionHash
import io.scalechain.blockchain.proto.codec.{TransactionHashCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
  * [Bitcoin Core Packets Captured]
  *
  *  7b 1e ab e0 20 9b 1f e7  94 12 45 75 ef 80 70 57
  *  c7 7a da 21 38 ae 4f a8  d6 c4 de 03 98 a1 4f 3f  ... 32 bytes of hash
  */
class TransactionHashSpec extends PayloadTestSuite[TransactionHash]  {

  val codec = TransactionHashCodec.codec

  val payload = bytes(
    """
      7b 1e ab e0 20 9b 1f e7  94 12 45 75 ef 80 70 57
      c7 7a da 21 38 ae 4f a8  d6 c4 de 03 98 a1 4f 3f
    """)

  val message = null// TransactionHash()

}

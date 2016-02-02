package io.scalechain.blockchain.proto.codec.messageparts

import io.scalechain.blockchain.proto.MerkleRootHash
import io.scalechain.blockchain.proto.codec.{MerkleRootHashCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
  * [Bitcoin Core Packets Captured]
  *
  *  7b 1e ab e0 20 9b 1f e7  94 12 45 75 ef 80 70 57
  *  c7 7a da 21 38 ae 4f a8  d6 c4 de 03 98 a1 4f 3f  ... 32 bytes of hash
  */
class MerkleRootHashSpec extends PayloadTestSuite[MerkleRootHash]  {

  val codec = MerkleRootHashCodec.codec

  val payload = bytes(
    """
      7b 1e ab e0 20 9b 1f e7  94 12 45 75 ef 80 70 57
      c7 7a da 21 38 ae 4f a8  d6 c4 de 03 98 a1 4f 3f
    """)

  val message = MerkleRootHash(bytes("3f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e7b"))

}

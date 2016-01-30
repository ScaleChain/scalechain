package io.scalechain.blockchain.proto.codec.blockparts

import io.scalechain.blockchain.proto.MerkleRootHash
import io.scalechain.blockchain.proto.codec.{MerkleRootHashCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
[Bitcoin Core Packets Captured]
  */
class MerkleRootHashSpec extends PayloadTestSuite[MerkleRootHash]  {

  val codec = MerkleRootHashCodec.codec

  val payload = bytes("")

  val message = null//MerkleRootHash()

}

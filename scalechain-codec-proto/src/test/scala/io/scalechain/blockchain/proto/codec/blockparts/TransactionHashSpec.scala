package io.scalechain.blockchain.proto.codec.blockparts

import io.scalechain.blockchain.proto.TransactionHash
import io.scalechain.blockchain.proto.codec.{TransactionHashCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
[Bitcoin Core Packets Captured]
  */
class TransactionHashSpec extends PayloadTestSuite[TransactionHash]  {

  val codec = TransactionHashCodec.codec

  val payload = bytes("")

  val message = null// TransactionHash()

}

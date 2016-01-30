package io.scalechain.blockchain.proto.codec.blockparts

import io.scalechain.blockchain.proto.TransactionInput
import io.scalechain.blockchain.proto.codec.{TransactionInputCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
[Bitcoin Core Packets Captured]
  */
class TransactionInputSpec extends PayloadTestSuite[TransactionInput]  {

  val codec = TransactionInputCodec.codec

  val payload = bytes("")

  val message = null//TransactionInput()

}

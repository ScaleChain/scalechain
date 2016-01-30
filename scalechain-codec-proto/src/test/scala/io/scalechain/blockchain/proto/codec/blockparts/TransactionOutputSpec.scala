package io.scalechain.blockchain.proto.codec.blockparts

import io.scalechain.blockchain.proto.TransactionOutput
import io.scalechain.blockchain.proto.codec.{TransactionOutputCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
[Bitcoin Core Packets Captured]
  */
class TransactionOutputSpec extends PayloadTestSuite[TransactionOutput]  {

  val codec = TransactionOutputCodec.codec

  val payload = bytes("")

  val message = null//TransactionOutput()

}

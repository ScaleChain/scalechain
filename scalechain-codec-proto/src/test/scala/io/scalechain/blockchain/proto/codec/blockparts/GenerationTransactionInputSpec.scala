package io.scalechain.blockchain.proto.codec.blockparts

import io.scalechain.blockchain.proto.GenerationTransactionInput
import io.scalechain.blockchain.proto.codec.{GenerationTransactionInputCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
[Bitcoin Core Packets Captured]
  */
class GenerationTransactionInputSpec extends PayloadTestSuite[GenerationTransactionInput]  {

  val codec = GenerationTransactionInputCodec.codec

  val payload = bytes("")

  val message = null//GenerationTransactionInput()

}

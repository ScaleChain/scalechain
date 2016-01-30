package io.scalechain.blockchain.proto.codec.blockparts

import io.scalechain.blockchain.proto.NormalTransactionInput
import io.scalechain.blockchain.proto.codec.{NormalTransactionInputCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
[Bitcoin Core Packets Captured]
  */
class NormalTransactionInputSpec extends PayloadTestSuite[NormalTransactionInput]  {

  val codec = NormalTransactionInputCodec.codec

  val payload = bytes("")

  val message = null// NormalTransactionInput()

}

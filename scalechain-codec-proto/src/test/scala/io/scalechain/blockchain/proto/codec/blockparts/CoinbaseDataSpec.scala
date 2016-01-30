package io.scalechain.blockchain.proto.codec.blockparts

import io.scalechain.blockchain.proto.CoinbaseData
import io.scalechain.blockchain.proto.codec.{CoinbaseDataCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._

/**
[Bitcoin Core Packets Captured]
  */
class CoinbaseDataSpec extends PayloadTestSuite[CoinbaseData]  {

  val codec = CoinbaseDataCodec.codec

  val payload = bytes("")

  val message = null//CoinbaseData()

}

package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.FilterClear
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
  *  [Bitcoin Core Packets Not Captured]
  *  No Payload
  */
class FilterClearSpec extends PayloadTestSuite[FilterClear]  {

  val codec = FilterClearCodec.codec

  val payload = bytes("") // No Payload.

  val message = null//FilterClear()

}

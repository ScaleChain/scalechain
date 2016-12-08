package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.util.HexUtil.bytes

/**
  *  <Bitcoin Core Packets Not Captured>
  *
  *  02 ......... Filter bytes: 2
  *  b50f ....... Filter: 1010 1101 1111 0000
  *  0b000000 ... nHashFuncs: 11
  *  00000000 ... nTweak: 0/none
  *  00 ......... nFlags: BLOOM_UPDATE_NONE
  *
  */
/*

@RunWith(KTestJUnitRunner::class)
class FilterLoadSpec : PayloadTestSuite<FilterLoad>  {

  val codec = FilterLoadCodec.codec

  val payload = bytes(
    """
      02
      b50f
      0b000000
      00000000
      00
    """)

  val message = null//FilterLoad()
}
*/
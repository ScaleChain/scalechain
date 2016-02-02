package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.FilterAdd
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
  * [Bitcoin Core Packets Not Captured]
  *
  *  20 ................................. Element bytes: 32
  *  fdacf9b3eb077412e7a968d2e4f11b9a
  *  9dee312d666187ed77ee7d26af16cb0b ... Element (A TXID)
  *
  */
class FilterAddSpec extends PayloadTestSuite[FilterAdd]  {

  val codec = FilterAddCodec.codec


  val payload = bytes(
    """
      20
      fdacf9b3eb077412e7a968d2e4f11b9a
      9dee312d666187ed77ee7d26af16cb0b
    """)

  val message = null//FilterAdd()

}

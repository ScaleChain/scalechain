package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.Mempool
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
 *  [Bitcoin Core Packets Not Captured]
 *  No payload.
 */
class MempoolSpec extends PayloadTestSuite[Mempool]  {

  val codec = MempoolCodec.codec

  val payload = bytes("") // No payload.

  val message = null//Mempool()

}

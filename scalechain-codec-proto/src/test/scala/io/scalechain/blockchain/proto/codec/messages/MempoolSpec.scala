package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.Mempool
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
    [Bitcoin Core Packets Not Captured]
 */
class MempoolSpec extends EnvelopeTestSuite[Mempool]  {

  val codec = MempoolCodec.codec

  val envelopeHeader = bytes("""
                             """)

  val payload = bytes("")

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "mempool",
    payload.length.toInt,
    Checksum.fromHex(""),
    BitVector.view(payload)
  )

  val message = null//Mempool()

}

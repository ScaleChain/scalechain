package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.FilterLoad
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
    [Bitcoin Core Packets Not Captured]
  */
class FilterLoadSpec extends EnvelopeTestSuite[FilterLoad]  {

  val codec = FilterLoadCodec.codec

  val envelopeHeader = bytes("""
                             """)

  val payload = bytes("")

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "filterload",
    payload.length.toInt,
    Checksum.fromHex(""),
    BitVector.view(payload)
  )

  val message = null//FilterLoad()

}

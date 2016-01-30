package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.FilterClear
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
    [Bitcoin Core Packets Not Captured]
  */
class FilterClearSpec extends EnvelopeTestSuite[FilterClear]  {

  val codec = FilterClearCodec.codec

  val envelopeHeader = bytes("""
                             """)

  val payload = bytes("")

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "filterclear",
    payload.length.toInt,
    Checksum.fromHex(""),
    BitVector.view(payload)
  )

  val message = null//FilterClear()

}

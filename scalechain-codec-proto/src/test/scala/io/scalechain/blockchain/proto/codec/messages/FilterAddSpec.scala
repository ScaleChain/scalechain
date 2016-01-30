package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.FilterAdd
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
    [Bitcoin Core Packets Not Captured]
  */
class FilterAddSpec extends EnvelopeTestSuite[FilterAdd]  {

  val codec = FilterAddCodec.codec

  val envelopeHeader = bytes("""
                             """)

  val payload = bytes("")

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "filteradd",
    payload.length.toInt,
    Checksum.fromHex(""),
    BitVector.view(payload)
  )

  val message = null//FilterAdd()

}

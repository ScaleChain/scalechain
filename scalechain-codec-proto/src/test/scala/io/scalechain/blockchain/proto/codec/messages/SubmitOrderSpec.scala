package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.SubmitOrder
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
    [Bitcoin Core Packets Not Captured]
  */
class SubmitOrderSpec extends EnvelopeTestSuite[SubmitOrder]  {

  val codec = SubmitOrderCodec.codec

  val envelopeHeader = bytes("""
                             """)

  val payload = bytes("")

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "",
    payload.length.toInt,
    Checksum.fromHex(""),
    BitVector.view(payload)
  )

  val message = null//SubmitOrder()

}

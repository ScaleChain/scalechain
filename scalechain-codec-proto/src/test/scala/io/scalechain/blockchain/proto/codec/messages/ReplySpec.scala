package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.Reply
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
    [Bitcoin Core Packets Not Captured]
  */
class ReplySpec extends EnvelopeTestSuite[Reply]  {

  val codec = ReplyCodec.codec

  val envelopeHeader = bytes("""
                             """)

  val payload = bytes("")

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "reply",
    payload.length.toInt,
    Checksum.fromHex(""),
    BitVector.view(payload)
  )

  val message = null//Reply()

}

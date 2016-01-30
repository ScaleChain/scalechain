package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.CheckOrder
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
   [Bitcoin Core Packets Not Captured]
 */
class CheckOrderSpec extends EnvelopeTestSuite[CheckOrder]  {

  val codec = CheckOrderCodec.codec

  val envelopeHeader = bytes("""
                             """)

  val payload = bytes("")

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "checkorder",
    payload.length.toInt,
    Checksum.fromHex(""),
    BitVector.view(payload)
  )

  val message = null//CheckOrder()

}

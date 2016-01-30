package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.Alert
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
[Bitcoin Core Packets Captured]
  */
class AlertSpec extends EnvelopeTestSuite[Alert]  {

  val codec = AlertCodec.codec

  val envelopeHeader = bytes("""
                             """)

  val payload = bytes("")

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "alert",
    payload.length.toInt,
    Checksum.fromHex(""),
    BitVector.view(payload)
  )

  val message = null//Alert()

}

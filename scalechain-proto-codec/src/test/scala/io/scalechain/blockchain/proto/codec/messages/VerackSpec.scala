package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
[Bitcoin Core Packets Captured]
  [NET] recv; header:
  <Header> Magic:ù¾´Ù, Command:verack, Size:0, Checksum:-488573347
  dumping data len : 24
  00000000  f9 be b4 d9 76 65 72 61  63 6b 00 00 00 00 00 00  ù¾´Ùverack......
  00000010  00 00 00 00 5d f6 e0 e2                           ....]öàâ
  */
class VerackSpec extends EnvelopeTestSuite[Verack]  {

  val codec = VerackCodec.codec

  val envelopeHeader = bytes(
    """
      f9 be b4 d9 76 65 72 61  63 6b 00 00 00 00 00 00
      00 00 00 00 5d f6 e0 e2
    """)

  val payload = bytes("")

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "verack",
    payload.length.toInt,
    Checksum.fromHex("e2 e0 f6 5d"),
    BitVector.view(payload)
  )

  val message = Verack()

}

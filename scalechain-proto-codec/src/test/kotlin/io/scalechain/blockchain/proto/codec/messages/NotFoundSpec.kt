package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
<Bitcoin Core Packets Captured>
 4471018 <Header> Magic:ù¾´Ù, Command:notfound, Size:37, Checksum:376371400
 4471019 dumping data len : 61
 4471020 00000000  f9 be b4 d9 6e 6f 74 66  6f 75 6e 64 00 00 00 00  ù¾´Ùnotfound....
 4471021 00000010  25 00 00 00 c8 f8 6e 16  01 01 00 00 00 73 46 c8  %...Èøn......sFÈ
 4471022 00000020  dc 3f 79 b1 11 37 10 60  92 1a 14 01 a3 6e 22 4c  Ü?y±.7.`<92>...£n"L
 4471023 00000030  ee 0a c1 97 12 4e 8d f8  f8 62 06 1d 35           î.Á<97>.N<8d>øøb..5

  */
class NotFoundSpec : EnvelopeTestSuite<NotFound>  {

  val codec = NotFoundCodec.codec

  val envelopeHeader = bytes(
    """
      f9 be b4 d9 6e 6f 74 66  6f 75 6e 64 00 00 00 00
      25 00 00 00 c8 f8 6e 16
    """)

  val payload = bytes(
    """
                               01 01 00 00 00 73 46 c8
      dc 3f 79 b1 11 37 10 60  92 1a 14 01 a3 6e 22 4c
      ee 0a c1 97 12 4e 8d f8  f8 62 06 1d 35
    """)

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "notfound",
    payload.length.toInt,
    Checksum.fromHex("c8 f8 6e 16"),
    BitVector.view(payload)
  )

  val message = NotFound(List(InvVector(InvType.MSG_TX, Hash(bytes("351d0662f8f88d4e1297c10aee4c226ea301141a9260103711b1793fdcc84673")))))
}

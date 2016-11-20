package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
[Bitcoin Core Packets Captured]
  [NET] recv; header:
  <Header> Magic:ù¾´Ù, Command:pong, Size:8, Checksum:-2134269906
  dumping data len : 24
  00000000  f9 be b4 d9 70 6f 6e 67  00 00 00 00 00 00 00 00  ù¾´Ùpong........
  00000010  08 00 00 00 2e a0 c9 80                           ..... É<80>
  [NET] recv; data:
  dumping data len : 8
  00000000  54 d5 0b 63 8d 1b bc 16                           TÕ.c<8d>.¼.
  */
class PongSpec extends EnvelopeTestSuite[Pong]  {

  val codec = PongCodec.codec

  val envelopeHeader = bytes("""
     f9 be b4 d9 70 6f 6e 67  00 00 00 00 00 00 00 00
     08 00 00 00 2e a0 c9 80
  """)

  val payload = bytes("54 d5 0b 63 8d 1b bc 16")

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "pong",
    payload.length.toInt,
    Checksum.fromHex("2e a0 c9 80"),
    BitVector.view(payload)
  )

  val message = Pong(BigInt(payload.reverse).toLong)
}


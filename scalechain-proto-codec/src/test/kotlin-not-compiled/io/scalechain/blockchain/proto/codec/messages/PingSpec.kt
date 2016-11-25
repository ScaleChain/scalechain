package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
  <Bitcoin Core Packets Captured>
    <NET> recv; header:
    <Header> Magic:ù¾´Ù, Command:ping, Size:8, Checksum:-694810323
    dumping data len : 24
    00000000  f9 be b4 d9 70 69 6e 67  00 00 00 00 00 00 00 00  ù¾´Ùping........
    00000010  08 00 00 00 2d 09 96 d6                           ....-.<96>Ö
    <NET> recv; data:
    dumping data len : 8
    00000000  ce dd 07 a4 6c 33 bf 4a                           ÎÝ.¤l3¿J
 */
class PingSpec : EnvelopeTestSuite<Ping>  {

  val codec = PingCodec.codec

  val envelopeHeader = bytes("""
     f9 be b4 d9 70 69 6e 67  00 00 00 00 00 00 00 00
     08 00 00 00 2d 09 96 d6
  """)

  val payload = bytes("ce dd 07 a4 6c 33 bf 4a")

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "ping",
    payload.length.toInt,
    Checksum.fromHex("2d 09 96 d6"),
    BitVector.view(payload)
  )

  val message = Ping(BigInt(payload.reverse))

}

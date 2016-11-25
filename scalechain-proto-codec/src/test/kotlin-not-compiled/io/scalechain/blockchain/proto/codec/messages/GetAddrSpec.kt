package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
<Bitcoin Core Packets Captured>
  <NET> SocketSendData
  <Header> Magic:ù¾´Ù, Command:getaddr, Size:0, Checksum:-488573347
  dumping data len : 24
  00000000  f9 be b4 d9 67 65 74 61  64 64 72 00 00 00 00 00  ù¾´Ùgetaddr.....
  00000010  00 00 00 00 5d f6 e0 e2                           ....>öàâ

  */
class GetAddrSpec : EnvelopeTestSuite<GetAddr>  {

  val codec = GetAddrCodec.codec

  val envelopeHeader = bytes(
    """
      f9 be b4 d9 67 65 74 61  64 64 72 00 00 00 00 00
      00 00 00 00 5d f6 e0 e2
    """)

  val payload = bytes("")

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "getaddr",
    payload.length.toInt,
    Checksum.fromHex("5d f6 e0 e2"),
    BitVector.view(payload)
  )

  val message = GetAddr()

}

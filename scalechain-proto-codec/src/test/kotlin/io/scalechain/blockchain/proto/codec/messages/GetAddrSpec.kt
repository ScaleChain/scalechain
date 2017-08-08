package io.scalechain.blockchain.proto.codec.messages

import io.kotlintest.KTestJUnitRunner
import io.netty.buffer.Unpooled
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

/**
<Bitcoin Core Packets Captured>
  <NET> SocketSendData
  <Header> Magic:ù¾´Ù, Command:getaddr, Size:0, Checksum:-488573347
  dumping data len : 24
  00000000  f9 be b4 d9 67 65 74 61  64 64 72 00 00 00 00 00  ù¾´Ùgetaddr.....
  00000010  00 00 00 00 5d f6 e0 e2                           ....>öàâ

  */

@RunWith(KTestJUnitRunner::class)
class GetAddrSpec : EnvelopeTestSuite<GetAddr>()  {

  override val codec = GetAddrCodec

  override val envelopeHeader = bytes(
    """
      f9 be b4 d9 67 65 74 61  64 64 72 00 00 00 00 00
      00 00 00 00 5d f6 e0 e2
    """)

  override val payload = bytes("")

  override val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "getaddr",
    payload.size,
    Checksum.fromHex("5d f6 e0 e2"),
    Unpooled.wrappedBuffer(payload)
  )

  override val message = GetAddr()

}

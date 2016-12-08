package io.scalechain.blockchain.proto.codec.messages

import io.kotlintest.KTestJUnitRunner
import io.netty.buffer.Unpooled
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith


/**
<Bitcoin Core Packets Captured>
  <NET> recv; header:
  <Header> Magic:ù¾´Ù, Command:verack, Size:0, Checksum:-488573347
  dumping data len : 24
  00000000  f9 be b4 d9 76 65 72 61  63 6b 00 00 00 00 00 00  ù¾´Ùverack......
  00000010  00 00 00 00 5d f6 e0 e2                           ....>öàâ
  */

@RunWith(KTestJUnitRunner::class)
class VerackSpec : EnvelopeTestSuite<Verack>()  {

  override val codec = VerackCodec

  override val envelopeHeader = bytes(
    """
      f9 be b4 d9 76 65 72 61  63 6b 00 00 00 00 00 00
      00 00 00 00 5d f6 e0 e2
    """)

  override val payload = bytes("")

  override val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "verack",
    payload.size,
    Checksum.fromHex("5d f6 e0 e2"),
    Unpooled.wrappedBuffer(payload)
  )

  override val message = Verack()

}

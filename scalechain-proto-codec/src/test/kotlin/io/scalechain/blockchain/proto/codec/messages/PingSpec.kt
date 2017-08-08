package io.scalechain.blockchain.proto.codec.messages

import io.kotlintest.KTestJUnitRunner
import io.netty.buffer.Unpooled
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith
import java.math.BigInteger


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

@RunWith(KTestJUnitRunner::class)
class PingSpec : EnvelopeTestSuite<Ping>()  {

  override val codec = PingCodec

  override val envelopeHeader = bytes("""
     f9 be b4 d9 70 69 6e 67  00 00 00 00 00 00 00 00
     08 00 00 00 2d 09 96 d6
  """)

  override val payload = bytes("ce dd 07 a4 6c 33 bf 4a")

  override val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "ping",
    payload.size,
    Checksum.fromHex("2d 09 96 d6"),
    Unpooled.wrappedBuffer(payload)
  )

  override val message = Ping(BigInteger(payload.reversedArray()))

}

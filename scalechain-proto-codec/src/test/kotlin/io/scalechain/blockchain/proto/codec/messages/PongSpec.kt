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
  <Header> Magic:ù¾´Ù, Command:pong, Size:8, Checksum:-2134269906
  dumping data len : 24
  00000000  f9 be b4 d9 70 6f 6e 67  00 00 00 00 00 00 00 00  ù¾´Ùpong........
  00000010  08 00 00 00 2e a0 c9 80                           ..... É<80>
  <NET> recv; data:
  dumping data len : 8
  00000000  54 d5 0b 63 8d 1b bc 16                           TÕ.c<8d>.¼.
  */

@RunWith(KTestJUnitRunner::class)
class PongSpec : EnvelopeTestSuite<Pong>()  {

  override val codec = PongCodec

  override val envelopeHeader = bytes("""
     f9 be b4 d9 70 6f 6e 67  00 00 00 00 00 00 00 00
     08 00 00 00 2e a0 c9 80
  """)

  override val payload = bytes("54 d5 0b 63 8d 1b bc 16")

  override val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "pong",
    payload.size,
    Checksum.fromHex("2e a0 c9 80"),
    Unpooled.wrappedBuffer(payload)
  )

  override val message = Pong(BigInteger(payload.reversedArray()))
}


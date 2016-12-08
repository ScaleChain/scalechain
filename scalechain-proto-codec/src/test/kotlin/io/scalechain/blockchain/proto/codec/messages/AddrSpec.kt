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
  <Header> Magic:ù¾´Ù, Command:addr, Size:61, Checksum:-1676403837
  dumping data len : 24
  00000000  f9 be b4 d9 61 64 64 72  00 00 00 00 00 00 00 00  ù¾´Ùaddr........
  00000010  3d 00 00 00 83 1b 14 9c                           =...<83>..<9c>
  <NET> recv; data:
  dumping data len : 61
  00000000  02 56 34 ab 56 01 00 00  00 00 00 00 00 00 00 00  .V4«V...........
  00000010  00 00 00 00 00 00 00 ff  ff 3d ae 7e f8 20 8d 55  .......ÿÿ=®~ø <8d>U
  00000020  36 ab 56 01 00 00 00 00  00 00 00 00 00 00 00 00  6«V.............
  00000030  00 00 00 00 00 ff ff 2d  21 55 3a 20 8d           .....ÿÿ-!U: <8d>

  */

@RunWith(KTestJUnitRunner::class)
class AddrSpec : EnvelopeTestSuite<Addr>()  {

  override val codec = AddrCodec

  override val envelopeHeader = bytes(
    """
      f9 be b4 d9 61 64 64 72  00 00 00 00 00 00 00 00
      3d 00 00 00 83 1b 14 9c
    """)

  override val payload = bytes(
    """
      02 56 34 ab 56 01 00 00  00 00 00 00 00 00 00 00
      00 00 00 00 00 00 00 ff  ff 3d ae 7e f8 20 8d 55
      36 ab 56 01 00 00 00 00  00 00 00 00 00 00 00 00
      00 00 00 00 00 ff ff 2d  21 55 3a 20 8d
    """)

  override val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "addr",
    payload.size,
    Checksum.fromHex("83 1b 14 9c"),
    Unpooled.wrappedBuffer(payload)
  )

  override val message = Addr(listOf(NetworkAddressWithTimestamp(1454060630L, NetworkAddress(BigInteger("1"), IPv6Address(bytes("00000000000000000000ffff3dae7ef8")), 8333)),NetworkAddressWithTimestamp(1454061141L, NetworkAddress(BigInteger("1"), IPv6Address(bytes("00000000000000000000ffff2d21553a")), 8333))))

}

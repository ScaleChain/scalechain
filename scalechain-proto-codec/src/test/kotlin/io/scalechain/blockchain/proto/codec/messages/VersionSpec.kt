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
  5127266 <NET> recv; header:
  5127267 <Header> Magic:ù¾´Ù, Command:version, Size:102, Checksum:1522660210
  5127268 dumping data len : 24
  5127269 00000000  f9 be b4 d9 76 65 72 73  69 6f 6e 00 00 00 00 00  ù¾´Ùversion.....
  5127270 00000010  66 00 00 00 72 f3 c1 5a                           f...róÁZ
  5127271 <NET> recv; data:
  5127272 dumping data len : 102
  5127273 00000000  72 11 01 00 01 00 00 00  00 00 00 00 48 2e ab 56  r...........H.«V
  5127274 00000010  00 00 00 00 01 00 00 00  00 00 00 00 00 00 00 00  ................
  5127275 00000020  00 00 00 00 00 00 ff ff  00 00 00 00 00 00 01 00  ......ÿÿ........
  5127276 00000030  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  ................
  5127277 00000040  ff ff 00 00 00 00 20 8d  fc 2d 58 23 bc a3 a4 49  ÿÿ.... <8d>ü-X#¼£¤I
  5127278 00000050  10 2f 53 61 74 6f 73 68  69 3a 30 2e 31 31 2e 32  ./Satoshi:0.11.2
  5127279 00000060  2f 41 09 06 00 01                                 /A....
  */

@RunWith(KTestJUnitRunner::class)
class VersionSpec : EnvelopeTestSuite<Version>()  {

  override val codec = VersionCodec

  override val envelopeHeader = bytes(
    """
      f9 be b4 d9 76 65 72 73  69 6f 6e 00 00 00 00 00
      66 00 00 00 72 f3 c1 5a
    """)

  override val payload = bytes(
    """
      72 11 01 00 01 00 00 00  00 00 00 00 48 2e ab 56
      00 00 00 00 01 00 00 00  00 00 00 00 00 00 00 00
      00 00 00 00 00 00 ff ff  00 00 00 00 00 00 01 00
      00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00
      ff ff 00 00 00 00 20 8d  fc 2d 58 23 bc a3 a4 49
      10 2f 53 61 74 6f 73 68  69 3a 30 2e 31 31 2e 32
      2f 41 09 06 00 01
    """)

  override val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "version",
    payload.size,
    Checksum.fromHex("72 f3 c1 5a"),
    Unpooled.wrappedBuffer(payload)
  )

  override val message = Version(70002, BigInteger("1"), 1454059080L, NetworkAddress(BigInteger("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 0), NetworkAddress(BigInteger("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 8333), BigInteger("5306546289391447548"), "/Satoshi:0.11.2/", 395585, true)
}

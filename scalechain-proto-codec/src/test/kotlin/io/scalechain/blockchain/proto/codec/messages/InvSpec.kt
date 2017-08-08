package io.scalechain.blockchain.proto.codec.messages

import io.kotlintest.KTestJUnitRunner
import io.netty.buffer.Unpooled
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith


/**
<Bitcoin Core Packets Captured>
  <NET> recv; header:
  <Header> Magic:ù¾´Ù, Command:inv, Size:145, Checksum:-318450844
  dumping data len : 24
  00000000  f9 be b4 d9 69 6e 76 00  00 00 00 00 00 00 00 00  ù¾´Ùinv.........
  00000010  91 00 00 00 64 d3 04 ed                           <91>...dÓ.í
  <NET> recv; data:
  dumping data len : 145
  00000000  04 01 00 00 00 f9 41 01  9e 11 a0 67 47 9d 6f bd  .....ùA.<9e>. gG<9d>o½
  00000010  a4 41 bb 63 06 15 45 ba  e9 ab f0 dd f3 8d 4a b0  ¤A»c..Eºé«ðÝó<8d>J°
  00000020  ff fe bd e4 b2 01 00 00  00 77 01 72 98 fc 7a ad  ÿþ½ä²....w.r<98>üz­
  00000030  60 c6 e1 5d 06 a8 eb 38  15 22 6b e4 48 bd f3 4f  `Æá>.¨ë8."käH½óO
  00000040  6b 29 cd 81 8f bd 53 fe  80 01 00 00 00 5d 98 cd  k)Í<81><8f>½Sþ<80>....><98>Í
  00000050  06 aa 65 bf e9 37 4f 12  32 b0 73 f2 42 e3 e6 1f  .ªe¿é7O.2°sòBãæ.
  00000060  d4 a6 fc a4 11 50 13 76  01 65 45 58 f7 01 00 00  Ô¦ü¤.P.v.eEX÷...
  00000070  00 47 cc b6 e5 91 3c 62  4e cf f8 28 ca f4 5e e2  .GÌ¶å<91><bNÏø(Êô^â
  00000080  0e eb 01 6c 8d bd 04 23  4e 0a 5d 6b 53 ab 15 63  .ë.l<8d>½.#N.>kS«.c
  00000090  7c                                                |
  */

@RunWith(KTestJUnitRunner::class)
class InvSpec : EnvelopeTestSuite<Inv>()  {

  override val codec = InvCodec

  override val envelopeHeader = bytes(
    """
      f9 be b4 d9 69 6e 76 00  00 00 00 00 00 00 00 00
      91 00 00 00 64 d3 04 ed
    """)

  override val payload = bytes(
    """
      04 01 00 00 00 f9 41 01  9e 11 a0 67 47 9d 6f bd
      a4 41 bb 63 06 15 45 ba  e9 ab f0 dd f3 8d 4a b0
      ff fe bd e4 b2 01 00 00  00 77 01 72 98 fc 7a ad
      60 c6 e1 5d 06 a8 eb 38  15 22 6b e4 48 bd f3 4f
      6b 29 cd 81 8f bd 53 fe  80 01 00 00 00 5d 98 cd
      06 aa 65 bf e9 37 4f 12  32 b0 73 f2 42 e3 e6 1f
      d4 a6 fc a4 11 50 13 76  01 65 45 58 f7 01 00 00
      00 47 cc b6 e5 91 3c 62  4e cf f8 28 ca f4 5e e2
      0e eb 01 6c 8d bd 04 23  4e 0a 5d 6b 53 ab 15 63
      7c
    """)

  override val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "inv",
    payload.size,
    Checksum.fromHex("64 d3 04 ed"),
    Unpooled.wrappedBuffer(payload)
  )

  override val message = Inv(listOf(InvVector(InvType.MSG_TX, Hash(Bytes.from("b2e4bdfeffb04a8df3ddf0abe9ba45150663bb41a4bd6f9d4767a0119e0141f9"))),InvVector(InvType.MSG_TX, Hash(Bytes.from("80fe53bd8f81cd296b4ff3bd48e46b221538eba8065de1c660ad7afc98720177"))),InvVector(InvType.MSG_TX, Hash(Bytes.from("f75845650176135011a4fca6d41fe6e342f273b032124f37e9bf65aa06cd985d"))),InvVector(InvType.MSG_TX, Hash(Bytes.from("7c6315ab536b5d0a4e2304bd8d6c01eb0ee25ef4ca28f8cf4e623c91e5b6cc47")))))

}

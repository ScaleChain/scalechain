package io.scalechain.blockchain.proto.codec.messages

import io.kotlintest.KTestJUnitRunner
import io.netty.buffer.Unpooled
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.blockchain.proto.*
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith


/**
<Bitcoin Core Packets Captured>
  <NET> SocketSendData
  <Header> Magic:ù¾´Ù, Command:getdata, Size:109, Checksum:1894877236
  dumping data len : 133
  00000000  f9 be b4 d9 67 65 74 64  61 74 61 00 00 00 00 00  ù¾´Ùgetdata.....
  00000010  6d 00 00 00 34 88 f1 70  03 01 00 00 00 10 90 2f  m...4<88>ñp......<90>/
  00000020  f8 39 89 a6 87 9a 29 44  2c 4c c8 9e cf 22 87 07  ø9<89>¦<87><9a>)D,LÈ<9e>Ï"<87>.
  00000030  98 99 35 cb 4f b8 27 17  45 b9 08 13 41 01 00 00  <98><99>5ËO¸'.E¹..A...
  00000040  00 ce 82 ca 53 70 f6 9d  e8 2e 64 db 3b ed 56 1c  .Î<82>ÊSpö<9d>è.dÛ;íV.
  00000050  98 d4 de 40 e1 61 1e 54  81 b6 eb 0b b4 da 2c 74  <98>ÔÞ@áa.T<81>¶ë.´Ú,t
  00000060  af 01 00 00 00 99 ec fb  5d fe cf fe 8c 16 f6 9a  ¯....<99>ìû>þÏþ<8c>.ö<9a>
  00000070  b2 66 ec 76 3e af a8 e1  3b 28 c0 1a 09 e7 42 d6  ²fìv>¯¨á;(À..çBÖ
  00000080  82 8e 14 39 3a                                    <82><8e>.9:
  */

@RunWith(KTestJUnitRunner::class)
class GetDataSpec : EnvelopeTestSuite<GetData>()  {

  override val codec = GetDataCodec

  override val envelopeHeader = bytes(
    """
      f9 be b4 d9 67 65 74 64  61 74 61 00 00 00 00 00
      6d 00 00 00 34 88 f1 70
    """)

  override val payload = bytes(
    """
                               03 01 00 00 00 10 90 2f
      f8 39 89 a6 87 9a 29 44  2c 4c c8 9e cf 22 87 07
      98 99 35 cb 4f b8 27 17  45 b9 08 13 41 01 00 00
      00 ce 82 ca 53 70 f6 9d  e8 2e 64 db 3b ed 56 1c
      98 d4 de 40 e1 61 1e 54  81 b6 eb 0b b4 da 2c 74
      af 01 00 00 00 99 ec fb  5d fe cf fe 8c 16 f6 9a
      b2 66 ec 76 3e af a8 e1  3b 28 c0 1a 09 e7 42 d6
      82 8e 14 39 3a
    """)

  override val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "getdata",
    payload.size,
    Checksum.fromHex("34 88 f1 70"),
    Unpooled.wrappedBuffer(payload)
  )

  override val message = GetData(listOf(InvVector(InvType.MSG_TX, Hash(Bytes.from("411308b9451727b84fcb359998078722cf9ec84c2c44299a87a68939f82f9010"))),InvVector(InvType.MSG_TX, Hash(Bytes.from("af742cdab40bebb681541e61e140ded4981c56ed3bdb642ee89df67053ca82ce"))),InvVector(InvType.MSG_TX, Hash(Bytes.from("3a39148e82d642e7091ac0283be1a8af3e76ec66b29af6168cfecffe5dfbec99")))))
}

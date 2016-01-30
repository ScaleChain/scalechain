package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.Headers
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
[Bitcoin Core Packets Captured]
  <Header> Magic:ù¾´Ù, Command:headers, Size:82, Checksum:1515787379
  dumping data len : 24
  00000000  f9 be b4 d9 68 65 61 64  65 72 73 00 00 00 00 00  ù¾´Ùheaders.....
  00000010  52 00 00 00 73 14 59 5a                           R...s.YZ
  [NET] recv; data:
  dumping data len : 82
  00000000  01 04 00 00 00 63 54 17  3f cc 4b 38 c2 d4 59 48  .....cT.?ÌK8ÂÔYH
  00000010  b1 9d 2a 52 7c 4f 10 ad  a6 83 47 00 06 00 00 00  ±<9d>*R|O.­¦<83>G.....
  00000020  00 00 00 00 00 66 1e dc  92 f8 62 78 de 6b 38 ab  .....f.Ü<92>øbxÞk8«
  00000030  55 b5 60 cc a3 f6 38 9c  17 c4 44 e4 61 10 87 be  Uµ`Ì£ö8<9c>.ÄDäa.<87>¾
  00000040  12 51 bd c8 db 5a 2c ab  56 f0 28 09 18 f0 09 a5  .Q½ÈÛZ,«Vð(..ð.¥
  00000050  d0 00                                             Ð.
  */
class HeadersSpec extends EnvelopeTestSuite[Headers]  {

  val codec = HeadersCodec.codec

  val envelopeHeader = bytes(
    """
      f9 be b4 d9 68 65 61 64  65 72 73 00 00 00 00 00
      52 00 00 00 73 14 59 5a
    """)

  val payload = bytes(
    """
      01 04 00 00 00 63 54 17  3f cc 4b 38 c2 d4 59 48
      b1 9d 2a 52 7c 4f 10 ad  a6 83 47 00 06 00 00 00
      00 00 00 00 00 66 1e dc  92 f8 62 78 de 6b 38 ab
      55 b5 60 cc a3 f6 38 9c  17 c4 44 e4 61 10 87 be
      12 51 bd c8 db 5a 2c ab  56 f0 28 09 18 f0 09 a5
      d0 00
    """)

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "headers",
    payload.length.toInt,
    Checksum.fromHex("5a 59 14 73"),
    BitVector.view(payload)
  )

  val message = null//Headers()

}

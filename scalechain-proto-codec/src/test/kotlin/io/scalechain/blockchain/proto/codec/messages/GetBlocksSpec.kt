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
  <Header> Magic:ù¾´Ù, Command:getblocks, Size:997, Checksum:-1374272182
  dumping data len : 24
  00000000  f9 be b4 d9 67 65 74 62  6c 6f 63 6b 73 00 00 00  ù¾´Ùgetblocks...
  00000010  e5 03 00 00 4a 45 16 ae                           å...JE.®
  <NET> recv; data:
  dumping data len : 997
  00000000  72 11 01 00 1e d1 5b d2  ce ad ae b0 06 27 94 f6  r....Ñ<ÒÎ­®°.'<94>ö
  00000010  d5 b1 9d 58 cb c9 6b 45  2a 98 37 17 04 00 00 00  Õ±<9d>XËÉkE*<98>7.....
  00000020  00 00 00 00 00 20 59 1c  19 57 04 99 48 4f b0 58  ..... Y..W.<99>HO°X
  00000030  f9 81 22 a4 b8 00 7e 1e  2d e1 ce c4 06 00 00 00  ù<81>"¤¸.~.-áÎÄ....
  00000040  00 00 00 00 00 4a ea 35  7f 78 b6 4b f3 7b 35 af  .....Jê5^?x¶Kó{5¯
  00000050  31 11 47 4f 2d 55 c5 2b  2f 0c 08 9a 07 00 00 00  1.GO-UÅ+/..<9a>....
  00000060  00 00 00 00 00 c6 3a a3  98 3e c5 1c 5f a4 d4 e2  .....Æ:£<98>>Å.*¤Ôâ
  00000070  fb cc 6e 83 30 41 74 32  2d ba 17 01 02 00 00 00  ûÌn<83>0At2-º......
  00000080  00 00 00 00 00 da b7 ef  79 a7 b6 e0 54 3e 9b 63  .....Ú·ïy§¶àT><9b>c
  00000090  a4 e9 a9 3f b0 3e 28 55  e9 dd 38 1e 06 00 00 00  ¤é©?°>(UéÝ8.....
  000000a0  00 00 00 00 00 03 b4 af  4d a1 4e 72 54 ec a4 d8  ......´¯M¡NrTì¤Ø
  000000b0  7e 7f 69 c0 24 91 e2 a0  06 c4 a3 ae 03 00 00 00  ~^?iÀ$<91>â .Ä£®....
  000000c0  00 00 00 00 00 25 ab 32  d1 e4 95 29 dc e1 73 14  .....%«2Ñä<95>)Üás.
  000000d0  37 ad 81 3a 77 02 39 bd  a4 2c b7 a4 06 00 00 00  7­<81>:w.9½¤,·¤....
  000000e0  00 00 00 00 00 35 a5 8e  f2 0b dd d8 0a 18 00 2a  .....5¥<8e>ò.ÝØ...*
  000000f0  7a 5f b6 e6 4d eb 55 47  fc a2 43 41 08 00 00 00  z_¶æMëUGü¢CA....
  00000100  00 00 00 00 00 18 9d 4d  2e f2 97 5d f8 92 19 05  ......<9d>M.ò<97>>ø<92>..
  00000110  89 a6 15 a4 12 3c fa 64  cd 86 bd 86 08 00 00 00  <89>¦.¤.<údÍ<86>½<86>....
  00000120  00 00 00 00 00 87 3b 63  12 0e 1e 3b cc 3c 2b 93  .....<87>;c...;Ì<+<93>
  00000130  b8 c1 bd c2 c7 48 33 f4  cc 6e 71 89 05 00 00 00  ¸Á½ÂÇH3ôÌnq<89>....
  00000140  00 00 00 00 00 0f 38 82  4b 80 68 eb 76 48 cf f9  ......8<82>K<80>hëvHÏù
  00000150  50 2b 0e 6d ae 7c a3 21  dd c1 62 18 00 00 00 00  P+.m®|£!ÝÁb.....
  00000160  00 00 00 00 00 48 a6 ec  52 8c c1 4c 32 0a 1b 56  .....H¦ìR<8c>ÁL2..V
  00000170  55 3d c3 7c 96 43 0f f6  ff 93 ba 51 02 00 00 00  U=Ã|<96>C.öÿ<93>ºQ....
  00000180  00 00 00 00 00 1d f4 3f  b4 70 79 d8 67 3c c9 cd  ......ô?´pyØg<ÉÍ
  00000190  a6 97 12 a7 b7 5a 24 38  7b 05 8b d7 05 00 00 00  ¦<97>.§·Z$8{.<8b>×....
  000001a0  00 00 00 00 00 13 61 50  17 6a 82 35 c9 6a 99 8f  ......aP.j<82>5Éj<99><8f>
  000001b0  d5 1b 42 a0 4d 9b 79 48  2b 56 72 66 07 00 00 00  Õ.B M<9b>yH+Vrf....
  000001c0  00 00 00 00 00 55 c9 4c  da d6 bd 1b 96 50 8e 46  .....UÉLÚÖ½.<96>P<8e>F
  000001d0  7a be 0c 30 de 08 1f b3  e4 0e b4 5e 06 00 00 00  z¾.0Þ..³ä.´^....
  000001e0  00 00 00 00 00 d2 fb 07  f3 8c e3 c1 97 f9 06 cf  .....Òû.ó<8c>ãÁ<97>ù.Ï
  000001f0  db d9 3b 5d 35 fe ce c8  b6 26 56 6f 00 00 00 00  ÛÙ;>5þÎÈ¶&Vo....
  00000200  00 00 00 00 00 83 12 15  df 7a 14 21 0f 5f c8 21  .....<83>..ßz.!.*È!
  00000210  27 85 65 a4 15 19 da 2c  ee c6 65 66 03 00 00 00  '<85>e¤..Ú,îÆef....
  00000220  00 00 00 00 00 f4 fc 26  2c 90 c8 f5 b3 fa a7 66  .....ôü&,<90>Èõ³ú§f
  00000230  d3 bd 2a c6 2d 33 ab b5  86 0a 1d 04 04 00 00 00  Ó½*Æ-3«µ<86>.......
  00000240  00 00 00 00 00 5f 90 25  71 3e 6e ab 78 5e eb b7  .....*<90>%q>n«x^ë·
  00000250  85 34 9a 37 53 a4 d6 ab  3c 47 19 9b 03 00 00 00  <85>4<9a>7S¤Ö«<G.<9b>....
  00000260  00 00 00 00 00 99 db 19  cb c9 87 e2 17 68 7f ff  .....<99>Û.ËÉ<87>â.h^?ÿ
  00000270  4b 96 e9 2a d7 44 c2 b8  83 f4 50 d2 01 00 00 00  K<96>é*×DÂ¸<83>ôPÒ....
  00000280  00 00 00 00 00 a1 9f 8b  f2 59 b5 b3 cf ad 6e b0  .....¡<9f><8b>òYµ³Ï­n°
  00000290  91 42 7f e5 0f 15 52 93  a5 aa 41 97 09 00 00 00  <91>B^?å..R<93>¥ªA<97>....
  000002a0  00 00 00 00 00 03 22 be  00 7c 99 72 b0 71 60 d8  ......"¾.|<99>r°q`Ø
  000002b0  bd 89 27 a0 0c 58 30 57  a7 77 b3 c9 02 00 00 00  ½<89>' .X0W§w³É....
  000002c0  00 00 00 00 00 93 c6 8b  5d 2b 12 60 f1 46 be e1  .....<93>Æ<8b>>+.`ñF¾á
  000002d0  46 73 82 4c 96 bf e6 63  52 c1 1c 30 05 00 00 00  Fs<82>L<96>¿æcRÁ.0....
  000002e0  00 00 00 00 00 40 6e 25  98 b3 6d 76 37 2b d7 a8  .....@n%<98>³mv7+×¨
  000002f0  bf a6 96 f3 8c e3 13 a6  61 46 a4 f2 00 00 00 00  ¿¦<96>ó<8c>ã.¦aF¤ò....
  00000300  00 00 00 00 00 ce 7e 1c  07 92 33 d0 08 90 90 5e  .....Î~..<92>3Ð.<90><90>^
  00000310  fb 9f cf 0f 58 56 3e 5b  1f 2d 93 56 06 00 00 00  û<9f>Ï.XV><.-<93>V....
  00000320  00 00 00 00 00 b5 57 fb  26 60 4a 8e cc 1e 41 ce  .....µWû&`J<8e>Ì.AÎ
  00000330  ce d9 6c 3a cf 24 37 76  24 b2 c4 78 07 00 00 00  ÎÙl:Ï$7v$²Äx....
  00000340  00 00 00 00 00 47 f2 23  f3 5c cf c0 59 46 9a 84  .....Gò#ó\ÏÀYF<9a><84>
  00000350  f4 28 72 ab 2d 48 55 b9  71 7e 84 1e 00 00 00 00  ô(r«-HU¹q~<84>.....
  00000360  00 00 00 00 00 40 47 5a  e6 87 86 fb b5 e9 ff 19  .....@GZæ<87><86>ûµéÿ.
  00000370  06 d2 b8 a0 b0 ca 19 91  06 4f 2b 4c 09 0b 00 00  .Ò¸ °Ê.<91>.O+L....
  00000380  00 00 00 00 00 61 50 83  bd f1 51 10 02 ff 5b 78  .....aP<83>½ñQ..ÿ<x
  00000390  e9 c5 b9 01 7f 68 68 bc  fd ce 00 45 76 de 09 00  éÅ¹.^?hh¼ýÎ.EvÞ..
  000003a0  00 00 00 00 00 6f e2 8c  0a b6 f1 b3 72 c1 a6 a2  .....oâ<8c>.¶ñ³rÁ¦¢
  000003b0  46 ae 63 f7 4f 93 1e 83  65 e1 5a 08 9c 68 d6 19  F®c÷O<93>.<83>eáZ.<9c>hÖ.
  000003c0  00 00 00 00 00 8c e4 4b  9b 58 8b 8c 28 ca c4 f5  .....<8c>äK<9b>X<8b><8c>(ÊÄõ
  000003d0  28 64 03 8d f1 df 31 d0  d7 f7 f9 99 07 00 00 00  (d.<8d>ñß1Ð×÷ù<99>....
  000003e0  00 00 00 00 00                                    .....
  */

@RunWith(KTestJUnitRunner::class)
class GetBlocksSpec : EnvelopeTestSuite<GetBlocks>()  {

  override val codec = GetBlocksCodec

  override val envelopeHeader = bytes(
    """
      f9 be b4 d9 67 65 74 62  6c 6f 63 6b 73 00 00 00
      e5 03 00 00 4a 45 16 ae
    """)

  override val payload = bytes(
    """
      72 11 01 00 1e d1 5b d2  ce ad ae b0 06 27 94 f6
      d5 b1 9d 58 cb c9 6b 45  2a 98 37 17 04 00 00 00
      00 00 00 00 00 20 59 1c  19 57 04 99 48 4f b0 58
      f9 81 22 a4 b8 00 7e 1e  2d e1 ce c4 06 00 00 00
      00 00 00 00 00 4a ea 35  7f 78 b6 4b f3 7b 35 af
      31 11 47 4f 2d 55 c5 2b  2f 0c 08 9a 07 00 00 00
      00 00 00 00 00 c6 3a a3  98 3e c5 1c 5f a4 d4 e2
      fb cc 6e 83 30 41 74 32  2d ba 17 01 02 00 00 00
      00 00 00 00 00 da b7 ef  79 a7 b6 e0 54 3e 9b 63
      a4 e9 a9 3f b0 3e 28 55  e9 dd 38 1e 06 00 00 00
      00 00 00 00 00 03 b4 af  4d a1 4e 72 54 ec a4 d8
      7e 7f 69 c0 24 91 e2 a0  06 c4 a3 ae 03 00 00 00
      00 00 00 00 00 25 ab 32  d1 e4 95 29 dc e1 73 14
      37 ad 81 3a 77 02 39 bd  a4 2c b7 a4 06 00 00 00
      00 00 00 00 00 35 a5 8e  f2 0b dd d8 0a 18 00 2a
      7a 5f b6 e6 4d eb 55 47  fc a2 43 41 08 00 00 00
      00 00 00 00 00 18 9d 4d  2e f2 97 5d f8 92 19 05
      89 a6 15 a4 12 3c fa 64  cd 86 bd 86 08 00 00 00
      00 00 00 00 00 87 3b 63  12 0e 1e 3b cc 3c 2b 93
      b8 c1 bd c2 c7 48 33 f4  cc 6e 71 89 05 00 00 00
      00 00 00 00 00 0f 38 82  4b 80 68 eb 76 48 cf f9
      50 2b 0e 6d ae 7c a3 21  dd c1 62 18 00 00 00 00
      00 00 00 00 00 48 a6 ec  52 8c c1 4c 32 0a 1b 56
      55 3d c3 7c 96 43 0f f6  ff 93 ba 51 02 00 00 00
      00 00 00 00 00 1d f4 3f  b4 70 79 d8 67 3c c9 cd
      a6 97 12 a7 b7 5a 24 38  7b 05 8b d7 05 00 00 00
      00 00 00 00 00 13 61 50  17 6a 82 35 c9 6a 99 8f
      d5 1b 42 a0 4d 9b 79 48  2b 56 72 66 07 00 00 00
      00 00 00 00 00 55 c9 4c  da d6 bd 1b 96 50 8e 46
      7a be 0c 30 de 08 1f b3  e4 0e b4 5e 06 00 00 00
      00 00 00 00 00 d2 fb 07  f3 8c e3 c1 97 f9 06 cf
      db d9 3b 5d 35 fe ce c8  b6 26 56 6f 00 00 00 00
      00 00 00 00 00 83 12 15  df 7a 14 21 0f 5f c8 21
      27 85 65 a4 15 19 da 2c  ee c6 65 66 03 00 00 00
      00 00 00 00 00 f4 fc 26  2c 90 c8 f5 b3 fa a7 66
      d3 bd 2a c6 2d 33 ab b5  86 0a 1d 04 04 00 00 00
      00 00 00 00 00 5f 90 25  71 3e 6e ab 78 5e eb b7
      85 34 9a 37 53 a4 d6 ab  3c 47 19 9b 03 00 00 00
      00 00 00 00 00 99 db 19  cb c9 87 e2 17 68 7f ff
      4b 96 e9 2a d7 44 c2 b8  83 f4 50 d2 01 00 00 00
      00 00 00 00 00 a1 9f 8b  f2 59 b5 b3 cf ad 6e b0
      91 42 7f e5 0f 15 52 93  a5 aa 41 97 09 00 00 00
      00 00 00 00 00 03 22 be  00 7c 99 72 b0 71 60 d8
      bd 89 27 a0 0c 58 30 57  a7 77 b3 c9 02 00 00 00
      00 00 00 00 00 93 c6 8b  5d 2b 12 60 f1 46 be e1
      46 73 82 4c 96 bf e6 63  52 c1 1c 30 05 00 00 00
      00 00 00 00 00 40 6e 25  98 b3 6d 76 37 2b d7 a8
      bf a6 96 f3 8c e3 13 a6  61 46 a4 f2 00 00 00 00
      00 00 00 00 00 ce 7e 1c  07 92 33 d0 08 90 90 5e
      fb 9f cf 0f 58 56 3e 5b  1f 2d 93 56 06 00 00 00
      00 00 00 00 00 b5 57 fb  26 60 4a 8e cc 1e 41 ce
      ce d9 6c 3a cf 24 37 76  24 b2 c4 78 07 00 00 00
      00 00 00 00 00 47 f2 23  f3 5c cf c0 59 46 9a 84
      f4 28 72 ab 2d 48 55 b9  71 7e 84 1e 00 00 00 00
      00 00 00 00 00 40 47 5a  e6 87 86 fb b5 e9 ff 19
      06 d2 b8 a0 b0 ca 19 91  06 4f 2b 4c 09 0b 00 00
      00 00 00 00 00 61 50 83  bd f1 51 10 02 ff 5b 78
      e9 c5 b9 01 7f 68 68 bc  fd ce 00 45 76 de 09 00
      00 00 00 00 00 6f e2 8c  0a b6 f1 b3 72 c1 a6 a2
      46 ae 63 f7 4f 93 1e 83  65 e1 5a 08 9c 68 d6 19
      00 00 00 00 00 8c e4 4b  9b 58 8b 8c 28 ca c4 f5
      28 64 03 8d f1 df 31 d0  d7 f7 f9 99 07 00 00 00
      00 00 00 00 00
    """
  )

  override val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "getblocks",
    payload.size,
    Checksum.fromHex("4a 45 16 ae"),
    Unpooled.wrappedBuffer(payload)
  )

  override val message = GetBlocks(70002L, listOf(Hash(Bytes.from("0000000000000000041737982a456bc9cb589db1d5f6942706b0aeadced25bd1")),Hash(Bytes.from("000000000000000006c4cee12d1e7e00b8a42281f958b04f48990457191c5920")),Hash(Bytes.from("0000000000000000079a080c2f2bc5552d4f471131af357bf34bb6787f35ea4a")),Hash(Bytes.from("0000000000000000020117ba2d32744130836eccfbe2d4a45f1cc53e98a33ac6")),Hash(Bytes.from("0000000000000000061e38dde955283eb03fa9e9a4639b3e54e0b6a779efb7da")),Hash(Bytes.from("000000000000000003aea3c406a0e29124c0697f7ed8a4ec54724ea14dafb403")),Hash(Bytes.from("000000000000000006a4b72ca4bd3902773a81ad371473e1dc2995e4d132ab25")),Hash(Bytes.from("0000000000000000084143a2fc4755eb4de6b65f7a2a00180ad8dd0bf28ea535")),Hash(Bytes.from("00000000000000000886bd86cd64fa3c12a415a689051992f85d97f22e4d9d18")),Hash(Bytes.from("00000000000000000589716eccf43348c7c2bdc1b8932b3ccc3b1e0e12633b87")),Hash(Bytes.from("0000000000000000001862c1dd21a37cae6d0e2b50f9cf4876eb68804b82380f")),Hash(Bytes.from("00000000000000000251ba93fff60f43967cc33d55561b0a324cc18c52eca648")),Hash(Bytes.from("000000000000000005d78b057b38245ab7a71297a6cdc93c67d87970b43ff41d")),Hash(Bytes.from("0000000000000000076672562b48799b4da0421bd58f996ac935826a17506113")),Hash(Bytes.from("0000000000000000065eb40ee4b31f08de300cbe7a468e50961bbdd6da4cc955")),Hash(Bytes.from("0000000000000000006f5626b6c8cefe355d3bd9dbcf06f997c1e38cf307fbd2")),Hash(Bytes.from("0000000000000000036665c6ee2cda1915a465852721c85f0f21147adf151283")),Hash(Bytes.from("000000000000000004041d0a86b5ab332dc62abdd366a7fab3f5c8902c26fcf4")),Hash(Bytes.from("0000000000000000039b19473cabd6a453379a3485b7eb5e78ab6e3e7125905f")),Hash(Bytes.from("000000000000000001d250f483b8c244d72ae9964bff7f6817e287c9cb19db99")),Hash(Bytes.from("0000000000000000099741aaa59352150fe57f4291b06eadcfb3b559f28b9fa1")),Hash(Bytes.from("000000000000000002c9b377a75730580ca02789bdd86071b072997c00be2203")),Hash(Bytes.from("000000000000000005301cc15263e6bf964c827346e1be46f160122b5d8bc693")),Hash(Bytes.from("000000000000000000f2a44661a613e38cf396a6bfa8d72b37766db398256e40")),Hash(Bytes.from("00000000000000000656932d1f5b3e56580fcf9ffb5e909008d03392071c7ece")),Hash(Bytes.from("00000000000000000778c4b224763724cf3a6cd9cece411ecc8e4a6026fb57b5")),Hash(Bytes.from("0000000000000000001e847e71b955482dab7228f4849a4659c0cf5cf323f247")),Hash(Bytes.from("000000000000000b094c2b4f069119cab0a0b8d20619ffe9b5fb8687e65a4740")),Hash(Bytes.from("00000000000009de764500cefdbc68687f01b9c5e9785bff021051f1bd835061")),Hash(Bytes.from("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"))), Hash(Bytes.from("00000000000000000799f9f7d7d031dff18d036428f5c4ca288c8b589b4be48c")))
}

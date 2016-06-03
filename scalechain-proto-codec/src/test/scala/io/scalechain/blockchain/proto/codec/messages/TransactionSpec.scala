package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
[Bitcoin Core Packets Captured]
  [NET] recv; header:
  <Header> Magic:ù¾´Ù, Command:tx, Size:815, Checksum:-1561851910
  dumping data len : 24
  00000000  f9 be b4 d9 74 78 00 00  00 00 00 00 00 00 00 00  ù¾´Ùtx..........
  00000010  2f 03 00 00 fa 07 e8 a2                           /...ú.è¢
  [NET] recv; data:
  dumping data len : 815
  00000000  01 00 00 00 05 53 25 21  90 60 8c 62 7b df c6 60  .....S%!<90>`<8c>b{ßÆ`
  00000010  18 97 7c d2 7f 7e 0c b9  c8 6d d5 a6 4c 5b 3f 23  .<97>|Ò^?~.¹ÈmÕ¦L[?#
  00000020  f3 62 87 82 42 01 00 00  00 6b 48 30 45 02 21 00  ób<87><82>B....kH0E.!.
  00000030  e5 5e 79 7b 51 71 11 48  69 2b 9c c4 a2 c8 06 52  å^y{Qq.Hi+<9c>Ä¢È.R
  00000040  c6 9b d7 8d 41 03 43 8d  ec 41 e0 16 70 c0 46 f8  Æ<9b>×<8d>A.C<8d>ìAà.pÀFø
  00000050  02 20 7e f8 05 74 57 0c  fd e8 f6 a7 88 a6 b2 90  . ~ø.tW.ýèö§<88>¦²<90>
  00000060  03 43 d0 11 15 64 5b 6a  03 77 73 c4 3d f5 07 49  .CÐ..d[j.wsÄ=õ.I1
  00000070  32 94 01 21 02 77 25 55  48 3a 34 45 ba b2 cd d3  2<94>.!.w%UH:4Eº²ÍÓ
  00000080  88 6a 1c c9 b3 62 f4 e9  99 1d b5 0d 09 44 68 9a  <88>j.É³bôé<99>.µ..Dh<9a>
  00000090  ed 90 b6 7b 19 ff ff ff  ff b3 cd ca 76 41 77 1b  í<90>¶{.ÿÿÿÿ³ÍÊvAw.
  000000a0  13 fa 5d 56 7b 81 9d 98  04 f3 2f fb 45 82 f5 20  .ú]V{<81><9d><98>.ó/ûE<82>õ
  000000b0  0e 37 7c 60 e6 82 5f 05  b4 01 00 00 00 6b 48 30  .7|`æ<82>_.´....kH0
  000000c0  45 02 21 00 84 4f 55 bd  c9 bf a1 2f 46 85 60 ee  E.!.<84>OU½É¿¡/F<85>`î
  000000d0  29 32 19 43 3a d9 60 60  53 8a 6e e6 df 7d 09 22  )2.C:Ù``S<8a>næß}."
  000000e0  d7 e4 b8 06 02 20 4d a8  d7 3e 02 4e 35 3f b5 59  ×ä¸.. M¨×>.N5?µY
  000000f0  8d da 67 a6 af e8 42 56  77 b7 ee 48 3c bb 21 47  <8d>Úg¦¯èBVw·îH<»!G
  00000100  ce d5 bf c5 a6 29 01 21  03 df 96 db 89 1b 82 4f  ÎÕ¿Å¦).!.ß<96>Û<89>.<82>O
  00000110  53 f5 ac 42 33 68 b6 27  31 90 71 b9 54 09 49 19  Sõ¬B3h¶'1<90>q¹T.I.
  00000120  89 e6 41 ee e2 d7 32 68  64 ff ff ff ff fe 72 e4  <89>æAîâ×2hdÿÿÿÿþrä
  00000130  56 c2 ad 03 e3 c6 c2 37  1c c5 17 8c 44 27 9c 51  VÂ­.ãÆÂ7.Å.<8c>D'<9c>Q
  00000140  34 2b af df 27 e1 56 a9  ce 4c 3a 37 db 01 00 00  4+¯ß'áV©ÎL:7Û...
  00000150  00 6b 48 30 45 02 21 00  82 ca 08 51 f3 a9 4d 4e  .kH0E.!.<82>Ê.Qó©MN
  00000160  0d 00 67 a4 7f 6b d1 32  b4 7b 79 8a 83 e4 0e 7a  ..g¤^?kÑ2´{y<8a><83>ä.z
  00000170  40 80 11 d4 02 1d 7e c5  02 20 34 b7 53 58 57 57  @<80>.Ô..~Å. 4·SXWW
  00000180  54 bf e5 82 29 61 0d 12  74 9c 60 e7 29 5d 94 72  T¿å<82>)a..t<9c>`ç)]<94>r
  00000190  79 00 14 06 7d 36 ea b8  8d d1 01 21 03 df 96 db  y...}6ê¸<8d>Ñ.!.ß<96>Û
  000001a0  89 1b 82 4f 53 f5 ac 42  33 68 b6 27 31 90 71 b9  <89>.<82>OSõ¬B3h¶'1<90>q¹
  000001b0  54 09 49 19 89 e6 41 ee  e2 d7 32 68 64 ff ff ff  T.I.<89>æAîâ×2hdÿÿÿ
  000001c0  ff 35 6f ce ed fe d3 86  d0 d6 e6 77 7d 53 c7 be  ÿ5oÎíþÓ<86>ÐÖæw}SÇ¾
  000001d0  9e 88 38 0b 89 a5 7b e1  63 2a 95 98 29 1d b4 2b  <9e><88>8.<89>¥{ác*<95><98>).´+
  000001e0  6c 00 00 00 00 69 46 30  43 02 20 70 d2 b6 10 ac  l....iF0C. pÒ¶.¬
  000001f0  64 d9 64 3a 87 d3 3d 78  59 96 59 66 12 e3 43 fe  dÙd:<87>Ó=xY<96>Yf.ãCþ
  00000200  62 f7 a6 a9 4f 7a b7 32  ff bb 8a 02 1f 1b 1b ee  b÷¦©Oz·2ÿ»<8a>....î
  00000210  85 4f aa 65 95 be 85 6c  64 a8 cf 50 86 82 1d 38  <85>Oªe<95>¾<85>ld¨ÏP<86><82>.8
  00000220  67 a1 da 5e 12 bc 27 ab  59 05 ab 95 01 21 02 51  g¡Ú^.¼'«Y.«<95>.!.Q
  00000230  57 1c 71 1c 89 34 3b 47  7e a2 09 60 15 42 b9 82  W.q.<89>4;G~¢.`.B¹<82>
  00000240  7d ad 55 e1 24 9c a3 5b  b8 cc 0f 65 ef ad 1c ff  }­Uá$<9c>£[¸Ì.eï­.ÿ
  00000250  ff ff ff f4 d0 29 cf 2c  83 ae 07 01 94 39 d8 ce  ÿÿÿôÐ)Ï,<83>®..<94>9ØÎ
  00000260  96 05 cb 2c e6 c6 79 67  a7 6f 91 18 b7 33 6e e8  <96>.Ë,æÆyg§o<91>.·3nè
  00000270  fc 5c ae 02 00 00 00 6a  47 30 44 02 21 00 fe c8  ü\®....jG0D.!.þÈ
  00000280  0c c7 0b 82 27 4c 0b 29  2c 79 a1 c0 cb 67 2d 83  .Ç.<82>'L.),y¡ÀËg-<83>
  00000290  b0 bc c7 9e a6 1a 55 e7  b5 0a e6 c3 2f 6b 02 1f  °¼Ç<9e>¦.Uçµ.æÃ/k..
  000002a0  4d c2 53 4e 64 7b bc ec  0d 27 41 e4 44 21 a0 80  MÂSNd{¼ì.'AäD! <80>
  000002b0  ad 19 2e dc cc ca bc 74  2d a7 15 98 0d be 94 01  ­..ÜÌÊ¼t-§.<98>.¾<94>.
  000002c0  21 02 35 47 f5 78 7c 54  ca f6 7b e8 35 9a 48 25  !.5Gõx|TÊö{è5<9a>H%
  000002d0  53 ee 83 a8 e7 7e b1 35  26 47 5a d1 74 75 07 7d  Sî<83>¨ç~±5&GZÑtu.}
  000002e0  88 8c ff ff ff ff 02 62  e7 b1 03 00 00 00 00 19  <88><8c>ÿÿÿÿ.bç±......
  000002f0  76 a9 14 f9 2a 54 b6 0a  e8 b9 ea f4 23 4a ea 1b  v©.ù*T¶.è¹êô#Jê.
  00000300  51 d0 c0 65 9d 6a ed 88  ac 71 92 00 00 00 00 00  QÐÀe<9d>jí<88>¬q<92>.....
  00000310  00 19 76 a9 14 08 24 04  70 d3 e7 e1 a3 dd 26 e7  ..v©..$.pÓçá£Ý&ç
  00000320  65 5e 3f b9 03 73 bf a5  bb 88 ac 00 00 00 00     e^?¹.s¿¥»<88>¬....

  */
class TransactionSpec extends EnvelopeTestSuite[Transaction]  {

  val codec = TransactionCodec.codec

  val envelopeHeader = bytes(
    """
      f9 be b4 d9 74 78 00 00  00 00 00 00 00 00 00 00
      2f 03 00 00 fa 07 e8 a2
    """)

  val payload = bytes(
    """
      01 00 00 00 05 53 25 21  90 60 8c 62 7b df c6 60
      18 97 7c d2 7f 7e 0c b9  c8 6d d5 a6 4c 5b 3f 23
      f3 62 87 82 42 01 00 00  00 6b 48 30 45 02 21 00
      e5 5e 79 7b 51 71 11 48  69 2b 9c c4 a2 c8 06 52
      c6 9b d7 8d 41 03 43 8d  ec 41 e0 16 70 c0 46 f8
      02 20 7e f8 05 74 57 0c  fd e8 f6 a7 88 a6 b2 90
      03 43 d0 11 15 64 5b 6a  03 77 73 c4 3d f5 07 49
      32 94 01 21 02 77 25 55  48 3a 34 45 ba b2 cd d3
      88 6a 1c c9 b3 62 f4 e9  99 1d b5 0d 09 44 68 9a
      ed 90 b6 7b 19 ff ff ff  ff b3 cd ca 76 41 77 1b
      13 fa 5d 56 7b 81 9d 98  04 f3 2f fb 45 82 f5 20
      0e 37 7c 60 e6 82 5f 05  b4 01 00 00 00 6b 48 30
      45 02 21 00 84 4f 55 bd  c9 bf a1 2f 46 85 60 ee
      29 32 19 43 3a d9 60 60  53 8a 6e e6 df 7d 09 22
      d7 e4 b8 06 02 20 4d a8  d7 3e 02 4e 35 3f b5 59
      8d da 67 a6 af e8 42 56  77 b7 ee 48 3c bb 21 47
      ce d5 bf c5 a6 29 01 21  03 df 96 db 89 1b 82 4f
      53 f5 ac 42 33 68 b6 27  31 90 71 b9 54 09 49 19
      89 e6 41 ee e2 d7 32 68  64 ff ff ff ff fe 72 e4
      56 c2 ad 03 e3 c6 c2 37  1c c5 17 8c 44 27 9c 51
      34 2b af df 27 e1 56 a9  ce 4c 3a 37 db 01 00 00
      00 6b 48 30 45 02 21 00  82 ca 08 51 f3 a9 4d 4e
      0d 00 67 a4 7f 6b d1 32  b4 7b 79 8a 83 e4 0e 7a
      40 80 11 d4 02 1d 7e c5  02 20 34 b7 53 58 57 57
      54 bf e5 82 29 61 0d 12  74 9c 60 e7 29 5d 94 72
      79 00 14 06 7d 36 ea b8  8d d1 01 21 03 df 96 db
      89 1b 82 4f 53 f5 ac 42  33 68 b6 27 31 90 71 b9
      54 09 49 19 89 e6 41 ee  e2 d7 32 68 64 ff ff ff
      ff 35 6f ce ed fe d3 86  d0 d6 e6 77 7d 53 c7 be
      9e 88 38 0b 89 a5 7b e1  63 2a 95 98 29 1d b4 2b
      6c 00 00 00 00 69 46 30  43 02 20 70 d2 b6 10 ac
      64 d9 64 3a 87 d3 3d 78  59 96 59 66 12 e3 43 fe
      62 f7 a6 a9 4f 7a b7 32  ff bb 8a 02 1f 1b 1b ee
      85 4f aa 65 95 be 85 6c  64 a8 cf 50 86 82 1d 38
      67 a1 da 5e 12 bc 27 ab  59 05 ab 95 01 21 02 51
      57 1c 71 1c 89 34 3b 47  7e a2 09 60 15 42 b9 82
      7d ad 55 e1 24 9c a3 5b  b8 cc 0f 65 ef ad 1c ff
      ff ff ff f4 d0 29 cf 2c  83 ae 07 01 94 39 d8 ce
      96 05 cb 2c e6 c6 79 67  a7 6f 91 18 b7 33 6e e8
      fc 5c ae 02 00 00 00 6a  47 30 44 02 21 00 fe c8
      0c c7 0b 82 27 4c 0b 29  2c 79 a1 c0 cb 67 2d 83
      b0 bc c7 9e a6 1a 55 e7  b5 0a e6 c3 2f 6b 02 1f
      4d c2 53 4e 64 7b bc ec  0d 27 41 e4 44 21 a0 80
      ad 19 2e dc cc ca bc 74  2d a7 15 98 0d be 94 01
      21 02 35 47 f5 78 7c 54  ca f6 7b e8 35 9a 48 25
      53 ee 83 a8 e7 7e b1 35  26 47 5a d1 74 75 07 7d
      88 8c ff ff ff ff 02 62  e7 b1 03 00 00 00 00 19
      76 a9 14 f9 2a 54 b6 0a  e8 b9 ea f4 23 4a ea 1b
      51 d0 c0 65 9d 6a ed 88  ac 71 92 00 00 00 00 00
      00 19 76 a9 14 08 24 04  70 d3 e7 e1 a3 dd 26 e7
      65 5e 3f b9 03 73 bf a5  bb 88 ac 00 00 00 00

    """)

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "tx",
    payload.length.toInt,
    Checksum.fromHex("fa 07 e8 a2"),
    BitVector.view(payload)
  )

  val message = TransactionSpec.SampleTransaction
}

object TransactionSpec {
  val SampleTransaction = Transaction(version=1, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("42828762f3233f5b4ca6d56dc8b90c7e7fd27c971860c6df7b628c6090212553")), outputIndex=1, unlockingScript=UnlockingScript(bytes("483045022100e55e797b51711148692b9cc4a2c80652c69bd78d4103438dec41e01670c046f802207ef80574570cfde8f6a788a6b2900343d01115645b6a037773c43df507493294012102772555483a3445bab2cdd3886a1cc9b362f4e9991db50d0944689aed90b67b19")), sequenceNumber=4294967295L),NormalTransactionInput(outputTransactionHash=Hash(bytes("b4055f82e6607c370e20f58245fb2ff304989d817b565dfa131b774176cacdb3")), outputIndex=1, unlockingScript=UnlockingScript(bytes("483045022100844f55bdc9bfa12f468560ee293219433ad96060538a6ee6df7d0922d7e4b80602204da8d73e024e353fb5598dda67a6afe8425677b7ee483cbb2147ced5bfc5a629012103df96db891b824f53f5ac423368b627319071b95409491989e641eee2d7326864")), sequenceNumber=4294967295L),NormalTransactionInput(outputTransactionHash=Hash(bytes("db373a4ccea956e127dfaf2b34519c27448c17c51c37c2c6e303adc256e472fe")), outputIndex=1, unlockingScript=UnlockingScript(bytes("48304502210082ca0851f3a94d4e0d0067a47f6bd132b47b798a83e40e7a408011d4021d7ec5022034b75358575754bfe58229610d12749c60e7295d9472790014067d36eab88dd1012103df96db891b824f53f5ac423368b627319071b95409491989e641eee2d7326864")), sequenceNumber=4294967295L),NormalTransactionInput(outputTransactionHash=Hash(bytes("6c2bb41d2998952a63e17ba5890b38889ebec7537d77e6d6d086d3feedce6f35")), outputIndex=0, unlockingScript=UnlockingScript(bytes("463043022070d2b610ac64d9643a87d33d785996596612e343fe62f7a6a94f7ab732ffbb8a021f1b1bee854faa6595be856c64a8cf5086821d3867a1da5e12bc27ab5905ab9501210251571c711c89343b477ea209601542b9827dad55e1249ca35bb8cc0f65efad1c")), sequenceNumber=4294967295L),NormalTransactionInput(outputTransactionHash=Hash(bytes("ae5cfce86e33b718916fa76779c6e62ccb0596ced839940107ae832ccf29d0f4")), outputIndex=2, unlockingScript=UnlockingScript(bytes("473044022100fec80cc70b82274c0b292c79a1c0cb672d83b0bcc79ea61a55e7b50ae6c32f6b021f4dc2534e647bbcec0d2741e44421a080ad192edccccabc742da715980dbe940121023547f5787c54caf67be8359a482553ee83a8e77eb13526475ad17475077d888c")), sequenceNumber=4294967295L)), outputs=List(TransactionOutput(value=61990754L, lockingScript=LockingScript(bytes("76a914f92a54b60ae8b9eaf4234aea1b51d0c0659d6aed88ac"))),TransactionOutput(value=37489L, lockingScript=LockingScript(bytes("76a91408240470d3e7e1a3dd26e7655e3fb90373bfa5bb88ac")))), lockTime=0L)
}
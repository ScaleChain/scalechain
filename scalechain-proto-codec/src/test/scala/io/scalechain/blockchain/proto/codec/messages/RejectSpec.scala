package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
[Bitcoin Core Packets Captured]
  [NET] recv; header:
  <Header> Magic:ù¾´Ù, Command:reject, Size:60, Checksum:1810173973
  dumping data len : 24
  00000000  f9 be b4 d9 72 65 6a 65  63 74 00 00 00 00 00 00  ù¾´Ùreject......
  00000010  3c 00 00 00 15 10 e5 6b                           <.....åk
  [NET] recv; data:
  dumping data len : 60
  00000000  02 74 78 42 17 6d 65 6d  70 6f 6f 6c 20 6d 69 6e  .txB.mempool min
  00000010  20 66 65 65 20 6e 6f 74  20 6d 65 74 6e 53 2a e3   fee not metnS*ã
  00000020  34 33 ca e1 81 90 ea 2f  e3 e1 9a 9a 21 81 69 8f  43Êá<81><90>ê/ãá<9a><9a>!<81>i<8f>
  00000030  2d aa e2 15 88 85 67 bf  e4 e0 02 9a              -ªâ.<88><85>g¿äà.<9a>
*/
class RejectSpec extends EnvelopeTestSuite[Reject]  {

  val codec = RejectCodec.codec

  val envelopeHeader = bytes(
    """
      f9 be b4 d9 72 65 6a 65  63 74 00 00 00 00 00 00
      3c 00 00 00 15 10 e5 6b
    """)

  val payload = bytes(
    """
      02 74 78 42 17 6d 65 6d  70 6f 6f 6c 20 6d 69 6e
      20 66 65 65 20 6e 6f 74  20 6d 65 74 6e 53 2a e3
      34 33 ca e1 81 90 ea 2f  e3 e1 9a 9a 21 81 69 8f
      2d aa e2 15 88 85 67 bf  e4 e0 02 9a
    """)

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "reject",
    payload.length.toInt,
    Checksum.fromHex("6b e5 10 15"),
    BitVector.view(payload)
  )

  val message = Reject("tx", RejectType.REJECT_INSUFFICIENTFEE, "mempool min fee not met", bytes("6e532ae33433cae18190ea2fe3e19a9a2181698f2daae215888567bfe4e0029a"))


}

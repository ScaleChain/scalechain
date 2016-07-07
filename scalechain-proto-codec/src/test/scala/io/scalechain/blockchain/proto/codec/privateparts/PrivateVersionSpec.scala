package io.scalechain.blockchain.proto.codec.privateparts

import io.scalechain.blockchain.proto.{PrivateVersion, IPv6Address, NetworkAddress, Version}
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector



class PrivateVersionSpec extends EnvelopeTestSuite[PrivateVersion]  {

  val codec = PrivateVersionCodec.codec

  val envelopeHeader = bytes(
    """
      f9 be b4 d9 70 72 69 76  76 65 72 73 69 6f 6e 00
      23 00 00 00 72 f3 c1 5a
    """)

  val payload = bytes(
    """
      22 31 46 31 74 41 61 7a  35 78 31 48 55 58 72 43
      4e 4c 62 74 4d 44 71 63  77 36 6f 35 47 4e 6e 34
      78 71 58
    """)

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "privversion",
    payload.length.toInt,
    Checksum.fromHex("72 f3 c1 5a"),
    BitVector.view(payload)
  )

  val message = PrivateVersion("1F1tAaz5x1HUXrCNLbtMDqcw6o5GNn4xqX")

}
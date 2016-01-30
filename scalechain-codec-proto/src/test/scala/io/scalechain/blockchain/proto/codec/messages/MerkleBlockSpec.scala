package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.MerkleBlock
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
    [Bitcoin Core Packets Not Captured]
  */
class MerkleBlockSpec extends EnvelopeTestSuite[MerkleBlock]  {

  val codec = MerkleBlockCodec.codec

  val envelopeHeader = bytes("""
                             """)

  val payload = bytes("")

  val envelope = BitcoinMessageEnvelope(
    Magic.MAIN,
    "merkleblock",
    payload.length.toInt,
    Checksum.fromHex(""),
    BitVector.view(payload)
  )

  val message = null//MerkleBlock()

}

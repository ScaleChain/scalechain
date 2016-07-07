package io.scalechain.blockchain.proto.codec.privateparts

import io.scalechain.blockchain.proto.codec.messages.TransactionSpec
import io.scalechain.blockchain.proto.{BlockSignature, InPoint, Hash, TransactionPoolEntry}
import io.scalechain.blockchain.proto.codec.{BlockSignatureCodec, TransactionPoolEntryCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._


class BlockSignatureSpec extends PayloadTestSuite[BlockSignature]  {

  val codec = BlockSignatureCodec.codec


  val payload = bytes(
    """b0a10000000000000000000000000000000000000000000000000000000000000000
    """)

  val message = BlockSignature(
    magic = BlockSignature.MAGIC,
    blockHash = Hash.ALL_ZERO
  )
}
package io.scalechain.blockchain.proto.codec.indexparts

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{OrphanBlockDescriptorCodec, TransactionCountCodec, PayloadTestSuite}
import io.scalechain.util.HexUtil._



class OrphanBlockDescriptorCodecSpec extends PayloadTestSuite[OrphanBlockDescriptor]  {

  val codec = OrphanBlockDescriptorCodec.codec

  val payload = bytes(
    """
      040000007b1eabe0209b1fe794124575ef807057c77ada2138ae4fa8d6c4de0398a14f3f701eabe0209b1fe794124575ef807057c77ada2138ae4fa8d6c4de0398a14f4fd202964940420f00d2040000ec030000010000000a00000000000000c8000000
    """)

  val message = OrphanBlockDescriptor(
    blockHeader = BlockHeader(
      version = 4,
      hashPrevBlock = Hash(bytes("3f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e7b")),
      hashMerkleRoot = Hash(bytes("4f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e70")),
      timestamp = 1234567890L,
      target = 1000000L,
      nonce = 1234L
    ),
    transactionCount = 1004,
    blockLocator =
      FileRecordLocator(
        fileIndex = 1,
        RecordLocator(
          offset = 10,
          size = 200
        )
      )
  )
}

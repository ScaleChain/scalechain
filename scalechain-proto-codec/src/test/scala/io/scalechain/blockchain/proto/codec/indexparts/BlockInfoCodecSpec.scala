package io.scalechain.blockchain.proto.codec.indexparts

import io.scalechain.blockchain.proto.codec.{BlockInfoCodec, PayloadTestSuite}
import io.scalechain.blockchain.proto._
import io.scalechain.util.HexUtil._

class BlockInfoCodecSpec extends PayloadTestSuite[BlockInfo]  {

  val codec = BlockInfoCodec.codec

  val payload = bytes(
    """64000000a086010000000000ff701eabe0209b1fe794124575ef807057c77ada2138ae4fa8d6c4de0398a14f2fec03000000000000040000007b1eabe0209b1fe794124575ef807057c77ada2138ae4fa8d6c4de0398a14f3f701eabe0209b1fe794124575ef807057c77ada2138ae4fa8d6c4de0398a14f4fd202964940420f00d204000000
    """)

  val message = BlockInfo(
    height = 100,
    chainWork = 100000L,
    nextBlockHash = Some(Hash("2f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e70")),
    transactionCount = 1004,
    status = 0,
    blockHeader = BlockHeader(
      version = 4,
      hashPrevBlock = Hash(bytes("3f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e7b")),
      hashMerkleRoot = Hash(bytes("4f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e70")),
      timestamp = 1234567890L,
      target = 1000000L,
      nonce = 1234L
    ),
    blockLocatorOption = None
  )
}

class BlockInfoCodecSpecWithBlockLocator extends PayloadTestSuite[BlockInfo]  {

  val codec = BlockInfoCodec.codec

  val payload = bytes(
    """64000000a08601000000000000ec03000000000000040000007b1eabe0209b1fe794124575ef807057c77ada2138ae4fa8d6c4de0398a14f3f701eabe0209b1fe794124575ef807057c77ada2138ae4fa8d6c4de0398a14f4fd202964940420f00d2040000ff010000000a00000000000000c8000000
    """.stripMargin)

  val message = BlockInfo(
    height = 100,
    chainWork = 100000L,
    nextBlockHash = None,
    transactionCount = 1004,
    status = 0,
    blockHeader = BlockHeader(
      version = 4,
      hashPrevBlock = Hash(bytes("3f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e7b")),
      hashMerkleRoot = Hash(bytes("4f4fa19803dec4d6a84fae3821da7ac7577080ef75451294e71f9b20e0ab1e70")),
      timestamp = 1234567890L,
      target = 1000000L,
      nonce = 1234L
    ),
    blockLocatorOption = Some (
      FileRecordLocator(
        fileIndex = 1,
        RecordLocator(
          offset = 10,
          size = 200
        )
      )
    )
  )
}

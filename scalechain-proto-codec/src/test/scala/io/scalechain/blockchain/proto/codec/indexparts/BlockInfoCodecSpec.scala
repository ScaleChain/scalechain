package io.scalechain.blockchain.proto.codec.indexparts

import io.scalechain.blockchain.proto.codec.{BlockInfoCodec, PayloadTestSuite}
import io.scalechain.blockchain.proto._
import io.scalechain.util.HexUtil._

class BlockInfoCodecSpec extends PayloadTestSuite[BlockInfo]  {

  val codec = BlockInfoCodec.codec

  val payload = bytes(
    """
      64 00 00 00 ec 03 00 00  00 00 00 00 04 00 00 00
      7b 1e ab e0 20 9b 1f e7  94 12 45 75 ef 80 70 57
      c7 7a da 21 38 ae 4f a8  d6 c4 de 03 98 a1 4f 3f
      70 1e ab e0 20 9b 1f e7  94 12 45 75 ef 80 70 57
      c7 7a da 21 38 ae 4f a8  d6 c4 de 03 98 a1 4f 4f
      d2 02 96 49 40 42 0f 00  d2 04 00 00 00
    """)

  val message = BlockInfo(
    height = 100,
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
    """
      64 00 00 00 ec 03 00 00  00 00 00 00 04 00 00 00
      7b 1e ab e0 20 9b 1f e7  94 12 45 75 ef 80 70 57
      c7 7a da 21 38 ae 4f a8  d6 c4 de 03 98 a1 4f 3f
      70 1e ab e0 20 9b 1f e7  94 12 45 75 ef 80 70 57
      c7 7a da 21 38 ae 4f a8  d6 c4 de 03 98 a1 4f 4f
      d2 02 96 49 40 42 0f 00  d2 04 00 00 ff 01 00 00
      00 0a 00 00 00 00 00 00  00 c8 00 00 00)

    """)

  val message = BlockInfo(
    height = 100,
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

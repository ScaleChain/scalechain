package io.scalechain.blockchain.storage.test

import io.netty.buffer.Unpooled
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.BlockCodec
import io.scalechain.blockchain.proto.codec.CodecTestUtil
import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.blockchain.script.hash
//import io.scalechain.blockchain.proto.codec.CodecTestUtil
//import io.scalechain.blockchain.script.HashSupported.*
import io.scalechain.io.HexFileLoader
import io.scalechain.test.TestMethods.filledString
import io.scalechain.util.HexUtil
import io.scalechain.util.ListExt

/**
  * Created by kangmo on 3/13/16.
  */
object TestData : ProtoTestData, CodecTestUtil {

  val txHash1 = transaction1().hash()
  val txHash2 = transaction2().hash()

  val rawBlockData = HexFileLoader.load("../data/unittest/codec/block-size231721.hex")
  val block = decodeFully(BlockCodec, rawBlockData)
  val blockHash = block.header.hash()

  // The genesis block
  val block1 = block.copy (
    header = block.header.copy(
      hashPrevBlock = Hash.ALL_ZERO
    )
  )

  val blockHash1 = block1.header.hash()

  // The block right after the genesis block.
  val block2 = block1.copy(
    header = block1.header.copy(
      version = 5,
      nonce = 1234,
      hashPrevBlock = blockHash1,
      timestamp = 123456789L
    )
  )
  val blockHash2 = block2.header.hash()

  /**
   * Create a dummy hash.
   *
   * @param num should be a one digit integer such as 1 or 2
   * @return The dummy hash value which fills the hash with the given digit.
   */
  fun dummyHash(num: Int) : Hash {
    assert(num >= 0 && num <= 9)
    return Hash(HexUtil.bytes(filledString(64, ('0'+num).toByte() )))
  }
}

package io.scalechain.blockchain.storage.test

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.BlockCodec
//import io.scalechain.blockchain.proto.codec.CodecTestUtil
import io.scalechain.blockchain.proto.test.ProtoTestData
//import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.io.HexFileLoader
import io.scalechain.util.HexUtil

/*
/**
  * Created by kangmo on 3/13/16.
  */
object TestData : ProtoTestData with CodecTestUtil {

  val txHash1 = transaction1.hash
  val txHash2 = transaction2.hash

  val rawBlockData = HexFileLoader.load("data/unittest/codec/block-size231721.hex")
  val block = decodeFully(BitVector.view(rawBlockData))(BlockCodec.codec)
  val blockHash = block.header.hash

  // The genesis block
  val block1 = block.copy (
    header = block.header.copy(
      hashPrevBlock = Hash(ALL_ZERO_HASH.value)
    )
  )

  val blockHash1 = block1.header.hash

  // The block right after the genesis block.
  val block2 = block1.copy(
    header = block1.header.copy(
      version = 5,
      nonce = 1234,
      hashPrevBlock = blockHash1,
      timestamp = 123456789L
    )
  )
  val blockHash2 = block2.header.hash
}
*/
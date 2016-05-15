package io.scalechain.blockchain.storage.test

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec.{BlockCodec, CodecTestUtil}
import io.scalechain.blockchain.proto.test.ProtoTestData
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.io.HexFileLoader
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector


/**
  * Created by kangmo on 3/13/16.
  */
object TestData extends ProtoTestData with CodecTestUtil {

  val txHash1 = Hash( HashCalculator.transactionHash(transaction1) )
  val txHash2 = Hash( HashCalculator.transactionHash(transaction2) )

  val rawBlockData = HexFileLoader.load("data/unittest/codec/block-size231721.hex")
  val block = decodeFully(BitVector.view(rawBlockData))(BlockCodec.codec)
  val blockHash = Hash (HashCalculator.blockHeaderHash(block.header))

  // The genesis block
  val block1 = block.copy (
    header = block.header.copy(
      hashPrevBlock = BlockHash(ALL_ZERO_HASH.value)
    )
  )

  val blockHash1 = Hash( HashCalculator.blockHeaderHash(block1.header))

  // The block right after the genesis block.
  val block2 = block1.copy(
    header = block1.header.copy(
      version = 5,
      nonce = 1234,
      hashPrevBlock = BlockHash(blockHash1.value),
      timestamp = 123456789L
    )
  )
  val blockHash2 = Hash( HashCalculator.blockHeaderHash(block2.header))
}

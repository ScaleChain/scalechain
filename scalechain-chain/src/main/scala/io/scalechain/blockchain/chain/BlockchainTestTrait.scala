package io.scalechain.blockchain.chain

import java.io.File
import java.lang.ref.WeakReference

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.blockchain.storage.{DiskBlockStorage, Storage}
import org.apache.commons.io.FileUtils
import org.scalatest._

trait BlockchainTestTrait extends FlatSpec with BeforeAndAfterEach {

  this: Suite =>

  val testPath : File

  Storage.initialize()

  val TEST_RECORD_FILE_SIZE = 1024 * 1024

  var storage : DiskBlockStorage = null
  var chain : Blockchain = null

  override def beforeEach() {
    // initialize a test.

    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    storage = new DiskBlockStorage(testPath, TEST_RECORD_FILE_SIZE)
    chain = new Blockchain(storage)
    Blockchain.theBlockchain = chain

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // finalize a test.
    storage.close()

    storage = null
    chain = null
    Blockchain.theBlockchain = null

    FileUtils.deleteDirectory(testPath)
  }


  val SampleData = new ChainSampleData(None)

  def createBlock(height : Int ) = {
    assert(height > 0)
    SampleData.S1_Block.copy(
      header = SampleData.S1_Block.header.copy(
        hashPrevBlock = Hash( chain.getBlockHash(height-1).value),
        nonce = height
      )
    )
  }

  def numberToHash(blockHeight : Int) = {
    createBlock(blockHeight).header.hash
/*
    new Hash( HashCalculator.blockHeaderHash( createBlock(blockHeight).header ).value ) {
      // Put height of the block on the hash for debugging purpose.
      val height = blockHeight
      override def equals(o : Any) = {
        super.equals(this.asInstanceOf[Hash], o.asInstanceOf[Hash])
      }
    }
*/
  }


  def putBlocks(blockCount : Int) = {
    for (blockHeight <- 1 to blockCount) {
      val blockHash = numberToHash(blockHeight)
      println(s"putblocks : ${blockCount}, ${blockHash}, ${createBlock(blockHeight)} ")
      // put a block using genesis block, as we don't check if the block hash matches in the putBlock method.
      chain.putBlock(blockHash, createBlock(blockHeight))
    }
  }
  /*
  def numberToHash(num : Int) : Hash = {
    val hexNum = BigInteger.valueOf(num).toString(16)
    val hexHash = ("000000000000000000000000000000" + hexNum ).takeRight(64)
    Hash(HexUtil.bytes(hexHash))
  }

  def hashToNumber(hash : Hash) : Int = {
    val hexValues = HexUtil.hex(hash.value)
    new BigInteger(hexValues, 16).intValue()
  }
  */
}
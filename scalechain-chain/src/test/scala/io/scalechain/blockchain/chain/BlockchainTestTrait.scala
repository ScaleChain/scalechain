package io.scalechain.blockchain.chain

import java.io.File
import java.math.BigInteger

import io.scalechain.blockchain.proto.BlockHash
import io.scalechain.blockchain.storage.{DiskBlockStorage, Storage}
import io.scalechain.blockchain.transaction.ChainEnvironment
import io.scalechain.util.HexUtil
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

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // finalize a test.
    storage.close()
    storage = null
    chain = null

    FileUtils.deleteDirectory(testPath)
  }

  def numberToHash(num : Int) : BlockHash = {
    val hexNum = BigInteger.valueOf(num).toString(16)
    val hexHash = ("0000000000000000000000000000000000000000000000000000000000000000" + hexNum ).takeRight(64)
    BlockHash(HexUtil.bytes(hexHash))
  }

  def hashToNumber(hash : BlockHash) : Int = {
    val hexValues = HexUtil.hex(hash.value)
    new BigInteger(hexValues, 16).intValue()
  }
}
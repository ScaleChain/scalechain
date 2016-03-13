package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.codec.{BlockCodec, TransactionCodec, BlockHeaderCodec}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.storage.record.{BlockRecordStorage, RecordStorage}
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 11/2/15.
  */
class BlockWriterSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {
  this: Suite =>

  import TestData._

  Storage.initialize()

  // Use record storage with maxFileSize 1M, instead of using BlockRecordStorage, which uses 100M file size limit.
  var writer : BlockWriter = null
  var storage : BlockRecordStorage = null
  override def beforeEach() {

    val testPath = new File("./target/unittests-BlockWriterSpec/")
    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    storage = new BlockRecordStorage(testPath)
    writer = new BlockWriter(storage)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    storage.close()
  }

  "appendBlock" should "write a block and produce valid locators for each transaction" in {
    // Step 1 : Write a block
    val appendBlockResult : AppendBlockResult = writer.appendBlock(block1)

    // Step 2 : Read a block header and check if the block header matches.
    val blockHeader = storage.readRecord( appendBlockResult.headerLocator)(BlockHeaderCodec)
    blockHeader shouldBe block1.header

    // Step 3 : Create a map from a transaction hash to transaction locator.
    val txLocatorByHash =
        (appendBlockResult.txLocators.map( _.txHash ) zip
         appendBlockResult.txLocators.map(_.txLocator)
        ).toMap

    // Step 4 : Read each transaction on each transaction locator.
    for (transaction <- block1.transactions) {
      // Step 4.1 : Calculate transaction hash.
      val txHash = Hash( HashCalculator.transactionHash(transaction) )
      // Step 4.2 : Get the transaction locator.
      val txLocatorOptin = txLocatorByHash.get(txHash)

      // Step 4.3 : Read the transaction using the locator
      val readTransaction = storage.readRecord(txLocatorOptin.get)(TransactionCodec)

      // Step 4.4 : Make sure that the transaction matches.
      readTransaction shouldBe transaction

      // Step 4.5 : Make sure the transaction hash matches.
      Hash( HashCalculator.transactionHash(readTransaction)) shouldBe txHash
    }
  }

  "appendBlock" should "be compatible with BlockCodec" in {
    // Step 1 : Write using appendBlock
    val appendBlockResult : AppendBlockResult = writer.appendBlock(block1)

    // Step 2 : Get the block locator.
    // The AppendBlockResult.headerLocator has its size 80(the size of block header)
    // We need to use the last transaction's (offset + size), which is the block size to get the block locator.
    val lastTxLocator = appendBlockResult.txLocators.last.txLocator.recordLocator
    val blockSize = lastTxLocator.offset + lastTxLocator.size

    val blockLocator = appendBlockResult.headerLocator.copy(
      recordLocator = appendBlockResult.headerLocator.recordLocator.copy (
        size = blockSize.toInt
      )
    )

    // Step 3 : Read using BlockCodec
    val readBlock = storage.readRecord(blockLocator)(BlockCodec)

    readBlock shouldBe block1
  }
}


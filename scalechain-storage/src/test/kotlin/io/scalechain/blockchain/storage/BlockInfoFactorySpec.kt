package io.scalechain.blockchain.storage.record

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.BlockInfo
import io.scalechain.blockchain.proto.FileRecordLocator
import io.scalechain.blockchain.proto.RecordLocator
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.BlockInfoFactory
import io.scalechain.blockchain.storage.GenesisBlock
import io.scalechain.crypto.HashEstimation
import org.junit.runner.RunWith
import io.scalechain.blockchain.storage.test.TestData
import io.scalechain.blockchain.storage.test.TestData.block1
import io.scalechain.blockchain.storage.test.TestData.block2
import kotlin.test.assertTrue

/**
 * Created by kangmo on 16/01/2017.
 */

@RunWith(KTestJUnitRunner::class)
class BlockInfoFactorySpec : FlatSpec(), Matchers {
  override fun beforeEach() {
    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
  }

  // BlockIndex is a trait. No need to create test cases for a trait.
  init {
    "create" should "create a block info" {
      /*
      val prevBlocKInfo = BlockInfo(
        height = 1L,
        transactionCount = 1,
        chainWork = 1000,
        nextBlockHash = null,
        // BUGBUG : Need to use enumeration
        status = 0,
        blockHeader = GenesisBlock.BLOCK.header,
        blockLocatorOption = FileRecordLocator(fileIndex=0, recordLocator= RecordLocator(offset=10,size=20))
      )
      */

      // For genesis block
      val genesisBlockLocation = FileRecordLocator(fileIndex=0, recordLocator= RecordLocator(offset=10,size=20))
      val genesisBlockInfo = BlockInfoFactory.create(
        null,
        block1.header,
        block1.header.hash(),
        block1.transactions.size,
        genesisBlockLocation
      )

      genesisBlockInfo.height shouldBe 0L
      genesisBlockInfo.transactionCount shouldBe block1.transactions.size
      genesisBlockInfo.chainWork shouldBe HashEstimation.getHashCalculations(block1.header.hash().value.array)
      assertTrue(genesisBlockInfo.nextBlockHash == null)
      assertTrue(genesisBlockInfo.status == 0)
      genesisBlockInfo.blockHeader shouldBe block1.header
      genesisBlockInfo.blockLocatorOption shouldBe genesisBlockLocation

      // For the first block on top of the genesis block.
      val firstBlockLocation = FileRecordLocator(fileIndex=0, recordLocator= RecordLocator(offset=30,size=50))
      val firstBlockInfo = BlockInfoFactory.create(
        genesisBlockInfo,
        block2.header,
        block2.header.hash(),
        block2.transactions.size,
        firstBlockLocation
      )

      firstBlockInfo.height shouldBe 1L
      firstBlockInfo.transactionCount shouldBe block2.transactions.size
      firstBlockInfo.chainWork shouldBe genesisBlockInfo.chainWork + HashEstimation.getHashCalculations(block2.header.hash().value.array)
      assertTrue(firstBlockInfo.nextBlockHash == null)
      assertTrue(firstBlockInfo.status == 0)
      firstBlockInfo.blockHeader shouldBe block2.header
      firstBlockInfo.blockLocatorOption shouldBe firstBlockLocation
    }
  }
}
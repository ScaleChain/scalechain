package io.scalechain.blockchain.net

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import java.io.File

import io.scalechain.blockchain.chain.TransactionSampleData
import io.scalechain.blockchain.chain.BlockchainTestTrait
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.BlockHeader
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.transaction.ChainTestTrait
import org.junit.runner.RunWith


/**
  * Source code copied from : https://github.com/ACINQ/bitcoin-lib/blob/master/src/test/scala/fr/acinq/bitcoin/Base58Spec.scala
  * License : Apache v2.
  */
@RunWith(KTestJUnitRunner::class)
class BlockGatewaySpec : BlockchainTestTrait(), ChainTestTrait, Matchers {
  override val testPath = File("./build/unittests-BlockGatewaySpec/")

  lateinit var bgate : BlockGateway

  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()

    Node.theNode = null

    Node.create(PeerCommunicator(PeerSet.create()), chain)

    chain.putBlock(db, env().GenesisBlockHash, env().GenesisBlock )
    bgate = BlockGateway()
  }

  override fun afterEach() {
    super.afterEach()

  }

  fun putConsensualHeader(header : BlockHeader) = bgate.putConsensualHeader(header)
  fun putBlock(block : Block) = bgate.putReceivedBlock(block.header.hash(), block)

  init {
  
    "CH(Consensual Header)1 only" should "not be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putConsensualHeader(B.BLK01.header)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
    }
  
    "Block1 only" should "not be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putBlock(B.BLK01)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
    }
  
    "CH(Consensual Header)1, Block1" should "be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putConsensualHeader(B.BLK01.header)
      putBlock(B.BLK01)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
    }
  
    "Block1, CH(Consensual Header)1" should "be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putBlock(B.BLK01)
      putConsensualHeader(B.BLK01.header)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
    }
  
    "CH(Consensual Header)1, Block1, CH2, Block2" should "be attached to the blockchain" {
  
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putConsensualHeader(B.BLK01.header)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putBlock(B.BLK01)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
  
      putConsensualHeader(B.BLK02.header)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
  
      putBlock(B.BLK02)
      chain.getBestBlockHash(db) shouldBe B.BLK02.header.hash()
    }
  
    "CH(Consensual Header)1, Block1, Block2, CH2" should "be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putConsensualHeader(B.BLK01.header)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putBlock(B.BLK01)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
  
      putBlock(B.BLK02)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
  
      putConsensualHeader(B.BLK02.header)
      chain.getBestBlockHash(db) shouldBe B.BLK02.header.hash()
    }
  
    "CH(Consensual Header)1, Block2, Block1, CH2" should "be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putConsensualHeader(B.BLK01.header)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putBlock(B.BLK02)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putBlock(B.BLK01)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
  
      putConsensualHeader(B.BLK02.header)
      chain.getBestBlockHash(db) shouldBe B.BLK02.header.hash()
    }
  
    "CH(Consensual Header)1, Block2, CH2, Block1" should "be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putConsensualHeader(B.BLK01.header)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putBlock(B.BLK02)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putConsensualHeader(B.BLK02.header)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putBlock(B.BLK01)
      chain.getBestBlockHash(db) shouldBe B.BLK02.header.hash()
    }
  
    "CH(Consensual Header)1, CH2, Block2, Block1" should "be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putConsensualHeader(B.BLK01.header)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putConsensualHeader(B.BLK02.header)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putBlock(B.BLK02)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putBlock(B.BLK01)
      chain.getBestBlockHash(db) shouldBe B.BLK02.header.hash()
    }
  
    "CH(Consensual Header)1, CH2, Block1, Block2" should "be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putConsensualHeader(B.BLK01.header)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putConsensualHeader(B.BLK02.header)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putBlock(B.BLK01)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
  
      putBlock(B.BLK02)
      chain.getBestBlockHash(db) shouldBe B.BLK02.header.hash()
    }
  
  
    "Block1, CH(Consensual Header)1, CH2, Block2" should "be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putBlock(B.BLK01)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putConsensualHeader(B.BLK01.header)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
  
      putConsensualHeader(B.BLK02.header)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
  
      putBlock(B.BLK02)
      chain.getBestBlockHash(db) shouldBe B.BLK02.header.hash()
    }
  
    "Block1, CH(Consensual Header)1, Block2, CH2" should "be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putBlock(B.BLK01)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putConsensualHeader(B.BLK01.header)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
  
      putBlock(B.BLK02)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
  
      putConsensualHeader(B.BLK02.header)
      chain.getBestBlockHash(db) shouldBe B.BLK02.header.hash()
    }
  
    "Block1, Block2, CH(Consensual Header)1, CH2" should "be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putBlock(B.BLK01)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putBlock(B.BLK02)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putConsensualHeader(B.BLK01.header)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
  
      putConsensualHeader(B.BLK02.header)
      chain.getBestBlockHash(db) shouldBe B.BLK02.header.hash()
    }
  
    /*
    "Block1, Block2, CH2, CH(Consensual Header)1" should "be attached to the blockchain" {
      // This case does not happen. BFT algorithm ensures that CH1 always comes before CH2.
    }
    */
  
    /*
    "Block1, CH2, Block2, CH(Consensual Header)1" should "be attached to the blockchain" {
      // This case does not happen. BFT algorithm ensures that CH1 always comes before CH2.
    }
    */
  
    /*
    "Block1, CH2, CH(Consensual Header)1, Block2" should "be attached to the blockchain" {
      // This case does not happen. BFT algorithm ensures that CH1 always comes before CH2.
    }
    */
  
    /*
    "CH2(Consensual Header), CH1, ... " should "be attached to the blockchain" {
  
    }
    */
  
    "Block2, CH(Consensual Header)1, Block1, CH2" should "be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putBlock(B.BLK02)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putConsensualHeader(B.BLK01.header)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putBlock(B.BLK01)
      chain.getBestBlockHash(db) shouldBe B.BLK01.header.hash()
  
      putConsensualHeader(B.BLK02.header)
      chain.getBestBlockHash(db) shouldBe B.BLK02.header.hash()
    }
  
    "Block2, CH(Consensual Header)1, CH2, Block1" should "be attached to the blockchain" {
      val data = TransactionSampleData(db)
      val B = data.Block
  
      putBlock(B.BLK02)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putConsensualHeader(B.BLK01.header)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putConsensualHeader(B.BLK02.header)
      chain.getBestBlockHash(db) shouldBe env().GenesisBlockHash
  
      putBlock(B.BLK01)
      chain.getBestBlockHash(db) shouldBe B.BLK02.header.hash()
    }
  
    /*
    "Block2, CH2, CH(Consensual Header)1, Block1" should "be attached to the blockchain" {
      // This case does not happen. BFT algorithm ensures that CH1 always comes before CH2.
    }
    */
  
    "Block2, Block1, CH(Consensual Header)1, CH2" should "be attached to the blockchain" {
  
    }
  
    /*
    "Block2, Block1, CH2, CH(Consensual Header)1" should "be attached to the blockchain" {
      // This case does not happen. BFT algorithm ensures that CH1 always comes before CH2.
    }
    */
  }
}


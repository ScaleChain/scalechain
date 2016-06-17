package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._
import HashSupported._

/**
  * Created by kangmo on 6/16/16.
  */
class TransactionMagnetSpec extends BlockchainTestTrait with TransactionTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-TransactionMagnetSpec/")

  import BlockSampleData._
  import BlockSampleData.Tx._
  import BlockSampleData.Block._

  var tm : TransactionMagnet = null

  override def beforeEach() {
    // initialize a test.

    super.beforeEach()

    // put the genesis block
    chain.putBlock(env.GenesisBlockHash, env.GenesisBlock)

    tm = chain.txMagnet
  }

  override def afterEach() {
    super.afterEach()

    // finalize a test.
    tm = null
  }

  "markOutputSpent" should "" in {
  }

  "markOutputUnspent" should "" in {
  }

  "markAllOutputsUnspent" should "" in {
  }

  "detachTransactionInput" should "" in {
  }

  "detachTransactionInputs" should "" in {
  }

  "detachTransaction" should "" in {
  }

  "attachTransactionInput" should "" in {
  }

  "attachTransactionInputs" should "" in {
  }

  "attachTransaction" should "" in {
  }
}

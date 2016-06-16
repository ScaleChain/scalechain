package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._

/**
  * Created by kangmo on 6/16/16.
  */
class TransactionPoolSpec extends BlockchainTestTrait with TransactionTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-TransactionPoolSpec/")

  import BlockSampleData._
  import BlockSampleData.Tx._
  import BlockSampleData.Block._

  override def beforeEach() {
    // initialize a test.

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    // finalize a test.
  }

  "getTransactionsFromPool" should "" in {
  }

  "addTransactionToPool" should "" in {
  }

  "removeTransactionFromPool" should "" in {
  }
}

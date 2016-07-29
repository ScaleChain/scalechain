package io.scalechain.blockchain.cli.command.stresstests

import java.io.File

import io.scalechain.blockchain.cli.command.stresstests.MultiThreadTransactionTester.TransactionTestCase
import io.scalechain.blockchain.proto.Transaction

import io.scalechain.blockchain.cli.command.stresstests.TransactionReader.TransactionListener

object MultiThreadTransactionTester {
  type TransactionTestCase = TransactionListener
}

/**
  * Run tests in parallel for each transaction group, written by generaterawtransaction RPC.
  */
class MultiThreadTransactionTester {
  /**
    * Read transaction group files, run test cases for each transaction in the group. Run the tests for each group in parallel.
    *
    * @param initialSplitTxTest
    * @param transactionTests
    */
  def test(initialSplitTxTest : TransactionTestCase, transactionTests : IndexedSeq[TransactionTestCase]) : Unit = {
    // Step 1 : Run the test for the initial split transaction.
    val initialTxReader = new TransactionReader( new File( GenerateRawTransactions.initialSplitTransactionFileName() ) )
    initialTxReader.read(initialSplitTxTest)

    // Step 2 : Load each transaction group file, run tests in parallel for each group.
    val threads =
      (0 until transactionTests.length).map { i =>
        new Thread() {
          override def run(): Unit = {
            val txTest = transactionTests(i)
            val txReader = new TransactionReader( new File( GenerateRawTransactions.transactionGroupFileName(i) ) )

            var txCount = 0
            val countShowingTest = (tx : Transaction) => {
              txTest(tx)
              txCount += 1
              if ( ((txCount >> 7) << 7) == txCount) { // txCount % 128 = 0
                println(s"Processed ${txCount} transactions")
              }
            }
            txReader.read(countShowingTest)
          }
        }
      }

    threads foreach { _.start } // Start all threads
    threads foreach { _.join }  // Join all threads
  }

  /**
    * Run the same test for all transaction groups and the initial split transaction.
    *
    * @param txGroupCount The number of transaction groups
    * @param transactionTest The test to run for each test.
    */
  def test(txGroupCount : Int, transactionTest : TransactionTestCase) : Unit = {
    test(transactionTest, IndexedSeq.fill(txGroupCount)(transactionTest))
  }
}

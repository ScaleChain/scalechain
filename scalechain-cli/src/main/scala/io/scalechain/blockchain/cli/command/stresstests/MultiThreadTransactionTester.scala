package io.scalechain.blockchain.cli.command.stresstests

import java.io.File

import io.scalechain.blockchain.cli.command.stresstests.MultiThreadTransactionTester.{ThreadGroupIndexFilter, RawTransactionWithGroupListener, TransactionWithGroupListener}
import io.scalechain.blockchain.proto.Transaction

import io.scalechain.blockchain.cli.command.stresstests.TransactionReader.RawTransactionListener
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.util.HexUtil

object MultiThreadTransactionTester {
  // (Thread group index, transaction) => Unit
  type TransactionWithGroupListener = (Int, Transaction) => Unit
  // (Thread group index, raw transaction string) => Unit
  type RawTransactionWithGroupListener = (Int, String) => Unit

  // Thread group index => Whether to run the thread group
  type ThreadGroupIndexFilter = Int => Boolean
}

/**
  * Run tests in parallel for each transaction group, written by generaterawtransaction RPC.
  */
class MultiThreadTransactionTester(threadGroupIndexFilter : Option[ThreadGroupIndexFilter] = None) {
  /**
    * Read transaction group files, run test cases for each transaction in the group. Run the tests for each group in parallel.
    *
    * @param initialSplitTxTest
    * @param transactionTests
    */
  def testTransaction(initialSplitTxTest : TransactionWithGroupListener, transactionTests : IndexedSeq[TransactionWithGroupListener]) : Unit = {

    def toTxWithGroupListener( txGroupListener : TransactionWithGroupListener ) : RawTransactionWithGroupListener = {
      (txGroupIndex : Int, rawTransaction : String) => {
        val rawTransactionBytes = HexUtil.bytes(rawTransaction)
        val transaction = TransactionCodec.parse(rawTransactionBytes)
        txGroupListener(txGroupIndex, transaction)
      }
    }

    val txTests = transactionTests.map { testCase =>
      toTxWithGroupListener(testCase)
    }
    testRawTransaction( toTxWithGroupListener(initialSplitTxTest), txTests )
  }

  def testRawTransaction(initialSplitTxTestWithGroup : RawTransactionWithGroupListener, transactionTests : IndexedSeq[RawTransactionWithGroupListener]) : Unit = {
    // Step 1 : Run the test for the initial split transaction.
    val initialTxReader = new TransactionReader( new File( GenerateRawTransactions.initialSplitTransactionFileName() ) )

    val initialSplitTxTest = (rawTx : String) => {
      initialSplitTxTestWithGroup(0, rawTx)
    }

    initialTxReader.read(initialSplitTxTest)

    // Step 2 : Load each transaction group file, run tests in parallel for each group.
    val threads =
      (0 until transactionTests.length).filter { i =>
        if (threadGroupIndexFilter.isDefined) { // Filter threads first if any threadGroupIndexFilter is defined.
          val filter : ThreadGroupIndexFilter = threadGroupIndexFilter.get
          filter(i)
        } else {
          true
        }
      }.map { i =>
        new Thread() {
          override def run(): Unit = {
            val txTest = transactionTests(i)
            val txReader = new TransactionReader( new File( GenerateRawTransactions.transactionGroupFileName(i) ) )

            var txCount = 0
            val countShowingTest = (rawTx : String) => {
              txTest(i, rawTx)
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
  def testTransaction(txGroupCount : Int, transactionTest : TransactionWithGroupListener) : Unit = {
    testTransaction(transactionTest, IndexedSeq.fill(txGroupCount)(transactionTest))
  }

  def testRawTransaction(txGroupCount : Int, transactionTest : RawTransactionWithGroupListener) : Unit = {
    testRawTransaction(transactionTest, IndexedSeq.fill(txGroupCount)(transactionTest))
  }
}

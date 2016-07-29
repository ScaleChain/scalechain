package io.scalechain.blockchain.cli.command.stresstests

import io.scalechain.blockchain.cli.command.Command

import io.scalechain.blockchain.cli.command.stresstests.MultiThreadTransactionTester.TransactionTestCase
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.codec.TransactionCodec

case class TestCase(name : String, initialSplitTxTest : TransactionTestCase, transactionTest : TransactionTestCase)

/**
  * Created by kangmo on 7/29/16.
  */
object MultiThreadTestLayers extends Command {
  val protoCodecTestCase : TransactionTestCase =
    (transaction : Transaction) => {
      val serialized = TransactionCodec.serialize(transaction)
      val parsed = TransactionCodec.parse(serialized)
      assert( parsed == transaction )
    }

  val testCases = List(
    TestCase("proto-codec", protoCodecTestCase, protoCodecTestCase)
  )

  def invoke(command : String, args : Array[String]) = {
    val transactionGroupCount = Integer.parseInt(args(1))

    testCases foreach { testCase : TestCase =>
      println(s"Testing ${testCase.name}")
      val transactionTests = IndexedSeq.fill(transactionGroupCount)(testCase.transactionTest)
      new MultiThreadTransactionTester().test(testCase.initialSplitTxTest, transactionTests)
    }
  }
}

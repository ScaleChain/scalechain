package io.scalechain.blockchain.cli.command.stresstests

import io.scalechain.blockchain.cli.command.stresstests.MultiThreadTransactionTester.TransactionWithGroupListener
import io.scalechain.blockchain.cli.command.{RpcParameters, Command}

import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.codec.TransactionCodec

case class TestCase(name : String, initialSplitTxTest : TransactionWithGroupListener, transactionTest : TransactionWithGroupListener)

/**
  * Created by kangmo on 7/29/16.
  */
object MultiThreadTestLayers extends Command {
  val protoCodecTestCase : TransactionWithGroupListener =
    (txGroupIndex : Int, transaction : Transaction) => {
      val serialized = TransactionCodec.serialize(transaction)
      val parsed = TransactionCodec.parse(serialized)
      assert( parsed == transaction )
    }

  val testCases = List(
    TestCase("proto-codec", protoCodecTestCase, protoCodecTestCase)
  )

  def invoke(command : String, args : Array[String], rpcParams : RpcParameters) = {
    val transactionGroupCount = Integer.parseInt(args(1))

    testCases foreach { testCase : TestCase =>
      println(s"Testing ${testCase.name}")
      val transactionTests = IndexedSeq.fill(transactionGroupCount)(testCase.transactionTest)
      new MultiThreadTransactionTester().testTransaction(testCase.initialSplitTxTest, transactionTests)
    }
  }
}

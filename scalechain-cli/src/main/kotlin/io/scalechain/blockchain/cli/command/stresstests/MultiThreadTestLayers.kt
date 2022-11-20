package io.scalechain.blockchain.cli.command.stresstests

import io.scalechain.blockchain.cli.command.RpcParameters
import io.scalechain.blockchain.cli.command.Command
import io.scalechain.blockchain.cli.command.CommandDescriptor

import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.util.ListExt

data class TestCase(val name : String, val initialSplitTxTest : TransactionWithGroupListener, val transactionTest : TransactionWithGroupListener)

/**
  * Created by kangmo on 7/29/16.
  */
object MultiThreadTestLayers : Command {
    override val descriptor = CommandDescriptor( "multithreadtestlayers", 1, "multithreadtestlayers <transaction group count>. multithreadtestlayers tests each layer using multi-threads. You need to run generaterawtransaction to generate transaction files used as inputs of this RPC.")
    val protoCodecTestCase = object : TransactionWithGroupListener {
      override fun onTransaction(txGroupIndex: Int, transaction: Transaction) {
          val serialized = TransactionCodec.encode(transaction)
          val parsed = TransactionCodec.decode(serialized)!!
          assert( parsed == transaction )
      }
  }

  val testCases = listOf(
    TestCase("proto-codec", protoCodecTestCase, protoCodecTestCase)
  )

  override fun invoke(command : String, args : Array<String>, rpcParams : RpcParameters) {
    val transactionGroupCount = Integer.parseInt(args[0])

    testCases.forEach { testCase : TestCase ->
      println("Testing ${testCase.name}")
      val transactionTests = ListExt.fill(transactionGroupCount, testCase.transactionTest)
      MultiThreadTransactionTester().testTransaction(testCase.initialSplitTxTest, transactionTests)
    }
  }
}

package io.scalechain.blockchain.proto

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.util.HexUtil
import org.junit.runner.RunWith


// Test case for data class Transaction
@RunWith(KTestJUnitRunner::class)
class TransactionSpec : FlatSpec(), Matchers {
    override fun beforeEach() {
        // set-up code
        //

        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

        // Need to revert the printer to null at the end of this case
        TransactionPrinter.printer = null
    }

    val transaction = Transaction(
        version = 1,
        inputs = listOf( NormalTransactionInput(TestData.ALL_ONE_HASH, 1, UnlockingScript(HexUtil.bytes("0001")), 1000) ),
        outputs = listOf( TransactionOutput(10, LockingScript(HexUtil.bytes("0203")))),
        lockTime = 2000L
    )

    init {
        "toString" should "return default string representation if the printer is not set" {
            println("Transaction : " + transaction.toString())
            transaction.toString() shouldBe """Transaction(version=1, inputs=List(NormalTransactionInput(outputTransactionHash=Hash("0101010101010101010101010101010101010101010101010101010101010101"), outputIndex=1L, unlockingScript=UnlockingScript("0001"), sequenceNumber=1000L)), outputs=List(TransactionOutput(value=10L, lockingScript=LockingScript("0203"))), lockTime=2000L)"""
        }

        "toString" should "return the printer's result if the printer is set" {
            TransactionPrinter.printer = object : TransactionPrinter {
                override fun toString(transaction: Transaction): String {
                    return "TransactionByPrinter(${transaction.version})"
                }
            }
            // Need to revert the printer to null at the end of this case
            //println("Transaction : " + transaction.toString())
            transaction.toString() shouldBe """TransactionByPrinter(1)"""
        }
    }
}
package io.scalechain.blockchain.proto

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.util.Bytes
import io.scalechain.util.HexUtil
import org.junit.runner.RunWith

// Test case for data class LockingScript
@RunWith(KTestJUnitRunner::class)
class UnLockingScriptSpec : FlatSpec(), Matchers {
    override fun beforeEach() {
        // set-up code
        //

        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

        // Need to revert the printer to null at the end of this case
        UnlockingScriptPrinter.printer = null
    }
    init {
        "toString" should "return default string representation if the printer is not set" {
            //println("" + LockingScript( HexUtil.bytes("010203")))
            UnlockingScript( Bytes.from("010203")).toString() shouldBe """UnlockingScript("010203")"""
        }

        "toString" should "return the printer's result if the printer is set" {
            UnlockingScriptPrinter.printer = object : UnlockingScriptPrinter {
                override fun toString(unlockingScript: UnlockingScript): String {
                    return "UnlockingScriptByPrinter(${HexUtil.hex(unlockingScript.data.array)})"
                }
            }

            //println("" + LockingScript( HexUtil.bytes("010203")))
            UnlockingScript( Bytes.from("010203")).toString() shouldBe """UnlockingScriptByPrinter(010203)"""
        }
    }
}

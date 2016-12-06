package io.scalechain.blockchain.proto

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.util.HexUtil
import org.junit.runner.RunWith

// Test case for data class LockingScript
@RunWith(KTestJUnitRunner::class)
class LockingScriptSpec : FlatSpec(), Matchers {
    override fun beforeEach() {
        // set-up code
        //

        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()

        // Need to revert the printer to null at the end of this case
        LockingScriptPrinter.printer = null
    }
    init {
        "toString" should "return default string representation if the printer is not set" {
            //println("" + LockingScript( HexUtil.bytes("010203")))
            LockingScript( HexUtil.bytes("010203")).toString() shouldBe """LockingScript("010203")"""
        }

        "toString" should "return the printer's result if the printer is set" {
            LockingScriptPrinter.printer = object : LockingScriptPrinter {
                override fun toString(lockingScript: LockingScript): String {
                    return "LockingScriptByPrinter(${HexUtil.hex(lockingScript.data)})"
                }
            }

            //println("" + LockingScript( HexUtil.bytes("010203")))
            LockingScript( HexUtil.bytes("010203")).toString() shouldBe """LockingScriptByPrinter(010203)"""
        }
    }
}

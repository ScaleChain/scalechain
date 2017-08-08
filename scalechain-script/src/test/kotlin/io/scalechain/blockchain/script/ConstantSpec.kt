package io.scalechain.blockchain.script

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.ScriptEvalException
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.script.ops.*
import org.junit.runner.RunWith

/** Test pseudo word operations in PseudoWord.scala
  *
  */
@RunWith(KTestJUnitRunner::class)
class ConstantSpec : OperationTestTrait() {

  val string75bytes = "123456789012345678901234567890123456789012345678901234567890123456789012345"

  val operations =
    table(
      // column names
      headers("inputValues","operation", "expectedOutputValue"),
      // OP_0 or OP_FALSE(0x00) : An empty array is pushed onto the stack
      stackTest(stack(), Op0(), stack("") ),
      // 1-75(0x01-0x4b) : Push the next N bytes onto the stack, where N is 1 to 75 bytes
      stackTest(stack(), OpPush(1, ScriptValue.valueOf("1")), stack("1")),
      stackTest(stack(), OpPush(75, ScriptValue.valueOf(string75bytes)), stack(string75bytes)),

      // OP_PUSHDATA1(0x4c), OP_PUSHDATA2(0x4d), OP_PUSHDATA4(0x4e) : The next script byte contains N, push the following N bytes onto the stack
      stackTest(stack(), OpPushData(1, ScriptValue.valueOf("1")), stack("1")),
      stackTest(stack(), OpPushData(1, ScriptValue.valueOf(string75bytes)), stack(string75bytes)),

      stackTest(stack(), OpPushData(2, ScriptValue.valueOf("1")), stack("1")),
      stackTest(stack(), OpPushData(2, ScriptValue.valueOf(string75bytes)), stack(string75bytes)),

      stackTest(stack(), OpPushData(4, ScriptValue.valueOf("1")), stack("1")),
      stackTest(stack(), OpPushData(4, ScriptValue.valueOf(string75bytes)), stack(string75bytes)),

      // OP_1NEGATE(0x4f) : Push the value "–1" onto the stack
      stackTest(stack(), Op1Negate(), stack(-1)),
      stackTest(stack("a"), Op1Negate(), stack("a", -1)),

      // OP_1 or OP_TRUE(0x51) : Push the value "1" onto the stack
      stackTest(stack(), Op1(), stack(1)),
      stackTest(stack("a"), Op1(), stack("a", 1)),

      // OpNum; A common data class for OP_2 to OP_16(0x52 to 0x60).
      // For OP_N, push the value "N" onto the stack. E.g., OP_2 pushes "2"
      stackTest(stack(), OpNum(2), stack(2)),
      stackTest(stack(), OpNum(3), stack(3)),
      stackTest(stack(), OpNum(4), stack(4)),
      stackTest(stack(), OpNum(5), stack(5)),
      stackTest(stack(), OpNum(6), stack(6)),
      stackTest(stack(), OpNum(7), stack(7)),
      stackTest(stack(), OpNum(8), stack(8)),
      stackTest(stack(), OpNum(9), stack(9)),
      stackTest(stack(), OpNum(10), stack(10)),
      stackTest(stack(), OpNum(11), stack(11)),
      stackTest(stack(), OpNum(12), stack(12)),
      stackTest(stack(), OpNum(13), stack(13)),
      stackTest(stack(), OpNum(14), stack(14)),
      stackTest(stack(), OpNum(15), stack(15)),
      stackTest(stack(), OpNum(16), stack(16))
    )

    init {
        "operations" should "run and push expected value on the stack." {
            forAll(operations) { inputValues : Array<ScriptValue>, operation : ScriptOp, expectation : Any ->
                verifyOperations(inputValues, listOf(operation), expectation);
            }
        }
    }

}
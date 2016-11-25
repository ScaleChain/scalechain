package io.scalechain.blockchain.script

import io.scalechain.blockchain.{ScriptEvalException, ErrorCode}
import io.scalechain.blockchain.script.ops._
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table

/** Test pseudo word operations in PseudoWord.scala
  *
  */
class ConstantSpec : FlatSpec with BeforeAndAfterEach with OperationTestTrait {

  this: Suite =>

  override fun beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  val string75bytes = "123456789012345678901234567890123456789012345678901234567890123456789012345"

  val operations =
    Table(
      // column names
      ("inputValues","operation", "expectedOutputValue"),
      // OP_0 or OP_FALSE(0x00) : An empty array is pushed onto the stack
      (stack(), Op0(), stack("") ),
      // 1-75(0x01-0x4b) : Push the next N bytes onto the stack, where N is 1 to 75 bytes
      (stack(), OpPush(1, ScriptValue.valueOf("1")), stack("1")),
      (stack(), OpPush(75, ScriptValue.valueOf(string75bytes)), stack(string75bytes)),

      // OP_PUSHDATA1(0x4c), OP_PUSHDATA2(0x4d), OP_PUSHDATA4(0x4e) : The next script byte contains N, push the following N bytes onto the stack
      (stack(), OpPushData(1, ScriptValue.valueOf("1")), stack("1")),
      (stack(), OpPushData(1, ScriptValue.valueOf(string75bytes)), stack(string75bytes)),

      (stack(), OpPushData(2, ScriptValue.valueOf("1")), stack("1")),
      (stack(), OpPushData(2, ScriptValue.valueOf(string75bytes)), stack(string75bytes)),

      (stack(), OpPushData(4, ScriptValue.valueOf("1")), stack("1")),
      (stack(), OpPushData(4, ScriptValue.valueOf(string75bytes)), stack(string75bytes)),

      // OP_1NEGATE(0x4f) : Push the value "–1" onto the stack
      (stack(), Op1Negate(), stack(-1)),
      (stack("a"), Op1Negate(), stack("a", -1)),

      // OP_1 or OP_TRUE(0x51) : Push the value "1" onto the stack
      (stack(), Op1(), stack(1)),
      (stack("a"), Op1(), stack("a", 1)),

      // OpNum; A common data class for OP_2 to OP_16(0x52 to 0x60).
      // For OP_N, push the value "N" onto the stack. E.g., OP_2 pushes "2"
      (stack(), OpNum(2), stack(2)),
      (stack(), OpNum(3), stack(3)),
      (stack(), OpNum(4), stack(4)),
      (stack(), OpNum(5), stack(5)),
      (stack(), OpNum(6), stack(6)),
      (stack(), OpNum(7), stack(7)),
      (stack(), OpNum(8), stack(8)),
      (stack(), OpNum(9), stack(9)),
      (stack(), OpNum(10), stack(10)),
      (stack(), OpNum(11), stack(11)),
      (stack(), OpNum(12), stack(12)),
      (stack(), OpNum(13), stack(13)),
      (stack(), OpNum(14), stack(14)),
      (stack(), OpNum(15), stack(15)),
      (stack(), OpNum(16), stack(16))
    )

  "operations" should "run and push expected value on the stack." in {
    forAll(operations) { ( inputValues : Array<ScriptValue>, operation : ScriptOp, expectation : AnyRef )  =>
      verifyOperations(inputValues, List(operation), expectation);
    }
  }

}
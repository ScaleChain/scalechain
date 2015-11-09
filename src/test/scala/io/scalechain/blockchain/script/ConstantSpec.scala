package io.scalechain.blockchain.script

import io.scalechain.blockchain.{ScriptEvalException, ErrorCode}
import io.scalechain.blockchain.script.ops._
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table

/** Test pseudo word operations in PseudoWord.scala
  *
  */
class ConstantSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {

  this: Suite =>

  override def beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  val string75bytes = "123456789012345678901234567890123456789012345678901234567890123456789012345"

  val operations =
    Table(
      // column names
      ("operation", "expectedScriptValue"),
      // test cases with input value, script operation, output value

      // OP_0 or OP_FALSE(0x00) : An empty array is pushed onto the stack
      (Op0(), ScriptValue.valueOf("")),
      // 1-75(0x01-0x4b) : Push the next N bytes onto the stack, where N is 1 to 75 bytes
      (OpPush(1, ScriptValue.valueOf("1")), ScriptValue.valueOf("1")),
      (OpPush(75, ScriptValue.valueOf(string75bytes)), ScriptValue.valueOf(string75bytes))
    )

  "operations" should "run and push expected value on the stack." in {
    forAll(operations) { (operation: ScriptOp, expectedScriptValue: ScriptValue) =>
      // Arithmetic operations do not use script chunk, so it is ok to pass null for the parsed script.
      val env = new ScriptEnvironment()

      println(s"Testing with operation : ${operation}")

      operation.execute(env)

      val actualOutput = env.stack.pop()

      assert(actualOutput.value sameElements expectedScriptValue.value)
    }
  }
}
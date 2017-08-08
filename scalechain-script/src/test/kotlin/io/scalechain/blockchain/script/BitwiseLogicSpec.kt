package io.scalechain.blockchain.script

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.script.ops.*
import org.junit.runner.RunWith

/** Test bitwise logic operations in BitwiseLogic.scala
  *
  */
@RunWith(KTestJUnitRunner::class)
class BitwiseLogicSpec : OperationTestTrait() {
  val operations =
    table(
      // column names
      headers("inputValues", "operation", "expectedOutputValue"),
      // test cases with input value, script operation, output value
      // The input value is pushed on to the script execution stack from left to right.
      // OP_EQUAL(0x87) : Push TRUE (1) if top two items are exactly equal, push FALSE (0) otherwise
      stackTest(stack("", ""),                OpEqual(),      stack(1L)),
      stackTest(stack("", "a"),               OpEqual(),      stack(0L)),
      stackTest(stack("a", ""),               OpEqual(),      stack(0L)),
      stackTest(stack("Hello", "Hello"),      OpEqual(),      stack(1L)),
      stackTest(stack("Hello", "World"),      OpEqual(),      stack(0L)),

      // OP_EQUALVERIFY(0x88) : Same as OP_EQUAL, but run OP_VERIFY after to halt if not TRUE
      stackTest(stack("", ""),                OpEqualVerify(),  stack()),
      stackTest(stack("", "a"),               OpEqualVerify(),  ErrorCode.InvalidTransaction),
      stackTest(stack("a", ""),               OpEqualVerify(),  ErrorCode.InvalidTransaction),
      stackTest(stack("Hello", "Hello"),      OpEqualVerify(),  stack()),
      stackTest(stack("Hello", "World"),      OpEqualVerify(),  ErrorCode.InvalidTransaction)
    )

    init {
        "operations" should "run and push expected value on the stack." {
            forAll(operations) { inputValues : Array<ScriptValue>, operation : ScriptOp, expectation : Any ->
                verifyOperations(inputValues, listOf(operation), expectation);
            }
        }
    }

  val disabledOperations =
    table(
      // precondition
      headers("operation"),
      row<ScriptOp>(OpInvert()),
      row<ScriptOp>(OpAnd()),
      row<ScriptOp>(OpOr()),
      row<ScriptOp>(OpXor())
    )

    init {
        "disabled operations" should "throw ScriptEvalException with DisabledScriptOperation error code." {
            forAll(disabledOperations) { operation: ScriptOp ->
                verifyOperations(stack(), listOf(operation), ErrorCode.DisabledScriptOperation);
            }
        }
    }
}
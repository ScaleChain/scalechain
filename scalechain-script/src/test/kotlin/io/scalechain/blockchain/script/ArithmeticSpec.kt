package io.scalechain.blockchain.script

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.properties.Row3
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.script.ops.*
import org.junit.runner.RunWith


/** Test arithmetic operations in Arithmetic.scala
 *
 */
@RunWith(KTestJUnitRunner::class)
class ArithmeticSpec : OperationTestTrait() {
  val operations =
    table(
      // column names
      headers("inputValues","operation", "expectedOutputValue"),
      // test cases with input value, script operation, output value
      // The input value is pushed on to the script execution stack from left to right.
      stackTest(stack(-1),      Op1Add(),      stack(0)),
      stackTest(stack(0),       Op1Add(),      stack(1)),
      stackTest(stack(100),     Op1Add(),      stack(101)),
      stackTest(stack(0),       Op1Sub(),      stack(-1)),
      stackTest(stack(1),       Op1Sub(),      stack(0)),
      stackTest(stack(100),     Op1Sub(),      stack(99)),
      stackTest(stack(100),     OpNegate(),    stack(-100)),
      stackTest(stack(-100),    OpNegate(),    stack(100)),
      stackTest(stack(0),       OpNegate(),    stack(0)),
      stackTest(stack(100),     OpAbs(),       stack(100)),
      stackTest(stack(-100),    OpAbs(),       stack(100L)),
      stackTest(stack(0),       OpAbs(),       stack(0)),
      // OP_NOT : If the input is 0 or 1, it is flipped. Otherwise the output will be 0.
      stackTest(stack(0),       OpNot(),       stack(1)),
      stackTest(stack(1),       OpNot(),       stack(0)),
      stackTest(stack(2),       OpNot(),       stack(0)),
      stackTest(stack(-1),      OpNot(),       stack(0)),
      // OP_0NOTEQUAL : Returns 0 if the input is 0. 1 otherwise.
      stackTest(stack(0),       Op0NotEqual(), stack(0)),
      stackTest(stack(-1),      Op0NotEqual(), stack(1)),
      stackTest(stack(1),       Op0NotEqual(), stack(1)),
      // OP_ADD(0x93) : Pop top two items, add them and push result
      stackTest(stack(1,100),  OpAdd(),       stack(101)),
      stackTest(stack(0,100),  OpAdd(),       stack(100)),
      stackTest(stack(-1,100), OpAdd(),       stack(99)),
      stackTest(stack(0, 0),   OpAdd(),       stack(0)),
      // OP_SUB(0x94) : Pop top two items, subtract first from second, push result
      stackTest(stack(100, 1), OpSub(),       stack(99)),
      stackTest(stack(0, 1),   OpSub(),       stack(-1)),
      stackTest(stack(1, 0),   OpSub(),       stack(1)),
      stackTest(stack(0, 0),   OpSub(),       stack(0)),
      // OP_BOOLAND(0x9a) : Boolean AND of top two items
      stackTest(stack(0, 0),   OpBoolAnd(),   stack(0)),
      stackTest(stack(0, 1),   OpBoolAnd(),   stack(0)),
      stackTest(stack(1, 0),   OpBoolAnd(),   stack(0)),
      stackTest(stack(1, 1),   OpBoolAnd(),   stack(1)),
      // OP_BOOLOR(0x9b) : Boolean OR of top two items
      stackTest(stack(0, 0),   OpBoolOr(),    stack(0)),
      stackTest(stack(0, 1),   OpBoolOr(),    stack(1)),
      stackTest(stack(1, 0),   OpBoolOr(),    stack(1)),
      stackTest(stack(1, 1),   OpBoolOr(),    stack(1)),
      // OP_NUMEQUAL(0x9c) : Return TRUE if top two items are equal numbers
      stackTest(stack(0, 0),   OpNumEqual(),  stack(1)),
      stackTest(stack(1, 1),   OpNumEqual(),  stack(1)),
      stackTest(stack(-1,-1),  OpNumEqual(),  stack(1)),
      stackTest(stack(-1, 0),  OpNumEqual(),  stack(0)),
      stackTest(stack(0, -1),  OpNumEqual(),  stack(0)),
      // OP_NUMEQUALVERIFY(0x9d) : Same as OP_NUMEQUAL, but runs OP_VERIFY afterward.
      stackTest(stack(0, 0),   OpNumEqualVerify(),    stack()),
      stackTest(stack(1, 1),   OpNumEqualVerify(),    stack()),
      stackTest(stack(-1,-1),  OpNumEqualVerify(),    stack()),
      stackTest(stack(-1, 0),  OpNumEqualVerify(),    ErrorCode.InvalidTransaction),
      stackTest(stack(0, -1),  OpNumEqualVerify(),    ErrorCode.InvalidTransaction),
      // OP_NUMNOTEQUAL(0x9e) : Return TRUE if top two items are not equal numbers
      stackTest(stack(0, 0),   OpNumNotEqual(),    stack(0)),
      stackTest(stack(1, 1),   OpNumNotEqual(),    stack(0)),
      stackTest(stack(-1,-1),  OpNumNotEqual(),    stack(0)),
      stackTest(stack(-1, 0),  OpNumNotEqual(),    stack(1)),
      stackTest(stack(0, -1),  OpNumNotEqual(),    stack(1)),
      // OP_LESSTHAN(0x9f) : Return TRUE if second item is less than top item
      stackTest(stack(0,  0),  OpLessThan(),    stack(0)),
      stackTest(stack(1, 1),   OpLessThan(),    stack(0)),
      stackTest(stack(-1,-1),  OpLessThan(),    stack(0)),
      stackTest(stack(-1, 0),  OpLessThan(),    stack(1)),
      stackTest(stack(0, -1),  OpLessThan(),    stack(0)),
      stackTest(stack(1,  0),  OpLessThan(),    stack(0)),
      stackTest(stack(0,  1),  OpLessThan(),    stack(1)),
      stackTest(stack(-1, 1),  OpLessThan(),    stack(1)),
      stackTest(stack(1, -1),  OpLessThan(),    stack(0)),
      stackTest(stack(1, 2),   OpLessThan(),    stack(1)),
      stackTest(stack(2, 1),   OpLessThan(),    stack(0)),
      stackTest(stack(-1, -2), OpLessThan(),    stack(0)),
      stackTest(stack(-2, -1), OpLessThan(),    stack(1)),
      // OP_GREATERTHAN(0xa0) : Return TRUE if second item is greater than top item
      stackTest(stack(0,  0),  OpGreaterThan(), stack(0)),
      stackTest(stack(1, 1),   OpGreaterThan(), stack(0)),
      stackTest(stack(-1,-1),  OpGreaterThan(), stack(0)),
      stackTest(stack(-1, 0),  OpGreaterThan(), stack(0)),
      stackTest(stack(0, -1),  OpGreaterThan(), stack(1)),
      stackTest(stack(1,  0),  OpGreaterThan(), stack(1)),
      stackTest(stack(0,  1),  OpGreaterThan(), stack(0)),
      stackTest(stack(-1, 1),  OpGreaterThan(), stack(0)),
      stackTest(stack(1, -1),  OpGreaterThan(), stack(1)),
      stackTest(stack(1, 2),   OpGreaterThan(), stack(0)),
      stackTest(stack(2, 1),   OpGreaterThan(), stack(1)),
      stackTest(stack(-1, -2), OpGreaterThan(), stack(1)),
      stackTest(stack(-2, -1), OpGreaterThan(), stack(0)),
      // OP_LESSTHANOREQUAL(0xa1) : Return TRUE if second item is less than or equal to top item
      stackTest(stack(0,  0),  OpLessThanOrEqual(),    stack(1)),
      stackTest(stack(1, 1),   OpLessThanOrEqual(),    stack(1)),
      stackTest(stack(-1,-1),  OpLessThanOrEqual(),    stack(1)),
      stackTest(stack(-1, 0),  OpLessThanOrEqual(),    stack(1)),
      stackTest(stack(0, -1),  OpLessThanOrEqual(),    stack(0)),
      stackTest(stack(1,  0),  OpLessThanOrEqual(),    stack(0)),
      stackTest(stack(0,  1),  OpLessThanOrEqual(),    stack(1)),
      stackTest(stack(-1, 1),  OpLessThanOrEqual(),    stack(1)),
      stackTest(stack(1, -1),  OpLessThanOrEqual(),    stack(0)),
      stackTest(stack(1, 2),   OpLessThanOrEqual(),    stack(1)),
      stackTest(stack(2, 1),   OpLessThanOrEqual(),    stack(0)),
      stackTest(stack(-1, -2), OpLessThanOrEqual(),    stack(0)),
      stackTest(stack(-2, -1), OpLessThanOrEqual(),    stack(1)),
      // OP_GREATERTHANOREQUAL(0xa2) : Return TRUE if second item is great than or equal to top item
      stackTest(stack(0,  0),  OpGreaterThanOrEqual(),    stack(1)),
      stackTest(stack(1, 1),   OpGreaterThanOrEqual(),    stack(1)),
      stackTest(stack(-1,-1),  OpGreaterThanOrEqual(),    stack(1)),
      stackTest(stack(-1, 0),  OpGreaterThanOrEqual(),    stack(0)),
      stackTest(stack(0, -1),  OpGreaterThanOrEqual(),    stack(1)),
      stackTest(stack(1,  0),  OpGreaterThanOrEqual(),    stack(1)),
      stackTest(stack(0,  1),  OpGreaterThanOrEqual(),    stack(0)),
      stackTest(stack(-1, 1),  OpGreaterThanOrEqual(),    stack(0)),
      stackTest(stack(1, -1),  OpGreaterThanOrEqual(),    stack(1)),
      stackTest(stack(1, 2),   OpGreaterThanOrEqual(),    stack(0)),
      stackTest(stack(2, 1),   OpGreaterThanOrEqual(),    stack(1)),
      stackTest(stack(-1, -2), OpGreaterThanOrEqual(),    stack(1)),
      stackTest(stack(-2, -1), OpGreaterThanOrEqual(),    stack(0)),
      // OP_MIN(0xa3) : Return the smaller of the two top items
      stackTest(stack(0,  0),  OpMin(),    stack(0)),
      stackTest(stack(1, 1),   OpMin(),    stack(1)),
      stackTest(stack(-1,-1),  OpMin(),    stack(-1)),
      stackTest(stack(-1, 0),  OpMin(),    stack(-1)),
      stackTest(stack(0, -1),  OpMin(),    stack(-1)),
      stackTest(stack(1,  0),  OpMin(),    stack(0)),
      stackTest(stack(0,  1),  OpMin(),    stack(0)),
      stackTest(stack(-1, 1),  OpMin(),    stack(-1)),
      stackTest(stack(1, -1),  OpMin(),    stack(-1)),
      stackTest(stack(1, 2),   OpMin(),    stack(1)),
      stackTest(stack(2, 1),   OpMin(),    stack(1)),
      stackTest(stack(-1, -2), OpMin(),    stack(-2)),
      stackTest(stack(-2, -1), OpMin(),    stack(-2)),
      // OP_MAX(0xa4) : Return the larger of the two top items
      stackTest(stack(0,  0),  OpMax(),    stack(0)),
      stackTest(stack(1, 1),   OpMax(),    stack(1)),
      stackTest(stack(-1,-1),  OpMax(),    stack(-1)),
      stackTest(stack(-1, 0),  OpMax(),    stack(0)),
      stackTest(stack(0, -1),  OpMax(),    stack(0)),
      stackTest(stack(1,  0),  OpMax(),    stack(1)),
      stackTest(stack(0,  1),  OpMax(),    stack(1)),
      stackTest(stack(-1, 1),  OpMax(),    stack(1)),
      stackTest(stack(1, -1),  OpMax(),    stack(1)),
      stackTest(stack(1, 2),   OpMax(),    stack(2)),
      stackTest(stack(2, 1),   OpMax(),    stack(2)),
      stackTest(stack(-1, -2), OpMax(),    stack(-1)),
      stackTest(stack(-2, -1), OpMax(),    stack(-1)),
      // OP_WITHIN(0xa5) : Return TRUE if the third item is between the second item (or equa) and first item
      stackTest(stack(-2, -1, 1), OpWithin(), stack(0)),
      stackTest(stack(-1, -1, 1), OpWithin(), stack(1)),
      stackTest(stack( 0, -1, 1), OpWithin(), stack(1)),
      stackTest(stack( 1, -1, 1), OpWithin(), stack(0)),
      stackTest(stack( 2, -1, 1), OpWithin(), stack(0))
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
      row<ScriptOp>(Op2Mul()),
      row<ScriptOp>(Op2Div()),
      row<ScriptOp>(OpMul()),
      row<ScriptOp>(OpDiv()),
      row<ScriptOp>(OpMod()),
      row<ScriptOp>(OpLShift()),
      row<ScriptOp>(OpRShift())
    )

    init {
        "operations" should "work even though we have overflow on the output." {
            // TODO : Implement it later.
        }

        "disabled operations" should "throw ScriptEvalException with DisabledScriptOperation error code." {
            forAll(disabledOperations) { operation : ScriptOp ->
                verifyOperations(stack(), listOf(operation), ErrorCode.DisabledScriptOperation);
            }
        }

    }

}
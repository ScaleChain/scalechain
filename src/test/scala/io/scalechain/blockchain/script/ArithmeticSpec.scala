package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.ops._
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._

/** Test arithmetic operations in Arithmetic.scala
 *
 */
class ArithmeticSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {

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
//  (Array(1L, 100L), OpAdd(), 101L),

  val operations =
    Table(
      // column names
      ("inputValues","operation", "expectedOutputValue"),
      // test cases with input value, script operation, output value
      // The input value is pushed on to the script execution stack from left to right.
      (Array(-1L),      Op1Add(),      Right(0L)),
      (Array(0L),       Op1Add(),      Right(1L)),
      (Array(100L),     Op1Add(),      Right(101L)),
      (Array(0L),       Op1Sub(),      Right(-1L)),
      (Array(1L),       Op1Sub(),      Right(0L)),
      (Array(100L),     Op1Sub(),      Right(99L)),
      (Array(100L),     OpNegate(),    Right(-100L )),
      (Array(-100L),    OpNegate(),    Right(100L )),
      (Array(0L),       OpNegate(),    Right(0L )),
      (Array(100L),     OpAbs(),       Right(100L)),
      (Array(-100L),    OpAbs(),       Right(100L)),
      (Array(0L),       OpAbs(),       Right(0L)),
      // OP_NOT : If the input is 0 or 1, it is flipped. Otherwise the output will be 0.
      (Array(0L),       OpNot(),       Right(1L)),
      (Array(1L),       OpNot(),       Right(0L)),
      (Array(2L),       OpNot(),       Right(0L)),
      (Array(-1L),      OpNot(),       Right(0L)),
      // OP_0NOTEQUAL : Returns 0 if the input is 0. 1 otherwise.
      (Array(0L),       Op0NotEqual(), Right(0L)),
      (Array(-1L),      Op0NotEqual(), Right(1L)),
      (Array(1L),       Op0NotEqual(), Right(1L)),
      // OP_ADD(0x93) : Pop top two items, add them and push result
      (Array(1L,100L),  OpAdd(),       Right(101L)),
      (Array(0L,100L),  OpAdd(),       Right(100L)),
      (Array(-1L,100L), OpAdd(),       Right(99L)),
      (Array(0L, 0L),   OpAdd(),       Right(0L)),
      // OP_SUB(0x94) : Pop top two items, subtract first from second, push result
      (Array(100L, 1L), OpSub(),       Right(99L)),
      (Array(0L, 1L),   OpSub(),       Right(-1L)),
      (Array(1L, 0L),   OpSub(),       Right(1L)),
      (Array(0L, 0L),   OpSub(),       Right(0L)),
      // OP_BOOLAND(0x9a) : Boolean AND of top two items
      (Array(0L, 0L),   OpBoolAnd(),   Right(0L)),
      (Array(0L, 1L),   OpBoolAnd(),   Right(0L)),
      (Array(1L, 0L),   OpBoolAnd(),   Right(0L)),
      (Array(1L, 1L),   OpBoolAnd(),   Right(1L)),
      // OP_BOOLOR(0x9b) : Boolean OR of top two items
      (Array(0L, 0L),   OpBoolOr(),    Right(0L)),
      (Array(0L, 1L),   OpBoolOr(),    Right(1L)),
      (Array(1L, 0L),   OpBoolOr(),    Right(1L)),
      (Array(1L, 1L),   OpBoolOr(),    Right(1L)),
      // OP_NUMEQUAL(0x9c) : Return TRUE if top two items are equal numbers
      (Array(0L, 0L),   OpNumEqual(),  Right(1L)),
      (Array(1L, 1L),   OpNumEqual(),  Right(1L)),
      (Array(-1L,-1L),  OpNumEqual(),  Right(1L)),
      (Array(-1L, 0L),  OpNumEqual(),  Right(0L)),
      (Array(0L, -1L),  OpNumEqual(),  Right(0L)),
      // OP_NUMEQUALVERIFY(0x9d) : Same as OP_NUMEQUAL, but runs OP_VERIFY afterward.
      (Array(0L, 0L),   OpNumEqualVerify(),    Right(1L)),
      (Array(1L, 1L),   OpNumEqualVerify(),    Right(1L)),
      (Array(-1L,-1L),  OpNumEqualVerify(),    Right(1L)),
      (Array(-1L, 0L),  OpNumEqualVerify(),    Left(ErrorCode.InvalidTransaction)),
      (Array(0L, -1L),  OpNumEqualVerify(),    Left(ErrorCode.InvalidTransaction)),
      // OP_NUMNOTEQUAL(0x9e) : Return TRUE if top two items are not equal numbers
      (Array(0L, 0L),   OpNumNotEqual(),    Right(0L)),
      (Array(1L, 1L),   OpNumNotEqual(),    Right(0L)),
      (Array(-1L,-1L),  OpNumNotEqual(),    Right(0L)),
      (Array(-1L, 0L),  OpNumNotEqual(),    Right(1L)),
      (Array(0L, -1L),  OpNumNotEqual(),    Right(1L)),
      // OP_LESSTHAN(0x9f) : Return TRUE if second item is less than top item
      (Array(0L,  0L),  OpLessThan(),    Right(0L)),
      (Array(1L, 1L),   OpLessThan(),    Right(0L)),
      (Array(-1L,-1L),  OpLessThan(),    Right(0L)),
      (Array(-1L, 0L),  OpLessThan(),    Right(1L)),
      (Array(0L, -1L),  OpLessThan(),    Right(0L)),
      (Array(1L,  0L),  OpLessThan(),    Right(0L)),
      (Array(0L,  1L),  OpLessThan(),    Right(1L)),
      (Array(-1L, 1L),  OpLessThan(),    Right(1L)),
      (Array(1L, -1L),  OpLessThan(),    Right(0L)),
      (Array(1L, 2L),   OpLessThan(),    Right(1L)),
      (Array(2L, 1L),   OpLessThan(),    Right(0L)),
      (Array(-1L, -2L), OpLessThan(),    Right(0L)),
      (Array(-2L, -1L), OpLessThan(),    Right(1L)),
      // OP_GREATERTHAN(0xa0) : Return TRUE if second item is greater than top item
      (Array(0L,  0L),  OpGreaterThan(), Right(0L)),
      (Array(1L, 1L),   OpGreaterThan(), Right(0L)),
      (Array(-1L,-1L),  OpGreaterThan(), Right(0L)),
      (Array(-1L, 0L),  OpGreaterThan(), Right(0L)),
      (Array(0L, -1L),  OpGreaterThan(), Right(1L)),
      (Array(1L,  0L),  OpGreaterThan(), Right(1L)),
      (Array(0L,  1L),  OpGreaterThan(), Right(0L)),
      (Array(-1L, 1L),  OpGreaterThan(), Right(0L)),
      (Array(1L, -1L),  OpGreaterThan(), Right(1L)),
      (Array(1L, 2L),   OpGreaterThan(), Right(0L)),
      (Array(2L, 1L),   OpGreaterThan(), Right(1L)),
      (Array(-1L, -2L), OpGreaterThan(), Right(1L)),
      (Array(-2L, -1L), OpGreaterThan(), Right(0L)),
      // OP_LESSTHANOREQUAL(0xa1) : Return TRUE if second item is less than or equal to top item
      (Array(0L,  0L),  OpLessThanOrEqual(),    Right(1L)),
      (Array(1L, 1L),   OpLessThanOrEqual(),    Right(1L)),
      (Array(-1L,-1L),  OpLessThanOrEqual(),    Right(1L)),
      (Array(-1L, 0L),  OpLessThanOrEqual(),    Right(1L)),
      (Array(0L, -1L),  OpLessThanOrEqual(),    Right(0L)),
      (Array(1L,  0L),  OpLessThanOrEqual(),    Right(0L)),
      (Array(0L,  1L),  OpLessThanOrEqual(),    Right(1L)),
      (Array(-1L, 1L),  OpLessThanOrEqual(),    Right(1L)),
      (Array(1L, -1L),  OpLessThanOrEqual(),    Right(0L)),
      (Array(1L, 2L),   OpLessThanOrEqual(),    Right(1L)),
      (Array(2L, 1L),   OpLessThanOrEqual(),    Right(0L)),
      (Array(-1L, -2L), OpLessThanOrEqual(),    Right(0L)),
      (Array(-2L, -1L), OpLessThanOrEqual(),    Right(1L)),
      // OP_GREATERTHANOREQUAL(0xa2) : Return TRUE if second item is great than or equal to top item
      (Array(0L,  0L),  OpGreaterThanOrEqual(),    Right(1L)),
      (Array(1L, 1L),   OpGreaterThanOrEqual(),    Right(1L)),
      (Array(-1L,-1L),  OpGreaterThanOrEqual(),    Right(1L)),
      (Array(-1L, 0L),  OpGreaterThanOrEqual(),    Right(0L)),
      (Array(0L, -1L),  OpGreaterThanOrEqual(),    Right(1L)),
      (Array(1L,  0L),  OpGreaterThanOrEqual(),    Right(1L)),
      (Array(0L,  1L),  OpGreaterThanOrEqual(),    Right(0L)),
      (Array(-1L, 1L),  OpGreaterThanOrEqual(),    Right(0L)),
      (Array(1L, -1L),  OpGreaterThanOrEqual(),    Right(1L)),
      (Array(1L, 2L),   OpGreaterThanOrEqual(),    Right(0L)),
      (Array(2L, 1L),   OpGreaterThanOrEqual(),    Right(1L)),
      (Array(-1L, -2L), OpGreaterThanOrEqual(),    Right(1L)),
      (Array(-2L, -1L), OpGreaterThanOrEqual(),    Right(0L)),
      // OP_MIN(0xa3) : Return the smaller of the two top items
      (Array(0L,  0L),  OpMin(),    Right(0L)),
      (Array(1L, 1L),   OpMin(),    Right(1L)),
      (Array(-1L,-1L),  OpMin(),    Right(-1L)),
      (Array(-1L, 0L),  OpMin(),    Right(-1L)),
      (Array(0L, -1L),  OpMin(),    Right(-1L)),
      (Array(1L,  0L),  OpMin(),    Right(0L)),
      (Array(0L,  1L),  OpMin(),    Right(0L)),
      (Array(-1L, 1L),  OpMin(),    Right(-1L)),
      (Array(1L, -1L),  OpMin(),    Right(-1L)),
      (Array(1L, 2L),   OpMin(),    Right(1L)),
      (Array(2L, 1L),   OpMin(),    Right(1L)),
      (Array(-1L, -2L), OpMin(),    Right(-2L)),
      (Array(-2L, -1L), OpMin(),    Right(-2L)),
       // OP_MAX(0xa4) : Return the larger of the two top items
      (Array(0L,  0L),  OpMax(),    Right(0L)),
      (Array(1L, 1L),   OpMax(),    Right(1L)),
      (Array(-1L,-1L),  OpMax(),    Right(-1L)),
      (Array(-1L, 0L),  OpMax(),    Right(0L)),
      (Array(0L, -1L),  OpMax(),    Right(0L)),
      (Array(1L,  0L),  OpMax(),    Right(1L)),
      (Array(0L,  1L),  OpMax(),    Right(1L)),
      (Array(-1L, 1L),  OpMax(),    Right(1L)),
      (Array(1L, -1L),  OpMax(),    Right(1L)),
      (Array(1L, 2L),   OpMax(),    Right(2L)),
      (Array(2L, 1L),   OpMax(),    Right(2L)),
      (Array(-1L, -2L), OpMax(),    Right(-1L)),
      (Array(-2L, -1L), OpMax(),    Right(-1L)),
      // OP_WITHIN(0xa5) : Return TRUE if the third item is between the second item (or equal) and first item
      (Array(-2L, -1L, 1L), OpWithin(), Right(0L)),
      (Array(-1L, -1L, 1L), OpWithin(), Right(1L)),
      (Array( 0L, -1L, 1L), OpWithin(), Right(1L)),
      (Array( 1L, -1L, 1L), OpWithin(), Right(0L)),
      (Array( 2L, -1L, 1L), OpWithin(), Right(0L))
    )

  "operations" should "run and push expected value on the stack." in {
    forAll(operations) { ( inputValues : Array[Long], operation : ScriptOp, expectation : Either[ErrorCode,Long] )  =>
      // Arithmetic operations do not use script chunk, so it is ok to pass null for the parsed script.
      val env = new ScriptEnvironment()

      for ( input : Long <-inputValues) {
        env.stack.pushInt( BigInteger.valueOf(input) )
      }

      println (s"Testing with input ${inputValues.mkString(",")}, operation : ${operation}" )

      expectation match {
        case Left(expectedErrorCode) => {
          val thrown = the[ScriptEvalException] thrownBy {
            operation.execute(env)
          }
          thrown.code should equal(expectedErrorCode)
          println (s"thrown expcetion : ${thrown}" )
        }

        case Right(expectedOutputValue) => {
          operation.execute(env)

          val actualOutput = env.stack.popInt()

          println (s"actual output : ${actualOutput.longValue()}" )

          actualOutput.longValue() should be (expectedOutputValue)
        }
      }
    }
  }


  val disabledOperations =
    Table(
      // precondition
      ("operation"),
      (Op2Mul()),
      (Op2Div()),
      (OpMul()),
      (OpDiv()),
      (OpMod()),
      (OpLShift()),
      (OpRShift())
    )

  "operations" should "work even though we have overflow on the output." in {
    // TODO : Implement it later.
  }

  "disabled operations" should "throw ScriptEvalException with DisabledScriptOperation error code." in {
    forAll(disabledOperations) { (operation : ScriptOp) =>
      val env = new ScriptEnvironment()

      val thrown = the[ScriptEvalException] thrownBy {
        operation.execute(env)
      }

      thrown.code should equal (ErrorCode.DisabledScriptOperation)
    }
  }

}
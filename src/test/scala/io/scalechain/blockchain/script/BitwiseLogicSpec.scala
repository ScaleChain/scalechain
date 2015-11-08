package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.ops._
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table

/** Test bitwise logic operations in BitwiseLogic.scala
  *
  */
class BitwiseLogicSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {

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

  val operations =
    Table(
      // column names
      ("inputValues","operation", "expectedOutputValue"),
      // test cases with input value, script operation, output value
      // The input value is pushed on to the script execution stack from left to right.
      // OP_EQUAL(0x87) : Push TRUE (1) if top two items are exactly equal, push FALSE (0) otherwise
      (Array( ScriptValue.valueOf(""), ScriptValue.valueOf("")),                OpEqual(),      Right(1L)),
      (Array( ScriptValue.valueOf(""), ScriptValue.valueOf("a")),               OpEqual(),      Right(0L)),
      (Array( ScriptValue.valueOf("a"), ScriptValue.valueOf("")),               OpEqual(),      Right(0L)),
      (Array( ScriptValue.valueOf("Hello"), ScriptValue.valueOf("Hello")),      OpEqual(),      Right(1L)),
      (Array( ScriptValue.valueOf("Hello"), ScriptValue.valueOf("World")),      OpEqual(),      Right(0L)),

      // OP_EQUALVERIFY(0x88) : Same as OP_EQUAL, but run OP_VERIFY after to halt if not TRUE
      (Array( ScriptValue.valueOf(""), ScriptValue.valueOf("")),                OpEqualVerify(),  Right(1L)),
      (Array( ScriptValue.valueOf(""), ScriptValue.valueOf("a")),               OpEqualVerify(),  Left(ErrorCode.InvalidTransaction)),
      (Array( ScriptValue.valueOf("a"), ScriptValue.valueOf("")),               OpEqualVerify(),  Left(ErrorCode.InvalidTransaction)),
      (Array( ScriptValue.valueOf("Hello"), ScriptValue.valueOf("Hello")),      OpEqualVerify(),  Right(1L)),
      (Array( ScriptValue.valueOf("Hello"), ScriptValue.valueOf("World")),      OpEqualVerify(),  Left(ErrorCode.InvalidTransaction))

    )

  "operations" should "run and push expected value on the stack." in {
    forAll(operations) { ( inputValues : Array[ScriptValue], operation : ScriptOp, expectation : Either[ErrorCode,Long] )  =>
      // Arithmetic operations do not use script chunk, so it is ok to pass null for the parsed script.
      val env = new ScriptEnvironment(null)

      for ( input : ScriptValue <-inputValues) {
        env.stack.push( input )
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
      (OpInvert()),
      (OpAnd()),
      (OpOr()),
      (OpXor())
    )

  "disabled operations" should "throw ScriptEvalException with DisabledScriptOperation error code." in {
    forAll(disabledOperations) { (operation: ScriptOp) =>
      val env = new ScriptEnvironment(null)

      val thrown = the[ScriptEvalException] thrownBy {
        operation.execute(env)
      }

      thrown.code should equal(ErrorCode.DisabledScriptOperation)
    }
  }
}
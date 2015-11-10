package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.ops._
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._


/** Test splice operations in Splice.scala
  *
  */
class SpliceSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {

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

  val EMPTY_ARRAY = new Array[ScriptValue](0)
/*
  val operations =
    Table(
      // column names
      ("inputValues","operation", "expectedOutputValue"),
      // test cases with input value, script operation, output value
      // The input value is pushed on to the script execution stack from left to right.
      (Array(""),  OpSize(), Right(0L)),
      (Array("a"),  OpSize(), Right(1L)),
      (Array("ab"),  OpSize(), Right(2L)),
      (Array("a",""),  OpSize(), Right(1L)),
      (Array("a","b"),  OpSize(), Right(1L)),
      (Array("a","bc"),  OpSize(), Right(1L)),
      (EMPTY_ARRAY,     OpSize(), Left(ErrorCode.InvalidScriptOperation))
    )

  "operations" should "run and push expected value on the stack." in {
    forAll(operations) { ( inputValues : Array[ScriptValue], operation : ScriptOp, expectation : Either[ErrorCode,Array[ScriptValue]] )  =>
      // Arithmetic operations do not use script chunk, so it is ok to pass null for the parsed script.
      val env = new ScriptEnvironment()

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
*/
}

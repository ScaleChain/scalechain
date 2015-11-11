package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.blockchain.{ScriptEvalException, ErrorCode}
import io.scalechain.blockchain.script.ops.ScriptOp
import io.scalechain.blockchain.util.Utils
import org.scalatest.{Suite, ShouldMatchers}

import scala.Long
import scala.collection.mutable.ArrayBuffer

/**
 * Created by kangmo on 11/10/15.
 */
class InvalidStackValueException extends Exception
class InvalidExpectationTypeException extends Exception

trait OperationTestTrait extends ShouldMatchers {
  this: Suite =>

  /** Verify a stack with expected values.
   *
   * @param subject The context of the stack. Just for debugging purpose.
   * @param actualStack The actual script stack.
   * @param expectedValues A list of expected values.
   */
  protected def verifyStack(subject : String, actualStack:ScriptStack, expectedValues : Array[ScriptValue]) : Unit = {
    println ("We are comparing the stack. subject : " + subject)
    //println ("stack : " + actualStack)

    for (i <- 0 until expectedValues.length) {
      val actualOutput = actualStack(expectedValues.length-1-i)
      val expectedOutput = expectedValues(i)

      println (s"expected output : ${expectedOutput}" )
      println (s"actual output : ${actualOutput}" )

      actualOutput should be (expectedOutput)
    }
  }

  /** Verify an operation without alt stack inputs and outputs.
   *
   * @param inputs initial values on the main stack.
   * @param operation the operation to verify
   * @param expectation final values on the main stack or an error code.
   */
  protected def verifyOperation( inputs : Array[ScriptValue],
                                 operation : ScriptOp,
                                 expectation : AnyRef
                                 ) : Unit = {
    verifyOperation(inputs, null, operation, expectation, null);
  }

  /** Push an array of values on to a stack.
   *
   * @param stack The stack where values are pushed.
   * @param values The array of ScriptValues(s) to push.
   */
  protected def pushValues(stack : ScriptStack, values : Array[ScriptValue]): Unit = {
    for ( value : ScriptValue <-values) {
      stack.push( value )
    }
  }

  /** Verify an operation with alt stack inputs and outputs.
   *
   * @param mainStackInputs initial values on the main stack.
   * @param altStackInputs initial values on the alt stack.
   * @param operation the operation to verify
   * @param expectation final values on the main stack or an error code.
   * @param altStackOutputs final values on the alt stack.
   */
  protected def verifyOperation( mainStackInputs : Array[ScriptValue],
                                 altStackInputs : Array[ScriptValue],
                                 operation : ScriptOp,
                                 expectation : AnyRef,
                                 altStackOutputs : Array[ScriptValue]
                               ) : Unit = {
    // Arithmetic operations do not use script chunk, so it is ok to pass null for the parsed script.
    val env = new ScriptEnvironment()

    pushValues(env.stack, mainStackInputs)

    if (altStackInputs != null)
      pushValues(env.altStack, altStackInputs)

    println (s"Testing with input ${mainStackInputs.mkString(",")}, operation : ${operation}" )

    expectation match {
      case errorCode : ErrorCode => {
        val thrown = the[ScriptEvalException] thrownBy {
          operation.execute(env)
        }
        thrown.code should be (errorCode)
      }
      case expectedOutputValues : Array[ScriptValue] => {
        operation.execute(env)

        println ("expected values (main) :" + expectedOutputValues.mkString(","))
        if (altStackOutputs != null)
        println ("expected values (alt) :" + altStackOutputs.mkString(","))


        verifyStack("actual main stack", env.stack, expectedOutputValues)

        if (altStackOutputs != null)
          verifyStack("actual alt stack",  env.altStack, altStackOutputs)
      }
      case _ => throw new InvalidExpectationTypeException()
    }
  }

  /** Create an array of ScriptValue(s) from the items argument.
   *
   * @param items The list of arguments which is converted to an array of ScriptValue(s)
   * @return The array of ScriptValues(s)
   */
  def stack(items : Any* ) : Array[ScriptValue] = {
  //  val buffer = new ArrayBuffer[ScriptValue]()
    val scriptValues = for (item : Any <- items) yield {
      item match {
        case value : Array[Byte] => ScriptValue.valueOf(value)
        case value : Int => ScriptValue.valueOf(value)
        case value : Long => ScriptValue.valueOf(value)
        case value : String => ScriptValue.valueOf(value)
        case _ => throw new InvalidStackValueException()
      }
    }
    scriptValues.toArray
  }
}



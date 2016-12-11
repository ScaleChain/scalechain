package io.scalechain.blockchain.script

import io.kotlintest.matchers.Matchers
import io.kotlintest.properties.Row3
import io.kotlintest.properties.Row5
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.proto.Script
import io.scalechain.blockchain.ScriptEvalException
import io.scalechain.blockchain.ScriptParseException
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.script.ops.ScriptOp

/**
 * Created by kangmo on 11/10/15.
 */
class InvalidStackValueException : Exception()
class InvalidExpectationTypeException : Exception()

abstract class OperationTestTrait : FlatSpec(), Matchers {

  fun stackTest(inputValues : Array<ScriptValue>, operation : ScriptOp, expectation : Any) =
      Row3<Array<ScriptValue>, ScriptOp, Any>(inputValues, operation, expectation)

  fun stackTest(mainStackInputs : Array<ScriptValue>,
                altStackInputs : Array<ScriptValue>?,
                operation : ScriptOp,
                expectation : Any,
                altStackOutputs : Array<ScriptValue>?) =
      Row5<Array<ScriptValue>, Array<ScriptValue>?, ScriptOp, Any, Array<ScriptValue>?>(
          mainStackInputs,
          altStackInputs,
          operation,
          expectation,
          altStackOutputs
      )

  /** Verify a stack with expected values.
   *
   * @param subject The context of the stack. Just for debugging purpose.
   * @param actualStack The actual script stack.
   * @param expectedValues A list of expected values.
   */
  fun verifyStack(subject : String, actualStack:ScriptStack, expectedValues : Array<ScriptValue>) : Unit {
    //println ("We are comparing the stack. subject : " + subject)
    //println ("stack : " + actualStack)

    for (i in 0 until expectedValues.size) {
      val actualOutput = actualStack.get(expectedValues.size-1-i)
      val expectedOutput = expectedValues[i]

      //println (s"expected output : ${expectedOutput}" )
      //println (s"actual output : ${actualOutput}" )

      actualOutput shouldBe (expectedOutput)
    }
  }

  /** Verify an operation without alt stack inputs and outputs.
   *
   * @param inputs initial values on the main stack.
   * @param operations the list of operation to verify
   * @param expectation final values on the main stack or an error code.
   */
  fun verifyOperations( inputs : Array<ScriptValue>,
                                 operations : List<ScriptOp>,
                                 expectation : Any,
                                 serializeAndExecute : Boolean = false
                                 ) : Unit {
    verifyOperationsWithAltStack(inputs, null, operations, expectation, null, serializeAndExecute);
  }

  /** Push an array of values on to a stack.
   *
   * @param stack The stack where values are pushed.
   * @param values The array of ScriptValues(s) to push.
   */
  fun pushValues(stack : ScriptStack, values : Array<ScriptValue>): Unit {
    for ( value : ScriptValue in values) {
      stack.push( value )
    }
  }

  /** Verify an operation with alt stack inputs and outputs.
   *
   * @param mainStackInputs initial values on the main stack.
   * @param altStackInputs initial values on the alt stack.
   * @param operations list of operations to verify
   * @param expectation final values on the main stack or an error code.
   * @param altStackOutputs final values on the alt stack.
   */
  fun verifyOperationsWithAltStack(
                  mainStackInputs : Array<ScriptValue>,
                  altStackInputs : Array<ScriptValue>?,
                  operations : List<ScriptOp>,
                  expectation : Any,
                  altStackOutputs : Array<ScriptValue>?,
                  serializeAndExecute : Boolean = false
                ) : Unit {
    //println (s"Testing with input ${mainStackInputs.mkString(",")}" )

    // Arithmetic operations do not use script chunk, so it is ok to pass null for the parsed script.
    val env = ScriptEnvironment()


    pushValues(env.stack, mainStackInputs)

    if (altStackInputs != null)
      pushValues(env.altStack, altStackInputs)

    fun executeOps() : Unit {

      val operationsToExecute =
        if (serializeAndExecute) {
          // Serialze and parse the serialized byte array to get the pseudo operations such as OpCond,
          // which is generated from OP_IF/OP_NOTIF, OP_ELSE, OP_ENDIF during parsing.
          val serializedOperations = ScriptSerializer.serialize(operations)
          ScriptParser.parse(object : Script { override val data = serializedOperations } ).operations
        } else {
          operations
        }

      for (op : ScriptOp in operationsToExecute) {
        //println (s"Executing operation : ${op}" )
        op.execute(env)
      }
    }

    when {
      expectation is ScriptParseException -> {
        val thrown = shouldThrow<ScriptParseException> {
          executeOps()
        }
        thrown.code shouldBe (expectation.code)
      }

      // BUGBUG :  Get rid of this pattern, change all test case to use the above pattern.
      expectation is ErrorCode -> {
        val thrown = shouldThrow<ScriptEvalException> {
          executeOps()
        }
        thrown.code shouldBe (expectation)
      }

      expectation is Array<*> -> {
        if (expectation.all{ it is ScriptValue}) {
          val expectedOutputValues = expectation.map{ it as ScriptValue }.toTypedArray()
          executeOps()

          //println ("expected values (main) :" + expectedOutputValues.mkString(","))
          if (altStackOutputs != null)
          //println ("expected values (alt) :" + altStackOutputs.mkString(","))


            verifyStack("actual main stack", env.stack, expectedOutputValues)

          if (altStackOutputs != null)
            verifyStack("actual alt stack",  env.altStack, altStackOutputs)
        }
      }

      else -> {
        throw InvalidExpectationTypeException()
      }
    }
  }

  /** Create an array of ScriptValue(s) from the items argument.
   *
   * @param items The list of arguments which is converted to an array of ScriptValue(s)
   * @return The array of ScriptValues(s)
   */
  fun stack(vararg items : Any ) : Array<ScriptValue> {
  //  val buffer = ArrayBuffer<ScriptValue>()
    val scriptValues = items.map { item ->
      when {
        item is ByteArray -> ScriptValue.valueOf(item)
        item is Int -> ScriptValue.valueOf(item.toLong())
        item is Long -> ScriptValue.valueOf(item)
        item is String -> ScriptValue.valueOf(item)
        else -> throw InvalidStackValueException()
      }
    }
    return scriptValues.toTypedArray()
  }

  /** Create ScriptOpList which has a list of ScriptOp(s)
   *
   * @param operations The list of operations
   * @return The ScriptOpList
   */
  fun ops(vararg operations : ScriptOp) : ScriptOpList {
    return ScriptOpList( operations.toList() )
  }
}



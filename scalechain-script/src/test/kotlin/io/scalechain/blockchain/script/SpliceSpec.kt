package io.scalechain.blockchain.script

import io.kotlintest.KTestJUnitRunner
import java.math.BigInteger

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ScriptEvalException
import io.scalechain.blockchain.script.ops.*
import org.junit.runner.RunWith

/** Test splice operations in Splice.scala
  *
  */
@RunWith(KTestJUnitRunner::class)
class SpliceSpec : OperationTestTrait() {
  val operations =
    table(
      // column names
      headers("inputValues","operation", "expectedOutputValue"),
      // test cases with input value, script operation, output value
      // The input value is pushed on to the script execution stack from left to right.

      // OP_SIZE(0x82) : Calculate string length of top item and push the result
      // Before : in
      // After  : in size
      stackTest(stack(""),  OpSize(), stack("", 0)),
      stackTest(stack("a"),  OpSize(), stack("a", 1)),
      stackTest(stack("ab"),  OpSize(), stack("ab", 2)),
      stackTest(stack("a",""),  OpSize(), stack("a","", 0)),
      stackTest(stack("a","b"),  OpSize(), stack("a","b", 1)),
      stackTest(stack("a","bc"),  OpSize(), stack("a","bc", 2)),
      stackTest(stack(),           OpSize(), ErrorCode.NotEnoughInput)
    )

    init {
        "operations" should "run and push expected value on the stack." {
            forAll(operations) { inputValues : Array<ScriptValue>, operation : ScriptOp, expectation : Any ->
                verifyOperations(inputValues, listOf(operation), expectation);
            }
        }
    }
}

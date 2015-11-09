package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.blockchain.script.ScriptStack
import io.scalechain.blockchain.util.Utils
import io.scalechain.blockchain.{ScriptEvalException, ErrorCode}
import io.scalechain.blockchain.script.ops._
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table

/** Test stack operations in Stack.scala
  *
  */
class StackSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {

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

  val EMPTY_ARRAY = new Array[Long](0)


  // OP_TOALTSTACK(0x6b) : Pop top item from stack and push to alternative stack
  // Before : x1
  // After  : (alt)x1
  //
  // OP_FROMALTSTACK(0x6c) : Pop top item from alternative stack and push to stack
  // Before : (alt)x1
  // After  : x1
  //
  val operations =
    Table(
      // column names
      ("inputStack","operation", "outputStack"),
      // test cases with input stack values, script operation, output stack values
      // The input value is pushed on to the script execution stack from left to right.

      // OP_IFDUP(0x73) : Duplicate the top item in the stack if it is not 0
      // Before : x
      // After  : x     ( if x == 0 )
      // After  : x x   ( if x != 0 )
      (Array(0L),                   OpIfDup(),  Right(Array(0L))),
      (Array(1L),                   OpIfDup(),  Right(Array(1L, 1L))),
      (Array(2L),                   OpIfDup(),  Right(Array(2L, 2L))),
      (EMPTY_ARRAY,                 OpIfDup(),  Left(ErrorCode.InvalidStackOperation)),

      // OP_DEPTH(0x74) : Count the items on the stack and push the resulting count
      // Before :
      // After  : <stack size>
      (EMPTY_ARRAY,                 OpDepth(),  Right(Array(0L))),
      (Array(-1L),                  OpDepth(),  Right(Array(1L))),
      (Array(0L),                   OpDepth(),  Right(Array(1L))),
      (Array(1L),                   OpDepth(),  Right(Array(1L))),
      (Array(1L,1L),                OpDepth(),  Right(Array(2L))),

      // OP_DROP(0x75) : Pop the top item in the stack
      // Before : x
      // After  :
      (Array(0L),                   OpDrop(),   Right(EMPTY_ARRAY)),
      (Array(0L, 2L),               OpDrop(),   Right(Array(0L))),
      (Array(2L, 0L),               OpDrop(),   Right(Array(2L))),
      (EMPTY_ARRAY,                 OpDrop(),   Left(ErrorCode.InvalidStackOperation)),

      // OP_DUP(0x76) : Duplicate the top item in the stack
      // Before : x
      // After  : x x
      (Array(0L),                   OpDup(),    Right(Array(0L, 0L))),
      (Array(2L),                   OpDup(),    Right(Array(2L, 2L))),
      (Array(0L, 2L),               OpDup(),    Right(Array(0L, 2L, 2L))),
      (EMPTY_ARRAY,                 OpDup(),    Left(ErrorCode.InvalidStackOperation)),

      // OP_NIP(0x77) : Pop the second item in the stack
      // Before : x1 x2
      // After  : x2
      (Array(2L, 3L),               OpNip(),    Right(Array(3L))),
      (Array(1L, 2L, 3L),           OpNip(),    Right(Array(1L, 3L))),
      (EMPTY_ARRAY,                 OpNip(),    Left(ErrorCode.InvalidStackOperation)),
      (Array(2L),                   OpNip(),    Left(ErrorCode.InvalidStackOperation)),

      // OP_OVER(0x78) : Copy the second item in the stack and push it onto the top
      // Before : x1 x2
      // After  : x1 x2 x1
      (Array(2L, 3L),               OpOver(),   Right(Array(2L, 3L, 2L))),
      (Array(1L, 2L, 3L),           OpOver(),   Right(Array(1L, 2L, 3L, 2L))),
      (EMPTY_ARRAY,                 OpOver(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(2L),                   OpOver(),   Left(ErrorCode.InvalidStackOperation)),

      // OP_PICK(0x79) : Pop value N from top, then copy the Nth item to the top of the stack
      // Before : xn ... x2 x1 x0 <n>
      // After  : xn ... x2 x1 x0 xn
      (Array(10L,0L),               OpPick(),   Right(Array(10L,10L))),
      (Array(10L,1L),               OpPick(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(10L,2L),               OpPick(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(11L,10L,0L),           OpPick(),   Right(Array(11L,10L,10L))),
      (Array(11L,10L,1L),           OpPick(),   Right(Array(11L,10L,11L))),
      (Array(11L,10L,2L),           OpPick(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(11L,10L,3L),           OpPick(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(12L,11L,10L,0L),       OpPick(),   Right(Array(12L,11L,10L,10L))),
      (Array(12L,11L,10L,1L),       OpPick(),   Right(Array(12L,11L,10L,11L))),
      (Array(12L,11L,10L,2L),       OpPick(),   Right(Array(12L,11L,10L,12L))),
      (Array(12L,11L,10L,3L),       OpPick(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(12L,11L,10L,4L),       OpPick(),   Left(ErrorCode.InvalidStackOperation)),
      (EMPTY_ARRAY,                 OpPick(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(0L),                   OpPick(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(1L),                   OpPick(),   Left(ErrorCode.InvalidStackOperation)),

      // OP_ROLL(0x7a) : Pop value N from top, then move the Nth item to the top of the stack
      // Before : xn ... x2 x1 x0 <n>
      // After  : ... x2 x1 x0 xn
      (Array(10L,0L),               OpRoll(),   Right(Array(10L))),
      (Array(10L,1L),               OpRoll(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(10L,2L),               OpRoll(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(11L,10L,0L),           OpRoll(),   Right(Array(11L,10L))),
      (Array(11L,10L,1L),           OpRoll(),   Right(Array(10L,11L))),
      (Array(11L,10L,2L),           OpRoll(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(11L,10L,3L),           OpRoll(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(12L,11L,10L,0L),       OpRoll(),   Right(Array(12L,11L,10L))),
      (Array(12L,11L,10L,1L),       OpRoll(),   Right(Array(12L,10L,11L))),
      (Array(12L,11L,10L,2L),       OpRoll(),   Right(Array(11L,10L,12L))),
      (Array(12L,11L,10L,3L),       OpRoll(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(12L,11L,10L,4L),       OpRoll(),   Left(ErrorCode.InvalidStackOperation)),
      (EMPTY_ARRAY,                 OpRoll(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(0L),                   OpRoll(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(1L),                   OpRoll(),   Left(ErrorCode.InvalidStackOperation)),

      // OP_ROT(0x7b) : Rotate the top three items in the stack
      // Before : x1 x2 x3
      // After  : x2 x3 x1
      (Array(2L,3L,4L),             OpRot(),    Right(Array(3L,4L,2L))),
      (Array(1L,2L,3L,4L),          OpRot(),    Right(Array(1L,3L,4L,2L))),
      (EMPTY_ARRAY,                 OpRot(),    Left(ErrorCode.InvalidStackOperation)),
      (Array(2L),                   OpRot(),    Left(ErrorCode.InvalidStackOperation)),
      (Array(2L,3L),                OpRot(),    Left(ErrorCode.InvalidStackOperation)),

      // OP_SWAP(0x7c) : Swap the top three items in the stack
      // Before : x1 x2
      // After  : x2 x1
      (Array(2L,3L),                OpSwap(),   Right(Array(3L,2L))),
      (Array(1L,2L,3L),             OpSwap(),   Right(Array(1L,3L,2L))),
      (EMPTY_ARRAY,                 OpSwap(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(2L),                   OpSwap(),   Left(ErrorCode.InvalidStackOperation)),

    // TODO : Fix issue.

      // OP_TUCK(0x7d) : Copy the top item and insert it between the top and second item.
      // Before : s x1 x2
      // After  : s x2 x1 x2
      (Array(2L,3L),                OpTuck(),   Right(Array(3L,2L,3L))),
      (Array(1L,2L,3L),             OpTuck(),   Right(Array(1L,3L,2L,3L))),
      (EMPTY_ARRAY,                 OpTuck(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(2L),                   OpTuck(),   Left(ErrorCode.InvalidStackOperation)),

      // OP_2DROP(0x6d) : Pop top two stack items
      // Before : x1 x2
      // After  :
      (Array(2L,3L),                Op2Drop(),  Right(EMPTY_ARRAY)),
      (Array(1L,2L,3L),             Op2Drop(),  Right(Array(1L))),
      (EMPTY_ARRAY,                 Op2Drop(),  Left(ErrorCode.InvalidStackOperation)),
      (Array(2L),                   Op2Drop(),  Left(ErrorCode.InvalidStackOperation)),

      // OP_2DUP(0x6e) : Duplicate top two stack items
      // Before : x1 x2
      // After  : x1 x2 x1 x2
      (Array(2L,3L),                Op2Dup(),   Right(Array(2L,3L,2L,3L))),
      (Array(1L,2L,3L),             Op2Dup(),   Right(Array(1L,2L,3L,2L,3L))),
      (EMPTY_ARRAY,                 Op2Dup(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(2L),                   Op2Dup(),   Left(ErrorCode.InvalidStackOperation)),

      // OP_3DUP(0x6f) : Duplicate top three stack items
      // Before : x1 x2 x3
      // After  : x1 x2 x3 x1 x2 x3
      (Array(2L,3L,4L),             Op3Dup(),   Right(Array(2L,3L,4L,2L,3L,4L))),
      (Array(1L,2L,3L,4L),          Op3Dup(),   Right(Array(1L,2L,3L,4L,2L,3L,4L))),
      (EMPTY_ARRAY,                 Op3Dup(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(2L),                   Op3Dup(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(2L,3L),                Op3Dup(),   Left(ErrorCode.InvalidStackOperation)),


      // OP_2OVER(0x70) : Copy the third and fourth items in the stack to the top
      // Before : x1 x2 x3 x4
      // After  : x1 x2 x3 x4 x1 x2
      (Array(2L,3L,4L,5L),          Op2Over(),  Right(Array(2L,3L,4L,5L,2L,3L))),
      (Array(1L,2L,3L,4L,5L),       Op2Over(),  Right(Array(1L,2L,3L,4L,5L,2L,3L))),
      (EMPTY_ARRAY,                 Op2Over(),  Left(ErrorCode.InvalidStackOperation)),
      (Array(2L),                   Op2Over(),  Left(ErrorCode.InvalidStackOperation)),
      (Array(2L,3L),                Op2Over(),  Left(ErrorCode.InvalidStackOperation)),
      (Array(2L,3L,3L),             Op2Over(),  Left(ErrorCode.InvalidStackOperation)),

      // OP_2ROT(0x71) : Move the fifth and sixth items in the stack to the top
      // Before : x1 x2 x3 x4 x5 x6
      // After  : x3 x4 x5 x6 x1 x2
      (Array(2L,3L,4L,5L,6L,7L),    Op2Rot(),   Right(Array(4L,5L,6L,7L,2L,3L))),
      (Array(1L,2L,3L,4L,5L,6L,7L), Op2Rot(),   Right(Array(1L,4L,5L,6L,7L,2L,3L))),
      (EMPTY_ARRAY,                 Op2Rot(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(2L),                   Op2Rot(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(2L,3L),                Op2Rot(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(2L,3L,3L),             Op2Rot(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(2L,3L,3L,4L),          Op2Rot(),   Left(ErrorCode.InvalidStackOperation)),
      (Array(2L,3L,3L,4L,5L),       Op2Rot(),   Left(ErrorCode.InvalidStackOperation)),

      // OP_2SWAP(0x72) : Swap the two top pairs of items in the stack
      // Before : x1 x2 x3 x4
      // After  : x3 x4 x1 x2
      (Array(2L,3L,4L,5L),          Op2Swap(),  Right(Array(4L,5L,2L,3L))),
      (Array(1L,2L,3L,4L,5L),       Op2Swap(),  Right(Array(1L,4L,5L,2L,3L))),
      (EMPTY_ARRAY,                 Op2Swap(),  Left(ErrorCode.InvalidStackOperation)),
      (Array(2L),                   Op2Swap(),  Left(ErrorCode.InvalidStackOperation)),
      (Array(2L,3L),                Op2Swap(),  Left(ErrorCode.InvalidStackOperation)),
      (Array(2L,3L,3L),             Op2Swap(),  Left(ErrorCode.InvalidStackOperation))
    )

  "operations" should "manipulate the stack correctly" in {
    forAll(operations) { ( inputValues : Array[Long], operation : ScriptOp, expectation : Either[ErrorCode, Array[Long]] )  =>
      // Arithmetic operations do not use script chunk, so it is ok to pass null for the parsed script.
      val env = new ScriptEnvironment()

      for ( input : Long <-inputValues) {
        env.stack.pushInt( BigInteger.valueOf(input) )
      }

      println (s"Testing with input ${inputValues.mkString(",")}, operation : ${operation}" )

      expectation match {
        case Left(errorCode) => {
          val thrown = the[ScriptEvalException] thrownBy {
            operation.execute(env)
          }
          thrown.code should be (errorCode)
        }
        case Right(expectedOutputValues) => {
          operation.execute(env)

          println ("expected values :" + expectedOutputValues.mkString(","))
          verifyStack("actual stack", env.stack, expectedOutputValues)
        }
      }
    }
  }

  private def verifyStack(subject : String, actualStack:ScriptStack, expectedValues : Array[Long]) : Unit = {
    println ("We are comparing the stack. subject : " + subject)
    //println ("stack : " + actualStack)

    toString
    for (i <- 0 until expectedValues.length) {
      val actualOutputBI : BigInteger = Utils.decodeStackInt( actualStack(expectedValues.length-1-i).value )
      val expectedOutput = expectedValues(i)

      println (s"expected output : ${expectedOutput}" )
      println (s"actual output : ${actualOutputBI.longValue()}" )

      actualOutputBI.longValue() should be (expectedOutput)
    }

  }

  val altStackOperations =
    Table(
      // column names
      ("mainStackInputs", "altStackInputs", "operation", "expectation" ),
      // test cases with input stack values, script operation, output stack values
      // The input value is pushed on to the script execution stack from left to right.


      // OP_TOALTSTACK(0x6b) : Pop top item from stack and push to alternative stack
      // Before : x1
      // After  : (alt)x1
      (Array(3L),       EMPTY_ARRAY,  OpToAltStack(),  Right((EMPTY_ARRAY, Array(3L)))),
      (Array(1L, 3L),   EMPTY_ARRAY,  OpToAltStack(),  Right((Array(1L), Array(3L)))),
      (EMPTY_ARRAY,     EMPTY_ARRAY,  OpToAltStack(),  Left(ErrorCode.InvalidStackOperation)),
      (EMPTY_ARRAY,     Array(1L),    OpToAltStack(),  Left(ErrorCode.InvalidStackOperation)),


      // OP_FROMALTSTACK(0x6c) : Pop top item from alternative stack and push to stack
      // Before : (alt)x1
      // After  : x1
      (EMPTY_ARRAY,     Array(3L),    OpFromAltStack(),  Right((Array(3L), EMPTY_ARRAY))),
      (EMPTY_ARRAY,     Array(1L,3L), OpFromAltStack(),  Right((Array(3L), Array(1L)))),
      (EMPTY_ARRAY,     EMPTY_ARRAY,  OpFromAltStack(),  Left(ErrorCode.InvalidStackOperation)),
      (Array(1L),       EMPTY_ARRAY,  OpFromAltStack(),  Left(ErrorCode.InvalidStackOperation))
    )

  "operations" should "manipulate the main stack and alt correctly" in {
    forAll(altStackOperations) { ( mainStackInputs : Array[Long], altStackInputs : Array[Long], operation : ScriptOp, expectation : Either[ErrorCode, (Array[Long],Array[Long])] )  =>
      // Arithmetic operations do not use script chunk, so it is ok to pass null for the parsed script.
      val env = new ScriptEnvironment()

      for ( input : Long <-mainStackInputs) {
        env.stack.pushInt( BigInteger.valueOf(input) )
      }

      for ( input : Long <-altStackInputs) {
        env.altStack.pushInt( BigInteger.valueOf(input) )
      }

      println (s"Testing with input ${mainStackInputs.mkString(",")}, operation : ${operation}" )

      expectation match {
        case Left(errorCode) => {
          val thrown = the[ScriptEvalException] thrownBy {
            operation.execute(env)
          }
          thrown.code should be (errorCode)
        }
        case Right((expectedMainStackInputs, expectedAltStackInputs)) => {
          operation.execute(env)

          verifyStack("actual main stack", env.stack, expectedMainStackInputs)
          verifyStack("actual alt stack",  env.altStack, expectedAltStackInputs)
        }
      }
    }
  }
}

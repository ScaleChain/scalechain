package io.scalechain.blockchain.script

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ScriptParseException
import io.scalechain.blockchain.script.ops._
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table

/** Test flow control operations in FlowControl.scala
  *
  */
class FlowControlSpec : FlatSpec with BeforeAndAfterEach with OperationTestTrait {

  this: Suite =>

  override fun beforeEach() {
    // set-up code
    //

    super.beforeEach()
  }

  override fun afterEach() {
    super.afterEach()
    // tear-down code
    //
  }

  val invalidIfStatements =
    Table(
      // column names
      ("inputValues","operation", "expectedOutputValue"),
      // test cases with input value, script operation, output value
      // The input value is pushed on to the script execution stack from left to right.

      /////////////////////////////////////////////////////////////////////////////
      // An if statement is nested on the then-statement-list part.
      /////////////////////////////////////////////////////////////////////////////
      // if true
      //
      // <missing end if>
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(
          OpIf(),
            Op1()
          // OpEndIf() is missing.
        ),
        ScriptParseException(ErrorCode.UnexpectedEndOfScript) // (expected) an exception should be thrown
      ),

      // if false
      //
      // <missing end if>
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(
          OpIf(),
            Op1()
          // OpEndIf() is missing.
        ),
        ScriptParseException(ErrorCode.UnexpectedEndOfScript) // (expected) an exception should be thrown
      ),


      // if true
      // else
      // <missing end if>
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(
          OpIf(),
            Op1(),
          OpElse(),
            OpNum(2)
          // OpEndIf() is missing.
        ),
        ScriptParseException(ErrorCode.UnexpectedEndOfScript) // (expected) an exception should be thrown
      ),

      // if false
      // else
      // <missing end if>
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(
          OpIf(),
            Op1(),
          OpElse(),
            OpNum(2)
          // OpEndIf() is missing.
        ),
        ScriptParseException(ErrorCode.UnexpectedEndOfScript) // (expected) an exception should be thrown
      )
    )

  "invalidIfStatements" should "should throw an exception." in {
    forAll(invalidIfStatements) { ( inputValues : Array<ScriptValue>, operations : List<ScriptOp>, expectation : AnyRef )  =>
      verifyOperations(inputValues, operations, expectation, serializeAndExecute=true);
    }
  }


  val nestedIfStatements =
      Table(
        // column names
        ("inputValues","operation", "expectedOutputValue"),
        // test cases with input value, script operation, output value
        // The input value is pushed on to the script execution stack from left to right.

        /////////////////////////////////////////////////////////////////////////////
        // An if statement is nested on the then-statement-list part.
        /////////////////////////////////////////////////////////////////////////////
        // if true
        //   if true
        //   endif
        // endif
        (
          stack(1),       // input stack.
          // list of operations to execute.
          List(
            OpIf(),
              Op1(),
              OpIf(),
                OpNum(8),
              OpElse(),
                OpNum(9),
              OpEndIf(),
            OpEndIf()),
          stack(8) // (expected) output stack.
        ),

        // if false
        //   if true
        //   endif
        // endif
        (
          stack(0),       // input stack.
          // list of operations to execute.
          List(
            OpIf(),
              Op1(),
              OpIf(),
                OpNum(8),
              OpElse(),
                OpNum(9),
              OpEndIf(),
            OpEndIf()),
          stack() // (expected) output stack.
        ),

        // if true
        //   if false
        //   endif
        // endif
        (
          stack(1),       // input stack.
          // list of operations to execute.
          List(
            OpIf(),
              Op0(),
              OpIf(),
                OpNum(8),
              OpElse(),
                OpNum(9),
              OpEndIf(),
            OpEndIf()),
          stack(9) // (expected) output stack.
        ),

        // if false
        //   if false
        //   endif
        // endif
        (
          stack(0),       // input stack.
          // list of operations to execute.
          List(
            OpIf(),
              Op0(),
              OpIf(),
                OpNum(8),
              OpElse(),
                OpNum(9),
              OpEndIf(),
            OpEndIf()),
          stack() // (expected) output stack.
        ),

        /////////////////////////////////////////////////////////////////////////////
        // An if statement is nested on the else-statement-list part.
        /////////////////////////////////////////////////////////////////////////////

        // if true
        // else
        //   if true
        //   endif
        // endif
        (
          stack(1),       // input stack.
          // list of operations to execute.
          List(
            OpIf(),
              OpNum(4),
            OpElse(),
              Op1(),
              OpIf(),
                OpNum(8),
              OpElse(),
                OpNum(9),
              OpEndIf(),
            OpEndIf()),
          stack(4) // (expected) output stack.
        ),

        // if false
        // else
        //   if true
        //   endif
        // endif
        (
          stack(0),       // input stack.
          // list of operations to execute.
          List(
            OpIf(),
              OpNum(4),
            OpElse(),
              Op1(),
              OpIf(),
                OpNum(8),
              OpElse(),
                OpNum(9),
              OpEndIf(),
            OpEndIf()),
          stack() // (expected) output stack.
        ),

        // if true
        // else
        //   if false
        //   endif
        // endif
        (
          stack(1),       // input stack.
          // list of operations to execute.
          List(
            OpIf(),
              OpNum(4),
            OpElse(),
              Op0(),
              OpIf(),
                OpNum(8),
              OpElse(),
                OpNum(9),
              OpEndIf(),
            OpEndIf()),
          stack(4) // (expected) output stack.
        ),

        // if false
        // else
        //   if false
        //   endif
        // endif
        (
          stack(0),       // input stack.
          // list of operations to execute.
          List(
            OpIf(),
              OpNum(4),
            OpElse(),
              Op0(),
              OpIf(),
                OpNum(8),
              OpElse(),
                OpNum(9),
              OpEndIf(),
            OpEndIf()),
          stack() // (expected) output stack.
        )
      )

  "nestedIfStatements" should "serialize and parse and execute." in {
    forAll(nestedIfStatements) { ( inputValues : Array<ScriptValue>, operations : List<ScriptOp>, expectation : AnyRef )  =>
      verifyOperations(inputValues, operations, expectation, serializeAndExecute=true);
    }
  }

  /** BUGBUG : Need to test if zero, negative zero, empty array are all evaluated to false.
   * Why? "False is zero or negative zero (using any number of bytes) or an empty array, and True is anything else."
   *
   * See https://en.bitcoin.it/wiki/Script
   */

  /** Parsing flow control statements.
   * The test input has OP_IF, OP_NOTIF, OP_ELSE, OP_ENDIF, and output has OpCond.
   * We need to implement script operation serialization to test the parsing part,
   * as the input of parser is a byte array, not a list of script operations.
   */

  val ifOperationsForParser =
    Table(
      // column names
      ("inputValues","operation", "expectedOutputValue"),
      // test cases with input value, script operation, output value
      // The input value is pushed on to the script execution stack from left to right.

      // if true stmt1 end
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(OpIf(), OpNum(4), OpEndIf()),
        stack(4) // (expected) output stack.
      ),
      // if false stmt1 end
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(OpIf(), OpNum(4), OpEndIf()),
        stack()       // (expected) output stack.
      ),
      // if true stmt1 else stmt2 end
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(OpIf(), OpNum(4), OpElse(), OpNum(8), OpEndIf()),
        stack(4)       // (expected) output stack.
      ),
      // if false stmt1 else stmt2 end
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(OpIf(), OpNum(4), OpElse(), OpNum(8), OpEndIf()),
        stack(8)       // (expected) output stack.
      ),
      // if true multi-stmts-1 end
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(OpIf(), OpNum(4), OpNum(5), OpEndIf()),
        stack(4,5)       // (expected) output stack.
      ),
      // if false multi-stmts-1 end
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(OpIf(), OpNum(4), OpNum(5), OpEndIf()),
        stack()       // (expected) output stack.
      ),
      // if true multi-stmts-1 else multi-stmts-2 end
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(OpIf(), OpNum(4), OpNum(5), OpElse(), OpNum(8), OpNum(9), OpEndIf()),
        stack(4, 5)       // (expected) output stack.
      ),
      // if false multi-stmts-1 else multi-stmts-2 end
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(OpIf(), OpNum(4), OpNum(5), OpElse(), OpNum(8), OpNum(9), OpEndIf()),
        stack(8, 9)
      ) // (expected) output stack.
    )

  "ifOperationsForParser" should "serialize and parse and execute." in {
    forAll(ifOperationsForParser) { ( inputValues : Array<ScriptValue>, operations : List<ScriptOp>, expectation : AnyRef )  =>
      verifyOperations(inputValues, operations, expectation, serializeAndExecute=true);
    }
  }


  /**
   * Evaluating flow control statements. The test case may contain pseudo operations.
   * Ex> OpCond ; converted from OP_IF, OP_NOTIF, OP_ELSE, OP_ENDIF.
   */

  val ifOperations =
    Table(
      // column names
      ("inputValues","operation", "expectedOutputValue"),
      // test cases with input value, script operation, output value
      // The input value is pushed on to the script execution stack from left to right.

      ///////////////////////////////////////////////////////////////////////////
      // Test OpCond(invert=false,...), which is converted from OP_IF
      ///////////////////////////////////////////////////////////////////////////
      // if true stmt1 end
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=false, ops(OpNum(4)), null)),
        stack(4) // (expected) output stack.
      ),
      // if false stmt1 end
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=false, ops(OpNum(4)), null)),
        stack()       // (expected) output stack.
      ),
      // if true stmt1 else stmt2 end
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=false, ops(OpNum(4)), ops(OpNum(8)))),
        stack(4)       // (expected) output stack.
      ),
      // if false stmt1 else stmt2 end
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=false, ops(OpNum(4)), ops(OpNum(8)))),
        stack(8)       // (expected) output stack.
      ),
      // if true multi-stmts-1 end
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=false, ops(OpNum(4),OpNum(5)), null)),
        stack(4,5)       // (expected) output stack.
      ),
      // if false multi-stmts-1 end
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=false, ops(OpNum(4),OpNum(5)), null)),
        stack()       // (expected) output stack.
      ),
      // if true multi-stmts-1 else multi-stmts-2 end
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=false, ops(OpNum(4),OpNum(5)), ops(OpNum(8), OpNum(9)))),
        stack(4, 5)       // (expected) output stack.
      ),
      // if false multi-stmts-1 else multi-stmts-2 end
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=false, ops(OpNum(4),OpNum(5)), ops(OpNum(8), OpNum(9)))),
        stack(8, 9)
      ), // (expected) output stack.

      ///////////////////////////////////////////////////////////////////////////
      // Test OpCond(invert=true,...), which is converted from OP_NOTIF
      ///////////////////////////////////////////////////////////////////////////
      // if not true stmt1 end
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=true, ops(OpNum(4)), null)),
        stack() // (expected) output stack.
      ),
      // if not false stmt1 end
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=true, ops(OpNum(4)), null)),
        stack(4)       // (expected) output stack.
        ),
      // if not true stmt1 else stmt2 end
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=true, ops(OpNum(4)), ops(OpNum(5)))),
        stack(5)       // (expected) output stack.
        ),
      // if not false stmt1 else stmt2 end
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=true, ops(OpNum(4)), ops(OpNum(5)))),
        stack(4)       // (expected) output stack.
        ),
      // if not true multi-stmts-1 end
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=true, ops(OpNum(4),OpNum(5)), null)),
        stack()       // (expected) output stack.
        ),
      // if not false multi-stmts-1 end
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=true, ops(OpNum(4),OpNum(5)), null)),
        stack(4,5)       // (expected) output stack.
        ),
      // if not true multi-stmts-1 else multi-stmts-2 end
      (
        stack(1),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=true, ops(OpNum(4),OpNum(5)), ops(OpNum(8), OpNum(9)))),
        stack(8, 9)       // (expected) output stack.
        ),
      // if not false multi-stmts-1 else multi-stmts-2 end
      (
        stack(0),       // input stack.
        // list of operations to execute.
        List(OpCond(invert=true, ops(OpNum(4),OpNum(5)), ops(OpNum(8), OpNum(9)))),
        stack(4, 5)
      ) // (expected) output stack.
    )

  "ifOperations" should "run and push expected value on the stack." in {
    forAll(ifOperations) { ( inputValues : Array<ScriptValue>, operations : List<ScriptOp>, expectation : AnyRef )  =>
      verifyOperations(inputValues, operations, expectation);
    }
  }
}

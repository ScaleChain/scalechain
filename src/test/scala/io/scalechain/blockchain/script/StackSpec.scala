package io.scalechain.blockchain.script

import io.scalechain.blockchain.{ErrorCode}
import io.scalechain.blockchain.script.ops._
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table

/** Test stack operations in Stack.scala
  *
  */
class StackSpec extends FlatSpec with BeforeAndAfterEach with OperationTestTrait {

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
      (stack(0),                   OpIfDup(),  stack(0)),
      (stack(1),                   OpIfDup(),  stack(1, 1)),
      (stack(2),                   OpIfDup(),  stack(2, 2)),
      (stack(),                     OpIfDup(),  ErrorCode.InvalidStackOperation),

      // OP_DEPTH(0x74) : Count the items on the stack and push the resulting count
      // Before :
      // After  : <stack size>
      (stack(),                     OpDepth(),  stack(0)),
      (stack(-1),                  OpDepth(),  stack(1)),
      (stack(0),                   OpDepth(),  stack(1)),
      (stack(1),                   OpDepth(),  stack(1)),
      (stack(1,1),                OpDepth(),  stack(2)),

      // OP_DROP(0x75) : Pop the top item in the stack
      // Before : x
      // After  :
      (stack(0),                   OpDrop(),   stack()),
      (stack(0, 2),               OpDrop(),   stack(0)),
      (stack(2, 0),               OpDrop(),   stack(2)),
      (stack(),                     OpDrop(),   ErrorCode.InvalidStackOperation),

      // OP_DUP(0x76) : Duplicate the top item in the stack
      // Before : x
      // After  : x x
      (stack(0),                   OpDup(),    stack(0, 0)),
      (stack(2),                   OpDup(),    stack(2, 2)),
      (stack(0, 2),               OpDup(),    stack(0, 2, 2)),
      (stack(),                     OpDup(),    ErrorCode.InvalidStackOperation),

      // OP_NIP(0x77) : Pop the second item in the stack
      // Before : x1 x2
      // After  : x2
      (stack(2, 3),               OpNip(),    stack(3)),
      (stack(1, 2, 3),           OpNip(),    stack(1, 3)),
      (stack(),                     OpNip(),    ErrorCode.InvalidStackOperation),
      (stack(2),                   OpNip(),    ErrorCode.InvalidStackOperation),

      // OP_OVER(0x78) : Copy the second item in the stack and push it onto the top
      // Before : x1 x2
      // After  : x1 x2 x1
      (stack(2, 3),               OpOver(),   stack(2, 3, 2)),
      (stack(1, 2, 3),           OpOver(),   stack(1, 2, 3, 2)),
      (stack(),                     OpOver(),   ErrorCode.InvalidStackOperation),
      (stack(2),                   OpOver(),   ErrorCode.InvalidStackOperation),

      // OP_PICK(0x79) : Pop value N from top, then copy the Nth item to the top of the stack
      // Before : xn ... x2 x1 x0 <n>
      // After  : xn ... x2 x1 x0 xn
      (stack(10,0),               OpPick(),   stack(10,10)),
      (stack(10,1),               OpPick(),   ErrorCode.InvalidStackOperation),
      (stack(10,2),               OpPick(),   ErrorCode.InvalidStackOperation),
      (stack(11,10,0),           OpPick(),   stack(11,10,10)),
      (stack(11,10,1),           OpPick(),   stack(11,10,11)),
      (stack(11,10,2),           OpPick(),   ErrorCode.InvalidStackOperation),
      (stack(11,10,3),           OpPick(),   ErrorCode.InvalidStackOperation),
      (stack(12,11,10,0),       OpPick(),   stack(12,11,10,10)),
      (stack(12,11,10,1),       OpPick(),   stack(12,11,10,11)),
      (stack(12,11,10,2),       OpPick(),   stack(12,11,10,12)),
      (stack(12,11,10,3),       OpPick(),   ErrorCode.InvalidStackOperation),
      (stack(12,11,10,4),       OpPick(),   ErrorCode.InvalidStackOperation),
      (stack(),                     OpPick(),   ErrorCode.InvalidStackOperation),
      (stack(0),                   OpPick(),   ErrorCode.InvalidStackOperation),
      (stack(1),                   OpPick(),   ErrorCode.InvalidStackOperation),

      // OP_ROLL(0x7a) : Pop value N from top, then move the Nth item to the top of the stack
      // Before : xn ... x2 x1 x0 <n>
      // After  : ... x2 x1 x0 xn
      (stack(10,0),               OpRoll(),   stack(10)),
      (stack(10,1),               OpRoll(),   ErrorCode.InvalidStackOperation),
      (stack(10,2),               OpRoll(),   ErrorCode.InvalidStackOperation),
      (stack(11,10,0),           OpRoll(),   stack(11,10)),
      (stack(11,10,1),           OpRoll(),   stack(10,11)),
      (stack(11,10,2),           OpRoll(),   ErrorCode.InvalidStackOperation),
      (stack(11,10,3),           OpRoll(),   ErrorCode.InvalidStackOperation),
      (stack(12,11,10,0),       OpRoll(),   stack(12,11,10)),
      (stack(12,11,10,1),       OpRoll(),   stack(12,10,11)),
      (stack(12,11,10,2),       OpRoll(),   stack(11,10,12)),
      (stack(12,11,10,3),       OpRoll(),   ErrorCode.InvalidStackOperation),
      (stack(12,11,10,4),       OpRoll(),   ErrorCode.InvalidStackOperation),
      (stack(),                     OpRoll(),   ErrorCode.InvalidStackOperation),
      (stack(0),                   OpRoll(),   ErrorCode.InvalidStackOperation),
      (stack(1),                   OpRoll(),   ErrorCode.InvalidStackOperation),

      // OP_ROT(0x7b) : Rotate the top three items in the stack
      // Before : x1 x2 x3
      // After  : x2 x3 x1
      (stack(2,3,4),             OpRot(),    stack(3,4,2)),
      (stack(1,2,3,4),          OpRot(),    stack(1,3,4,2)),
      (stack(),                     OpRot(),    ErrorCode.InvalidStackOperation),
      (stack(2),                   OpRot(),    ErrorCode.InvalidStackOperation),
      (stack(2,3),                OpRot(),    ErrorCode.InvalidStackOperation),

      // OP_SWAP(0x7c) : Swap the top three items in the stack
      // Before : x1 x2
      // After  : x2 x1
      (stack(2,3),                OpSwap(),   stack(3,2)),
      (stack(1,2,3),             OpSwap(),   stack(1,3,2)),
      (stack(),                     OpSwap(),   ErrorCode.InvalidStackOperation),
      (stack(2),                   OpSwap(),   ErrorCode.InvalidStackOperation),

      // OP_TUCK(0x7d) : Copy the top item and insert it between the top and second item.
      // Before : s x1 x2
      // After  : s x2 x1 x2
      (stack(2,3),                OpTuck(),   stack(3,2,3)),
      (stack(1,2,3),             OpTuck(),   stack(1,3,2,3)),
      (stack(),                     OpTuck(),   ErrorCode.InvalidStackOperation),
      (stack(2),                   OpTuck(),   ErrorCode.InvalidStackOperation),

      // OP_2DROP(0x6d) : Pop top two stack items
      // Before : x1 x2
      // After  :
      (stack(2,3),                Op2Drop(),  stack()),
      (stack(1,2,3),             Op2Drop(),  stack(1)),
      (stack(),                     Op2Drop(),  ErrorCode.InvalidStackOperation),
      (stack(2),                   Op2Drop(),  ErrorCode.InvalidStackOperation),

      // OP_2DUP(0x6e) : Duplicate top two stack items
      // Before : x1 x2
      // After  : x1 x2 x1 x2
      (stack(2,3),                Op2Dup(),   stack(2,3,2,3)),
      (stack(1,2,3),             Op2Dup(),   stack(1,2,3,2,3)),
      (stack(),                     Op2Dup(),   ErrorCode.InvalidStackOperation),
      (stack(2),                   Op2Dup(),   ErrorCode.InvalidStackOperation),

      // OP_3DUP(0x6f) : Duplicate top three stack items
      // Before : x1 x2 x3
      // After  : x1 x2 x3 x1 x2 x3
      (stack(2,3,4),             Op3Dup(),   stack(2,3,4,2,3,4)),
      (stack(1,2,3,4),          Op3Dup(),   stack(1,2,3,4,2,3,4)),
      (stack(),                     Op3Dup(),   ErrorCode.InvalidStackOperation),
      (stack(2),                   Op3Dup(),   ErrorCode.InvalidStackOperation),
      (stack(2,3),                Op3Dup(),   ErrorCode.InvalidStackOperation),


      // OP_2OVER(0x70) : Copy the third and fourth items in the stack to the top
      // Before : x1 x2 x3 x4
      // After  : x1 x2 x3 x4 x1 x2
      (stack(2,3,4,5),          Op2Over(),  stack(2,3,4,5,2,3)),
      (stack(1,2,3,4,5),       Op2Over(),  stack(1,2,3,4,5,2,3)),
      (stack(),                     Op2Over(),  ErrorCode.InvalidStackOperation),
      (stack(2),                   Op2Over(),  ErrorCode.InvalidStackOperation),
      (stack(2,3),                Op2Over(),  ErrorCode.InvalidStackOperation),
      (stack(2,3,3),             Op2Over(),  ErrorCode.InvalidStackOperation),

      // OP_2ROT(0x71) : Move the fifth and sixth items in the stack to the top
      // Before : x1 x2 x3 x4 x5 x6
      // After  : x3 x4 x5 x6 x1 x2
      (stack(2,3,4,5,6,7),    Op2Rot(),   stack(4,5,6,7,2,3)),
      (stack(1,2,3,4,5,6,7), Op2Rot(),   stack(1,4,5,6,7,2,3)),
      (stack(),                     Op2Rot(),   ErrorCode.InvalidStackOperation),
      (stack(2),                   Op2Rot(),   ErrorCode.InvalidStackOperation),
      (stack(2,3),                Op2Rot(),   ErrorCode.InvalidStackOperation),
      (stack(2,3,3),             Op2Rot(),   ErrorCode.InvalidStackOperation),
      (stack(2,3,3,4),          Op2Rot(),   ErrorCode.InvalidStackOperation),
      (stack(2,3,3,4,5),       Op2Rot(),   ErrorCode.InvalidStackOperation),

      // OP_2SWAP(0x72) : Swap the two top pairs of items in the stack
      // Before : x1 x2 x3 x4
      // After  : x3 x4 x1 x2
      (stack(2,3,4,5),          Op2Swap(),  stack(4,5,2,3)),
      (stack(1,2,3,4,5),       Op2Swap(),  stack(1,4,5,2,3)),
      (stack(),                     Op2Swap(),  ErrorCode.InvalidStackOperation),
      (stack(2),                   Op2Swap(),  ErrorCode.InvalidStackOperation),
      (stack(2,3),                Op2Swap(),  ErrorCode.InvalidStackOperation),
      (stack(2,3,3),             Op2Swap(),  ErrorCode.InvalidStackOperation)
    )

  "operations" should "manipulate the stack correctly" in {
    forAll(operations) { ( inputValues : Array[ScriptValue], operation : ScriptOp, expectation : AnyRef )  =>
      verifyOperation(inputValues, operation, expectation);
    }
  }

  val altStackOperations =
    Table(
      // column names
      ("mainStackInputs", "altStackInputs", "operation", "expectation", "altStackOutputs"),
      // test cases with input stack values, script operation, output stack values
      // The input value is pushed on to the script execution stack from left to right.


      // OP_TOALTSTACK(0x6b) : Pop top item from stack and push to alternative stack
      // Before : x1
      // After  : (alt)x1
      (stack(3),       stack(),  OpToAltStack(),  stack(), stack(3)),
      (stack(1, 3),   stack(),  OpToAltStack(),  stack(1), stack(3)),
      (stack(),     stack(),  OpToAltStack(),  ErrorCode.InvalidStackOperation, null),
      (stack(),     stack(1),    OpToAltStack(),  ErrorCode.InvalidStackOperation, null),


      // OP_FROMALTSTACK(0x6c) : Pop top item from alternative stack and push to stack
      // Before : (alt)x1
      // After  : x1
      (stack(),     stack(3),    OpFromAltStack(),  stack(3), stack()),
      (stack(),     stack(1,3), OpFromAltStack(),  stack(3), stack(1)),
      (stack(),     stack(),  OpFromAltStack(),  ErrorCode.InvalidStackOperation, null),
      (stack(1),       stack(),  OpFromAltStack(),  ErrorCode.InvalidStackOperation, null)
    )

  "operations" should "manipulate the main stack and alt correctly" in {
    forAll(altStackOperations) { ( mainStackInputs : Array[ScriptValue],
                                   altStackInputs : Array[ScriptValue],
                                   operation : ScriptOp,
                                   expectation : AnyRef,
                                   altStackOutputs : Array[ScriptValue] )  =>
      verifyOperation(mainStackInputs, altStackInputs, operation, expectation, altStackOutputs)
    }
  }
}

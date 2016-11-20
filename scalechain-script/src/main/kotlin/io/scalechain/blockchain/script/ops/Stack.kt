package io.scalechain.blockchain.script.ops

import java.math.BigInteger

import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.ScriptEnvironment
import io.scalechain.util.Utils

trait StackOperation extends ScriptOp

/** OP_TOALTSTACK(0x6b) : Pop top item from stack and push to alternative stack
  * Before : x1
  * After  : (alt)x1
  */
case class OpToAltStack() extends StackOperation {
  def opCode() = OpCode(0x6b)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 1 ) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpToAltStack")
    }

    val item = env.stack.pop()
    // No need to copy, as we are moving the item.
    env.altStack.push(item)
  }
}

/** OP_FROMALTSTACK(0x6c) : Pop top item from alternative stack and push to stack
  * Before : (alt)x1
  * After  : x1
  */
case class OpFromAltStack() extends StackOperation {
  def opCode() = OpCode(0x6c)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.altStack.size() < 1 ) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpFromAltStack")
    }

    val item = env.altStack.pop()
    // No need to copy, as we are moving the item.
    env.stack.push(item)
  }
}

/** OP_IFDUP(0x73) : Duplicate the top item in the stack if it is not 0
  * Before : x
  * After  : x     ( if x == 0 )
  * After  : x x   ( if x != 0 )
  */
case class OpIfDup() extends StackOperation {
  def opCode() = OpCode(0x73)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 1) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpIfDup")
    }
    val top = env.stack.top()
    if (Utils.castToBool(top.value)) {
      env.stack.push(top.copy())
    }
  }
}

/** OP_DEPTH(0x74) : Count the items on the stack and push the resulting count
  * Before :
  * After  : <stack size>
  */
case class OpDepth() extends StackOperation {
  def opCode() = OpCode(0x74)

  def execute(env : ScriptEnvironment): Unit = {
    val stackSize : Int = env.stack.size()
    env.stack.pushInt( BigInteger.valueOf(stackSize) )
  }
}

/** OP_DROP(0x75) : Pop the top item in the stack
  * Before : x
  * After  :
  */
case class OpDrop() extends StackOperation {
  def opCode() = OpCode(0x75)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 1) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpDrop")
    }

    env.stack.pop()
  }
}

/** OP_DUP(0x76) : Duplicate the top item in the stack
  * Before : x
  * After  : x x
  */
case class OpDup() extends StackOperation {
  def opCode() = OpCode(0x76)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 1) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpDup")
    }

    val value = env.stack.top()
    env.stack.push(value.copy())
  }
}

/** OP_NIP(0x77) : Pop the second item in the stack
  * Before : x1 x2
  * After  : x2
  */
case class OpNip() extends StackOperation {
  def opCode() = OpCode(0x77)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 2) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpNip")
    }

    // Remove the second item on the stack.
    // The top item has index 0, so the second item in the stack has index 1.
    env.stack.remove(1)
  }
}

/** OP_OVER(0x78) : Copy the second item in the stack and push it onto the top
  * Before : x1 x2
  * After  : x1 x2 x1
  */
case class OpOver() extends StackOperation {
  def opCode() = OpCode(0x78)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 2) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpOver")
    }

    val secondItem = env.stack(1)
    env.stack.push( secondItem.copy() )

  }
}

/** OP_PICK(0x79) : Pop value N from top, then copy the Nth item to the top of the stack
  * Before : xn ... x2 x1 x0 <n>
  * After  : xn ... x2 x1 x0 xn
  */
case class OpPick() extends StackOperation {
  def opCode() = OpCode(0x79)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 2) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpPick")
    }

    // Get the <n> value
    val stackIndex = env.stack.popInt().intValue()

    // Now, the stack should have at least stackIndex + 1 items.
    if (env.stack.size() < stackIndex + 1) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpPick, Not enough input after getting the stack index")
    }
    val itemAtIndex = env.stack(stackIndex)
    env.stack.push(itemAtIndex.copy())
  }
}


/** OP_ROLL(0x7a) : Pop value N from top, then move the Nth item to the top of the stack
  * Before : xn ... x2 x1 x0 <n>
  * After  : ... x2 x1 x0 xn
  */
case class OpRoll() extends StackOperation {
  def opCode() = OpCode(0x7a)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 2) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpRoll")
    }

    // Get the <n> value
    val stackIndex = env.stack.popInt().intValue()

    // Now, the stack should have at least stackIndex + 1 items.
    if (env.stack.size() < stackIndex + 1) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpRoll, Not enough input after getting the stack index")
    }
    val itemAtIndex = env.stack.remove(stackIndex)
    // No need to copy the item, as we are moving the item.
    env.stack.push(itemAtIndex)
  }
}


/** OP_ROT(0x7b) : Rotate the top three items in the stack
  * Before : x1 x2 x3
  * After  : x2 x3 x1
  */
case class OpRot() extends StackOperation {
  def opCode() = OpCode(0x7b)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 3) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpRot")
    }

    val thirdItem = env.stack.remove(2)

    // No need to copy the item, as we are moving the item.
    env.stack.push(thirdItem)
  }
}



/** OP_SWAP(0x7c) : Swap the top three items in the stack
  * Before : x1 x2
  * After  : x2 x1
  */
case class OpSwap() extends StackOperation {
  def opCode() = OpCode(0x7c)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 2) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpSwap")
    }

    val secondItem = env.stack.remove(1)

    // No need to copy the item, as we are moving the item.
    env.stack.push(secondItem)
  }
}


/** OP_TUCK(0x7d) : The item at the top of the stack is copied and inserted before the second-to-top item.
  * Before : s x1 x2
  * After  : s x2 x1 x2
  */
case class OpTuck() extends StackOperation {
  def opCode() = OpCode(0x7d)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 2) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpTuck")
    }

    val topItem = env.stack.top()
    // Insert the element at the given position.
    env.stack.insert(1, topItem.copy())
  }
}


/** OP_2DROP(0x6d) : Pop top two stack items
  * Before : x1 x2
  * After  :
  */
case class Op2Drop() extends StackOperation {
  def opCode() = OpCode(0x6d)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 2) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:Op2Drop")
    }

    env.stack.pop()
    env.stack.pop()
  }
}

/** OP_2DUP(0x6e) : Duplicate top two stack items
  * Before : x1 x2
  * After  : x1 x2 x1 x2
  */
case class Op2Dup() extends StackOperation {
  def opCode() = OpCode(0x6e)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 2) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:Op2Dup")
    }
    val topItem = env.stack(0)
    val secondItem = env.stack(1)

    env.stack.push(secondItem.copy())
    env.stack.push(topItem.copy())
  }
}


/** OP_3DUP(0x6f) : Duplicate top three stack items
  * Before : x1 x2 x3
  * After  : x1 x2 x3 x1 x2 x3
  */
case class Op3Dup() extends StackOperation {
  def opCode() = OpCode(0x6f)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 3) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:Op3Dup")
    }
    val topItem = env.stack(0)
    val secondItem = env.stack(1)
    val thirdItem = env.stack(2)

    env.stack.push(thirdItem.copy())
    env.stack.push(secondItem.copy())
    env.stack.push(topItem.copy())
  }
}


/** OP_2OVER(0x70) : Copy the third and fourth items in the stack to the top
  * Before : x1 x2 x3 x4
  * After  : x1 x2 x3 x4 x1 x2
  */
case class Op2Over() extends StackOperation {
  def opCode() = OpCode(0x70)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 4) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:Op2Over")
    }

    val thirdItem = env.stack(2)
    val fourthItem = env.stack(3)

    env.stack.push(fourthItem)
    env.stack.push(thirdItem)
  }
}


/** OP_2ROT(0x71) : Move the fifth and sixth items in the stack to the top
  * Before : x1 x2 x3 x4 x5 x6
  * After  : x3 x4 x5 x6 x1 x2
  */
case class Op2Rot() extends StackOperation {
  def opCode() = OpCode(0x71)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 6) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:Op2Rot")
    }

    val fifthItem = env.stack.remove(4)
    // instead of removing element at index 5, we have to remove an element at 4,
    // because the previous element at index 4 was removed.
    val sixthItem = env.stack.remove(4)

    env.stack.push(sixthItem)
    env.stack.push(fifthItem)
  }
}


/** OP_2SWAP(0x72) : Swap the two top pairs of items in the stack
  * Before : x1 x2 x3 x4
  * After  : x3 x4 x1 x2
  */
case class Op2Swap() extends StackOperation {
  def opCode() = OpCode(0x72)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 4) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:Op2Swap")
    }

    val thirdItem  = env.stack.remove(2)
    // instead of removing element at index 3, we have to remove an element at 2,
    // because the previous element at index 2 was removed.
    val fourthItem = env.stack.remove(2)

    env.stack.push(fourthItem)
    env.stack.push(thirdItem)
  }
}

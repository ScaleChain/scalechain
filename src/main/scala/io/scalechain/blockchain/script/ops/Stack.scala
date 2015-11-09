package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.ScriptEnvironment
import io.scalechain.blockchain.util.Utils

trait StackOperation extends ScriptOp

/** OP_TOALTSTACK(0x6b) : Pop top item from stack and push to alternative stack
  * Before : x1
  * After  : (alt)x1
  */
case class OpToAltStack() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

/** OP_FROMALTSTACK(0x6c) : Pop top item from alternative stack and push to stack
  * Before : (alt)x1
  * After  : x1
  */
case class OpFromAltStack() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

/** OP_IFDUP(0x73) : Duplicate the top item in the stack if it is not 0
  * Before : x
  * After  : x     ( if x == 0 )
  * After  : x x   ( if x != 0 )
  */
case class OpIfDup() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 1) {
      throw new ScriptEvalException(ErrorCode.InvalidStackOperation)
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
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

/** OP_DROP(0x75) : Pop the top item in the stack
  * Before : x
  * After  :
  */
case class OpDrop() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    env.stack.pop()
  }
}

/** OP_DUP(0x76) : Duplicate the top item in the stack
  * Before : x
  * After  : x x
  */
case class OpDup() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    val value = env.stack.top()
    env.stack.push(value.copy())
  }
}

/** OP_NIP(0x77) : Pop the second item in the stack
  * Before : x1 x2
  * After  : x2
  */
case class OpNip() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

/** OP_OVER(0x78) : Copy the second item in the stack and push it onto the top
  * Before : x1 x2
  * After  : x1 x2 x1
  */
case class OpOver() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

/** OP_PICK(0x79) : Pop value N from top, then copy the Nth item to the top of the stack
  * Before : xn ... x2 x1 x0 <n>
  * After  : xn ... x2 x1 x0 xn
  */
case class OpPick() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}


/** OP_ROLL(0x7a) : Pop value N from top, then move the Nth item to the top of the stack
  * Before : xn ... x2 x1 x0 <n>
  * After  : ... x2 x1 x0 xn
  */
case class OpRoll() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}


/** OP_ROT(0x7b) : Rotate the top three items in the stack
  * Before : x1 x2 x3
  * After  : x2 x3 x1
  */
case class OpRot() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}



/** OP_SWAP(0x7c) : Swap the top three items in the stack
  * Before : x1 x2
  * After  : x2 x1
  */
case class OpSwap() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}


/** OP_TUCK(0x7d) : Copy the top item and insert it between the top and second item.
  * Before : s x1 x2
  * After  : s x2 x1 x2
  */
case class OpTuck() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}


/** OP_2DROP(0x6d) : Pop top two stack items
  * Before : x1 x2
  * After  :
  */
case class Op2Drop() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    env.stack.pop()
    env.stack.pop()
  }
}

/** OP_2DUP(0x6e) : Duplicate top two stack items
  * Before : x1 x2
  * After  : x1 x2 x1 x2
  */
case class Op2Dup() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
  }
}


/** OP_3DUP(0x6f) : Duplicate top three stack items
  * Before : x1 x2 x3
  * After  : x1 x2 x3 x1 x2 x3
  */
case class Op3Dup() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}


/** OP_2OVER(0x70) : Copy the third and fourth items in the stack to the top
  * Before : x1 x2 x3 x4
  * After  : x1 x2 x3 x4 x1 x2
  */
case class Op2Over() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}


/** OP_2ROT(0x71) : Move the fifth and sixth items in the stack to the top
  * Before : x1 x2 x3 x4 x5 x6
  * After  : x3 x4 x5 x6 x1 x2
  */
case class Op2Rot() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}


/** OP_2SWAP(0x72) : Swap the two top pairs of items in the stack
  * Before : x1 x2 x3 x4
  * After  : x3 x4 x1 x2
  */
case class Op2Swap() extends StackOperation {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

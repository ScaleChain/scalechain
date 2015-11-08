package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.script.ScriptEnvironment
trait StackOperation extends ScriptOp

/** OP_TOALTSTACK(0x6b) : Pop top item from stack and push to alternative stack
  */
case class OpTotalStack() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

/** OP_FROMALTSTACK(0x6c) : Pop top item from alternative stack and push to stack
  */
case class OpFromAltStack() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

/** OP_IFDUP(0x73) : Duplicate the top item in the stack if it is not 0
  */
case class OpIfDup() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

/** OP_DEPTH(0x74) : Count the items on the stack and push the resulting count
  */
case class OpDepth() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

/** OP_DROP(0x75) : Pop the top item in the stack
  */
case class OpDrop() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

/** OP_DUP(0x76) : Duplicate the top item in the stack
  */
case class OpDup() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

/** OP_NIP(0x77) : Pop the second item in the stack
  */
case class OpNip() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

/** OP_OVER(0x78) : Copy the second item in the stack and push it onto the top
  */
case class OpOver() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

/** OP_PICK(0x79) : Pop value N from top, then copy the Nth item to the top of the stack
  */
case class OpPick() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}


/** OP_ROLL(0x7a) : Pop value N from top, then move the Nth item to the top of the stack
  */
case class OpRoll() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}


/** OP_ROT(0x7b) : Rotate the top three items in the stack
  */
case class OpRot() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}



/** OP_SWAP(0x7c) : Swap the top three items in the stack
  */
case class OpSwap() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}


/** OP_TUCK(0x7d) : Copy the top item and insert it between the top and second item.
  */
case class OpTuck() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}


/** OP_2DROP(0x6d) : Pop top two stack items

  */
case class Op2Drop() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

/** OP_2DUP(0x6e) : Duplicate top two stack items
  */
case class Op2Dup() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}


/** OP_3DUP(0x6f) : Duplicate top three stack items
  */
case class Op3Dup() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}


/** OP_2OVER(0x70) : Copy the third and fourth items in the stack to the top
  */
case class Op2Over() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}


/** OP_2ROT(0x71) : Move the fifth and sixth items in the stack to the top
  */
case class Op2Rot() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}


/** OP_2SWAP(0x72) : Swap the two top pairs of items in the stack
  */
case class Op2Swap() extends StackOperation {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.script.{ScriptParser, ScriptEnvironment}

trait FlowControl extends ScriptOp

/** OP_NOP(0x61) : Do nothing. An operation for OP_NOP for a flow control.
  */
case class OpNop() extends FlowControl {
  def opCode() = OpCode(0x61)

  def execute(env : ScriptEnvironment): Unit = {
    // Do nothing.
  }
}

/** OP_IF(0x63) : Execute the statements following if top of stack is not 0
  */
case class OpIf() extends FlowControl {
  def opCode() = OpCode(0x63)

  def execute(env : ScriptEnvironment): Unit = {
    // never executed.
    assert(false);
  }

  override def create(rawScript: Array[Byte], offset : Int): (ScriptOp, Int) = {
    // Call parse, check OP_ELSE, OP_ENDIF to produce thenStatementList, elseStatementList
    // Create OpCond with invert = false, thenStatementList, elseStatementList
/*
    (thenStatementList, foundOp, bytesConsumed) =
      ScriptParser.parseUntil(rawScript, offset, OpElse(), OpEndIf())
    if ( foundOp.opCode() == OpElse().opCode()) {
      (elseStatementList, foundOp2, bytesConsumed) =
         ScriptParser.parseUntil(rawScript, offset + bytesConsumed, OpEndIf())
    } else {
      assert( foundOp.opCode() == OpEndIf().opCode() )
    }

    ( OpCond(invert=false, thenStatementList, elseStatementList))
*/
    // TODO : Implement
    assert(false);
    (this, 0)
  }

}

/** OP_NOTIF(0x64) : Execute the statements following if top of stack is 0
  */
case class OpNotIf() extends FlowControl {
  def opCode() = OpCode(0x64)

  def execute(env : ScriptEnvironment): Unit = {
    // never executed.
    assert(false);
  }

  override def create(rawScript: Array[Byte], offset : Int): (ScriptOp, Int) = {
    // Call parse, check OP_ELSE, OP_ENDIF to produce thenStatementList, elseStatementList
    // Create OpCond with invert = false, thenStatementList, elseStatementList

    // TODO : Implement
    assert(false);
    (this, 0)
  }

}

/**  OP_ELSE(0x67) : Execute only if the previous statements were not executed
  */
case class OpElse() extends FlowControl {
  def opCode() = OpCode(0x67)

  def execute(env : ScriptEnvironment): Unit = {
    // never executed.
    assert(false);
  }
}

/** OP_ENDIF(0x68) : End the OP_IF, OP_NOTIF, OP_ELSE block
  */
case class OpEndIf() extends FlowControl {
  def opCode() = OpCode(0x68)

  def execute(env : ScriptEnvironment): Unit = {
    // never executed.
    assert(false);
  }
}

/** OP_VERIFY(0x69) : Check the top of the stack, halt and invalidate transaction if not TRUE
  */
case class OpVerify() extends FlowControl {
  def opCode() = OpCode(0x69)

  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

/** OP_RETURN(0x6a) : Halt and invalidate transaction
  */
case class OpReturn() extends FlowControl with InvalidScriptOpIfExecuted {
  def opCode() = OpCode(0x6a)
}


package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.block.Script
import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.{ParseResult, ScriptOpList, ScriptParser, ScriptEnvironment}
import io.scalechain.util.Utils

trait FlowControl extends ScriptOp

/** OP_NOP(0x61) : Do nothing. An operation for OP_NOP for a flow control.
  */
case class OpNop() extends FlowControl {
  def opCode() = OpCode(0x61)

  def execute(env : ScriptEnvironment): Unit = {
    // Do nothing.
  }
}

trait IfOrNotIfOp extends FlowControl {
  def create(script: Script, offset : Int, invert : Boolean): (ScriptOp, Int) = {
    // Call parse, check OP_ELSE, OP_ENDIF to produce thenStatementList, elseStatementList
    // Create OpCond with invert = false, thenStatementList, elseStatementList

    val thenPart : ParseResult =
      ScriptParser.parseUntil(script, offset, OpElse(), OpEndIf())

    // TODO : Implement equals method for ScriptOp.
    val (elseScriptOpList, elsePartBytesConsumed) =
      if ( thenPart.foundFenceOp.opCode() == OpElse().opCode()) {
        val elsePart : ParseResult =
          ScriptParser.parseUntil(script, offset + thenPart.bytesConsumed, OpEndIf())
        assert(elsePart.foundFenceOp.opCode() == OpEndIf().opCode)
        (elsePart.scriptOpList, elsePart.bytesConsumed)
      } else {
        assert( thenPart.foundFenceOp.opCode() == OpEndIf().opCode() )
        (null, 0)
      }

    ( OpCond(invert, thenPart.scriptOpList, elseScriptOpList ),
      thenPart.bytesConsumed + elsePartBytesConsumed )
  }
}

/** OP_IF(0x63) : Execute the statements following if top of stack is not 0
  */
case class OpIf() extends IfOrNotIfOp {
  def opCode() = OpCode(0x63)

  def execute(env : ScriptEnvironment): Unit = {
    // never executed, because OP_IF ~ OP_ENDIF is converted to OpCond() during the parsing phase.
    assert(false);
  }

  override def create(script: Script, offset : Int): (ScriptOp, Int) = {
    // Call parse, check OP_ELSE, OP_ENDIF to produce thenStatementList, elseStatementList
    // Create OpCond with invert = false, thenStatementList, elseStatementList
    super.create(script, offset, invert=false)
  }

}

/** OP_NOTIF(0x64) : Execute the statements following if top of stack is 0
  */
case class OpNotIf() extends IfOrNotIfOp {
  def opCode() = OpCode(0x64)

  def execute(env : ScriptEnvironment): Unit = {
    // never executed, because OP_NOTIF ~ OP_ENDIF is converted to OpCond() during the parsing phase.
    assert(false);
  }

  override def create(script: Script, offset : Int): (ScriptOp, Int) = {
    // Call parse, check OP_ELSE, OP_ENDIF to produce thenStatementList, elseStatementList
    // Create OpCond with invert = false, thenStatementList, elseStatementList
    super.create(script, offset, invert=true)
  }

}

/**  OP_ELSE(0x67) : Execute only if the previous statements were not executed
  */
case class OpElse() extends FlowControl {
  def opCode() = OpCode(0x67)

  def execute(env : ScriptEnvironment): Unit = {
    // never executed, because OP_IF ~ OP_ENDIF is converted to OpCond() during the parsing phase.
    assert(false);
  }
}

/** OP_ENDIF(0x68) : End the OP_IF, OP_NOTIF, OP_ELSE block
  */
case class OpEndIf() extends FlowControl {
  def opCode() = OpCode(0x68)

  def execute(env : ScriptEnvironment): Unit = {
    // never executed, because OP_IF ~ OP_ENDIF is converted to OpCond() during the parsing phase.
    assert(false);
  }
}

/** OP_VERIFY(0x69) : Check the top of the stack, halt and invalidate transaction if not TRUE
  */
case class OpVerify() extends FlowControl {
  def opCode() = OpCode(0x69)

  def execute(env : ScriptEnvironment): Unit = {
    super.verify(env);
  }
}

/** OP_RETURN(0x6a) : Halt and invalidate transaction
  */
case class OpReturn() extends FlowControl with InvalidScriptOpIfExecuted {
  def opCode() = OpCode(0x6a)
}


package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.proto.Script
import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.{ParseResult, ScriptOpList, ScriptParser, ScriptEnvironment}
import io.scalechain.util.Utils

trait FlowControl : ScriptOp

/** OP_NOP(0x61) : Do nothing. An operation for OP_NOP for a flow control.
  */
data class OpNop() : FlowControl {
  fun opCode() = OpCode(0x61)

  fun execute(env : ScriptEnvironment): Unit {
    // Do nothing.
  }
}

trait IfOrNotIfOp : FlowControl {
  fun create(script: Script, offset : Int, invert : Boolean): (ScriptOp, Int) {
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
data class OpIf() : IfOrNotIfOp {
  fun opCode() = OpCode(0x63)

  fun execute(env : ScriptEnvironment): Unit {
    // never executed, because OP_IF ~ OP_ENDIF is converted to OpCond() during the parsing phase.
    assert(false);
  }

  override fun create(script: Script, offset : Int): (ScriptOp, Int) {
    // Call parse, check OP_ELSE, OP_ENDIF to produce thenStatementList, elseStatementList
    // Create OpCond with invert = false, thenStatementList, elseStatementList
    super.create(script, offset, invert=false)
  }

}

/** OP_NOTIF(0x64) : Execute the statements following if top of stack is 0
  */
data class OpNotIf() : IfOrNotIfOp {
  fun opCode() = OpCode(0x64)

  fun execute(env : ScriptEnvironment): Unit {
    // never executed, because OP_NOTIF ~ OP_ENDIF is converted to OpCond() during the parsing phase.
    assert(false);
  }

  override fun create(script: Script, offset : Int): (ScriptOp, Int) {
    // Call parse, check OP_ELSE, OP_ENDIF to produce thenStatementList, elseStatementList
    // Create OpCond with invert = false, thenStatementList, elseStatementList
    super.create(script, offset, invert=true)
  }

}

/**  OP_ELSE(0x67) : Execute only if the previous statements were not executed
  */
data class OpElse() : FlowControl {
  fun opCode() = OpCode(0x67)

  fun execute(env : ScriptEnvironment): Unit {
    // never executed, because OP_IF ~ OP_ENDIF is converted to OpCond() during the parsing phase.
    assert(false);
  }
}

/** OP_ENDIF(0x68) : End the OP_IF, OP_NOTIF, OP_ELSE block
  */
data class OpEndIf() : FlowControl {
  fun opCode() = OpCode(0x68)

  fun execute(env : ScriptEnvironment): Unit {
    // never executed, because OP_IF ~ OP_ENDIF is converted to OpCond() during the parsing phase.
    assert(false);
  }
}

/** OP_VERIFY(0x69) : Check the top of the stack, halt and invalidate transaction if not TRUE
  */
data class OpVerify() : FlowControl {
  fun opCode() = OpCode(0x69)

  fun execute(env : ScriptEnvironment): Unit {
    super.verify(env)
  }
}

/** OP_RETURN(0x6a) : Halt and invalidate transaction
  */
data class OpReturn() : FlowControl with InvalidScriptOpIfExecuted {
  fun opCode() = OpCode(0x6a)
}


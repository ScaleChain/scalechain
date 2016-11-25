package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.proto.Script
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ScriptEvalException
import io.scalechain.blockchain.script.ParseResult
import io.scalechain.blockchain.script.ScriptOpList
import io.scalechain.blockchain.script.ScriptParser
import io.scalechain.blockchain.script.ScriptEnvironment
import io.scalechain.util.Utils

interface FlowControl : ScriptOp

/** OP_NOP(0x61) : Do nothing. An operation for OP_NOP for a flow control.
  */
class OpNop() : FlowControl {
  override fun opCode() = OpCode(0x61)

  override fun execute(env : ScriptEnvironment): Unit {
    // Do nothing.
  }
}

abstract class IfOrNotIfOp : FlowControl {
  fun create(script: Script, offset : Int, invert : Boolean): Pair<ScriptOp, Int> {
    // Call parse, check OP_ELSE, OP_ENDIF to produce thenStatementList, elseStatementList
    // Create OpCond with invert = false, thenStatementList, elseStatementList

    val thenPart : ParseResult =
      ScriptParser.parseUntil(script, offset, OpElse(), OpEndIf())

    // TODO : Implement equals method for ScriptOp.
    val (elseScriptOpList, elsePartBytesConsumed) =
      if ( thenPart.foundFenceOp!!.opCode() == OpElse().opCode()) {
        val elsePart : ParseResult =
          ScriptParser.parseUntil(script, offset + thenPart.bytesConsumed, OpEndIf())
        assert(elsePart.foundFenceOp!!.opCode() == OpEndIf().opCode())
        Pair(elsePart.scriptOpList, elsePart.bytesConsumed)
      } else {
        assert( thenPart.foundFenceOp!!.opCode() == OpEndIf().opCode() )
        Pair(null, 0)
      }

    return Pair<ScriptOp, Int>( OpCond(invert, thenPart.scriptOpList, elseScriptOpList ),
      thenPart.bytesConsumed + elsePartBytesConsumed )
  }
}

/** OP_IF(0x63) : Execute the statements following if top of stack is not 0
  */
class OpIf() : IfOrNotIfOp() {
  override fun opCode() = OpCode(0x63)

  override fun execute(env : ScriptEnvironment): Unit {
    // never executed, because OP_IF ~ OP_ENDIF is converted to OpCond() during the parsing phase.
    assert(false);
  }

  override fun create(script: Script, offset : Int): Pair<ScriptOp, Int> {
    // Call parse, check OP_ELSE, OP_ENDIF to produce thenStatementList, elseStatementList
    // Create OpCond with invert = false, thenStatementList, elseStatementList
    return super.create(script, offset, invert=false)
  }
}

/** OP_NOTIF(0x64) : Execute the statements following if top of stack is 0
  */
class OpNotIf() : IfOrNotIfOp() {
  override fun opCode() = OpCode(0x64)

  override fun execute(env : ScriptEnvironment): Unit {
    // never executed, because OP_NOTIF ~ OP_ENDIF is converted to OpCond() during the parsing phase.
    assert(false);
  }

  override fun create(script: Script, offset : Int): Pair<ScriptOp, Int> {
    // Call parse, check OP_ELSE, OP_ENDIF to produce thenStatementList, elseStatementList
    // Create OpCond with invert = false, thenStatementList, elseStatementList
    return super.create(script, offset, invert=true)
  }

}

/**  OP_ELSE(0x67) : Execute only if the previous statements were not executed
  */
class OpElse() : FlowControl {
  override fun opCode() = OpCode(0x67)

  override fun execute(env : ScriptEnvironment): Unit {
    // never executed, because OP_IF ~ OP_ENDIF is converted to OpCond() during the parsing phase.
    assert(false);
  }
}

/** OP_ENDIF(0x68) : End the OP_IF, OP_NOTIF, OP_ELSE block
  */
class OpEndIf() : FlowControl {
  override fun opCode() = OpCode(0x68)

  override fun execute(env : ScriptEnvironment): Unit {
    // never executed, because OP_IF ~ OP_ENDIF is converted to OpCond() during the parsing phase.
    assert(false);
  }
}

/** OP_VERIFY(0x69) : Check the top of the stack, halt and invalidate transaction if not TRUE
  */
class OpVerify() : FlowControl {
  override fun opCode() = OpCode(0x69)

  override fun execute(env : ScriptEnvironment): Unit {
    super.verify(env)
  }
}

/** OP_RETURN(0x6a) : Halt and invalidate transaction
  */
class OpReturn() : InvalidScriptOpIfExecuted {
  override fun opCode() = OpCode(0x6a)
}


package io.scalechain.blockchain.script.ops

import java.math.BigInteger

import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.ScriptEnvironment

trait Splice : ScriptOp

/** OP_CAT(0x7e) : Disabled (concatenates top two items)
  */
data class OpCat() : Splice with DisabledScriptOp
{
  fun opCode() = OpCode(0x7e)
}

/** OP_SUBSTR(0x7f) : Disabled (returns substring)
  */
data class OpSubstr() : Splice with DisabledScriptOp
{
  fun opCode() = OpCode(0x7f)
}


/** OP_LEFT(0x80) : Disabled (returns left substring)
  */
data class OpLeft() : Splice with DisabledScriptOp
{
  fun opCode() = OpCode(0x80)
}


/** OP_RIGHT(0x81) : Disabled (returns right substring)
  */
data class OpRight() : Splice with DisabledScriptOp
{
  fun opCode() = OpCode(0x81)
}

/** OP_SIZE(0x82) : Calculate string length of top item and push the result
  * Before : in
  * After  : in size
  */
data class OpSize() : Splice {
  fun opCode() = OpCode(0x82)

  fun execute(env : ScriptEnvironment): Unit {
    if (env.stack.size() < 1) {
      throw ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpSize")
    }

    val topValue = env.stack.top()
    env.stack.pushInt( BigInteger.valueOf(topValue.value.length) )
  }
}

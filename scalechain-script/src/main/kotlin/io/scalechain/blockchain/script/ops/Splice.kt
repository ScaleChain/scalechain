package io.scalechain.blockchain.script.ops

import java.math.BigInteger

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ScriptEvalException
import io.scalechain.blockchain.script.ScriptEnvironment

interface Splice : ScriptOp

/** OP_CAT(0x7e) : Disabled (concatenates top two items)
  */
class OpCat() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x7e)
}

/** OP_SUBSTR(0x7f) : Disabled (returns substring)
  */
class OpSubstr() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x7f)
}


/** OP_LEFT(0x80) : Disabled (returns left substring)
  */
class OpLeft() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x80)
}


/** OP_RIGHT(0x81) : Disabled (returns right substring)
  */
class OpRight() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x81)
}

/** OP_SIZE(0x82) : Calculate string length of top item and push the result
  * Before : in
  * After  : in size
  */
class OpSize() : Splice {
  override fun opCode() = OpCode(0x82)

  override fun execute(env : ScriptEnvironment): Unit {
    if (env.stack.isEmpty()) {
      throw ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpSize")
    }

    val topValue = env.stack.top()
    env.stack.pushInt( BigInteger.valueOf(topValue.value.size.toLong()) )
  }
}

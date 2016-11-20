package io.scalechain.blockchain.script.ops

import java.math.BigInteger

import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.ScriptEnvironment

trait Splice extends ScriptOp

/** OP_CAT(0x7e) : Disabled (concatenates top two items)
  */
case class OpCat() extends Splice with DisabledScriptOp
{
  def opCode() = OpCode(0x7e)
}

/** OP_SUBSTR(0x7f) : Disabled (returns substring)
  */
case class OpSubstr() extends Splice with DisabledScriptOp
{
  def opCode() = OpCode(0x7f)
}


/** OP_LEFT(0x80) : Disabled (returns left substring)
  */
case class OpLeft() extends Splice with DisabledScriptOp
{
  def opCode() = OpCode(0x80)
}


/** OP_RIGHT(0x81) : Disabled (returns right substring)
  */
case class OpRight() extends Splice with DisabledScriptOp
{
  def opCode() = OpCode(0x81)
}

/** OP_SIZE(0x82) : Calculate string length of top item and push the result
  * Before : in
  * After  : in size
  */
case class OpSize() extends Splice {
  def opCode() = OpCode(0x82)

  def execute(env : ScriptEnvironment): Unit = {
    if (env.stack.size() < 1) {
      throw new ScriptEvalException(ErrorCode.NotEnoughInput, "ScriptOp:OpSize")
    }

    val topValue = env.stack.top()
    env.stack.pushInt( BigInteger.valueOf(topValue.value.length) )
  }
}

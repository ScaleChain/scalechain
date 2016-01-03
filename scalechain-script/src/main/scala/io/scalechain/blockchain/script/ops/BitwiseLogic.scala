package io.scalechain.blockchain.script.ops

import java.math.BigInteger

import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.{ScriptValue, ScriptEnvironment}
import io.scalechain.blockchain.util.Utils

trait BitwiseLogic extends ScriptOp

/** OP_INVERT(0x83) : Disabled (Flip the bits of the top item)
  */
case class OpInvert() extends BitwiseLogic with DisabledScriptOp
{
  def opCode() = OpCode(0x83)
}


/** OP_AND(0x84) : Disabled (Boolean AND of two top items)
  */
case class OpAnd() extends BitwiseLogic with DisabledScriptOp
{
  def opCode() = OpCode(0x84)
}

/** OP_OR(0x85) : Disabled (Boolean OR of two top items)
  */
case class OpOr() extends BitwiseLogic with DisabledScriptOp
{
  def opCode() = OpCode(0x85)
}

/** OP_XOR(0x86) : Disabled (Boolean XOR of two top items)
  */
case class OpXor() extends BitwiseLogic with DisabledScriptOp
{
  def opCode() = OpCode(0x86)
}

/** OP_EQUAL(0x87) : Push TRUE (1) if top two items are exactly equal, push FALSE (0) otherwise
  */
case class OpEqual() extends BitwiseLogic {
  def opCode() = OpCode(0x87)

  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation(env, (value1, value2) => {
      if ( value1.value sameElements( value2.value)) {
        ScriptValue.valueOf(1L)
      } else {
        ScriptValue.valueOf(0L)
      }
    })
  }
}

/** OP_EQUALVERIFY(0x88) : Same as OP_EQUAL, but run OP_VERIFY after to halt if not TRUE
  * Before : x1 x2
  * After :
  */
case class OpEqualVerify() extends BitwiseLogic {
  def opCode() = OpCode(0x88)

  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation(env, (value1, value2) => {
      if ( value1.value sameElements( value2.value)) {
        ScriptValue.valueOf(1L)
      } else {
        ScriptValue.valueOf("")
      }
    })
    super.verify(env)
  }
}
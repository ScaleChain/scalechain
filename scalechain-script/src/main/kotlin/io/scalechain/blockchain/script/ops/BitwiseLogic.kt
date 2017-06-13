package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.script.ScriptValue
import io.scalechain.blockchain.script.ScriptEnvironment
import java.util.*

interface BitwiseLogic : ScriptOp

/** OP_INVERT(0x83) : Disabled (Flip the bits of the top item)
  */
class OpInvert() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x83)
}


/** OP_AND(0x84) : Disabled (Boolean AND of two top items)
  */
class OpAnd() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x84)
}

/** OP_OR(0x85) : Disabled (Boolean OR of two top items)
  */
class OpOr() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x85)
}

/** OP_XOR(0x86) : Disabled (Boolean XOR of two top items)
  */
class OpXor() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x86)
}

/** OP_EQUAL(0x87) : Push TRUE (1) if top two items are exactly equal, push FALSE (0) otherwise
  */
class OpEqual() : BitwiseLogic {
  override fun opCode() = OpCode(0x87)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryOperation(env, { l, r ->
      if ( Arrays.equals( l.value, r.value)) {
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
class OpEqualVerify() : BitwiseLogic {
  override fun opCode() = OpCode(0x88)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryOperation(env, { l, r ->
      if ( Arrays.equals( l.value, r.value)) {
        ScriptValue.valueOf(1L)
      } else {
        ScriptValue.valueOf("")
      }
    })
    super.verify(env)
  }
}
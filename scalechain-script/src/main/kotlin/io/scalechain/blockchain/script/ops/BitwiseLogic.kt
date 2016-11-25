package io.scalechain.blockchain.script.ops

import java.math.BigInteger

import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.{ScriptValue, ScriptEnvironment}
import io.scalechain.util.Utils

trait BitwiseLogic : ScriptOp

/** OP_INVERT(0x83) : Disabled (Flip the bits of the top item)
  */
data class OpInvert() : BitwiseLogic with DisabledScriptOp
{
  fun opCode() = OpCode(0x83)
}


/** OP_AND(0x84) : Disabled (Boolean AND of two top items)
  */
data class OpAnd() : BitwiseLogic with DisabledScriptOp
{
  fun opCode() = OpCode(0x84)
}

/** OP_OR(0x85) : Disabled (Boolean OR of two top items)
  */
data class OpOr() : BitwiseLogic with DisabledScriptOp
{
  fun opCode() = OpCode(0x85)
}

/** OP_XOR(0x86) : Disabled (Boolean XOR of two top items)
  */
data class OpXor() : BitwiseLogic with DisabledScriptOp
{
  fun opCode() = OpCode(0x86)
}

/** OP_EQUAL(0x87) : Push TRUE (1) if top two items are exactly equal, push FALSE (0) otherwise
  */
data class OpEqual() : BitwiseLogic {
  fun opCode() = OpCode(0x87)

  fun execute(env : ScriptEnvironment): Unit {
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
data class OpEqualVerify() : BitwiseLogic {
  fun opCode() = OpCode(0x88)

  fun execute(env : ScriptEnvironment): Unit {
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
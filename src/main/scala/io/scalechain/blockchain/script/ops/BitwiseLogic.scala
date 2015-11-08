package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.script.ScriptEnvironment

trait BitwiseLogic extends ScriptOp

/** OP_INVERT(0x83) : Disabled (Flip the bits of the top item)
  */
case class OpInvert() extends BitwiseLogic with DisabledScriptOp

/** OP_AND(0x84) : Disabled (Boolean AND of two top items)
  */
case class OpAnd() extends BitwiseLogic with DisabledScriptOp

/** OP_OR(0x85) : Disabled (Boolean OR of two top items)
  */
case class OpOr() extends BitwiseLogic with DisabledScriptOp

/** OP_XOR(0x86) : Disabled (Boolean XOR of two top items)
  */
case class OpXor() extends BitwiseLogic with DisabledScriptOp

/** OP_EQUAL(0x87) : Push TRUE (1) if top two items are exactly equal, push FALSE (0) otherwise
  */
case class OpEqual() extends BitwiseLogic {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

/** OP_EQUALVERIFY(0x88) : Same as OP_EQUAL, but run OP_VERIFY after to halt if not TRUE
  */
case class OpEqualVerify() extends BitwiseLogic {
  def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}
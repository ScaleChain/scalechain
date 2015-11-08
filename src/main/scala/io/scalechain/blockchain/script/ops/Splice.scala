package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.script.ScriptEnvironment

trait Splice extends ScriptOp

/** OP_CAT(0x7e) : Disabled (concatenates top two items)
  */
case class OpCat() extends Splice with DisabledScriptOp

/** OP_SUBSTR(0x7f) : Disabled (returns substring)
  */
case class OpSubstr() extends Splice with DisabledScriptOp

/** OP_LEFT(0x80) : Disabled (returns left substring)
  */
case class OpLeft() extends Splice with DisabledScriptOp

/** OP_RIGHT(0x81) : Disabled (returns right substring)
  */
case class OpRight() extends Splice with DisabledScriptOp

/** OP_SIZE(0x82) : Calculate string length of top item and push the result
  */
case class OpSize() extends Splice {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.script.ScriptEnvironment

trait ReservedWords extends ScriptOp

/** OP_RESERVED(0x50) : Halt - Invalid transaction unless found in an unexecuted OP_IF clause
  */
case class OpReserved() extends ReservedWords with InvalidScriptOpIfExecuted

/** P_VER(0x62) : Halt - Invalid transaction unless found in an unexecuted OP_IF clause
  */
case class OpVer() extends ReservedWords with InvalidScriptOpIfExecuted

/** OP_VERIF(0x65) : Halt - Invalid transaction
  */
case class OpVerIf() extends ReservedWords with AlwaysInvalidScriptOp

/** OP_VERNOTIF(0x66) : Halt - Invalid transaction
  */
case class OpVerNotIf() extends ReservedWords with AlwaysInvalidScriptOp

/** OP_RESERVED1(0x89) : Halt - Invalid transaction unless found in an unexecuted OP_IF clause
  */
case class OpReserved1() extends ReservedWords with InvalidScriptOpIfExecuted

/** OP_RESERVED2(0x8a) : Halt - Invalid transaction unless found in an unexecuted OP_IF clause
  */
case class OpReserved2() extends ReservedWords with InvalidScriptOpIfExecuted {

}
/** OP_NOP1-OP_NOP10(0xb0-0xb9) : Does nothing, ignored. A case class for OP_NOP1 ~ OP_NOP10.
  *
  * @param value The number from 1 to 10.
  */
case class OpNop(val value : Int) extends ReservedWords {
  def execute(env : ScriptEnvironment): Int= {
    // TODO : Implement
    assert(false);
    0
  }
}

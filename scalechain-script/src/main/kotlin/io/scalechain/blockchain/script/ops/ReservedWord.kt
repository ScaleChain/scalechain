package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.script.ScriptEnvironment

trait ReservedWords : ScriptOp

/** OP_RESERVED(0x50) : Halt - Invalid transaction unless found in an unexecuted OP_IF clause
  */
data class OpReserved() : ReservedWords with InvalidScriptOpIfExecuted
{
  fun opCode() = OpCode(0x50)
}

/** P_VER(0x62) : Halt - Invalid transaction unless found in an unexecuted OP_IF clause
  */
data class OpVer() : ReservedWords with InvalidScriptOpIfExecuted
{
  fun opCode() = OpCode(0x62)
}

/** OP_VERIF(0x65) : Halt - Invalid transaction
  */
data class OpVerIf() : ReservedWords with AlwaysInvalidScriptOp
{
  fun opCode() = OpCode(0x65)
}

/** OP_VERNOTIF(0x66) : Halt - Invalid transaction
  */
data class OpVerNotIf() : ReservedWords with AlwaysInvalidScriptOp
{
  fun opCode() = OpCode(0x66)
}

/** OP_RESERVED1(0x89) : Halt - Invalid transaction unless found in an unexecuted OP_IF clause
  */
data class OpReserved1() : ReservedWords with InvalidScriptOpIfExecuted
{
  fun opCode() = OpCode(0x89)
}

/** OP_RESERVED2(0x8a) : Halt - Invalid transaction unless found in an unexecuted OP_IF clause
  */
data class OpReserved2() : ReservedWords with InvalidScriptOpIfExecuted
{
  fun opCode() = OpCode(0x8a)
}

/** OP_NOP1-OP_NOP10(0xb0-0xb9) : Does nothing, ignored. A data class for OP_NOP1 ~ OP_NOP10.
  * New opcodes can be added by means of a carefully designed and executed softfork using OP_NOP1 - OP_NOP10.
  *
  * @param value The number from 1 to 10.
  */
data class OpNopN(val value : Int) : ReservedWords {
  assert(value >= 1)
  assert(value <= 10)

  fun opCode() = opCodeFromBase(0xaf, value)

  fun execute(env : ScriptEnvironment): Unit {
    // Do nothing.
  }
}

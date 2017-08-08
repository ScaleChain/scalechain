package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.script.ScriptEnvironment

interface ReservedWords : ScriptOp

/** OP_RESERVED(0x50) : Halt - Invalid transaction unless found in an unexecuted OP_IF clause
  */
class OpReserved() : InvalidScriptOpIfExecuted
{
  override fun opCode() = OpCode(0x50)
}

/** P_VER(0x62) : Halt - Invalid transaction unless found in an unexecuted OP_IF clause
  */
class OpVer() : InvalidScriptOpIfExecuted
{
  override fun opCode() = OpCode(0x62)
}

/** OP_VERIF(0x65) : Halt - Invalid transaction
  */
class OpVerIf() : AlwaysInvalidScriptOp
{
  override fun opCode() = OpCode(0x65)
}

/** OP_VERNOTIF(0x66) : Halt - Invalid transaction
  */
class OpVerNotIf() : AlwaysInvalidScriptOp
{
  override fun opCode() = OpCode(0x66)
}

/** OP_RESERVED1(0x89) : Halt - Invalid transaction unless found in an unexecuted OP_IF clause
  */
class OpReserved1() : InvalidScriptOpIfExecuted
{
  override fun opCode() = OpCode(0x89)
}

/** OP_RESERVED2(0x8a) : Halt - Invalid transaction unless found in an unexecuted OP_IF clause
  */
class OpReserved2() : InvalidScriptOpIfExecuted
{
  override fun opCode() = OpCode(0x8a)
}

/** OP_NOP1-OP_NOP10(0xb0-0xb9) : Does nothing, ignored. A data class for OP_NOP1 ~ OP_NOP10.
  * New opcodes can be added by means of a carefully designed and executed softfork using OP_NOP1 - OP_NOP10.
  *
  * @param value The number from 1 to 10.
  */
class OpNopN(val value : Int) : ReservedWords {
  init {
    assert(value >= 1)
    assert(value <= 10)
  }

  override fun opCode() = opCodeFromBase(0xaf, value)

  override fun execute(env : ScriptEnvironment): Unit {
    // Do nothing.
  }
}

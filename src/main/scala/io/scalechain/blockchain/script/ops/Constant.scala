package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.script.{ScriptValue, ScriptEnvironment}

trait Constant extends ScriptOp

/** OP_0 or OP_FALSE(0x00) : An empty array is pushed onto the stack
  */
case class Op0() extends Constant {
  def execute(env : ScriptEnvironment): Unit = {
    env.stack.push(ScriptValue.valueOf(Array[Byte]()))
  }
}

/** 1-75(0x01-0x4b) : Push the next N bytes onto the stack, where N is 1 to 75 bytes
  */
case class OpPush(val byteCount : Int, val inputValue : ScriptValue = null) extends Constant {

  def execute(env : ScriptEnvironment) : Unit = {
    // inputValue field is set by calling copyInputFrom while the script parser runs.
    // If it is null, it means that there is an internal error.
    assert(inputValue != null)
    env.stack.push( inputValue )
  }

  /** create an OpPush object by copying the input data from the raw script.
   * This is called by script parser before execution.
   * @param rawScript The raw script before it is parsed.
   * @param offset The offset where the input is read.
   * @return The number of bytes consumed to copy the input value.
   */
  override def createWithInput(rawScript : Array[Byte], offset : Int) : (ScriptOp, Int) = {
    val value = ScriptValue.valueOf(rawScript, offset, byteCount)

    ( OpPush(byteCount, value), byteCount)
  }
}

/** OP_PUSHDATA1(0x4c), OP_PUSHDATA2(0x4d), OP_PUSHDATA4(0x4e) : The next script byte contains N, push the following N bytes onto the stack
  */
case class OpPushData(val lengthBytes : Int) extends Constant {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

/** OP_1NEGATE(0x4f) : Push the value "–1" onto the stack
  */
case class Op1Negate() extends Constant {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

/** OP_1 or OP_TRUE(0x51) : Push the value "1" onto the stack
  */
case class Op1() extends Constant {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

/** A common case class for OP_2 to OP_16(0x52 to 0x60).
 * For OP_N, push the value "N" onto the stack. E.g., OP_2 pushes "2"
 *
 * @param number The value to push on to the stack. It is from 2 to 16.
 */
case class Op2Num(val number : Int) extends Constant {
  def execute(env : ScriptEnvironment): Unit = {
    // TODO : Implement
    assert(false);
  }
}

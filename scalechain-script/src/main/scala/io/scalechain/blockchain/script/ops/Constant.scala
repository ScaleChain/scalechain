package io.scalechain.blockchain.script.ops

import java.math.BigInteger
import io.scalechain.blockchain.block.Script
import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.blockchain.script.{ScriptValue, ScriptEnvironment}

import scala.collection.mutable.ArrayBuffer

trait Constant extends ScriptOp

/** OP_0 or OP_FALSE(0x00) : An empty array is pushed onto the stack
  */
case class Op0() extends Constant {
  def opCode() = OpCode(0x00)

  def execute(env : ScriptEnvironment): Unit = {
    super.pushFalse(env)
  }
}

/** 1-75(0x01-0x4b) : Push the next N bytes onto the stack, where N is 1 to 75 bytes
  */
case class OpPush(val byteCount : Int, val inputValue : ScriptValue = null) extends Constant {
  // 0x00 is the base value for opCode. The actual op code is calculated by adding byteCount.
  def opCode() = opCodeFromBase(0, byteCount)

  def execute(env : ScriptEnvironment) : Unit = {
    // inputValue field is set by calling copyInputFrom while the script parser runs.
    // If it is null, it means that there is an internal error.
    assert(inputValue != null)
    env.stack.push( inputValue )
  }

  /** create an OpPush object by copying the input data from the raw script.
   * This is called by script parser before execution.
   * @param script The raw script before it is parsed.
   * @param offset The offset where the input is read.
   * @return The number of bytes consumed to copy the input value.
   */
  override def create(script : Script, offset : Int) : (ScriptOp, Int) = {
    val value = ScriptValue.valueOf(script.data, offset, byteCount)

    ( OpPush(byteCount, value), byteCount)
  }
}

/** OP_PUSHDATA1(0x4c), OP_PUSHDATA2(0x4d), OP_PUSHDATA4(0x4e) : The next script byte contains N, push the following N bytes onto the stack
  */
case class OpPushData(val lengthBytes : Int, val inputValue : ScriptValue = null) extends Constant {
  def opCode() = {
    val opCodeMap = Map((1, 0x4c), (2, 0x4d), (4, 0x4e))
    val opCodeOption = opCodeMap.get(lengthBytes)
    OpCode( opCodeOption.get.toByte )
  }

  def execute(env : ScriptEnvironment): Unit = {
    // inputValue field is set by calling copyInputFrom while the script parser runs.
    // If it is null, it means that there is an internal error.
    assert(inputValue != null)
    env.stack.push( inputValue )
  }

  /** create an OpPushData object by copying the input data from the raw script.
    * This is called by script parser before execution.
    * @param script The raw script before it is parsed.
    * @param offset The offset where the byte count is read.
    * @return The number of bytes consumed to copy the input value.
    */
  override def create(script : Script, offset : Int) : (ScriptOp, Int) = {
    val byteCount = getByteCount(script.data, offset, lengthBytes)
    val value = ScriptValue.valueOf(script.data, offset + lengthBytes, byteCount)

    ( OpPushData(lengthBytes, value), lengthBytes + byteCount)
  }

  /** Get how much bytes we need to read from script to get the inputValue to push.
   *
   * @param rawScript The raw script before it is parsed.
   * @param offset The offset where the byte count is read.
   * @param length The length of bytes to read to get the byte count.
   * @return The byte count read form the raw script.
   */
  protected def getByteCount(rawScript : Array[Byte], offset : Int, length : Int) : Int = {
    if (offset + length > rawScript.length) {
      throw new ScriptEvalException(ErrorCode.NotEnoughScriptData)
    }

    var result = 0L

    if (length ==1) {
      result = (rawScript(offset) & 0xFF).toLong
    } else if (length == 2) {
      result = (rawScript(offset) & 0xFF).toLong +
               ((rawScript(offset+1) & 0xFF).toLong << 8)
    } else if (length == 4) {
      result = (rawScript(offset) & 0xFF).toLong +
               ((rawScript(offset+1) & 0xFF).toLong << 8) +
               ((rawScript(offset+1) & 0xFF).toLong << 16) +
               ((rawScript(offset+1) & 0xFF).toLong << 24)
    } else {
      assert(false);
    }
    assert(result < Int.MaxValue)
    result.toInt
  }
}

/** OP_1NEGATE(0x4f) : Push the value "–1" onto the stack
  */
case class Op1Negate() extends Constant {
  def opCode() = OpCode(0x4f)

  def execute(env : ScriptEnvironment): Unit = {
    env.stack.pushInt( BigInteger.valueOf(-1))
  }
}

/** OP_1 or OP_TRUE(0x51) : Push the value "1" onto the stack
  */
case class Op1() extends Constant {
  def opCode() = OpCode(0x51)

  def execute(env : ScriptEnvironment): Unit = {
    super.pushTrue(env)
  }
}

/** A common case class for OP_2 to OP_16(0x52 to 0x60).
 * For OP_N, push the value "N" onto the stack. E.g., OP_2 pushes "2"
 *
 * @param number The value to push on to the stack. It is from 2 to 16.
 */
case class OpNum(val number : Int) extends Constant {
  assert(number >= 2)
  assert(number <= 16)
  // 0x50 is the base value for opCode. The actual op code is calculated by adding byteCount.
  def opCode() = opCodeFromBase(0x50, number)

  def execute(env : ScriptEnvironment): Unit = {
    assert(2 <= number && number <=16)
    env.stack.pushInt( BigInteger.valueOf(number))
  }
}

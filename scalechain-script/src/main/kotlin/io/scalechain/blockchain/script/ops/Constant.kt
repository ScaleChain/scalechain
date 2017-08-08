package io.scalechain.blockchain.script.ops

import java.math.BigInteger
import io.scalechain.blockchain.proto.Script
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ScriptEvalException
import io.scalechain.blockchain.script.ScriptValue
import io.scalechain.blockchain.script.ScriptEnvironment

interface Constant : ScriptOp

/** OP_0 or OP_FALSE(0x00) : An empty array is pushed onto the stack
  */
class Op0(val dummy : Int = 1) : Constant {
  override fun opCode() = OpCode(0x00)

  override fun execute(env : ScriptEnvironment): Unit {
    super.pushFalse(env)
  }
}

/** 1-75(0x01-0x4b) : Push the next N bytes onto the stack, where N is 1 to 75 bytes
  */
class OpPush(val byteCount : Int, val inputValue : ScriptValue? = null) : Constant {
  // 0x00 is the base value for opCode. The actual op code is calculated by adding byteCount.
  override fun opCode() = opCodeFromBase(0, byteCount)

  override fun execute(env : ScriptEnvironment) : Unit {
    // inputValue field is set by calling copyInputFrom while the script parser runs.
    // If it is null, it means that there is an internal error.
    if (inputValue == null) throw AssertionError()
    env.stack.push( inputValue )
  }

  /** create an OpPush object by copying the input data from the raw script.
   * This is called by script parser before execution.
    *
    * @param script The raw script before it is parsed.
   * @param offset The offset where the input is read.
   * @return The number of bytes consumed to copy the input value.
   */
  override fun create(script : Script, offset : Int) : Pair<ScriptOp, Int> {
    //println(s"Script.length : ${script.data.length}, offset : $offset, byteCount : $byteCount")
    val value = ScriptValue.valueOf(script.data.array, offset, byteCount)

    return Pair( OpPush(byteCount, value), byteCount)
  }

  /** Serialize the script operation into an array buffer.
    *
    * @param buffer The array buffer where the script is serialized.
    */
  override fun serialize(buffer: MutableList<Byte>): Unit {
    buffer.add(opCode().code.toByte())
    if (inputValue == null) throw AssertionError()
    // BUGBUG : Optimize : Can we avoid calling inputValue.value.toList()?
    buffer.addAll(inputValue.value.toList())
  }

  companion object {
    val MAX_SIZE = 75
    fun from(value : ByteArray) : OpPush {
      assert(value.size <= MAX_SIZE)
      return OpPush(value.size, ScriptValue.valueOf(value))
    }
  }
}

/** OP_PUSHDATA1(0x4c), OP_PUSHDATA2(0x4d), OP_PUSHDATA4(0x4e) : The next script byte contains N, push the following N bytes onto the stack
  */
class OpPushData(val lengthBytes : Int, val inputValue : ScriptValue? = null) : Constant {
  override fun opCode() : OpCode {
    val opCodeMap = mapOf( 1 to 0x4c, 2 to 0x4d, 4 to 0x4e)
    val opCodeOption = opCodeMap.get(lengthBytes)
    return OpCode( opCodeOption!!.toShort() )
  }

  override fun execute(env : ScriptEnvironment): Unit {
    // inputValue field is set by calling copyInputFrom while the script parser runs.
    // If it is null, it means that there is an internal error.
    if (inputValue  == null) throw AssertionError()
    env.stack.push( inputValue )
  }

  /** create an OpPushData object by copying the input data from the raw script.
    * This is called by script parser before execution.
    *
    * @param script The raw script before it is parsed.
    * @param offset The offset where the byte count is read.
    * @return The number of bytes consumed to copy the input value.
    */
  override fun create(script : Script, offset : Int) : Pair<ScriptOp, Int> {
    val byteCount = getByteCount(script.data.array, offset, lengthBytes)
    val value = ScriptValue.valueOf(script.data.array, offset + lengthBytes, byteCount)

    return Pair( OpPushData(lengthBytes, value), lengthBytes + byteCount)
  }

  /** Get how much bytes we need to read from script to get the inputValue to push.
   *
   * @param rawScript The raw script before it is parsed.
   * @param offset The offset where the byte count is read.
   * @param length The length of bytes to read to get the byte count.
   * @return The byte count read form the raw script.
   */
  protected fun getByteCount(rawScript : ByteArray, offset : Int, length : Int) : Int {
    if (offset + length > rawScript.size) {
      throw ScriptEvalException(ErrorCode.NotEnoughScriptData, "ScriptOp:OpPushData")
    }

    var result = 0L

    if (length ==1) {
      result = (rawScript[offset].toInt() and 0xFF).toLong()
    } else if (length == 2) {
      result = (rawScript[offset].toInt() and 0xFF).toLong() +
               ((rawScript[offset+1].toInt() and 0xFF).toLong() shl 8)
    } else if (length == 4) {
      result = (rawScript[offset].toInt() and 0xFF).toLong() +
               ((rawScript[offset+1].toInt() and 0xFF).toLong() shl 8) +
               ((rawScript[offset+2].toInt() and 0xFF).toLong() shl 16) +
               ((rawScript[offset+3].toInt() and 0xFF).toLong() shl 24)
    } else {
      assert(false);
    }
    assert(result < Int.MAX_VALUE)
    return result.toInt()
  }

  /** Serialize the script operation into an array buffer.
    *
    * @param buffer The array buffer where the script is serialized.
    */
  override fun serialize(buffer: MutableList<Byte>): Unit {
    // TODO : BUGBUG : Need to implement
    assert(false)
//    buffer.append(opCode().code.toByte)
//    buffer.append(inputValue.value)
  }

}

/** OP_1NEGATE(0x4f) : Push the value "–1" onto the stack
  */
class Op1Negate() : Constant {
  override fun opCode() = OpCode(0x4f)

  override fun execute(env : ScriptEnvironment): Unit {
    env.stack.pushInt( BigInteger.valueOf(-1))
  }
}

/** OP_1 or OP_TRUE(0x51) : Push the value "1" onto the stack
  */
class Op1() : Constant {
  override fun opCode() = OpCode(0x51)

  override fun execute(env : ScriptEnvironment): Unit {
    super.pushTrue(env)
  }
}

/** A common data class for OP_2 to OP_16(0x52 to 0x60).
 * For OP_N, push the value "N" onto the stack. E.g., OP_2 pushes "2"
 *
 * @param num The value to push on to the stack. It is from 2 to 16.
 */
class OpNum(val num : Int) : Constant {
  init {
    assert(num >= 2)
    assert(num <= 16)
  }

  // 0x50 is the base value for opCode. The actual op code is calculated by adding byteCount.
  override fun opCode() = opCodeFromBase(0x50, num)

  override fun execute(env : ScriptEnvironment): Unit {
    assert(2 <= num && num <=16)
    env.stack.pushInt( BigInteger.valueOf(num.toLong()))
  }
}

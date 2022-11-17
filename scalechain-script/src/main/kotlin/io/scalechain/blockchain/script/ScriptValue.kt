package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.ScriptEvalException
import io.scalechain.util.Utils


import io.scalechain.util.HexUtil
import java.util.*


interface ScriptValue {
  val value : ByteArray
  fun copy() : ScriptValue

  companion object {
    /**
     * Get a ScriptValue which has a byte array of a given string.
     *
     * @param value The string which will be converted to a byte array.
     * @return The ScriptValue we created.
     */
    fun valueOf(value : String) : ScriptValue {
      return ScriptBytes( value.toByteArray() )
    }

    /** Get a ScriptValue which has the given byte array.
     *
     * @param value The byte array.
     * @return The ScriptValue we created.
     */
    fun valueOf(value : ByteArray) : ScriptValue {
      return ScriptBytes( value )
    }

    /** Get a ScriptValue by copying a specific area of a given byte array.
     *
     * @param source The source byte array
     * @param offset The offset to the source byte array.
     * @param length The number of bytes to copy.
     * @return The ScriptValue that has the given area of the byte array.
     */
    fun valueOf(source : ByteArray, offset : Int, length : Int ) : ScriptValue {
      val bytes = Arrays.copyOfRange(source, offset, offset + length)
      return ScriptBytes(bytes)
    }

    /** Get a ScriptValue which has the given long value.
     *
     * @param value The long value.
     * @return The ScriptValue we created.
     */
    fun valueOf(value : Long) : ScriptValue {
      return ScriptInteger( BigInteger.valueOf( value ) )
    }

    /**
     *
     * @param value
     * @return
     */
    fun valueOf(value:BigInteger) : ScriptValue {
      return ScriptInteger( value )
    }

    fun encodeStackInt(value: BigInteger): ByteArray {
      return Utils.reverseBytes(Utils.encodeMPI(value, false))
    }

    fun decodeStackInt(encoded: ByteArray): BigInteger {
      if (encoded.size > 4) throw ScriptEvalException(ErrorCode.TooBigScriptInteger, "The integer stack value to decode has more than 4 bytes.")
      return Utils.castToBigInteger(encoded)
    }
  }
}

data class ScriptInteger(val bigIntValue:BigInteger) : ScriptValue {
  override val value = ScriptValue.encodeStackInt( bigIntValue )
  override fun copy() : ScriptValue = ScriptInteger(bigIntValue)
  override fun toString() = "ScriptIntger($bigIntValue)"

  fun canEqual(a: Any) = a is ScriptInteger

  override fun equals(other: Any?): Boolean {
    return when (other) {
        null -> return false
        is ScriptInteger -> other.canEqual(this) && other.value.contentEquals(this.value)
      else -> false
    }
  }

  override fun hashCode():Int {
    return value.contentHashCode()
  }
}

data class ScriptBytes(val bytesValue:ByteArray) : ScriptValue {
  override val value = bytesValue
  override fun copy() : ScriptValue = ScriptBytes(bytesValue)
  override fun toString() = "ScriptBytes(${HexUtil.kotlinHex(bytesValue)})"

  fun canEqual(a: Any) = a is ScriptBytes

  override fun equals(other: Any?): Boolean {
    return when (other) {
        null -> return false
        is ScriptBytes -> other.canEqual(this) && other.value.contentEquals(this.value)
      else -> false
    }
  }

  override fun hashCode():Int {
    return value.contentHashCode()
  }

}


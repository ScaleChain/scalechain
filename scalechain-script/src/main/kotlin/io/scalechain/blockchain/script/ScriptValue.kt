package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.util.{ByteArray, HexUtil, Utils}
import HexUtil._


object ScriptValue {
  /**
   * Get a ScriptValue which has a byte array of a given string.
 *
   * @param value The string which will be converted to a byte array.
   * @return The ScriptValue we created.
   */
  fun valueOf(value : String) : ScriptValue {
    ScriptBytes( value.getBytes() )
  }

  /** Get a ScriptValue which has the given byte array.
   *
   * @param value The byte array.
    * @return The ScriptValue we created.
   */
  fun valueOf(value : Array<Byte>) : ScriptValue {
    ScriptBytes( value )
  }

  /** Get a ScriptValue by copying a specific area of a given byte array.
   *
   * @param source The source byte array
   * @param offset The offset to the source byte array.
   * @param length The number of bytes to copy.
   * @return The ScriptValue that has the given area of the byte array.
   */
  fun valueOf(source : Array<Byte>, offset : Int, length : Int ) : ScriptValue {
    val bytes = Array<Byte>(length)
    Array.copy(source, offset, bytes, 0, length)
    ScriptBytes(bytes)
  }

  /** Get a ScriptValue which has the given long value.
   *
   * @param value The long value.
   * @return The ScriptValue we created.
   */
  fun valueOf(value : Long) : ScriptValue {
    ScriptInteger( BigInteger.valueOf( value ) )
  }

  /**
   *
   * @param value
   * @return
   */
  fun valueOf(value:BigInteger) : ScriptValue {
    ScriptInteger( value )
  }

  fun encodeStackInt(value: BigInteger): Array<Byte> {
    return Utils.reverseBytes(Utils.encodeMPI(value, false))
  }

  fun decodeStackInt(encoded: Array<Byte>): BigInteger {
    if (encoded.length > 4) throw ScriptEvalException(ErrorCode.TooBigScriptInteger, "The integer stack value to decode has more than 4 bytes.")
    return Utils.castToBigInteger(encoded)
  }
}

trait ScriptValue {
  val value : Array<Byte>
  fun copy() : ScriptValue

  fun canEqual(a: Any) = a.isInstanceOf<ScriptValue>

  override fun equals(that: Any): Boolean =
    that match {
      case that: ScriptValue => that.canEqual(this) && that.value.sameElements(this.value)
      case _ => false
    }

  override fun hashCode:Int {
    val prime = 31
    var result = 1
    for (b : Byte <- value ) {
      result = prime * result + b;
    }
    return result
  }
}

data class ScriptInteger(val bigIntValue:BigInteger) : ScriptValue {
  override val value = ScriptValue.encodeStackInt( bigIntValue )
  fun copy() : ScriptValue = ScriptInteger(bigIntValue)
  override fun toString() = s"ScriptIntger($bigIntValue)"
  /*
  override fun canEqual(that:Any) = super.canEqual(that)
  override fun equals(that:Any) : Boolean = super.equals(that)
  override fun hashCode:Int = super.hashCode
  */
}

data class ScriptBytes(bytesValue:ByteArray) : ScriptValue {
  override val value = bytesValue.array
  fun copy() : ScriptValue = ScriptBytes(bytesValue)
  override fun toString() = s"ScriptBytes(${scalaHex(bytesValue.array)})"
  /*
  override fun canEqual(that:Any) = super.canEqual(that)
  override fun equals(that:Any) : Boolean = super.equals(that)
  override fun hashCode:Int = super.hashCode
  */
}


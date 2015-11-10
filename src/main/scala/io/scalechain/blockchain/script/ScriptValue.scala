package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.blockchain.util.Utils


object ScriptValue {
  /**
   * Get a ScriptValue which has a byte array of a given string.
   * @param value The string which will be converted to a byte array.
   * @return The ScriptValue we created.
   */
  def valueOf(value : String) : ScriptValue = {
    ScriptBytes( value.getBytes() )
  }

  /** Get a ScriptValue which has the given byte array.
   *
   * @param value The byte array.
    * @return The ScriptValue we created.
   */
  def valueOf(value : Array[Byte]) : ScriptValue = {
    ScriptBytes( value )
  }

  /** Get a ScriptValue by copying a specific area of a given byte array.
   *
   * @param source The source byte array
   * @param offset The offset to the source byte array.
   * @param length The number of bytes to copy.
   * @return The ScriptValue that has the given area of the byte array.
   */
  def valueOf(source : Array[Byte], offset : Int, length : Int ) : ScriptValue = {
    val bytes = new Array[Byte](length)
    Array.copy(source, offset, bytes, 0, length)
    ScriptBytes(bytes)
  }

  /** Get a ScriptValue which has the given long value.
   *
   * @param value The long value.
   * @return The ScriptValue we created.
   */
  def valueOf(value : Long) : ScriptValue = {
    ScriptInteger( BigInteger.valueOf( value ) )
  }

  /**
   *
   * @param value
   * @return
   */
  def valueOf(value:BigInteger) : ScriptValue = {
    ScriptInteger( value )
  }
}

trait ScriptValue {
  val value : Array[Byte]
  def copy() : ScriptValue
}

case class ScriptInteger(val bigIntValue:BigInteger) extends ScriptValue {
  override val value = Utils.encodeStackInt( bigIntValue )
  def copy() : ScriptValue = {
    ScriptInteger(bigIntValue)
  }
}

case class ScriptBytes(bytesValue:Array[Byte]) extends ScriptValue {
  override val value = bytesValue
  def copy() : ScriptValue = {
    ScriptBytes(bytesValue)
  }
}


package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.blockchain.util.Utils
import io.scalechain.util.HexUtil._


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

  def canEqual(a: Any) = a.isInstanceOf[ScriptValue]

  override def equals(that: Any): Boolean =
    that match {
      case that: ScriptValue => that.canEqual(this) && that.value.sameElements(this.value)
      case _ => false
    }

  override def hashCode:Int = {
    val prime = 31
    var result = 1
    for (b : Byte <- value ) {
      result = prime * result + b;
    }
    return result
  }
}

case class ScriptInteger(val bigIntValue:BigInteger) extends ScriptValue {
  override val value = Utils.encodeStackInt( bigIntValue )
  def copy() : ScriptValue = ScriptInteger(bigIntValue)
  override def toString() = s"ScriptIntger($bigIntValue)"
  /*
  override def canEqual(that:Any) = super.canEqual(that)
  override def equals(that:Any) : Boolean = super.equals(that)
  override def hashCode:Int = super.hashCode
  */
}

case class ScriptBytes(bytesValue:Array[Byte]) extends ScriptValue {
  override val value = bytesValue
  def copy() : ScriptValue = ScriptBytes(bytesValue)
  override def toString() = s"ScriptBytes(${scalaHex(bytesValue)})"
  /*
  override def canEqual(that:Any) = super.canEqual(that)
  override def equals(that:Any) : Boolean = super.equals(that)
  override def hashCode:Int = super.hashCode
  */
}


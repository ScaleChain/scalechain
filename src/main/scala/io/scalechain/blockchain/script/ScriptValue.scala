package io.scalechain.blockchain.script

trait ScriptValue

case class NumberValue(value : Int) extends ScriptValue
{
  def isTrue(): Boolean = {
    value != 0
  }
}


package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.blockchain.util.Utils


import scala.collection.mutable

/** Script Execution Stack. It holds data that Bitcoin Scripts push and pop.
 * Ex> OP_ADD pops two integer values from the stack, and pushes the result, which is an integer value that adds the two integers on to the stack.
 */
class ScriptStack {
  val stack = new mutable.Stack[ScriptValue]
  /** Push a ScriptValue on to the top of the stack.
   *
   * @param value
   */
  def push(value : ScriptValue ): Unit = {
    stack.push(value);
  }

  /** Pop a ScriptValue from the top of the stack.
   *
   * @return
   */
  def pop() : ScriptValue = {
    stack.pop();
  }

  /** Push an integer value on to the top of the stack.
   *
   * @param value The value to push
   */
  def pushInt(value : BigInteger): Unit = {
    val scriptValue = ScriptValue( Utils.encodeStackInt(value) )
    push(scriptValue)
  }

  /** Pop an integer value from the top of the stack.
   *
   * @return The popped value.
   */
  def popInt() : BigInteger  = {
    val scriptValue = pop()
    val value : BigInteger  = Utils.decodeStackInt(scriptValue.value)
    value
  }
}

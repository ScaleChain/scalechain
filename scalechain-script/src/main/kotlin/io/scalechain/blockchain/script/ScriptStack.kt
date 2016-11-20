package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.blockchain.{ErrorCode, ErrorCode$, ScriptEvalException}
import io.scalechain.util.Utils


import scala.collection.mutable

/** Script Execution Stack. It holds data that Bitcoin Scripts push and pop.
 * Ex> OP_ADD pops two integer values from the stack, and pushes the result, which is an integer value that adds the two integers on to the stack.
 */
class ScriptStack {
  /**
   * Use an array buffer to implement a script stack.
   * We can not use mutable.Stack, because it lacks a method that remove an n-th element.
   *
   * The bottom of the stack is the first element in this array.
   * The top of the stack is the last element of this array.
   */
  val array = new mutable.ArrayBuffer[ScriptValue]

  /**
   * Convert the stack index to the array index on the array field.
   * - Stack index 0 means top of the stack and it maps to array.length-1
   * - Stack index array.length-1 means the bottom of the stack and it maps to 0
   * @param stackIndex
   * @return
   */
  def toArrayIndex(stackIndex:Int) : Int = {
    array.length -1 -stackIndex
  }

  /** Push a ScriptValue on to the top of the stack.
   *
   * @param value
   */
  def push(value : ScriptValue ): Unit = {
    // The top of the stack is the end of the array.
    // Just append the element to the end of the array.
    array.append(value)
  }

  /** Pop a ScriptValue from the top of the stack.
   *
   * @return
   */
  def pop() : ScriptValue = {
    // The top of the stack is the end of the array.
    // Get rid of the last element of the array.
    val popped = array.remove( toArrayIndex(0) )
    popped
  }

  /** Get the top element without popping it.
    *
    * @return The top element.
    */
  def top() : ScriptValue = {
    this.apply(0)
  }

  /** Retrieve n-th element from stack, where top of stack has index 0.
   *
   * @param index The index from the top of the stack.
   * @return The n-th element.
   */
  def apply(index : Int) : ScriptValue = {
    array.apply( toArrayIndex(index) )
  }

  /** Remove the N-th element on the stack.
   * - The top element : N = 0
   * - The element right below the top element : N = 1
   */
  def remove(index : Int) : ScriptValue = {
    val removedValue = array.remove( toArrayIndex(index) )
    removedValue
  }

  /** Inserts new elements at a given index into this stack.
   *
   * @param index The index where the new element will exist after the insertion.
   * @param value The value to insert into this stack.
   */
  // TODO : Write a unit test for every edge cases for this method.
  def insert(index :Int, value : ScriptValue) : Unit = {
    array.insert( toArrayIndex(index), value)
  }

  /** Get the number of elements in the stack.
   *
   * @return The number of elements.
   */
  def size() : Int = {
    array.size
  }

  /** Push a big integer value on the top of the stack.
   *
   * @param value The value to push
   */
  def pushInt(value : BigInteger): Unit = {
    val scriptValue = ScriptValue.valueOf( value )
    push(scriptValue)
  }

  /** Pop an integer value from the top of the stack.
   *
   * @return The popped value.
   */
  def popInt() : BigInteger  = {
    val scriptValue = pop()
    val value : BigInteger  = ScriptValue.decodeStackInt(scriptValue.value)
    value
  }

/*
  override def toString() : String = {
    s"ScriptStack[${array.mkString(",")}}]"
  }
*/
}

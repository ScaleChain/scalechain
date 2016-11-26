package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.util.Utils

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
  val array = arrayListOf<ScriptValue>()

  /**
   * Convert the stack index to the array index on the array field.
   * - Stack index 0 means top of the stack and it maps to array.length-1
   * - Stack index array.length-1 means the bottom of the stack and it maps to 0
   * @param stackIndex
   * @return
   */
  fun toArrayIndex(stackIndex:Int) : Int {
    return array.size -1 -stackIndex
  }

  /** Push a ScriptValue on to the top of the stack.
   *
   * @param value
   */
  fun push(value : ScriptValue ): Unit {
    // The top of the stack is the end of the array.
    // Just append the element to the end of the array.
    array.add(value)
  }

  /** Pop a ScriptValue from the top of the stack.
   *
   * @return
   */
  fun pop() : ScriptValue {
    // The top of the stack is the end of the array.
    // Get rid of the last element of the array.
    val popped = array.removeAt( toArrayIndex(0) )
    return popped
  }

  /** Get the top element without popping it.
    *
    * @return The top element.
    */
  fun top() : ScriptValue {
    return this.get(0)
  }

  /** Retrieve n-th element from stack, where top of stack has index 0.
   *
   * @param index The index from the top of the stack.
   * @return The n-th element.
   */
  fun get(index : Int) : ScriptValue {
    return array.elementAt( toArrayIndex(index) )
  }

  /** Remove the N-th element on the stack.
   * - The top element : N = 0
   * - The element right below the top element : N = 1
   */
  fun remove(index : Int) : ScriptValue {
    val removedValue = array.removeAt( toArrayIndex(index) )
    return removedValue
  }

  /** Inserts elements at a given index into this stack.
   *
   * @param index The index where the new element will exist after the insertion.
   * @param value The value to insert into this stack.
   */
  // TODO : Write a unit test for every edge cases for this method.
  fun insert(index :Int, value : ScriptValue) : Unit {
    array.add( toArrayIndex(index), value)
  }

  /** Get the number of elements in the stack.
   *
   * @return The number of elements.
   */
  fun size() : Int {
    return array.size
  }

  /** Push a big integer value on the top of the stack.
   *
   * @param value The value to push
   */
  fun pushInt(value : BigInteger) {
    val scriptValue = ScriptValue.valueOf( value )
    push(scriptValue)
  }

  /** Pop an integer value from the top of the stack.
   *
   * @return The popped value.
   */
  fun popInt() : BigInteger  {
    val scriptValue = pop()
    val value : BigInteger  = ScriptValue.decodeStackInt(scriptValue.value)
    return value
  }

/*
  override fun toString() : String {
    s"ScriptStack<${array.mkString(",")}}>"
  }
*/
}

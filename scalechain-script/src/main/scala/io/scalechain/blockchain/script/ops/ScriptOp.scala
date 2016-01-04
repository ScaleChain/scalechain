package io.scalechain.blockchain.script.ops

import java.math.BigInteger
import io.scalechain.blockchain.block.Script
import io.scalechain.blockchain.script.{ScriptValue, ScriptEnvironment}
import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}
import io.scalechain.util.Utils

import scala.collection.mutable.ArrayBuffer

/** The OP code of an operation.
 *
 * @param code The OP code.
 */
case class OpCode(code : Short) {
  assert(code < 255)
}

trait ScriptOp {
  def opCode() : OpCode

 /** Execute the script operation using the given script execution environment.
   *
   * @param env The script execution environment.
   */
  def execute(env : ScriptEnvironment) : Unit


  /**
   * Copy a region of the raw script byte array, and create a ScriptOp by copying the data in the region.
   * For example, OP_PUSHDATA1(0x4c) reads one byte from the offset to get the number of bytes to copy from the raw Script.
   * And then, it copies that number of bytes from the raw Script and creates OpPushData with the copied data.

   * OpPushData will use the copied data when executed.
   *
   * For most of operations, this function simply returns the same object without copying any data.
   * Ex> OP_ADD just uses stack, without copying data from the raw script.
   *
   * This method is called while a raw script is parsed. After the script is parsed,
   * An instance of ScriptOp'subclass will have a ScriptValue field which has the copied data.
   *
   * Also, OP_CODESEPARATOR overrides this method to get the offset(=programCounter) parameter to store it
   * in the script execution environment.
   * The stored program counter will be used to find out the fence on the raw script
   * for checking signature by OP_CHECKSIG and OP_CHECKMULTISIG.
   *
   * @param script The raw script before it is parsed.
   * @param offset The offset where the input is read.
   * @return The number of bytes consumed to copy the input value.
   */
  def create(script : Script, offset : Int) : (ScriptOp, Int) = {
    (this, 0)
  }

  /**
   * Calculate an OP code from a base OP code and an index.
   * Ex> OpPush, 1-75(0x01-0x4b), returns 1 for OpPush(1), and OpPush has the baseOpCode 0.
   * @param baseOpCode The base OP code where the index is added to calculate the OP code.
   * @param index The index from the base OP code.
   * @return The calculated OP code.
   */
  protected def opCodeFromBase(baseOpCode: Int, index : Int) : OpCode = {
    val result = baseOpCode + index

    OpCode(result.toShort)
  }

  /** Verify if the top value of the stack is true. Halt script execution if false.
   *
   * @param env The script execution environment.
   * @throws ScriptEvalException if the top value of the stack was not true. code : ErrorCode.InvalidTransaction
   */
  def verify(env: ScriptEnvironment) : Unit = {
    val top = env.stack.pop()

    if (!Utils.castToBool(top.value)) {
      throw new ScriptEvalException(ErrorCode.InvalidTransaction)
    }
  }

  /** Push a false value on top of the stack.
   *
   * @param env The script execution environment.
   */
  def pushFalse(env:ScriptEnvironment) : Unit = {
    env.stack.push(ScriptValue.valueOf(Array[Byte]()))
  }


  /** Push a true value on top of the stack.
   *
   * @param env The script execution environment.
   */
  def pushTrue(env:ScriptEnvironment) : Unit = {
    env.stack.pushInt( BigInteger.valueOf(1))
  }

  /** Serialize the script operation into an array buffer.
   *
   * @param buffer The array buffer where the script is serialized.
   */
  def serialize(buffer: ArrayBuffer[Byte]): Unit = {
    buffer.append(opCode().code.toByte)
  }

  def unaryOperation(env : ScriptEnvironment, mutate : (ScriptValue) => (ScriptValue) ): Unit = {
    val value1 =  env.stack.pop()

    val result = mutate( value1 )

    env.stack.push( result )
  }

  def binaryOperation(env : ScriptEnvironment, mutate : (ScriptValue, ScriptValue) => (ScriptValue) ): Unit = {
    val value2 = env.stack.pop()
    val value1 =  env.stack.pop()

    val result = mutate( value1, value2 )

    env.stack.push( result )
  }

  def ternaryOperation(env : ScriptEnvironment, mutate : (ScriptValue, ScriptValue, ScriptValue) => (ScriptValue) ): Unit = {
    val value3 = env.stack.pop()
    val value2 = env.stack.pop()
    val value1 =  env.stack.pop()

    val result = mutate( value1, value2, value3 )

    env.stack.push( result )
  }

}

trait DisabledScriptOp {
  def execute(env : ScriptEnvironment) : Unit = {
    throw new ScriptEvalException(ErrorCode.DisabledScriptOperation)
  }
}

/**
 * Just for checking if an operation does not support opCode() method.
 */
trait ScriptOpWithoutCode extends ScriptOp {

}

/** The script operations are only for internal script execution engine.
 *
 */
trait InternalScriptOp {
  def execute(env : ScriptEnvironment) : Unit = {
    // do nothing.
  }
}

trait AlwaysInvalidScriptOp {
  /** Because we check if there is any *always* invalid script operation before executing the script,
   * the execute method should never run. So we implement this method to hit an assertion.
   *
   * @param env
   */
  def execute(env : ScriptEnvironment) : Unit = {
    assert(false)
  }
}

trait InvalidScriptOpIfExecuted {
  def execute(env : ScriptEnvironment) : Unit = {
    throw new ScriptEvalException(ErrorCode.InvalidTransaction)
  }
}





















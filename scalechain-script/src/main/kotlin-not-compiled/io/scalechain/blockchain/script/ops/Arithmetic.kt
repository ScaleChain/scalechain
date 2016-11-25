package io.scalechain.blockchain.script.ops

import java.math.BigInteger

import io.scalechain.blockchain.script.{ScriptValue, ScriptEnvironment}
import io.scalechain.blockchain.{ScriptEvalException, ErrorCode, FatalException}
import io.scalechain.util.Utils

trait Arithmetic : ScriptOp
{
  fun unaryIntOperation(env : ScriptEnvironment, mutate : (Long) => (Long) ): Unit {
    super.unaryOperation(env, (value1 : ScriptValue) => {
      val intValue1 = ScriptValue.decodeStackInt(value1.value)

      val intResult = mutate(intValue1.longValue())
      ScriptValue.valueOf( intResult )
    })
  }

  fun binaryIntOperation(env : ScriptEnvironment, mutate : (Long, Long) => (Long) ): Unit {
    super.binaryOperation(env, (value1 : ScriptValue, value2 : ScriptValue) => {
      val intValue1 = ScriptValue.decodeStackInt(value1.value)
      val intValue2 = ScriptValue.decodeStackInt(value2.value)

      val intResult = mutate(intValue1.longValue(), intValue2.longValue())
      ScriptValue.valueOf(intResult)
    })
  }

  fun ternaryIntOperation(env : ScriptEnvironment, mutate : (Long, Long, Long) => (Long) ): Unit {
    super.ternaryOperation(env, (value1 : ScriptValue, value2 : ScriptValue, value3 : ScriptValue) => {
      val intValue1 = ScriptValue.decodeStackInt(value1.value)
      val intValue2 = ScriptValue.decodeStackInt(value2.value)
      val intValue3 = ScriptValue.decodeStackInt(value3.value)

      val intResult = mutate(intValue1.longValue(), intValue2.longValue(), intValue3.longValue())
      ScriptValue.valueOf(intResult)
    })
  }
}

/** OP_1ADD(0x8b) : Add 1 to the top item
 */
data class Op1Add() : Arithmetic {
  fun opCode() = OpCode(0x8b)

  fun execute(env : ScriptEnvironment): Unit {
    unaryIntOperation(env, _ + 1L )
  }
}

/** OP_1SUB(0x8c) : Subtract 1 from the top item
 */
data class Op1Sub() : Arithmetic {
  fun opCode() = OpCode(0x8c)

  fun execute(env : ScriptEnvironment): Unit {
    unaryIntOperation(env, _ - 1L )
  }
}

/** OP_2MUL(0x8d) : Disabled (multiply top item by 2)
 */
data class Op2Mul() : Arithmetic with DisabledScriptOp
{
  fun opCode() = OpCode(0x8d)
}
/** OP_2DIV(0x8e) : Disabled (divide top item by 2)
 */
data class Op2Div() : Arithmetic with DisabledScriptOp
{
  fun opCode() = OpCode(0x8e)
}

/** OP_NEGATE(0x8f) : Flip the sign of top item
 */
data class OpNegate() : Arithmetic {
  fun opCode() = OpCode(0x8f)
  fun execute(env : ScriptEnvironment): Unit {
    unaryIntOperation(env, - _ )
  }
}

/** OP_ABS(0x90) : Change the sign of the top item to positive
 */
data class OpAbs() : Arithmetic {
  fun opCode() = OpCode(0x90)

  fun execute(env : ScriptEnvironment): Unit {
    unaryIntOperation(env, Math.abs( _ ) )
  }
}

/** OP_NOT(0x91) : If the input is 0 or 1, it is flipped. Otherwise the output will be 0.
 */
data class OpNot() : Arithmetic {
  fun opCode() = OpCode(0x91)

  fun execute(env : ScriptEnvironment): Unit {
    unaryIntOperation(
      env,
      (value) =>
        if (value == 0L) {
          1L
        } else if (value == 1L) {
          0L
        } else {
          0L
        }
    )
  }
}

/** OP_0NOTEQUAL(0x92) : Returns 0 if the input is 0. 1 otherwise.
 */
data class Op0NotEqual() : Arithmetic {
  fun opCode() = OpCode(0x92)

  fun execute(env : ScriptEnvironment): Unit {
    unaryIntOperation(env, (value) => (if (value == 0L) 0L else 1L ) )
  }
}

/** OP_ADD(0x93) : Pop top two items, add them and push result
 */
data class OpAdd() : Arithmetic {
  fun opCode() = OpCode(0x93)

  fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, _ + _ )
  }
}

/** OP_SUB(0x94) : Pop top two items, subtract first from second, push result
*/
data class OpSub() : Arithmetic {
  fun opCode() = OpCode(0x94)

  fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, _ - _ )
  }
}

/** OP_MUL(0x95) : Disabled (multiply top two items)
 */
data class OpMul() : Arithmetic with DisabledScriptOp
{
  fun opCode() = OpCode(0x95)
}

/** OP_DIV(0x96) : Disabled (divide second item by first item)
 */
data class OpDiv() : Arithmetic with DisabledScriptOp
{
  fun opCode() = OpCode(0x96)
}

/** OP_MOD(0x97) : Disabled (remainder divide second item by first item)
 */
data class OpMod() : Arithmetic with DisabledScriptOp
{
  fun opCode() = OpCode(0x97)
}

/** OP_LSHIFT(0x98) : Disabled (shift second item left by first item number of bits)
 */
data class OpLShift() : Arithmetic with DisabledScriptOp
{
  fun opCode() = OpCode(0x98)
}


/** OP_RSHIFT(0x99) : Disabled (shift second item right by first item number of bits)
 */
data class OpRShift() : Arithmetic with DisabledScriptOp
{
  fun opCode() = OpCode(0x99)
}

/** OP_BOOLAND(0x9a) : Boolean AND of top two items
 */
data class OpBoolAnd() : Arithmetic {
  fun opCode() = OpCode(0x9a)
  fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, ((l,r) => if (l!=0L && r!=0L) 1L else 0L) )
  }
}

/** OP_BOOLOR(0x9b) : Boolean OR of top two items
 */
data class OpBoolOr() : Arithmetic {
  fun opCode() = OpCode(0x9b)

  fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, ((l,r) => if (l!=0L || r!=0L) 1L else 0L) )
  }
}

/** OP_NUMEQUAL(0x9c) : Return TRUE if top two items are equal numbers
  */
data class OpNumEqual() : Arithmetic {
  fun opCode() = OpCode(0x9c)

  fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, ((l,r) => if (l == r) 1L else 0L) )
  }
}

/** OP_NUMEQUALVERIFY(0x9d) : Same as OP_NUMEQUAL, but runs OP_VERIFY afterward.
 *
 */
data class OpNumEqualVerify() : Arithmetic {
  fun opCode() = OpCode(0x9d)

  fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, ((l,r) => if (l == r) 1L else 0L) )

    unaryIntOperation( env, ((value) => if (value !=0L) value else throw ScriptEvalException(ErrorCode.InvalidTransaction, "ScriptOp:OpNumEqualVerify")) )
  }
}

/** OP_NUMNOTEQUAL(0x9e) : Return TRUE if top two items are not equal numbers
  */
data class OpNumNotEqual() : Arithmetic {
  fun opCode() = OpCode(0x9e)

  fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, ((l,r) => if (l != r) 1L else 0L) )
  }
}


/** OP_LESSTHAN(0x9f) : Return TRUE if second item is less than top item
  */
data class OpLessThan() : Arithmetic {
  fun opCode() = OpCode(0x9f)

  fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, ((l,r) => if (l < r) 1L else 0L) )
  }
}


/** OP_GREATERTHAN(0xa0) : Return TRUE if second item is greater than top item
  */
data class OpGreaterThan() : Arithmetic {
  fun opCode() = OpCode(0xa0)

  fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, ((l,r) => if (l > r) 1L else 0L) )
  }
}

/** OP_LESSTHANOREQUAL(0xa1) : Return TRUE if second item is less than or equal to top item
  */
data class OpLessThanOrEqual() : Arithmetic {
  fun opCode() = OpCode(0xa1)

  fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, ((l,r) => if (l <= r) 1L else 0L) )
  }
}

/** OP_GREATERTHANOREQUAL(0xa2) : Return TRUE if second item is great than or equal to top item
 */
data class OpGreaterThanOrEqual() : Arithmetic {
  fun opCode() = OpCode(0xa2)

  fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, ((l,r) => if (l >= r) 1L else 0L) )
  }
}

/** OP_MIN(0xa3) : Return the smaller of the two top items
  */
data class OpMin() : Arithmetic {
  fun opCode() = OpCode(0xa3)

  fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, Math.min(_,_) )
  }
}

/** OP_MAX(0xa4) : Return the larger of the two top items
  */
data class OpMax() : Arithmetic {
  fun opCode() = OpCode(0xa4)

  fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, Math.max(_,_) )
  }
}

/** OP_WITHIN(0xa5) : Return TRUE if the third item is between the second item (or equal) and first item
  * Returns 1 if x is within the specified range (left-inclusive), 0 otherwise.
  */
data class OpWithin() : Arithmetic {
  fun opCode() = OpCode(0xa5)

  fun execute(env : ScriptEnvironment): Unit {
    ternaryIntOperation( env, ((x,min,max) => if ((min<=x) && (x<max)) 1L else 0L) )
  }
}

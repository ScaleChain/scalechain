package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.script.ScriptValue
import io.scalechain.blockchain.script.ScriptEnvironment
import io.scalechain.blockchain.ScriptEvalException
import io.scalechain.blockchain.ErrorCode

interface Arithmetic : ScriptOp
{
  fun unaryIntOperation(env : ScriptEnvironment, mutate : (Long) -> (Long) ): Unit {
    super.unaryOperation(env, { value1 ->
      val intValue1 = ScriptValue.decodeStackInt(value1.value)

      val intResult = mutate(intValue1.toLong())
      ScriptValue.valueOf( intResult )
    })
  }

  fun binaryIntOperation(env : ScriptEnvironment, mutate : (Long, Long) -> (Long) ): Unit {
    super.binaryOperation(env, { value1, value2 ->
      val intValue1 = ScriptValue.decodeStackInt(value1.value)
      val intValue2 = ScriptValue.decodeStackInt(value2.value)

      val intResult = mutate(intValue1.toLong(), intValue2.toLong())
      ScriptValue.valueOf(intResult)
    })
  }

  fun ternaryIntOperation(env : ScriptEnvironment, mutate : (Long, Long, Long) -> (Long) ): Unit {
    super.ternaryOperation(env, { value1, value2, value3 ->
      val intValue1 = ScriptValue.decodeStackInt(value1.value)
      val intValue2 = ScriptValue.decodeStackInt(value2.value)
      val intValue3 = ScriptValue.decodeStackInt(value3.value)

      val intResult = mutate(intValue1.toLong(), intValue2.toLong(), intValue3.toLong())
      ScriptValue.valueOf(intResult)
    })
  }
}

/** OP_1ADD(0x8b) : Add 1 to the top item
 */
class Op1Add() : Arithmetic {
  override fun opCode() = OpCode(0x8b)

  override fun execute(env : ScriptEnvironment): Unit {
    unaryIntOperation(env, { it + 1L } )
  }
}

/** OP_1SUB(0x8c) : Subtract 1 from the top item
 */
class Op1Sub() : Arithmetic {
  override fun opCode() = OpCode(0x8c)

  override fun execute(env : ScriptEnvironment): Unit {
    unaryIntOperation(env, { it - 1L } )
  }
}

/** OP_2MUL(0x8d) : Disabled (multiply top item by 2)
 */
class Op2Mul() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x8d)
}
/** OP_2DIV(0x8e) : Disabled (divide top item by 2)
 */
class Op2Div() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x8e)
}

/** OP_NEGATE(0x8f) : Flip the sign of top item
 */
class OpNegate() : Arithmetic {
  override fun opCode() = OpCode(0x8f)
  override fun execute(env : ScriptEnvironment): Unit {
    unaryIntOperation(env, { - it } )
  }
}

/** OP_ABS(0x90) : Change the sign of the top item to positive
 */
class OpAbs() : Arithmetic {
  override fun opCode() = OpCode(0x90)

  override fun execute(env : ScriptEnvironment): Unit {
    unaryIntOperation(env, { Math.abs( it ) } )
  }
}

/** OP_NOT(0x91) : If the input is 0 or 1, it is flipped. Otherwise the output will be 0.
 */
class OpNot() : Arithmetic {
  override fun opCode() = OpCode(0x91)

  override fun execute(env : ScriptEnvironment): Unit {
    unaryIntOperation(
      env,
      { value ->
        if (value == 0L) {
          1L
        } else if (value == 1L) {
          0L
        } else {
          0L
        }
      }
    )
  }
}

/** OP_0NOTEQUAL(0x92) : Returns 0 if the input is 0. 1 otherwise.
 */
class Op0NotEqual() : Arithmetic {
  override fun opCode() = OpCode(0x92)

  override fun execute(env : ScriptEnvironment): Unit {
    unaryIntOperation(env, { if (it == 0L) 0L else 1L } )
  }
}

/** OP_ADD(0x93) : Pop top two items, add them and push result
 */
class OpAdd() : Arithmetic {
  override fun opCode() = OpCode(0x93)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, { l,r ->  l + r } )
  }
}

/** OP_SUB(0x94) : Pop top two items, subtract first from second, push result
*/
class OpSub() : Arithmetic {
  override fun opCode() = OpCode(0x94)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, { l,r ->  l - r } )
  }
}

/** OP_MUL(0x95) : Disabled (multiply top two items)
 */
class OpMul() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x95)
}

/** OP_DIV(0x96) : Disabled (divide second item by first item)
 */
class OpDiv() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x96)
}

/** OP_MOD(0x97) : Disabled (remainder divide second item by first item)
 */
class OpMod() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x97)
}

/** OP_LSHIFT(0x98) : Disabled (shift second item left by first item number of bits)
 */
class OpLShift() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x98)
}


/** OP_RSHIFT(0x99) : Disabled (shift second item right by first item number of bits)
 */
class OpRShift() : DisabledScriptOp
{
  override fun opCode() = OpCode(0x99)
}

/** OP_BOOLAND(0x9a) : Boolean AND of top two items
 */
class OpBoolAnd() : Arithmetic {
  override fun opCode() = OpCode(0x9a)
  override fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, { l,r -> if (l!=0L && r!=0L) 1L else 0L } )
  }
}

/** OP_BOOLOR(0x9b) : Boolean OR of top two items
 */
class OpBoolOr() : Arithmetic {
  override fun opCode() = OpCode(0x9b)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, { l,r -> if (l!=0L || r!=0L) 1L else 0L } )
  }
}

/** OP_NUMEQUAL(0x9c) : Return TRUE if top two items are equal numbers
  */
class OpNumEqual() : Arithmetic {
  override fun opCode() = OpCode(0x9c)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, { l,r -> if (l == r) 1L else 0L } )
  }
}

/** OP_NUMEQUALVERIFY(0x9d) : Same as OP_NUMEQUAL, but runs OP_VERIFY afterward.
 *
 */
class OpNumEqualVerify() : Arithmetic {
  override fun opCode() = OpCode(0x9d)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, { l,r -> if (l == r) 1L else 0L } )

    unaryIntOperation( env, { if (it !=0L) it else throw ScriptEvalException(ErrorCode.InvalidTransaction, "ScriptOp:OpNumEqualVerify") } )
  }
}

/** OP_NUMNOTEQUAL(0x9e) : Return TRUE if top two items are not equal numbers
  */
class OpNumNotEqual() : Arithmetic {
  override fun opCode() = OpCode(0x9e)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, { l,r -> if (l != r) 1L else 0L } )
  }
}


/** OP_LESSTHAN(0x9f) : Return TRUE if second item is less than top item
  */
class OpLessThan() : Arithmetic {
  override fun opCode() = OpCode(0x9f)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, { l,r -> if (l < r) 1L else 0L } )
  }
}


/** OP_GREATERTHAN(0xa0) : Return TRUE if second item is greater than top item
  */
class OpGreaterThan() : Arithmetic {
  override fun opCode() = OpCode(0xa0)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, { l,r -> if (l > r) 1L else 0L } )
  }
}

/** OP_LESSTHANOREQUAL(0xa1) : Return TRUE if second item is less than or equal to top item
  */
class OpLessThanOrEqual() : Arithmetic {
  override fun opCode() = OpCode(0xa1)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, { l,r -> if (l <= r) 1L else 0L } )
  }
}

/** OP_GREATERTHANOREQUAL(0xa2) : Return TRUE if second item is great than or equal to top item
 */
class OpGreaterThanOrEqual() : Arithmetic {
  override fun opCode() = OpCode(0xa2)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, { l,r -> if (l >= r) 1L else 0L } )
  }
}

/** OP_MIN(0xa3) : Return the smaller of the two top items
  */
class OpMin() : Arithmetic {
  override fun opCode() = OpCode(0xa3)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, { l,r -> Math.min(l,r) } )
  }
}

/** OP_MAX(0xa4) : Return the larger of the two top items
  */
class OpMax() : Arithmetic {
  override fun opCode() = OpCode(0xa4)

  override fun execute(env : ScriptEnvironment): Unit {
    binaryIntOperation( env, { l,r -> Math.max(l,r) } )
  }
}

/** OP_WITHIN(0xa5) : Return TRUE if the third item is between the second item (or equal) and first item
  * Returns 1 if x is within the specified range (left-inclusive), 0 otherwise.
  */
class OpWithin() : Arithmetic {
  override fun opCode() = OpCode(0xa5)

  override fun execute(env : ScriptEnvironment): Unit {
    ternaryIntOperation( env, { x,min,max -> if ((min<=x) && (x<max)) 1L else 0L } )
  }
}

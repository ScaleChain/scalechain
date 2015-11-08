package io.scalechain.blockchain.script

import java.math.BigInteger

import io.scalechain.blockchain.util.Utils
import io.scalechain.blockchain.{ScriptEvalException, ErrorCode, FatalException}

trait Arithmetic extends ScriptOp
{
  def unaryOperation(env : ScriptEnvironment, mutate : (Long) => (Long) ): Unit = {
    val value : BigInteger = env.stack.popInt()

    val result = BigInteger.valueOf(
      mutate(
        value.longValue()
      )
    )

    env.stack.pushInt( result )
  }

  def binaryOperation(env : ScriptEnvironment, mutate : (Long, Long) => (Long) ): Unit = {
    val value2 : BigInteger = env.stack.popInt()
    val value1 : BigInteger = env.stack.popInt()

    val result = BigInteger.valueOf (
       mutate(
         value1.longValue(),
         value2.longValue()
       )
     )


    env.stack.pushInt( result )
  }

  def ternaryOperation(env : ScriptEnvironment, mutate : (Long, Long, Long) => (Long) ): Unit = {
    val value3 : BigInteger = env.stack.popInt()
    val value2 : BigInteger = env.stack.popInt()
    val value1 : BigInteger = env.stack.popInt()

    val result = BigInteger.valueOf (
      mutate(
        value1.longValue(),
        value2.longValue(),
        value3.longValue()
      )
    )

    env.stack.pushInt( result )
  }

}

case class Op1Add() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    unaryOperation(env, _ + 1L )
  }
}

case class Op1Sub() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    unaryOperation(env, _ - 1L )
  }
}

case class Op2Mul() extends Arithmetic with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // Do not implement it, because it is disabled.
  }
}

case class Op2Div() extends Arithmetic with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // Do not implement it, because it is disabled.
  }
}


case class OpNegate() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    unaryOperation(env, - _ )
  }
}

case class OpAbs() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    unaryOperation(env, Math.abs( _ ) )
  }
}

/** OP_NOT : If the input is 0 or 1, it is flipped. Otherwise the output will be 0.
 *
 */
case class OpNot() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    unaryOperation(
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

/** OP_0NOTEQUAL : Returns 0 if the input is 0. 1 otherwise.
 *
 */
case class Op0NotEqual() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    unaryOperation(env, (value) => (if (value == 0L) 0L else 1L ) )
  }
}

case class OpAdd() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, _ + _ )
  }
}

case class OpSub() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, _ - _ )
  }
}

case class OpMul() extends Arithmetic with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // Do not implement it, because it is disabled.
  }
}

case class OpDiv() extends Arithmetic with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // Do not implement it, because it is disabled.
  }
}

case class OpMod() extends Arithmetic with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // Do not implement it, because it is disabled.
  }
}

case class OpLShift() extends Arithmetic with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // Do not implement it, because it is disabled.
  }
}

case class OpRShift() extends Arithmetic with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // Do not implement it, because it is disabled.
  }
}

case class OpBoolAnd() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l!=0L && r!=0L) 1L else 0L) )
  }
}

case class OpBoolOr() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l!=0L || r!=0L) 1L else 0L) )
  }
}

case class OpNumEqual() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l == r) 1L else 0L) )
  }
}

/** OP_NUMEQUALVERIFY : Same as OP_NUMEQUAL, but runs OP_VERIFY afterward.
 *
 */
case class OpNumEqualVerify() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l == r) 1L else 0L) )

    // BUGBUG : This code is likely to be duplicated with the OP_VERIFY code
    // OP_VERIFY : Marks transaction as invalid if top stack value is not true.
    unaryOperation( env, ((value) => if (value !=0L) value else throw new FatalException(ErrorCode.InvalidSriptOperation)) )
    assert(false);
    // TODO : Implement
  }
}

case class OpNumNotEqual() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l != r) 1L else 0L) )
  }
}


case class OpLessThan() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l < r) 1L else 0L) )
  }
}


case class OpGreaterThan() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l > r) 1L else 0L) )
  }
}


case class OpLessThanOrEqual() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l <= r) 1L else 0L) )
  }
}


case class OpGreaterThanOrEqual() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l >= r) 1L else 0L) )
  }
}

case class OpMin() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, Math.min(_,_) )
  }
}

case class OpMax() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, Math.max(_,_) )
  }
}

case class OpWithin() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    ternaryOperation( env, ((x,min,max) => if ((min<=x) && (x<=max)) 1L else 0L) )
  }
}

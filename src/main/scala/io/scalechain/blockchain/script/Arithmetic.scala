package io.scalechain.blockchain.script

import io.scalechain.blockchain.{ErrorCode, FatalException}

trait Arithmetic extends ScriptOp
{
  def unaryOperation(env : ScriptEnvironment, mutate : (Int) => (Int) ): Unit = {
    val value : ScriptValue = env.stack.pop()
    if (value.isInstanceOf[NumberValue]) {
      val newValue = NumberValue( mutate(value.asInstanceOf[NumberValue].value ) )
      env.stack.push( newValue )
    } else {
      throw new FatalException(ErrorCode.ScriptTypeMismatch)
    }
  }

  def binaryOperation(env : ScriptEnvironment, mutate : (Int, Int) => (Int) ): Unit = {
    val value2 : ScriptValue = env.stack.pop()
    val value1 : ScriptValue = env.stack.pop()
    if (value1.isInstanceOf[NumberValue] && value2.isInstanceOf[NumberValue]) {
      val newValue = NumberValue(
                       mutate(
                         value1.asInstanceOf[NumberValue].value,
                         value2.asInstanceOf[NumberValue].value ) )
      env.stack.push( newValue )
    } else {
      throw new FatalException(ErrorCode.ScriptTypeMismatch)
    }
  }

  def ternaryOperation(env : ScriptEnvironment, mutate : (Int, Int, Int) => (Int) ): Unit = {
    val value3 : ScriptValue = env.stack.pop()
    val value2 : ScriptValue = env.stack.pop()
    val value1 : ScriptValue = env.stack.pop()
    if (value1.isInstanceOf[NumberValue] && value2.isInstanceOf[NumberValue] && value3.isInstanceOf[NumberValue]) {
      val newValue = NumberValue(
        mutate(
          value1.asInstanceOf[NumberValue].value,
          value2.asInstanceOf[NumberValue].value,
          value3.asInstanceOf[NumberValue].value) )
      env.stack.push( newValue )
    } else {
      throw new FatalException(ErrorCode.ScriptTypeMismatch)
    }
  }

}

case class Op1Add() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    unaryOperation(env, _ + 1 )
  }
}

case class Op1Sub() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    unaryOperation(env, _ - 1 )
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
        if (value == 0) {
          1
        } else if (value == 1) {
          0
        } else {
          0
        }
    )
  }
}

/** OP_0NOTEQUAL : Returns 0 if the input is 0. 1 otherwise.
 *
 */
case class Op0NotEqual() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    unaryOperation(env, (value) => (if (value == 0) 0 else 1 ) )
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
    binaryOperation( env, ((l,r) => if (l!=0 && r!=0) 1 else 0) )
  }
}

case class OpBoolOr() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l!=0 || r!=0) 1 else 0) )
  }
}

case class OpNumEqual() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l == r) 1 else 0) )
  }
}

/** OP_NUMEQUALVERIFY : Same as OP_NUMEQUAL, but runs OP_VERIFY afterward.
 *
 */
case class OpNumEqualVerify() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l == r) 1 else 0) )

    // BUGBUG : This code is likely to be duplicated with the OP_VERIFY code
    // OP_VERIFY : Marks transaction as invalid if top stack value is not true.
    unaryOperation( env, ((value) => if (value !=0) value else throw new FatalException(ErrorCode.InvalidSriptOperation)) )
    assert(false);
    // TODO : Implement
  }
}

case class OpNumNotEqual() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l != r) 1 else 0) )
  }
}


case class OpLessThan() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l < r) 1 else 0) )
  }
}


case class OpGreaterThan() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l > r) 1 else 0) )
  }
}


case class OpLessThanOrEqual() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l <= r) 1 else 0) )
  }
}


case class OpGreaterThanOrEqual() extends Arithmetic {
  def execute(env : ScriptEnvironment): Unit = {
    binaryOperation( env, ((l,r) => if (l >= r) 1 else 0) )
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
    ternaryOperation( env, ((x,min,max) => if ((min<=x) && (x<=max)) 1 else 0) )
  }
}

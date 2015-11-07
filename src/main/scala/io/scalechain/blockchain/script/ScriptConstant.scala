package io.scalechain.blockchain.script

trait Constant extends ScriptOp

case class Op0() extends Constant {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpFalse() extends Constant {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpPush(val bytes : Int) extends Constant {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

// TODO : Extract a common clas for this case class.
case class OpPushData(val lengthBytes : Int) extends Constant {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class Op1Negate() extends Constant {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class Op1() extends Constant {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpTrue() extends Constant {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

/** A common case class for OP_2 ~ OP_16.
  *
  * @param number The value to push on to the stack. It is from 2 to 16.
  */
case class Op2Num(val number : Int) extends Constant {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

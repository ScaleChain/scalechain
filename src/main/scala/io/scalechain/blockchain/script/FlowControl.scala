package io.scalechain.blockchain.script

trait FlowControl extends ScriptOp

/** An operation for OP_NOP for a flow control.
  *
  */
case class OpFlowNop() extends FlowControl {
  def execute(env : ScriptEnvironment): Unit = {
    // Do nothing.
  }
}

case class OpIf() extends FlowControl {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpNotIf() extends FlowControl {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpElse() extends FlowControl {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpEndIf() extends FlowControl {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpVerify() extends FlowControl {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpReturn() extends FlowControl with InvalidScriptOpIfExecuted {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}
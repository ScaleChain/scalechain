package io.scalechain.blockchain.script

trait FlowControl extends ScriptOp

/** An operation for OP_NOP for a flow control.
  *
  */
case class OpFlowNop() extends FlowControl {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpIf() extends FlowControl {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpNotIf() extends FlowControl {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpElse() extends FlowControl {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpEndIf() extends FlowControl {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpVerify() extends FlowControl {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpReturn() extends FlowControl {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}
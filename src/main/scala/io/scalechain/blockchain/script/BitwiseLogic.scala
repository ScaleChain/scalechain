package io.scalechain.blockchain.script

trait BitwiseLogic extends ScriptOp

case class OpInvert() extends BitwiseLogic {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpAnd() extends BitwiseLogic {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpXor() extends BitwiseLogic {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpEqual() extends BitwiseLogic {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpEqualVerify() extends BitwiseLogic {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}
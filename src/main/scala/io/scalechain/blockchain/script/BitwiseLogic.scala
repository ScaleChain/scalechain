package io.scalechain.blockchain.script

trait BitwiseLogic extends ScriptOp

case class OpInvert() extends BitwiseLogic with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpAnd() extends BitwiseLogic with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpOr() extends BitwiseLogic with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpXor() extends BitwiseLogic with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpEqual() extends BitwiseLogic {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpEqualVerify() extends BitwiseLogic {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}
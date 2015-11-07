package io.scalechain.blockchain.script

trait PseudoWord extends ScriptOp

case class OpPubKeyHash() extends PseudoWord {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class OpPubKey() extends PseudoWord {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class OpInvalidOpCode() extends PseudoWord {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

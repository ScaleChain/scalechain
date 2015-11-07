package io.scalechain.blockchain.script

trait Splice extends ScriptOp

case class OpCat() extends Splice with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class OpSubstr() extends Splice with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class OpLeft() extends Splice with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}



case class OpRight() extends Splice with DisabledScriptOp {
  override def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}



case class OpSize() extends Splice {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

package io.scalechain.blockchain.script

trait Splice extends ScriptOp

case class Opcat() extends Splice {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class OpSubstr() extends Splice {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class OpLeft() extends Splice {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}



case class OpRight() extends Splice {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}



case class OpSize() extends Splice {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

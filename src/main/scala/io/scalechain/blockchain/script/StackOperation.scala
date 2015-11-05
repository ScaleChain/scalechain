package io.scalechain.blockchain.script

trait StackOperation extends ScriptOp

case class OpTotalStack() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpFromAltStack() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpIfDup() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpDepth() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpDrop() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpDup() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpNip() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpPick() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class OpRoll() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class OpRot() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}



case class OpSwap() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class OpTuck() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class Op2Drop() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class Op2Dup() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class Op3Dup() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class Op2Over() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class Op2Rot() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}


case class Op2Swap() extends StackOperation {
  def execute(stack : ScriptStack): Unit = {
    assert(false);
    // TODO : Implement
  }
}

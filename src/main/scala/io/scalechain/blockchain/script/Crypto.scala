package io.scalechain.blockchain.script

trait Crypto extends ScriptOp

case class OpRIPEMD160() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpSHA1() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpSHA256() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpHash160() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpHash256() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpCodeSparator() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpCheckSig() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpCheckSigVerify() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpCheckMultiSig() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

case class OpCheckMultiSigVerify() extends Crypto {
  def execute(env : ScriptEnvironment): Unit = {
    assert(false);
    // TODO : Implement
  }
}

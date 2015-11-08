package io.scalechain.blockchain.script.ops

import io.scalechain.blockchain.script.ScriptEnvironment

trait PseudoWord extends ScriptOp


/** OP_SMALLDATA(0xf9) : Represents small data field
 * Node : This operation is not listed in the Bitcoin Script wiki, but in Mastering Bitcoin book.
 */
case class OpSmallData() extends PseudoWord with InternalScriptOp {
  override def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

/** OP_SMALLINTEGER(0xfa) : Represents small integer data field
  * Node : This operation is not listed in the Bitcoin Script wiki, but in Mastering Bitcoin book.
  */
case class OpSmallInteger() extends PseudoWord with InternalScriptOp {
  override def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}



/** OP_PUBKEYHASH(0xfd) : Represents a public key hash field
  */
case class OpPubKeyHash() extends PseudoWord with InternalScriptOp {
  override def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}


/** OP_PUBKEY(0xfe) : Represents a public key field
  */
case class OpPubKey() extends PseudoWord with InternalScriptOp {
  override def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

/** OP_INVALIDOPCODE(0xff) : Represents any OP code not currently assigned
  */
case class OpInvalidOpCode() extends PseudoWord with InternalScriptOp {
  override def execute(env : ScriptEnvironment): Int = {
    // TODO : Implement
    assert(false);
    0
  }
}

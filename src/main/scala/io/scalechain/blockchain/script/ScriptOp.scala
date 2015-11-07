package io.scalechain.blockchain.script

import io.scalechain.blockchain.{ErrorCode, FatalException}

trait ScriptOp {
  def execute(env : ScriptEnvironment) : Unit
}

trait DisabledScriptOp {
  def execute(env : ScriptEnvironment) : Unit = {
    throw new FatalException(ErrorCode.DisabledScriptOperation)
  }
}

trait AlwaysInvalidScriptOp {
  /** Because we check if there is any *always* invalid script operation before executing the script,
   * the execute method should never run. So we implement this method to hit an assertion.
   *
   * @param env
   */
  def execute(env : ScriptEnvironment) : Unit = {
    assert(false)
  }
}

trait InvalidScriptOpIfExecuted {
  def execute(env : ScriptEnvironment) : Unit = {
    throw new FatalException(ErrorCode.DisabledScriptOperation)
  }
}





















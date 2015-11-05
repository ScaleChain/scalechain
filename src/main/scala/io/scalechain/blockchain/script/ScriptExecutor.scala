package io.scalechain.blockchain.script

/**
 * Created by kangmo on 11/6/15.
 */
class ScriptExecutor {
  def execute(items : List[ScriptOp]) : Option[ScriptValue] = {
    val stack = new ScriptStack()
    for ( scriptOp : ScriptOp <- items) {
      scriptOp.execute(stack)
    }
    val returnValue = stack.pop()
    if ( stack.isEmpty() ) {
      None
    } else {
      Some(returnValue)
    }
  }
}

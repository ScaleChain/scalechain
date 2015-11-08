package io.scalechain.blockchain

import io.scalechain.blockchain.script.{ScriptOperations, ScriptValue}
import io.scalechain.blockchain.script.ops.{OpNip, ScriptOp}

import scala.collection.mutable.ListBuffer


case class ParsedScript(val operations : List[ScriptOp])

/**
 * Parse byte array and produce list of script operations.
 *
 * @param rawScript The raw bytes of script that we did not parse yet.
 */
class ScriptParser(val rawScript : Array[Byte]) {
  def parse(): ParsedScript  = {
    val operations = new ListBuffer[ScriptOp]()

    var cursor = 0
    while ( cursor < rawScript.length) {
      // Read the script op code
      val opCode = rawScript(cursor)
      // Move the cursor forward
      cursor += 1

      // Get the script operation that matches the op code.
      val scriptOpOption = ScriptOperations.get(opCode)
      if (scriptOpOption.isDefined) {
        val scriptOpTemplate = scriptOpOption.get
        // A script operation can consume bytes in the script chunk.
        // Copy a chunk of bytes of
        val (scriptOp, bytesCopied) = scriptOpTemplate.createWithInput(rawScript, cursor)
        // Move the cursor to the next script operation we want to execute.
        operations.append(scriptOp)
        cursor += bytesCopied
      } else {
        throw new ScriptEvalException(ErrorCode.InvalidScriptOperation)
      }
    }

    ParsedScript(operations.toList)
  }


}

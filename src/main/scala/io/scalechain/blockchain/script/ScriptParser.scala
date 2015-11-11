package io.scalechain.blockchain.script

import io.scalechain.blockchain.script.ops.ScriptOp
import io.scalechain.blockchain.{ErrorCode, ScriptEvalException}

import scala.collection.mutable.ListBuffer


case class ScriptOpList(val operations : List[ScriptOp])

/**
 * Parse byte array and produce list of script operations.
 *
 * @param rawScript The raw bytes of script that we did not parse yet.
 */
object ScriptParser {
  def parse(rawScript : Array[Byte], offset : Int): ScriptOpList  = {
    val operations = new ListBuffer[ScriptOp]()

    var programCounter = 0
    while ( programCounter < rawScript.length) {
      // Read the script op code
      val opCode = rawScript(programCounter)
      // Move the cursor forward
      programCounter += 1

      // Get the script operation that matches the op code.
      val scriptOpOption = ScriptOperations.get(opCode)
      if (scriptOpOption.isDefined) {
        val scriptOpTemplate = scriptOpOption.get
        // A script operation can consume bytes in the script chunk.
        // Copy a chunk of bytes of
        val (scriptOp, bytesCopied) = scriptOpTemplate.create(programCounter, rawScript, programCounter)
        // Move the cursor to the next script operation we want to execute.
        operations.append(scriptOp)
        programCounter += bytesCopied
      } else {
        throw new ScriptEvalException(ErrorCode.InvalidScriptOperation)
      }
    }

    ScriptOpList(operations.toList)
  }


}

package io.scalechain.blockchain.script

import io.scalechain.blockchain.proto.Script
import io.scalechain.blockchain.script.ops.{ScriptOpWithoutCode, OpCond, ScriptOp}
import io.scalechain.blockchain.{ErrorCode, ScriptEvalException, ScriptParseException}
import io.scalechain.util.HexUtil
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer


case class ScriptOpList(val operations : List[ScriptOp]) {
  override def toString(): String = {
    s"ScriptOpList(operations=Array(${operations.mkString(",")}))"
  }
}


/** The parse result returned by parseUntil method.
 *
 * @param scriptOpList The list of operations we got as a result of parsing a raw script.
 * @param foundFenceOp The script operation found as a fence operation while parsing the script. null if no fence operation was found.
 * @param bytesConsumed The number of bytes consumed during parsing the script.
 */
case class ParseResult(scriptOpList : ScriptOpList, foundFenceOp : ScriptOp, bytesConsumed : Int)

/**
 * Parse byte array and produce list of script operations.
 *
 * @param rawScript The raw bytes of script that we did not parse yet.
 */
object ScriptParser {
  val logger = LoggerFactory.getLogger(ScriptParser.getClass)

  /** Parse a given raw script in a byte array to get the list of ScriptOp(s)
   *
   * @param script The input script.
   *
   * @return The list of ScriptOp(s).
   */
  def parse(script : Script): ScriptOpList  = {
    val parseResult = parseUntil(script, 0)
    parseResult.scriptOpList
  }

  /** An internal version of parse method. ScriptOp.create can call this function.
    * The parse function is a recursive function.
    * ScriptParser.parse -> ScriptOp.create -> ScriptParser.parse ...
    *
    * When the parse function calls ScriptOp.create, some of sub classes of ScriptOp such as OP_IF and OP_NOTIF.
    * Script operation case classes implementing OP_IF and OP_NOTIF are OpIf and OpNotIf.
    * These implements the create method to call parse function again and parses until it meets OP_ENDIF
    * to produce OpCond, which is a pseudo operation.
    *
    * See OpCond for the details.
    *
    * @param script The input script.
    * @param offset The offset of the script to start parsing.
    * @param fenceScriptOps Parsing continues until we meet any of the operations in fenceScriptOps.
    *                       If no fence operation is passed, parse the script until the end of script.
    *
    * Throw a parse exception with UnexpectedEndOfScript code if we did not meet any of fenceScriptOps
    * and we reached at the end of the script.
    *
    * @return The list of ScriptOp(s).
    */
  def parseUntil(script : Script, offset : Int, fenceScriptOps : ScriptOp*): ParseResult  = {
    val operations = new ListBuffer[ScriptOp]()

    var programCounter = offset
    var fenceOpOption : Option[ScriptOp] = None
    // BUGBUG : Improve readability of this code.

    while ( (programCounter < script.length) && // Loop until we meet the end of script and
            fenceOpOption.isEmpty) {               // we did not meet any fence operation.
      // Read the script op code
      val opCode = (script(programCounter) & 0xFF).toShort
      // Move the cursor forward
      programCounter += 1

      // Get the script operation that matches the op code.
      val scriptOpOption = ScriptOperations.get(opCode)
      if (scriptOpOption.isDefined) {
        val scriptOpTemplate = scriptOpOption.get

        // A script operation can consume bytes in the script chunk.
        // Copy a chunk of bytes of
        val (scriptOp, bytesConsumed) = scriptOpTemplate.create(script, programCounter)
        // Move the cursor to the next script operation we want to execute.

        // See if the scriptOp exists in the fenceScriptOps list.
        // For example, while parsing OP_IF/OP_NOTIF, we need to parse until we meet either OP_ELSE or OP_ENDIF.
        if (!scriptOp.isInstanceOf[ScriptOpWithoutCode]) { // BUGBUG : Improve this code without using runtime type checking.
          fenceOpOption = fenceScriptOps.find( scriptOp.opCode() == _.opCode() )
        }

        // Append to the list of operations only if it is not a fence operation.
        if (fenceOpOption.isEmpty) {
          operations.append(scriptOp)
        }

        programCounter += bytesConsumed
      } else {
        // Encountered an invalid OP code. This could be an attack, so dump the raw script onto log.
        logger.warn("InvalidScriptOperation. " +
                    "code : ${HexUtil.prettyHex(Array(opCode.toByte))}" +
                    "programCounter : $programCounter" +
                    "rawScript : ${HexUtil.prettyHex(rawScript)}")

        throw new ScriptParseException(ErrorCode.InvalidScriptOperation)
      }
    }

    // Throw an exception if we did not meet any fence operation
    // even though the caller of this method passed list of fence operations.
    if (fenceScriptOps.length > 0) {
      if (fenceOpOption.isEmpty) {
        throw new ScriptParseException(ErrorCode.UnexpectedEndOfScript)
      }
    }

    ParseResult( ScriptOpList(operations.toList), fenceOpOption.getOrElse(null), bytesConsumed = programCounter - offset )
  }


}

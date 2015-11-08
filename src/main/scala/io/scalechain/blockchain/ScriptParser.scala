package io.scalechain.blockchain

import io.scalechain.blockchain.script.ops.ScriptOp


case class ParsedScript(val bytes : Array[Byte])

/**
 * Parse byte array and produce list of script operations.
 *
 * @param rawScript The raw bytes of script that we did not parse yet.
 */
class ScriptParser(val rawScript : Array[Byte]) {
  def parse(): ParsedScript  = {
    // BUGBUG : Implement validation.
    ParsedScript(rawScript)
  }
}

package io.scalechain.blockchain

import io.scalechain.blockchain.script.ScriptOp


/**
 * Parse byte array and produce list of script operations.
 *
 * @param script The bytes
 */
class ScriptParser(val script : Array[Byte]) {
  def parse(): Array[ScriptOp]  = {

  }
}

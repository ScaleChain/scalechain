package io.scalechain.blockchain.script

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.script.ops.OpPush
import io.scalechain.util.HexUtil

/** Sets printer objects for case classes that require access to script layer.
  * BUGBUG : Need to call a method in this object to activate the printing methods.
  */
object BlockPrinterSetter {
  fun initialize() {
    LockingScriptPrinter.printer =
      object : LockingScriptPrinter {
        override fun toString(lockingScript:LockingScript): String {
          val scriptOps = ScriptParser.parse(lockingScript)

          return "LockingScript(HexUtil.bytes(${HexUtil.kotlinHex(lockingScript.data)})) /* ops:$scriptOps */ "
        }
      }

    UnlockingScriptPrinter.printer =
      object : UnlockingScriptPrinter {
        override fun toString(unlockingScript : UnlockingScript): String {
          val scriptOps = ScriptParser.parse(unlockingScript)

          val hashType =
            if (scriptOps.operations.size > 0) {
              // The last byte of the signature, hash type decides how to create a hash value from transaction and script.
              // The hash value and public key is used to verify the signature.
              val firstOp = scriptOps.operations[0]
              when {
                firstOp != null && firstOp is OpPush -> {
                  firstOp.inputValue!!.value.last()
                }
                else -> {
                  null
                }
              }
            } else {
              null
            }

          return "UnlockingScript(HexUtil.bytes(${HexUtil.kotlinHex(unlockingScript.data)})) /* ops:$scriptOps, hashType:$hashType */ "
        }
      }

    TransactionPrinter.printer =
      object : TransactionPrinter {
        override fun toString(transaction : Transaction) : String {
          return "Transaction(version=${transaction.version}, inputs=List(${transaction.inputs.joinToString(",")}), outputs=List(${transaction.outputs.joinToString(",")}), lockTime=${transaction.lockTime}L /* hash:${transaction.hash()} */)"
        }
      }
  }
}




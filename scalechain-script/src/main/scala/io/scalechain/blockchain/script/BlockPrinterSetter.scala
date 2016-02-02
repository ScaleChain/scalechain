package io.scalechain.blockchain.script

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.ops.OpPush
import io.scalechain.util.HexUtil._

/** Sets printer objects for case classes that require access to script layer.
  * BUGBUG : Need to call a method in this object to activate the printing methods.
  */
object BlockPrinterSetter {

  LockingScript.printer =
    new LockingScriptPrinter {
      def toString(lockingScript:LockingScript): String = {
        val scriptOps = ScriptParser.parse(lockingScript)

        s"LockingScript(${lockingScript.data}) /* ops:$scriptOps */ "
      }
    }

  UnlockingScript.printer =
    new UnlockingScriptPrinter {
      def toString(unlockingScript : UnlockingScript): String = {
        val scriptOps = ScriptParser.parse(unlockingScript)

        // The last byte of the signature, hash type decides how to create a hash value from transaction and script.
        // The hash value and public key is used to verify the signature.
        val hashType = scriptOps.operations(0) match {
          case signature : OpPush => {
            Some(signature.inputValue.value.last)
          }
          case _ => {
            None
          }
        }

        s"UnlockingScript(${unlockingScript.data}) /* ops:$scriptOps, hashType:$hashType */"
      }
    }

  Transaction.printer =
    new TransactionPrinter {
      override def toString(transaction : Transaction) : String = {
        s"Transaction(version=${transaction.version}, inputs=List(${transaction.inputs.mkString(",")}), outputs=List(${transaction.outputs.mkString(",")}), lockTime=${transaction.lockTime}L /* hash:${scalaHex(HashCalculator.transactionHash(transaction))} */)"
      }
    }
}




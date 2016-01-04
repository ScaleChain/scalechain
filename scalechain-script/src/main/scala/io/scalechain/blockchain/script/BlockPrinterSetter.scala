package io.scalechain.blockchain.script


import io.scalechain.blockchain.block._
import io.scalechain.blockchain.script.ops.OpPush
import io.scalechain.util.HexUtil._

/** Sets printer objects for case classes that require access to script layer.
  *
  */
object BlockPrinterSetter {

  LockingScript.printer =
    new LockingScriptPrinter {
      def toString(lockingScript:LockingScript): String = {
        val scriptOps = ScriptParser.parse(lockingScript)

        s"LockingScript(${scalaHex(lockingScript.data)}) /* ops:$scriptOps */ "
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

        s"UnlockingScript(${scalaHex(unlockingScript.data)}) /* ops:$scriptOps, hashType:$hashType */"
      }
    }

  Transaction.printer =
    new TransactionPrinter {
      override def toString(transaction : Transaction) : String = {
        s"Transaction(version=$transaction.version, inputs=Array(${transaction.inputs.mkString(",")}), outputs=Array(${transaction.outputs.mkString(",")}), lockTime=$transaction.lockTime /* hash:${scalaHex(HashCalculator.transactionHash(transaction))} */)"
      }
    }
}




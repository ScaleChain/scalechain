package io.scalechain.blockchain.script

import io.scalechain.blockchain.{GenerationTransactionVerifier, TransactionVerifier, NormalTransactionVerifier}
import io.scalechain.blockchain.block.{GenerationTransactionInput, LockingScript, Transaction, NormalTransactionInput}
import org.scalatest._

/**
 * Created by kangmo on 11/16/15.
 */
trait SignatureTestTrait extends ShouldMatchers {
  this: Suite =>

  protected def verifyTransactionInput(subject : String, spendingTransaction : Transaction, inputIndex : Int, lockingScript : LockingScript): Unit =
  {
    assert(inputIndex >= 0)
    assert(inputIndex < spendingTransaction.inputs.length)
    val env = new ScriptEnvironment(spendingTransaction, Some(inputIndex))

    val txInput = spendingTransaction.inputs(inputIndex)
    txInput match {
      case normalTxInput : NormalTransactionInput => {
        new NormalTransactionVerifier(normalTxInput).verify(env, lockingScript)
      }
      case generationTxInput : GenerationTransactionInput  => {
        new GenerationTransactionVerifier(generationTxInput).verify(null, null)
      }
    }
  }
}

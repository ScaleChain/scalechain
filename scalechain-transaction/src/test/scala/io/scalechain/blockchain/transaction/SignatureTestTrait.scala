package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.{Block, Transaction, LockingScript, GenerationTransactionInput, NormalTransactionInput}
import io.scalechain.blockchain.script.ScriptEnvironment
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

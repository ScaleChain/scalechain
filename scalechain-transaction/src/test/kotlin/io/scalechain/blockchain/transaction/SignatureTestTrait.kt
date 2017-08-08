package io.scalechain.blockchain.transaction

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.LockingScript
import io.scalechain.blockchain.proto.GenerationTransactionInput
import io.scalechain.blockchain.proto.NormalTransactionInput
import io.scalechain.blockchain.script.ScriptEnvironment
import org.junit.runner.RunWith

/**
 * Created by kangmo on 11/16/15.
 */
@RunWith(KTestJUnitRunner::class)
interface SignatureTestTrait : Matchers {

  fun verifyTransactionInput(subject : String, spendingTransaction : Transaction, inputIndex : Int, lockingScript : LockingScript): Unit
  {
    assert(inputIndex >= 0)
    assert(inputIndex < spendingTransaction.inputs.size)
    val env = ScriptEnvironment(spendingTransaction, inputIndex)

    val txInput = spendingTransaction.inputs[inputIndex]
    when {
      txInput is NormalTransactionInput -> {
        // We don't need to access db to get the locking script, as we already have the locking script.
        NormalTransactionVerifier(null/*db*/, txInput, spendingTransaction, inputIndex).verify(env, lockingScript)

      }
      txInput is GenerationTransactionInput -> {
        // The db parameter is not used yet.
        GenerationTransactionVerifier(null/*db*/, txInput).verify(null, null)
      }
    }
  }
}

package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.KeyValueDatabase

/**
 * See if the transaction is related with a specific output ownership.
 * Used by importaddress to check if a currently existing transaction has to be registered to the wallet when the rescan parameter is set to true.
 */
fun Transaction.isRelatedWith(db : KeyValueDatabase, coinsView : CoinsView, ownership: OutputOwnership) : Boolean {

    this.outputs.forEach { transactionOutput ->
      if (LockingScriptAnalyzer.extractPossibleOutputOwnerships(transactionOutput.lockingScript).contains(ownership)) {
        return true
      }
    }

    if (this.inputs[0].isCoinBaseInput()) {
      // do nothing, as a coinbase input does not point to any output.
    } else {
      this.inputs.forEach { transactionInput ->
        // Step 3 : Block Store : Get the transaction output the input is spending.
        val spentOutput = OutPoint(
          Hash(transactionInput.outputTransactionHash.value),
          transactionInput.outputIndex.toInt())

        val output = coinsView.getTransactionOutput(db, spentOutput)
        if (LockingScriptAnalyzer.extractPossibleOutputOwnerships(output.lockingScript).contains(ownership)) {
          return true
        }
      }
    }
    return false
}
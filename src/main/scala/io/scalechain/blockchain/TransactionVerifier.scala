package io.scalechain.blockchain

import io.scalechain.blockchain.block.index.BlockIndex
import io.scalechain.blockchain.block.{TransactionInput, LockingScript, Transaction}
import io.scalechain.blockchain.script.{ScriptParser, ScriptEnvironment}

/** Check if inputs of a transaction successfully unlocks the locking script attached to the UTXO, which the input references.
 *
  * @param spendingTransaction The transaction that has inputs with unlocking scripts.
 */
class TransactionVerifier(spendingTransaction : Transaction) {
  /** Check if a transaction's input successfully unlocks the locking script attached to the UTXO, which the input references.
   *
   * @param inputIndex Among multiple transaction inputs in the spending transaction, which one are we going to verify?
   * @param blockIndex A block index that can search a transaction by its hash.
   */
  def verifyInput(inputIndex : Int, blockIndex : BlockIndex): Unit = {
    val env = new ScriptEnvironment(spendingTransaction, Some(inputIndex))

    if (inputIndex < 0 || inputIndex >= spendingTransaction.inputs.length) {
      throw new TransactionVerificationException(ErrorCode.InvalidInputIndex)
    }

    spendingTransaction.inputs(inputIndex).verify(env, blockIndex)
  }

  /** Verify all inputs of the spendingTransaction.
   *
   * @param blockIndex A block index that can search a transaction by its hash.
   */
  def verify(blockIndex : BlockIndex) : Unit = {
    for ( inputIndex <- 0 until spendingTransaction.inputs.length) {
      val txInput = spendingTransaction.inputs(inputIndex)
    }
  }
}

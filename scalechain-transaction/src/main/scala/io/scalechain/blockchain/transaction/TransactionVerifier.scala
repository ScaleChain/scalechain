package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto.{Transaction, LockingScript, GenerationTransactionInput, NormalTransactionInput}
import io.scalechain.blockchain.script._
import io.scalechain.blockchain.script.ops.{OpEqual, OpHash160, OpPush, OpPushData}
import io.scalechain.blockchain.storage.BlockIndex
import io.scalechain.blockchain.{ScriptEvalException, ScriptParseException, ErrorCode, TransactionVerificationException}
import io.scalechain.util.Utils

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

    spendingTransaction.inputs(inputIndex) match {
      case tx : NormalTransactionInput => {
        new NormalTransactionVerifier(tx).verify(env, blockIndex)
      }
      case tx : GenerationTransactionInput => {
        new GenerationTransactionVerifier(tx).verify(env, blockIndex)
      }
    }
  }

  /** Verify all inputs of the spendingTransaction.
   */
  def verify(blockIndex : BlockIndex) : Unit = {
    for ( inputIndex <- 0 until spendingTransaction.inputs.length) {
      verifyInput(inputIndex, blockIndex)
    }
  }
}


/**
  * Created by kangmo on 1/3/16.
  */
class NormalTransactionVerifier(transaction : NormalTransactionInput) {
  /** Convert any exception happened within the body to TransactionVerificationException
    *
    * @param body A function that parses and executes scripts.
    * @tparam T The return type of the body function
    * @return Whatever the body returns.
    */
  protected def throwingTransactionVerificationException[T]( body : => T ) : T = {
    try {
      body
    } catch {
      case e : TransactionVerificationException => {
        throw e
      }
      case e : ScriptParseException => {
        throw new TransactionVerificationException(ErrorCode.ScriptParseFailure, e.getMessage(), e.getStackTrace())
      }
      case e : ScriptEvalException => {
        throw new TransactionVerificationException(ErrorCode.ScriptEvalFailure, e.getMessage(), e.getStackTrace())
      }
      case e : Exception => {
        throw new TransactionVerificationException(ErrorCode.GeneralFailure, e.getMessage(), e.getStackTrace())
      }
    }
  }

  /** Get the locking script attached to the UTXO which this input references with transactionHash and outputIndex.
    *
    * @param blockIndex A block index that can search a transaction by its hash.
    *
    * @return The locking script attached to the UTXO which this input references.
    */
  def getLockingScript(blockIndex : BlockIndex): LockingScript = {
    val outputTxOption = blockIndex.getTransaction(transaction.outputTransactionHash)
    if (outputTxOption.isEmpty) {
      // The transaction which produced the UTXO does not exist.
      throw new TransactionVerificationException(ErrorCode.InvalidOutputTransactionHash)
    }
    // The transaction that produced UTXO exists.
    val outputTx = outputTxOption.get
    if (transaction.outputIndex < 0 || transaction.outputIndex >= outputTx.outputs.length) {
      throw new TransactionVerificationException(ErrorCode.InvalidOutputIndex)
    }

    /**
      *  BUGBUG : Need to check if the UTXO is from Generation transaction to check 100 blocks are created?
      */

    outputTx.outputs(transaction.outputIndex.toInt).lockingScript
  }

  /** With a block index, verify that the unlocking script of the input successfully unlocks the locking script attached to the UTXO that this input references.
    *
    * @param env The script execution environment, which has (1) the transaction that this input belongs to, and (2) the index of the inputs of the transaction that corresponds to this transaction input.
    * @param blockIndex A block index that can search a transaction by its hash.
    * @throws TransactionVerificationException if the verification failed.
    */
  def verify(env : ScriptEnvironment, blockIndex : BlockIndex): Unit = {
    throwingTransactionVerificationException {
      val lockingScript : LockingScript = getLockingScript(blockIndex)

      verify(env, lockingScript)
    }
  }
  /** Get the redeem script if the lockingScriptOps match the P2SH pattern.
    *
    * @param unlockingScriptOps The unlocking script that is provided by the spending transaction.
    * @param lockingScriptOps The locking script attached to the UTXO.
    * @return Some(redeem script) if the locking script had the P2SH pattern. None otherwise.
    */
  def getRedeemScript(unlockingScriptOps : ScriptOpList,
                      lockingScriptOps : ScriptOpList) : Option[ScriptValue]= {
    // TODO : Extract the duplicate code to a method. see p2sh_filter
    lockingScriptOps.operations match {
      // TODO : Optimize by checking opCode() instead of slow pattern matching.
      // STEP 1 : Check if the locking script matches P2SH pattern.
      case List(OpHash160(), OpPush(20, _), OpEqual()) => {
        // STEP 2 : Get the redeem script from the last operation of unlocking script.
        val lastOperation = unlockingScriptOps.operations.last
        lastOperation match {
          // TODO : Verify assumption : the redeem script is provided by only OpPushData or OpPush
          case OpPushData(_, redeemingScript) => Some(redeemingScript)
          case OpPush(_, redeemingScript) => Some(redeemingScript)
          case _ => None
        }
      }
      case _ => None
    }
  }
  /** With a locking script, verify that the unlocking script of the input successfully unlocks the locking script attached to the UTXO that this input references.
    * This method supports P2SH.
    *
    * An example of P2SH is as follows.
    * unlocking script :
    *   Op0(), // A dummy value required for emulating a bug in the reference implementation by Satoshi for OP_CHECKMULTISIG.
    *   // Second signature
    *   OpPush(71,ScriptBytes(bytes("304402207dc87ea88c8a10bde69dbc91f80c827c7b8a3d18122409e358f1e51b54322e9a022042fe00abb6466dc5e4dbd7f982d2ad9f904e6dc69c6f7eb947223e936a00473e01"))),
    *   // First signature
    *   OpPush(72,ScriptBytes(bytes("3045022100f2e9163f61e50cf5984b94f3a388b2c13cf33e442bdf217a944a1b482319af1c022060c48f1e696ad084da336c20618dcc12c1cf14cdf0ef3bd1b0c86e026afe78da01"))),
    *   // Redeeming script
    *   OpPushData(1,ScriptBytes(bytes("522102a2a60f3f6ec13028e58e8fb9ccc53aab9391ea542f35a38b5560138b14bd20a62102b6dad19484515162f51ddc5446bc2a3f622f68dd111282b038b9af107be62ad821037e38809356db2eb6b40b0f14666641f00a041996137f7355a8e1745bac3a4cb453ae")))))
    *
    * locking script :
    *   OpHash160()
    *   OpPush(20,ScriptBytes(bytes("0ba8c243f2e21f963cb3c04338b3e0c6918a996c")))
    *   OpEqual()))
    *
    * @param env The script execution environment, which has (1) the transaction that this input belongs to, and (2) the index of the inputs of the transaction that corresponds to this transaction input.
    * @param lockingScript The locking script attached to the UTXO that this input references.
    */
  def verify(env : ScriptEnvironment, lockingScript : LockingScript): Unit = {
    throwingTransactionVerificationException {
      // Step 1 : Parse both unlocking script and locking script.
      val unlockingScriptOps : ScriptOpList = ScriptParser.parse(transaction.unlockingScript)
      val lockingScriptOps : ScriptOpList = ScriptParser.parse(lockingScript)

      // Step 2 : Run the unlocking script.
      ScriptInterpreter.eval_internal(env, unlockingScriptOps)

      // Step 3 : See if it is P2SH. If yes, copy the redeeming script.
      val redeemScript : Option[ScriptValue]
      = getRedeemScript(unlockingScriptOps, lockingScriptOps)

      // Step 4 : Run the locking script
      ScriptInterpreter.eval_internal(env, lockingScriptOps)

      // Step 5 : Check the top value of the stack
      val top = env.stack.pop()
      if ( !Utils.castToBool(top.value) ) {
        throw new TransactionVerificationException(ErrorCode.TopValueFalse)
      }

      // Step 6 : If we have any redeeming script, parse it, and run it.
      redeemScript.map { scriptValue : ScriptValue=>
        val redeemScriptOps : ScriptOpList =
          ScriptParser.parse(LockingScript(scriptValue.value))
        ScriptInterpreter.eval_internal(env, redeemScriptOps)

        // Step 6.1 : See if the result of the redeem script execution is true.
        val top = env.stack.pop()
        if ( !Utils.castToBool(top.value) ) {
          throw new TransactionVerificationException(ErrorCode.TopValueFalse)
        }
      }
    }
  }

}

class GenerationTransactionVerifier(transaction : GenerationTransactionInput) {
  /** Verify that 100 blocks are created after the generation transaction was created.
    * Generation transactions do not reference any UTXO, as it creates UTXO from the scratch.
    * So, we don't have to verify the locking script and unlocking script, but we need to make sure that at least 100 blocks are created
    * after the block where this generation transaction exists.
    *
    * @param env For a generation transaction, this is null, because we don't need to execute any script.
    * @param blockIndex For a generation tranasction, this is null, because we don't need to search any tranasction by its hash.
    *
    * @throws TransactionVerificationException if the verification failed.
    */
  def verify(env : ScriptEnvironment, blockIndex : BlockIndex): Unit = {
    //assert(env == null)
    //assert(blockIndex == null)
    // Do nothing.
    // TODO : Verify that 100 blocks are created after the generation transaction was created.
  }
}


package io.scalechain.blockchain.transaction

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script._
import io.scalechain.blockchain.script.ops.{OpEqual, OpHash160, OpPush, OpPushData}
import io.scalechain.blockchain.storage.BlockIndex
import io.scalechain.blockchain._
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
  def verifyInput(inputIndex : Int, blockIndex : BlockIndex) = {
    val env = new ScriptEnvironment(spendingTransaction, Some(inputIndex))

    if (inputIndex < 0 || inputIndex >= spendingTransaction.inputs.length) {
      throw new TransactionVerificationException(ErrorCode.InvalidInputIndex, "Invalid Input Index on Transaction Verifier")
    }

    spendingTransaction.inputs(inputIndex) match {
      case tx : NormalTransactionInput => {
        new NormalTransactionVerifier(tx, spendingTransaction, inputIndex).verify(env, blockIndex)
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

object NormalTransactionVerifier {
  var successCount = 0
  var verificationFailureCount = 0
  var parseFailureCount = 0
  var evalFailureCount = 0
  var generalFailureCount = 0

  def totalFailureCount =  verificationFailureCount + parseFailureCount + evalFailureCount + generalFailureCount

  def success() : Unit = {
    successCount += 1
  }

  def verificationFailure() : Unit = {
    verificationFailureCount += 1
  }

  def parseFailure() : Unit = {
    parseFailureCount += 1
  }

  def evalFailure() : Unit = {
    evalFailureCount += 1
  }

  def generalFailure() : Unit = {
    generalFailureCount += 1
  }

  def stats() = {
    s"TransactionVerification( success:$successCount, failure:$totalFailureCount, verificationFailure:$verificationFailureCount, parseFailure:$parseFailureCount, evalFailure:$evalFailureCount, generalFailure:$generalFailureCount)"
  }
}

/** Verify a normal transaction input.
  *
  * @param transactionInput The normal transaction input to verify.
  * @param transaction (for debugging) When an exception is thrown, we attach this transaction to create a MergedScript instance, which is logged for debugging Script execution.
  * @param inputIndex (for debugging) When an exception is thrown, this is necessary to create a MergedScript.
  */
class NormalTransactionVerifier(transactionInput : NormalTransactionInput, transaction : Transaction, inputIndex : Int) {

  /** Convert any exception happened within the body to TransactionVerificationException
    *
    * @param body A function that parses and executes scripts.
    * @param scriptOption The merged script to use for debugging purpose. When a transaction verification fails, we log the merged script, which has an unlocking script and a locking script into a log file. We can use it to debug why the transaction failed.
    * @tparam T The return type of the body function
    * @return Whatever the body returns.
    */
  protected def throwingTransactionVerificationException[T](scriptOption:Option[MergedScript]=None)( body : => T ) : T = {
    try {
      val returnValue = body
      NormalTransactionVerifier.success()
      returnValue
    } catch {
      case e : ScriptParseException => {
        NormalTransactionVerifier.parseFailure()
        throw new TransactionVerificationException(ErrorCode.ScriptParseFailure, s"[${e.code}]message=${e.message}", e.getStackTrace(), debuggingInfo = scriptOption)
      }
      case e : ScriptEvalException => {
        NormalTransactionVerifier.evalFailure()
        throw new TransactionVerificationException(ErrorCode.ScriptEvalFailure, s"[${e.code}]message=${e.message}", e.getStackTrace(), debuggingInfo = scriptOption)
      }
      case e: TransactionVerificationException => {
        NormalTransactionVerifier.verificationFailure()
        e.debuggingInfo = scriptOption
        throw e
      }
      case e : Exception => {
        NormalTransactionVerifier.generalFailure()
        throw new TransactionVerificationException(ErrorCode.GeneralFailure, s"message=${e.getMessage}", e.getStackTrace(), debuggingInfo = scriptOption)
      }
    }
  }

  protected def throwingTransactionVerificationException[T]( body : => T ) : T = {
    throwingTransactionVerificationException(None)(body)
  }

  /** Get the locking script attached to the UTXO which this input references with transactionHash and outputIndex.
    *
    * @param blockIndex A block index that can search a transaction by its hash.
    *
    * @return The locking script attached to the UTXO which this input references.
    */
  def getLockingScript(blockIndex : BlockIndex): LockingScript = {
    val outputTxOption = blockIndex.getTransaction(transactionInput.outputTransactionHash)
    if (outputTxOption.isEmpty) {
      // The transaction which produced the UTXO does not exist.
      throw new TransactionVerificationException(ErrorCode.InvalidOutputTransactionHash, message = s"Invalid Output Tranasction Hash")
    }
    // The transaction that produced UTXO exists.
    val outputTx = outputTxOption.get
    if (transactionInput.outputIndex < 0 || transactionInput.outputIndex >= outputTx.outputs.length) {
      throw new TransactionVerificationException(ErrorCode.InvalidOutputIndex, message = s"Invalid Output Index")
    }

    /**
      *  BUGBUG : Need to check if the UTXO is from Generation transaction to check 100 blocks are created?
      */
    outputTx.outputs(transactionInput.outputIndex.toInt).lockingScript
  }

  /** With a block index, verify that the unlocking script of the input successfully unlocks the locking script attached to the UTXO that this input references.
    *
    * @param env The script execution environment, which has (1) the transaction that this input belongs to, and (2) the index of the inputs of the transaction that corresponds to this transaction input.
    * @param blockIndex A block index that can search a transaction by its hash.
    * @return (unlocking script, locking script) pair. This is used for debugging purpose. When an transaction verification fails, we will log the information to execute the transaction again using our debugger.
    * @throws TransactionVerificationException if the verification failed.
    */
  def verify(env : ScriptEnvironment, blockIndex : BlockIndex): Unit = {
    val lockingScript : LockingScript = throwingTransactionVerificationException {
      getLockingScript(blockIndex)
    }

    // The merged script to use for debugging purpose.
    // When a transaction verification fails, we log the merged script,
    // which has an unlocking script and a locking script into a log file.
    // We can use it to debug why the transaction failed.
    val mergedScriptOption = Some(MergedScript(transaction, inputIndex, transactionInput.unlockingScript, lockingScript))
    throwingTransactionVerificationException(mergedScriptOption) {
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
      val unlockingScriptOps : ScriptOpList = ScriptParser.parse(transactionInput.unlockingScript)
      val lockingScriptOps : ScriptOpList = ScriptParser.parse(lockingScript)

      // Step 2 : Run the unlocking script.
      ScriptInterpreter.eval_internal(env, unlockingScriptOps)

      // Step 3 : See if it is P2SH. If yes, copy the redeeming script.
      val redeemScript : Option[ScriptValue]
      = getRedeemScript(unlockingScriptOps, lockingScriptOps)

      // Step 4 : Run the locking script
      ScriptInterpreter.eval_internal(env, lockingScriptOps)

      // Step 5 : Check the top value of the stack
      if (env.stack.size() > 0) {
        val top = env.stack.pop()
        if ( !Utils.castToBool(top.value) ) {
          throw new TransactionVerificationException(ErrorCode.TopValueFalse, message = s"Result of unlocking script execution : ${top.value}")
        }
      } else {
        throw new TransactionVerificationException(ErrorCode.NotEnoughStackValues, message = s"Not enough stack values after the script execution.")
      }

      // Step 6 : If we have any redeeming script, parse it, and run it.
      redeemScript.map { scriptValue : ScriptValue=>
        val redeemScriptOps : ScriptOpList =
          ScriptParser.parse(LockingScript(scriptValue.value))
        ScriptInterpreter.eval_internal(env, redeemScriptOps)

        // Step 6.1 : See if the result of the redeem script execution is true.
        if (env.stack.size() > 0) {
          val top = env.stack.pop()
          if (!Utils.castToBool(top.value)) {
            throw new TransactionVerificationException(ErrorCode.TopValueFalse, message = s"Result of redeem script execution : ${top.value}")
          }
        } else {
          throw new TransactionVerificationException(ErrorCode.NotEnoughStackValues, message = s"Not enough stack values after the redeem script execution.")
        }
      }

      // BUGBUG : Is there any case that redeemScript is None even though there is a redeem script?
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
  def verify(env : ScriptEnvironment, blockIndex : BlockIndex) : Unit = {
    //assert(env == null)
    //assert(blockIndex == null)
    // Do nothing.
    // TODO : Verify that 100 blocks are created after the generation transaction was created.
  }
}


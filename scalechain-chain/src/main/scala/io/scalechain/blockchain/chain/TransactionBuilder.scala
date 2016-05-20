package io.scalechain.blockchain.chain

import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.transaction.{CoinAmount, OutputOwnership, ParsedPubKeyScript}
import io.scalechain.util.HexUtil

import scala.collection.mutable.ListBuffer

/**
  * Build a transaction by using inputs and outputs provided.
  * Note that the builder does not check if this is a double spending transaction.
  * IOW, it does not check if the outputs pointed by inputs are already spent.
  *
  * @param coinsView The read-only view of coins in the blockchain. Need it to verify the sum of input amounts >= sum of output amounts.
  *
  */
class TransactionBuilder(coinsView : CoinsView) {
  /** The outputs spent by inputs. The order of spendingOutputs matches inputs.
    */
  val spendingOutputs = new ListBuffer[TransactionOutput]

  /** The inputs of the transaction
    */
  val inputs = new ListBuffer[TransactionInput]

  /** The outputs of the transaction
    */
  val outputs = new ListBuffer[TransactionOutput]

  /** Add a generation transaction input.
    *
    * @param coinbaseData The coinbase data to embed into the generation transaction input.
    * @param sequenceNumber The sequence number in the generation transaction input.
    */
  def addGenerationInput(coinbaseData : CoinbaseData, sequenceNumber : Long) : Unit = {
    // TODO : Need to move to a singleton to avoid writing the same code over and over.
    val allZeroHash = TransactionHash( HexUtil.bytes("0"*64) )
    // TODO : Need to make sure that the output index is serialized correctly for the generation transaction
    val outputIndex : Long = 0xFFFFFF

    inputs.append( GenerationTransactionInput(allZeroHash, outputIndex, coinbaseData, sequenceNumber) )
  }

  /** Add a normal transaction input.
    *
    * @param outPoint The out point which points to the output we want to spent.
    * @param unlockingScriptOption The unlocking script if any.
    *                              If None is passed, we will put an empty unlocking script,
    *                              and TransactionSigner will add the unlocking script with public keys and signatures.
    *                              If Some(script) is passed, we will use the given script.
    * @param sequenceNumberOption The sequence number.
    *                             If None is passed we will use the default value zero.
    *                             If Some(sequence) is passed, we will use the given value.
    *
    */
  def addInput(outPoint : OutPoint, unlockingScriptOption : Option[UnlockingScript] = None, sequenceNumberOption : Option[Long] = None) : Unit = {
    // TODO : Check if the sequenceNumberOption.get is the maximum of unsigned integer.
    val input = NormalTransactionInput(
      TransactionHash(outPoint.transactionHash.value),
      outPoint.outputIndex,
      unlockingScriptOption.getOrElse(UnlockingScript(Array[Byte]())),
      sequenceNumberOption.getOrElse(0L) )

    inputs.append( input )
  }

  /** Add a transaction output with a public key hash.
    *
    * @param value The amount of coins in satoshi.
    * @param publicKeyHash The public key hash to put into the locking script.
    */
  def addOutput(value : Long, publicKeyHash : Hash) : Unit = {
    val pubKeyScript = ParsedPubKeyScript.from(publicKeyHash.value.array)
    val output = TransactionOutput( value, pubKeyScript.lockingScript() )
    outputs.append( output )
  }

  /** Add a transaction output with an output ownership.
    *
    * @param value The amount of coins in satoshi.
    * @param outputOwnership The output ownership that owns the output.
    */
  def addOutput(value : Long, outputOwnership : OutputOwnership) : Unit = {
    val output = TransactionOutput( value, outputOwnership.lockingScript() )
    outputs.append( output )
  }

  protected[chain] def calculateFee(spendingOutputs : Seq[TransactionOutput], newOutputs : Seq[TransactionOutput]) : CoinAmount = {
    val fee = spendingOutputs.foldLeft(0L)(_ + _.value) - newOutputs.foldLeft(0L)(_ + _.value)
    CoinAmount.from(fee)
  }

  /** Check if the current status of the builder is valid.
    */
  protected[chain] def checkValidity(): Unit = {
    // Step 1 : Check if we have at least one input.
    if ( inputs.length == 0 )
      throw new GeneralException(ErrorCode.NotEnoughTransactionInput)

    // Step 2 : Check if we have at least one output.
    if (outputs.length == 0)
    throw new GeneralException(ErrorCode.NotEnoughTransactionOutput)

    // Step 3 : Check if sum of input values is greater than or equal to the sum of output values.
    if (calculateFee(spendingOutputs, outputs).value < 0) {
      throw new GeneralException(ErrorCode.NotEnoughInputAmounts)
    }

    // Step 4 : Check if we have other inputs when we have a generation input.
    if (inputs(0).isCoinBaseInput()) {
      if (inputs.length != 1)
        throw new GeneralException(ErrorCode.GenerationInputWithOtherInputs)
    }

    for ( i <- 1 until inputs.length) {
      if (inputs(i).isCoinBaseInput())
        throw new GeneralException(ErrorCode.GenerationInputWithOtherInputs)
    }
  }

  /** Get the built transaction.
    *
    * @param version The version of the transaction.
    * @param lockTime The lock time of the transaction.
    * @return The built transaction.
    */
  def getTransaction(version : Int, lockTime : Long) : Transaction = {
    checkValidity()

    Transaction(
      version = version,
      inputs.toList,
      outputs.toList,
      lockTime = lockTime
    )
  }
}

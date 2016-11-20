package io.scalechain.blockchain.chain

import io.scalechain.blockchain.script.ScriptSerializer
import io.scalechain.blockchain.script.ops.{OpPush, OpReturn}
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.{ErrorCode, GeneralException}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.transaction._
import io.scalechain.util.HexUtil

import scala.collection.mutable.ListBuffer

object TransactionBuilder {
  /** create a transaction builder.
    *
    * @return The transaction builder.
    */
  fun newBuilder() = TransactionBuilder()

  fun newGenerationTransaction(coinbaseData : CoinbaseData, minerAddress : CoinAddress) : Transaction {
    TransactionBuilder.newBuilder()
      .addGenerationInput(coinbaseData)
      .addOutput(CoinAmount(50), minerAddress)
      .build()
  }
}


/**
  * Build a transaction by using inputs and outputs provided.
  * Note that the builder does not check if this is a double spending transaction.
  * IOW, it does not check if the outputs pointed by inputs are already spent.
  *
  */
class TransactionBuilder() {
  /** The outputs spent by inputs. The order of spendingOutputs matches inputs.
    */
  val spendingOutputs = ListBuffer<TransactionOutput>

  /** The inputs of the transaction
    */
  val inputs = ListBuffer<TransactionInput>

  /** The outputs of the transaction
    */
  val newOutputs = ListBuffer<TransactionOutput>

  /** Add a generation transaction input.
    *
    * @param coinbaseData The coinbase data to embed into the generation transaction input.
    * @param sequenceNumber The sequence number in the generation transaction input.
    */
  fun addGenerationInput(coinbaseData : CoinbaseData, sequenceNumber : Long = 0) : TransactionBuilder {
    // TODO : Need to move to a singleton to avoid writing the same code over and over.
    val allZeroHash = Hash( HexUtil.bytes("0"*64) )
    // TODO : Need to make sure that the output index is serialized correctly for the generation transaction
    val outputIndex : Long = 0xFFFFFF

    inputs.append( GenerationTransactionInput(allZeroHash, outputIndex, coinbaseData, sequenceNumber) )
    this
  }

  /** Add a normal transaction input.
    *
    * @param coinsView The read-only view of coins in the blockchain. Need it to verify the sum of input amounts >= sum of output amounts.
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
  fun addInput(coinsView : CoinsView, outPoint : OutPoint, unlockingScriptOption : Option<UnlockingScript> = None, sequenceNumberOption : Option<Long> = None)(implicit db : KeyValueDatabase) : TransactionBuilder {
    // TODO : Check if the sequenceNumberOption.get is the maximum of unsigned integer.
    val input = NormalTransactionInput(
      Hash(outPoint.transactionHash.value),
      outPoint.outputIndex,
      unlockingScriptOption.getOrElse(UnlockingScript(Array<Byte>())),
      sequenceNumberOption.getOrElse(0L) )

    inputs.append( input )

    spendingOutputs.append(
      coinsView.getTransactionOutput(input.getOutPoint())
    )
    this
  }

  /** Add a transaction output with a public key hash.
    *
    * @param amount The amount of coins .
    * @param publicKeyHash The public key hash to put into the locking script.
    */
  fun addOutput(amount : CoinAmount, publicKeyHash : Hash) : TransactionBuilder {
    val pubKeyScript = ParsedPubKeyScript.from(publicKeyHash.value.array)
    val output = TransactionOutput( amount.coinUnits, pubKeyScript.lockingScript() )
    newOutputs.append( output )
    this
  }

  /** Add a transaction output with an output ownership.
    *
    * @param amount The amount of coins.
    * @param outputOwnership The output ownership that owns the output.
    */
  fun addOutput(amount : CoinAmount, outputOwnership : OutputOwnership) : TransactionBuilder {
    val output = TransactionOutput( amount.coinUnits, outputOwnership.lockingScript() )
    newOutputs.append( output )
    this
  }

  /**
    * Add an output whose locking script only contains the given bytes prefixed with OP_RETURN.
    *
    * Used by the block signer to create a transaction that contains the block hash to sign.
    *
    * @param data
    * @return
    */
  fun addOutput(data : Array<Byte>) : TransactionBuilder {
    val lockingScriptOps = List( OpReturn(), OpPush.from(data) )
    val lockingScriptData = ScriptSerializer.serialize(lockingScriptOps)
    val output = TransactionOutput( 0L, LockingScript(lockingScriptData))
    newOutputs.append(output)
    this
  }

  protected<chain> fun calculateFee(spendingOutputs : Seq<TransactionOutput>, newOutputs : Seq<TransactionOutput>) : CoinAmount {
    val fee = spendingOutputs.foldLeft(0L)(_ + _.value) - newOutputs.foldLeft(0L)(_ + _.value)
    CoinAmount.from(fee)
  }

  /** Check if the current status of the builder is valid.
    */
  protected<chain> fun checkValidity(): Unit {
    // Step 1 : Check if we have at least one input.
    if ( inputs.length == 0 )
      throw GeneralException(ErrorCode.NotEnoughTransactionInput)

    // Step 2 : Check if we have at least one output.
    if (newOutputs.length == 0)
    throw GeneralException(ErrorCode.NotEnoughTransactionOutput)

    // Step 3 : Check if we have other inputs when we have a generation input.
    if (inputs(0).isCoinBaseInput()) {
      if (inputs.length != 1)
        throw GeneralException(ErrorCode.GenerationInputWithOtherInputs)
    }

    for ( i <- 1 until inputs.length) {
      if (inputs(i).isCoinBaseInput())
        throw GeneralException(ErrorCode.GenerationInputWithOtherInputs)
    }

    // Step 4 : Check if sum of input values is greater than or equal to the sum of output values.
    if (!inputs(0).isCoinBaseInput()) {
      if (calculateFee(spendingOutputs, newOutputs).value < 0) {
        throw GeneralException(ErrorCode.NotEnoughInputAmounts)
      }
    }
  }

  /** Get the built transaction.
    *
    * @param lockTime The lock time of the transaction.
    * @param version The version of the transaction.
    * @return The built transaction.
    */
  fun build(lockTime : Long = 0, version : Int = ChainEnvironment.get.DefaultTransactionVersion) : Transaction {
    checkValidity()

    Transaction(
      version = version,
      inputs.toList,
      newOutputs.toList,
      lockTime = lockTime
    )
  }
}

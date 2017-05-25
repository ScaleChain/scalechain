package io.scalechain.blockchain.chain

import io.scalechain.blockchain.script.ScriptSerializer
import io.scalechain.blockchain.script.ops.OpPush
import io.scalechain.blockchain.script.ops.OpReturn
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.GeneralException
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.transaction.*
import io.scalechain.util.Bytes
import java.math.BigDecimal


/**
  * Build a transaction by using inputs and outputs provided.
  * Note that the builder does not check if this is a double spending transaction.
  * IOW, it does not check if the outputs pointed by inputs are already spent.
  *
  */
class TransactionBuilder() {
  /** The outputs spent by inputs. The order of spendingOutputs matches inputs.
    */
  val spendingOutputs = arrayListOf<TransactionOutput>()

  /** The inputs of the transaction
    */
  val inputs = arrayListOf<TransactionInput>()

  /** The outputs of the transaction
    */
  val newOutputs = arrayListOf<TransactionOutput>()

  /** Add a generation transaction input.
    *
    * @param coinbaseData The coinbase data to embed into the generation transaction input.
    * @param sequenceNumber The sequence number in the generation transaction input.
    */
  fun addGenerationInput(coinbaseData : CoinbaseData, sequenceNumber : Long = 0) : TransactionBuilder {
    // TODO : Need to move to a singleton to avoid writing the same code over and over.
    val allZeroHash = Hash.ALL_ZERO
    // TODO : Need to make sure that the output index is serialized correctly for the generation transaction
    val outputIndex : Long = 0xFFFFFFFFL

    inputs.add( GenerationTransactionInput(allZeroHash, outputIndex, coinbaseData, sequenceNumber) )
    return this
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
  fun addInput(db : KeyValueDatabase, coinsView : CoinsView, outPoint : OutPoint, unlockingScriptOption : UnlockingScript?, sequenceNumberOption : Long?) : TransactionBuilder {
    // TODO : Check if the sequenceNumberOption.get is the maximum of unsigned integer.
    val input = NormalTransactionInput(
      Hash(outPoint.transactionHash.value),
      outPoint.outputIndex.toLong(),
      unlockingScriptOption ?: UnlockingScript(Bytes(byteArrayOf())),
      sequenceNumberOption ?: 0L )

    inputs.add( input )

    spendingOutputs.add(
      coinsView.getTransactionOutput(db, input.getOutPoint())
    )
    return this
  }

  fun addInput(db : KeyValueDatabase, coinsView : CoinsView, outPoint : OutPoint) : TransactionBuilder {
    return addInput(db, coinsView, outPoint, null, null);
  }

    /** Add a transaction output with a public key hash.
    *
    * @param amount The amount of coins .
    * @param publicKeyHash The public key hash to put into the locking script.
    */
  fun addOutput(amount : CoinAmount, publicKeyHash : Hash) : TransactionBuilder {
    val pubKeyScript = ParsedPubKeyScript.from(publicKeyHash.value.array)
    val output = TransactionOutput( amount.coinUnits(), pubKeyScript.lockingScript() )
    newOutputs.add( output )
    return this
  }

  /** Add a transaction output with an output ownership.
    *
    * @param amount The amount of coins.
    * @param outputOwnership The output ownership that owns the output.
    */
  fun addOutput(amount : CoinAmount, outputOwnership : OutputOwnership) : TransactionBuilder {
    val output = TransactionOutput( amount.coinUnits(), outputOwnership.lockingScript() )
    newOutputs.add( output )
    return this
  }

  /**
    * Add an output whose locking script only contains the given bytes prefixed with OP_RETURN.
    *
    * Used by the block signer to create a transaction that contains the block hash to sign.
    *
    * @param data
    * @return
    */
  fun addOutput(data : ByteArray) : TransactionBuilder {
    val lockingScriptOps = listOf( OpReturn(), OpPush.from(data) )
    val lockingScriptData = ScriptSerializer.serialize(lockingScriptOps)
    val output = TransactionOutput( 0L, LockingScript(Bytes(lockingScriptData)))
    newOutputs.add(output)
    return this
  }

  protected fun calculateFee(spendingOutputs : List<TransactionOutput>, newOutputs : List<TransactionOutput>) : CoinAmount {
    val fee = spendingOutputs.fold(0L, { sum, item -> sum + item.value}) - newOutputs.fold(0L, { sum, item -> sum + item.value} )
    return CoinAmount.from(fee)
  }

  /** Check if the current status of the builder is valid.
    */
  protected fun checkValidity(): Unit {
    // Step 1 : Check if we have at least one input.
    if ( inputs.size == 0 )
      throw GeneralException(ErrorCode.NotEnoughTransactionInput)

    // Step 2 : Check if we have at least one output.
    if (newOutputs.size == 0)
    throw GeneralException(ErrorCode.NotEnoughTransactionOutput)

    // Step 3 : Check if we have other inputs when we have a generation input.
    if (inputs[0].isCoinBaseInput()) {
      if (inputs.size != 1)
        throw GeneralException(ErrorCode.GenerationInputWithOtherInputs)
    }

    for ( i in 1 until inputs.size) {
      if (inputs[i].isCoinBaseInput())
        throw GeneralException(ErrorCode.GenerationInputWithOtherInputs)
    }

    // Step 4 : Check if sum of input values is greater than or equal to the sum of output values.
    if (!inputs[0].isCoinBaseInput()) {
      if (calculateFee(spendingOutputs, newOutputs).value < BigDecimal.valueOf(0)) {
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
  fun build(lockTime : Long, version : Int) : Transaction {
    checkValidity()

    return Transaction(
      version,
      inputs,
      newOutputs,
      lockTime
    )
  }
  fun build() : Transaction {
    return build(0, ChainEnvironment.get().DefaultTransactionVersion)
  }

  companion object {
    /** create a transaction builder.
     *
     * @return The transaction builder.
     */
    @JvmStatic
    fun newBuilder() = TransactionBuilder()

    @JvmStatic
    fun newGenerationTransaction(coinbaseData : CoinbaseData, minerAddress : CoinAddress) : Transaction {
      return TransactionBuilder.newBuilder()
          .addGenerationInput(coinbaseData)
          .addOutput(CoinAmount(50), minerAddress)
          .build()
    }
  }
}

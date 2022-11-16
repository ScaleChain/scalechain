package io.scalechain.blockchain.chain

import io.scalechain.blockchain.chain.mining.BlockMining
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.*
import io.scalechain.blockchain.script.hash
import io.scalechain.crypto.HashEstimation
import io.scalechain.util.Bytes
import java.util.*

/** A transaction output with an outpoint of it.
  *
  * @param output The transaction output.
  * @param outPoint The transaction out point which is used for pointing the transaction output.
  */
data class OutputWithOutPoint(val output : TransactionOutput, val outPoint : OutPoint)


data class TransactionWithName(val name:String, val transaction:Transaction)

data class NewOutput(val amount : CoinAmount, val outputOwnership : OutputOwnership)

/**
 * An abstract block building test that has necessary methods for building test data.
 * @params chainView ; if this is not null, transactions are signed in the generated test data.
 */
abstract class AbstractBlockBuildingTest(private val chainView : BlockchainView? = null) : TransactionTestInterface {
  abstract val db : KeyValueDatabase

  open fun generateAccountAddress(account:String) : AddressData {
    assert( db != null )
    val addressData = generateAddress()
    onAddressGeneration(account, addressData.address)
    return addressData
  }

  open fun onAddressGeneration(account:String, address : CoinAddress) : Unit {
    // by default, do nothing.
  }

  fun getTxHash(transactionWithName : TransactionWithName) = transactionWithName.transaction.hash()
  fun getBlockHash(block : Block) = block.header.hash()

  /** Add all outputs in a transaction into an output set.
   *
   * @param outputSet The output set where each output of the given transaction is added.
   * @param transactionWithName The transaction that has outputs to be added to the set.
   */
  open fun addTransaction(outputSet : TransactionOutputSet, transactionWithName : TransactionWithName ) : Unit {
    val transactionHash = getTxHash(transactionWithName)
    var outputIndex = -1

    transactionWithName.transaction.outputs.forEach { output ->
      outputIndex += 1
      outputSet.addTransactionOutput( OutPoint(transactionHash, outputIndex), output )
    }
  }

  val availableOutputs = TransactionOutputSet()

  /** Create a generation transaction
   *
   * @param amount The amount of coins to generate
   * @param generatedBy The OutputOwnership that owns the newly generated coin. Ex> a coin address.
   * @return The newly generated transaction
   */
  fun generationTransaction( name : String,
                             amount : CoinAmount,
                             generatedBy : OutputOwnership
  ) : TransactionWithName {
    val transaction = TransactionBuilder.newBuilder()
      // Need to put a random number so that we have different transaction id for the generation transaction.
      .addGenerationInput( CoinbaseData(Bytes("Random:${Random().nextLong()}.ScaleChain by Kunwoo, Kwanho, Chanwoo, Kangmo.".toByteArray())))
      .addOutput(CoinAmount(50L), generatedBy)
      .build()
    val transactionWithName = TransactionWithName(name, transaction)
    addTransaction( availableOutputs, transactionWithName)
    return transactionWithName
  }

  /** Get an output of a given transaction.
   *
   * @param transactionWithName The transaction where we get an output.
   * @param outputIndex The index of the output. Zero-based index.
   * @return The transaction output with an out point.
   */
  fun getOutput(transactionWithName : TransactionWithName, outputIndex : Int) : OutputWithOutPoint {
    val transactionHash = transactionWithName.transaction.hash()
    return OutputWithOutPoint( transactionWithName.transaction.outputs[outputIndex], OutPoint(transactionHash, outputIndex))
  }


  /** Create a normal transaction
   *
   * @param spendingOutputs The list of spending outputs. These are spent by inputs.
   * @param newOutputs The list of newly created outputs.
   * @return
   */
  fun normalTransaction( name : String, spendingOutputs : List<OutputWithOutPoint>, newOutputs : List<NewOutput>, privateKeys : List<PrivateKey> = listOf()) : TransactionWithName {
    val builder = TransactionBuilder.newBuilder()

    spendingOutputs.forEach { output ->
      builder.addInput(db, availableOutputs, output.outPoint)
    }

    newOutputs.forEach { output ->
      builder.addOutput(output.amount, output.outputOwnership)
    }

    val transaction = builder.build()

    val finalTransaction = if (chainView != null) {
      sign(chainView, transaction, privateKeys)
    } else {
      transaction
    }

    val transactionWithName = TransactionWithName(name, finalTransaction)

    addTransaction( availableOutputs, transactionWithName)

    return transactionWithName
  }

  fun sign( chainView : BlockchainView, tx : Transaction, privateKeys : List<PrivateKey>) : Transaction {
    // Sign the transaction if we can.
    val signedTransaction = TransactionSigner(db).sign(tx, chainView, listOf(), privateKeys, SigHash.ALL)
    assert( signedTransaction.complete == true )
    return signedTransaction.transaction
  }

  fun newBlock(prevBlockHash : Hash, transactionsWithName : List<TransactionWithName>) : Block {
    val builder = BlockBuilder.newBuilder()

    transactionsWithName.forEach {
      builder.addTransaction(it.transaction)
    }

    val block = builder.build(prevBlockHash, System.currentTimeMillis() / 1000)

    return block
  }

  /**
   * Mine a block whose estimated hash calculation is the given one.
   *
   * @param block The block to mine. We will change nonce of the block for each iteration.
   * @param requiredHashCalculations The estimated hash calculations of the block should be this value.
   * @param nonce The nonce value.
   * @return The mined block.
   */

  tailrec fun doMining(block : Block, requiredHashCalculations : Int, nonce : Int = 0) : Block {
    val newBlockHeader = block.header.copy(nonce = nonce.toLong())
    val newBlockHash = newBlockHeader.hash()

    if (HashEstimation.getHashCalculations(newBlockHash.value.array) == requiredHashCalculations.toLong()) {
      val newBlock = block.copy( header = newBlockHeader )

      return newBlock
    } else {
      return doMining(block, requiredHashCalculations, nonce + 1)
    }
  }

  fun minerAddress() : CoinAddress {
    return CoinAddress.from(PrivateKey.generate())
  }

  fun mineBlock(db : KeyValueDatabase, chain : Blockchain) : Block {

    val blockMining = BlockMining(db, chain.txDescIndex(), chain.txPool, chain)
    val COINBASE_MESSAGE = CoinbaseData(Bytes("height:${chain.getBestBlockHeight() + 1}, ScaleChain by Kangmo.".toByteArray()))
    // Step 2 : Create the block template
    val blockTemplate = blockMining.getBlockTemplate(COINBASE_MESSAGE, minerAddress(), 1024*1024)
    val block = blockTemplate.createBlock( blockTemplate.getBlockHeader( chain.getBestBlockHash(db)!! ), nonce = 0 )
    return doMining( block, requiredHashCalculations = 4)
  }

}

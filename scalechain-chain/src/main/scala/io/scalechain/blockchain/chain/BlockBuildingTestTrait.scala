package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.transaction.{OutputOwnership, CoinAmount, TransactionTestDataTrait, CoinAddress}
import io.scalechain.blockchain.script.HashSupported._

/** A transaction output with an outpoint of it.
  *
  * @param output The transaction output.
  * @param outPoint The transaction out point which is used for pointing the transaction output.
  */
case class OutputWithOutPoint(output : TransactionOutput, outPoint : OutPoint)


case class TransactionWithName(name:String, transaction:Transaction)

/**
  * Created by kangmo on 6/14/16.
  */
trait BlockBuildingTestTrait extends TransactionTestDataTrait {
  def generateAddress(account:String) : AddressData = {
    val addressData = generateAddress()
    onAddressGeneration(account, addressData.address)
    addressData
  }

  def onAddressGeneration(account:String, address : CoinAddress) : Unit = {
    // by default, do nothing.
  }

  def getTxHash(transactionWithName : TransactionWithName) = transactionWithName.transaction.hash
  def getBlockHash(block : Block) = block.header.hash

  /** Add all outputs in a transaction into an output set.
    *
    * @param outputSet The output set where each output of the given transaction is added.
    * @param transactionWithName The transaction that has outputs to be added to the set.
    */
  def addTransaction(outputSet : TransactionOutputSet, transactionWithName : TransactionWithName ) : Unit = {
    val transactionHash = getTxHash(transactionWithName)
    var outputIndex = -1

    transactionWithName.transaction.outputs foreach { output =>
      outputIndex += 1
      outputSet.addTransactionOutput( OutPoint(transactionHash, outputIndex), output )
    }
  }

  val availableOutputs = new TransactionOutputSet()

  /** Create a generation transaction
    *
    * @param amount The amount of coins to generate
    * @param generatedBy The OutputOwnership that owns the newly generated coin. Ex> a coin address.
    * @return The newly generated transaction
    */
  def generationTransaction( name : String,
                             amount : CoinAmount,
                             generatedBy : OutputOwnership
                           ) : TransactionWithName = {
    val transaction = TransactionBuilder.newBuilder(availableOutputs)
      .addGenerationInput(CoinbaseData("The scalable crypto-current, ScaleChain by Kwanho, Chanwoo, Kangmo."))
      .addOutput(CoinAmount(50), generatedBy)
      .build()
    val transactionWithName = TransactionWithName(name, transaction)
    addTransaction( availableOutputs, transactionWithName)
    transactionWithName
  }

  /** Get an output of a given transaction.
    *
    * @param transactionWithName The transaction where we get an output.
    * @param outputIndex The index of the output. Zero-based index.
    * @return The transaction output with an out point.
    */
  def getOutput(transactionWithName : TransactionWithName, outputIndex : Int) : OutputWithOutPoint = {
    val transactionHash = transactionWithName.transaction.hash
    OutputWithOutPoint( transactionWithName.transaction.outputs(outputIndex), OutPoint(transactionHash, outputIndex))
  }

  case class NewOutput(amount : CoinAmount, outputOwnership : OutputOwnership)

  /** Create a new normal transaction
    *
    * @param spendingOutputs The list of spending outputs. These are spent by inputs.
    * @param newOutputs The list of newly created outputs.
    * @return
    */
  def normalTransaction( name : String, spendingOutputs : List[OutputWithOutPoint], newOutputs : List[NewOutput]) : TransactionWithName = {
    val builder = TransactionBuilder.newBuilder(availableOutputs)

    spendingOutputs foreach { output =>
      builder.addInput(output.outPoint)
    }

    newOutputs foreach { output =>
      builder.addOutput(output.amount, output.outputOwnership)
    }

    val transaction = builder.build()

    val transactionWithName = TransactionWithName(name, transaction)

    addTransaction( availableOutputs, transactionWithName)

    transactionWithName
  }

  def newBlock(prevBlockHash : Hash, transactionsWithName : List[TransactionWithName]) : Block = {
    val builder = BlockBuilder.newBuilder()

    transactionsWithName.map(_.transaction) foreach { transaction =>
      builder.addTransaction(transaction)
    }

    val block = builder.build(prevBlockHash, System.currentTimeMillis() / 1000)

    block
  }

}

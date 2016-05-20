package io.scalechain.blockchain.chain




import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.BlockIndex
import io.scalechain.blockchain.transaction.{OutputOwnership, CoinAddress, CoinAmount, TransactionTestDataTrait}
import io.scalechain.util.HexUtil

import scala.collection.mutable

class TestBlockIndex extends BlockIndex {
  var bestBlockHash : BlockHash = null
  var bestBlockHeight = -1
  val transactions = new mutable.HashMap[TransactionHash, Transaction]
  val blocks = new mutable.HashMap[BlockHash, (BlockInfo,Block)]

  def addBlock(block : Block, height : Int) : Unit = {
    val blockHash : BlockHash = BlockHash( HashCalculator.blockHeaderHash(block.header) )
    blocks.put(blockHash, (
      BlockInfo(
        height,
        block.transactions.length,
        0,
        block.header,
        None
      ),
      block)
    )
    if (bestBlockHeight < height) {
      bestBlockHash = blockHash
      bestBlockHeight = height
    }
  }

  /** Get a block by its hash.
    *
    * @param blockHash
    */
  def getBlock(blockHash : BlockHash) : Option[(BlockInfo, Block)] = {
    blocks.get(blockHash)
  }

  def addTransaction(transactionHash : TransactionHash, transaction : Transaction) : Unit = {
    transactions.put(transactionHash, transaction)
  }

  /** Get a transaction by its hash.
    *
    * @param transactionHash
    */
  def getTransaction(transactionHash : TransactionHash) : Option[Transaction] = {
    transactions.get(transactionHash)
  }
}

/**
  * Created by kangmo on 5/18/16.
  */
trait ChainTestDataTrait extends TransactionTestDataTrait {

  val blockIndex = new TestBlockIndex()

  val availableOutputs = new TransactionOutputSet()

  object Account {
    object Alice {
      val Addr1 = generateAddress() // for receiving from others
      val Addr2 = generateAddress() // for receiving changes
    }

    object Bob {
      val Addr1 = generateAddress() // for receiving from others
      val Addr2 = generateAddress() // for receiving changes
    }

    object Carry {
      val Addr1 = generateAddress() // for receiving from others
      val Addr2 = generateAddress() // for receiving changes
    }
  }


  class TestBlockchainView extends BlockchainView {
    def getTransactionOutput(outPoint : OutPoint) : Option[TransactionOutput] = {
      availableOutputs.getTransactionOutput(outPoint)
    }
    def getIterator(height : Long) : Iterator[ChainBlock] = {
      // unused.
      assert(false)
      null
    }
    def getBestBlockHeight() : Long = {
      blockIndex.bestBlockHeight
    }

    def getTransaction(transactionHash : Hash) : Option[Transaction] = {
      blockIndex.getTransaction(TransactionHash(transactionHash.value))
    }
  }

  import Account._

  /** Add all outputs in a transaction into an output set.
    *
    * @param outputSet The output set where each output of the given transaction is added.
    * @param transaction The transaction that has outputs to be added to the set.
    */
  def addTransaction(outputSet : TransactionOutputSet, transaction : Transaction ) = {
    val transactionHash = Hash( HashCalculator.transactionHash(transaction))
    var outputIndex = -1

    transaction.outputs foreach { output =>
      outputIndex += 1
      outputSet.addTransactionOutput( OutPoint(transactionHash, outputIndex), output )
    }

    blockIndex.addTransaction(TransactionHash(transactionHash.value), transaction)
  }

  /** Create a generation transaction
    *
    * @param amount The amount of coins to generate
    * @param generatedBy The OutputOwnership that owns the newly generated coin. Ex> a coin address.
    * @return The newly generated transaction
    */
  def generationTransaction( amount : CoinAmount,
                             generatedBy : OutputOwnership
                           ) : Transaction = {
    val transaction = TransactionBuilder.newBuilder(availableOutputs)
      .addGenerationInput(CoinbaseData("The scalable crypto-current, ScaleChain by Kwanho, Kangmo, Chanwoo, Rachel."))
      .addOutput(CoinAmount(50), Account.Alice.Addr1.address)
      .build()
    addTransaction( availableOutputs, transaction)
    transaction
  }

  /** A transaction output with an outpoint of it.
    *
    * @param output The transaction output.
    * @param outPoint The transaction out point which is used for pointing the transaction output.
    */
  case class OutputWithOutPoint(output : TransactionOutput, outPoint : OutPoint)

  /** Get an output of a given transaction.
    *
    * @param transaction The transaction where we get an output.
    * @param outputIndex The index of the output. Zero-based index.
    * @return The transaction output with an out point.
    */
  def getOutput(transaction : Transaction, outputIndex : Int) : OutputWithOutPoint = {
    val transactionHash = Hash(HashCalculator.transactionHash(transaction))
    OutputWithOutPoint( transaction.outputs(outputIndex), OutPoint(transactionHash, outputIndex))
  }

  case class NewOutput(amount : CoinAmount, outputOwnership : OutputOwnership)

  /** Create a new normal transaction
    *
    * @param spendingOutputs The list of spending outputs. These are spent by inputs.
    * @param newOutputs The list of newly created outputs.
    * @return
    */
  def normalTransaction( spendingOutputs : List[OutputWithOutPoint], newOutputs : List[NewOutput]) = {
    val builder = TransactionBuilder.newBuilder(availableOutputs)

    spendingOutputs foreach { output =>
      builder.addInput(output.outPoint)
    }

    newOutputs foreach { output =>
      builder.addOutput(output.amount, output.outputOwnership)
    }

    val transaction = builder.build()

    addTransaction( availableOutputs, transaction)

    transaction
  }

  def addBlock(block: Block) : Unit = {
    blockIndex.addBlock(block, blockIndex.bestBlockHeight+1)
  }

  def newBlock(transactions : List[Transaction]) : Block = {
    val builder = BlockBuilder.newBuilder()

    transactions foreach { transaction =>
        builder.addTransaction(transaction)
    }

    val block = builder.build(blockIndex.bestBlockHash, System.currentTimeMillis())
    addBlock(block)
    block
  }


  object History {
    // Put genesis block.
    addBlock(env.GenesisBlock)

    /////////////////////////////////////////////////////////////////////////////////
    // Block height 1
    /////////////////////////////////////////////////////////////////////////////////
    // Scenario : Coin Generation Only
    // Step 1 : Alice mines a coin with amount 50 SC.
    val AliceGenTx = generationTransaction( CoinAmount(50), Alice.Addr1.address )
    val AliceGenCoin_A50 = getOutput(AliceGenTx, 0)

    // Create the first block.
    newBlock(List(AliceGenTx))

    /////////////////////////////////////////////////////////////////////////////////
    // Block height 2
    /////////////////////////////////////////////////////////////////////////////////
    val BobGenTx = generationTransaction( CoinAmount(50), Bob.Addr1.address )
    val BobGenCoin_A50 = getOutput(BobGenTx, 0)
    /////////////////////////////////////////////////////////////////////////////////
    // Scenario : Send to one address keeping change
    // Step 2 : Alice sends 10 SC to Bob, and keeps 39 SC paying 1 SC as fee.
    val AliceToBobTx = normalTransaction(
                          spendingOutputs = List(AliceGenCoin_A50),
                          newOutputs = List(
                             NewOutput(CoinAmount(10), Bob.Addr1.address),
                             NewOutput(CoinAmount(39), Alice.Addr2.address)
                             // We have very expensive fee, 1 SC ㅋㅋㅋㅋㅋㅋㅋㅋㅋ
                         )
                       )

    val BobCoin1_A10         = getOutput(AliceToBobTx, 0)
    val AliceChangeCoin1_A39 = getOutput(AliceToBobTx, 1)

    // Create the second block.
    newBlock(List(BobGenTx, AliceToBobTx))

    /////////////////////////////////////////////////////////////////////////////////
    // Block height 3
    /////////////////////////////////////////////////////////////////////////////////
    val CarryGenTx = generationTransaction( CoinAmount(50), Carry.Addr1.address )
    val CarrayGenCoin_A50 = getOutput(CarryGenTx, 0)
    /////////////////////////////////////////////////////////////////////////////////
    // Scenario : Spending a coin, send to many addresses
    // Step 3 : Bob sends 2 SC to Alice, 3 SC to Carray, and keeps 5 SC paying no fee.
    val BobToAliceAndCarray = normalTransaction(
       spendingOutputs = List(BobCoin1_A10),
       newOutputs = List(
         NewOutput(CoinAmount(2), Alice.Addr1.address),
         NewOutput(CoinAmount(3), Carry.Addr1.address),
         NewOutput(CoinAmount(5), Bob.Addr2.address)
       )
     )
    val AliceCoin1_A2     = getOutput(BobToAliceAndCarray, 0)
    // The Carray.Addr1 has ownership.
    val CarrayCoin1_A3    = getOutput(BobToAliceAndCarray, 1)
    val BobChangeCoin1_A5 = getOutput(BobToAliceAndCarray, 2)

    // Create the second block.
    newBlock(List(CarryGenTx, BobToAliceAndCarray))

    /////////////////////////////////////////////////////////////////////////////////
    // Scenario : Spending a coin send to one address.
    // Step 4 : Alice sends 2 SC to Carray paying 1 SC as fee.
    val AliceToCarryTx = normalTransaction(
      spendingOutputs = List(AliceCoin1_A2),
      newOutputs = List(
        NewOutput(CoinAmount(2), Carry.Addr2.address)
      )
    )
    // The Carray.Addr2 has ownership.
    val CarryCoin2_A1     = getOutput(AliceToCarryTx, 0)

    /////////////////////////////////////////////////////////////////////////////////
    // Scenaro : Merge coins into one coin for an address
    // Step 5 : Carry uses two coins 3 SC and 1 SC to send 4SC to Alice without any fee.
    val CarryMergeToAliceTx = normalTransaction(
      spendingOutputs = List(CarrayCoin1_A3, CarryCoin2_A1),
      newOutputs = List(
        NewOutput(CoinAmount(4), Carry.Addr1.address)
      )
    )
    val AliceCoin3_A4     = getOutput(CarryMergeToAliceTx, 0)

  }

}

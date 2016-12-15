package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.BlockIndex
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.*
import io.scalechain.util.HexUtil

class TestBlockIndex : BlockIndex {
  var bestBlockHash : Hash? = null
  var bestBlockHeight = -1L
  val transactions = mutableMapOf<Hash, Transaction>()
  val blocks = mutableMapOf<Hash, Pair<BlockInfo,Block>>()

  fun addBlock(block : Block, height : Long) : Unit {
    val blockHash : Hash = block.header.hash()
    blocks.put(
      blockHash,
      Pair(
        BlockInfo(
          height,
          0,    // chain work
          null, // next block hash.
          block.transactions.size,
          0,
          block.header,
          null
        ),
        block
      )
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
  override fun getBlock(db : KeyValueDatabase, blockHash : Hash) : Pair<BlockInfo, Block>? {
    return blocks.get(blockHash)
  }

  fun addTransaction(transactionHash : Hash, transaction : Transaction) : Unit {
    transactions.put(transactionHash, transaction)
  }

  /** Get a transaction by its hash.
    *
    * @param transactionHash
    */
  override fun getTransaction(db : KeyValueDatabase, transactionHash : Hash) : Transaction? {
    return transactions.get(transactionHash)
  }
}

/**
  * A blockchain sample data for testing purpose only.
  */
open class ChainSampleData(override val db : KeyValueDatabase, private val chainEventListener: ChainEventListener?) : BlockBuildingTestTrait() {

  // create an environment before creating account addresses
  private val __env = env()
  private val blockIndex = TestBlockIndex()

  inner class AliceClass {
    val Addr1 = generateAccountAddress("Alice") // for receiving from others
    val Addr2 = generateAccountAddress("Alice") // for receiving changes
  }
  val Alice = AliceClass()

  inner class BobClass {
    val Addr1 = generateAccountAddress("Bob") // for receiving from others
    val Addr2 = generateAccountAddress("Bob") // for receiving changes
  }
  val Bob = BobClass()

  inner class CarryClass {
    val Addr1 = generateAccountAddress("Carry") // for receiving from others
    val Addr2 = generateAccountAddress("Carry") // for receiving changes
  }
  val Carry = CarryClass()


  inner class TestBlockchainViewClass : BlockchainView {
    override fun getTransactionOutput(db : KeyValueDatabase, outPoint : OutPoint) : TransactionOutput {
      return availableOutputs.getTransactionOutput(db, outPoint)
    }
    override fun getIterator(db : KeyValueDatabase, height : Long) : Iterator<ChainBlock> {
      // unused.
      throw UnsupportedOperationException()
    }
    override fun getBestBlockHeight() : Long {
      return blockIndex.bestBlockHeight
    }

    override fun getTransaction(db : KeyValueDatabase, transactionHash : Hash) : Transaction? {
      return blockIndex.getTransaction( db, transactionHash )
    }
  }
  val TestBlockchainView = TestBlockchainViewClass()


  /** Add all outputs in a transaction into an output set.
    *
    * @param outputSet The output set where each output of the given transaction is added.
    * @param transactionWithName The transaction that has outputs to be added to the set.
    */
  override fun addTransaction(outputSet : TransactionOutputSet, transactionWithName : TransactionWithName ) : Unit {
    super.addTransaction(outputSet, transactionWithName)

    val transactionHash = getTxHash(transactionWithName)
    blockIndex.addTransaction( transactionHash, transactionWithName.transaction)
    chainEventListener?.onNewTransaction(db, transactionHash, transactionWithName.transaction, null, null)
    //println(s"transaction(${transactionWithName.name}) added : ${transactionHash}")
  }


  fun addBlock(block: Block) : Unit {
    val blockHeight = blockIndex.bestBlockHeight+1
    blockIndex.addBlock(block, blockHeight)
    var transactionIndex = -1;
    block.transactions.forEach { transaction ->
      transactionIndex += 1
      chainEventListener?.onNewTransaction(
        db,
        transaction.hash(),
        transaction,
        ChainBlock(blockHeight.toLong(), block) ,
         transactionIndex
      )
    }
  }

  fun newBlock(transactionsWithName : List<TransactionWithName>) : Block {
    val block = newBlock(blockIndex.bestBlockHash!!, transactionsWithName)
    addBlock(block)
    return block
  }


  // Test cases may override this method to check the status of blockchain.
  open fun onStepFinish(stepNumber : Int): Unit {
    // to nothing
  }

  // Put genesis block.
  val __dummy1 = addBlock(env().GenesisBlock)

  /////////////////////////////////////////////////////////////////////////////////
  // Step 1
  /////////////////////////////////////////////////////////////////////////////////
  // Block height  : 1
  // Confirmations : 3
  /////////////////////////////////////////////////////////////////////////////////
  // Scenario : Coin Generation Only
  // Alice mines a coin with amount 50 SC.
  val S1_AliceGenTx = generationTransaction( "S1_AliceGenTx", CoinAmount(50), Alice.Addr1.address )
  val S1_AliceGenTxHash = getTxHash(S1_AliceGenTx)
  val S1_AliceGenCoin_A50 = getOutput(S1_AliceGenTx, 0)
  val __dummy2 = assert(S1_AliceGenCoin_A50.outPoint.outputIndex == 0)

  // Create the first block.
  val S1_Block = newBlock(listOf(S1_AliceGenTx))
  val S1_BlockHash = getBlockHash(S1_Block)
  val S1_BlockHeight = 1L

  val __dummy3 = onStepFinish(1)

  /////////////////////////////////////////////////////////////////////////////////
  // Step 2
  /////////////////////////////////////////////////////////////////////////////////
  // Block height  : 2
  // Confirmations : 2
  /////////////////////////////////////////////////////////////////////////////////
  val S2_BobGenTx = generationTransaction( "S2_BobGenTx", CoinAmount(50), Bob.Addr1.address )
  val S2_BobGenTxHash = getTxHash(S2_BobGenTx)
  val S2_BobGenCoin_A50 = getOutput(S2_BobGenTx, 0)
  /////////////////////////////////////////////////////////////////////////////////
  // Scenario : Send to one address keeping change
  // Alice sends 10 SC to Bob, and keeps 39 SC paying 1 SC as fee.
  val S2_AliceToBobTx = normalTransaction(
    "S2_AliceToBobTx",
    spendingOutputs = listOf(S1_AliceGenCoin_A50),
    newOutputs = listOf(
      NewOutput(CoinAmount(10), Bob.Addr1.address),
      NewOutput(CoinAmount(39), Alice.Addr2.address)
      // We have very expensive fee, 1 SC ㅋㅋㅋㅋㅋㅋㅋㅋㅋ
    )
  )
  val S2_AliceToBobTxHash = getTxHash(S2_AliceToBobTx)

  val S2_BobCoin1_A10         = getOutput(S2_AliceToBobTx, 0)
  val S2_AliceChangeCoin1_A39 = getOutput(S2_AliceToBobTx, 1)

  // Create the second block.
  val S2_Block = newBlock(listOf(S2_BobGenTx, S2_AliceToBobTx))
  val S2_BlockHash = getBlockHash(S2_Block)
  val S2_BlockHeight = 2L

  val __dummy4 = onStepFinish(2)

  /////////////////////////////////////////////////////////////////////////////////
  // Step 3
  /////////////////////////////////////////////////////////////////////////////////
  // Block height  : 3
  // Confirmations : 1
  /////////////////////////////////////////////////////////////////////////////////
  val S3_CarryGenTx = generationTransaction( "S3_CarryGenTx", CoinAmount(50), Carry.Addr1.address )
  val S3_CarryGenTxHash = getTxHash(S3_CarryGenTx)
  val S3_CarrayGenCoin_A50 = getOutput(S3_CarryGenTx, 0)
  /////////////////////////////////////////////////////////////////////////////////
  // Scenario : Spending a coin, send to many addresses
  // Step 3 : Bob sends 2 SC to Alice, 3 SC to Carray, and keeps 5 SC paying no fee.
  val S3_BobToAliceAndCarrayTx = normalTransaction(
    "S3_BobToAliceAndCarrayTx",
    spendingOutputs = listOf(S2_BobCoin1_A10),
    newOutputs = listOf(
      NewOutput(CoinAmount(2), Alice.Addr1.address),
      NewOutput(CoinAmount(3), Carry.Addr1.address),
      NewOutput(CoinAmount(5), Bob.Addr2.address)
    )
  )
  val S3_BobToAliceAndCarrayTxHash = getTxHash(S3_BobToAliceAndCarrayTx)

  val S3_AliceCoin1_A2     = getOutput(S3_BobToAliceAndCarrayTx, 0)
  // The Carray.Addr1 has ownership.
  val S3_CarrayCoin1_A3    = getOutput(S3_BobToAliceAndCarrayTx, 1)
  val S3_BobChangeCoin1_A5 = getOutput(S3_BobToAliceAndCarrayTx, 2)

  // Create the second block.
  val S3_Block = newBlock(listOf(S3_CarryGenTx, S3_BobToAliceAndCarrayTx))
  val S3_BlockHash = getBlockHash(S3_Block)
  val S3_BlockHeight = 3L

  val __dummy5 = onStepFinish(3)

  /////////////////////////////////////////////////////////////////////////////////
  // Step 4
  /////////////////////////////////////////////////////////////////////////////////
  // Scenario : Spending a coin send to one address.
  // Alice sends 2 SC to Carray paying 1 SC as fee.
  val S4_AliceToCarryTx = normalTransaction(
    "S4_AliceToCarryTx",
    spendingOutputs = listOf(S3_AliceCoin1_A2),
    newOutputs = listOf(
      NewOutput(CoinAmount(2), Carry.Addr2.address)
    )
  )
  val S4_AliceToCarryTxHash = getTxHash(S4_AliceToCarryTx)
  // The Carray.Addr2 has ownership.
  val S4_CarryCoin2_A1     = getOutput(S4_AliceToCarryTx, 0)
  val __dummy6 = onStepFinish(4)

  /////////////////////////////////////////////////////////////////////////////////
  // Step 5
  /////////////////////////////////////////////////////////////////////////////////
  // Scenaro : Merge coins into one coin for an address
  // Carry uses two coins 3 SC and 1 SC to send 4SC to Alice without any fee.
  val S5_CarryMergeToAliceTx = normalTransaction(
    "S5_CarryMergeToAliceTx",
    spendingOutputs = listOf(S3_CarrayCoin1_A3, S4_CarryCoin2_A1),
    newOutputs = listOf(
      NewOutput(CoinAmount(4), Alice.Addr1.address)
    )
  )
  val S5_CarryMergeToAliceTxHash = getTxHash(S5_CarryMergeToAliceTx)
  val S5_AliceCoin3_A4     = getOutput(S5_CarryMergeToAliceTx, 0)

  val __dummy7 = onStepFinish(5)
}

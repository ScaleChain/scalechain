package io.scalechain.blockchain.chain


import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.transaction.{OutputOwnership, CoinAddress, CoinAmount, TransactionTestDataTrait}
import io.scalechain.util.HexUtil

/**
  * Created by kangmo on 5/18/16.
  */
trait ChainTestDataTrait extends TransactionTestDataTrait {

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

  import Account._

  /** Add all outputs in a transaction into an output set.
    *
    * @param outputSet The output set where each output of the given transaction is added.
    * @param transaction The transaction that has outputs to be added to the set.
    */
  def addTransactionOutputs( outputSet : TransactionOutputSet, transaction : Transaction ) = {
    val transactionHash = Hash( HashCalculator.transactionHash(transaction))
    var outputIndex = -1

    transaction.outputs foreach { output =>
      outputIndex += 1
      outputSet.addTransactionOutput( OutPoint(transactionHash, outputIndex), output )
    }
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
    addTransactionOutputs( availableOutputs, transaction)
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

    addTransactionOutputs( availableOutputs, transaction)

    transaction
  }


  object History {
    /////////////////////////////////////////////////////////////////////////////////
    // Scenario : Coin Generation
    // Step 1 : Alice mines a coin with amount 50 SC.
    val AliceGenTx = generationTransaction( CoinAmount(50), Alice.Addr1.address )
    val AliceGenCoin_A50 = getOutput(AliceGenTx, 0)

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

    val BobCoin1_A10        = getOutput(AliceToBobTx, 0)
    val AliceChangeCoin1_A39 = getOutput(AliceToBobTx, 1)

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
    val CarrayCoin1_A3    = getOutput(BobToAliceAndCarray, 1)
    val BobChangeCoin1_A5 = getOutput(BobToAliceAndCarray, 2)


    /////////////////////////////////////////////////////////////////////////////////
    // Scenario : Spending a coin send to one address.
    // Step 4 : Alice sends 2 SC to Carray paying 1 SC as fee.
    val AliceToCarryTx = normalTransaction(
      spendingOutputs = List(AliceCoin1_A2),
      newOutputs = List(
        NewOutput(CoinAmount(2), Carry.Addr1.address)
      )
    )
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

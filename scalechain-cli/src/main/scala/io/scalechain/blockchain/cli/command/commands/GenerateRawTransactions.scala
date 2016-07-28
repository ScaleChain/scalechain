package io.scalechain.blockchain.cli.command.commands

import java.io.{PrintWriter, File}
import java.util

import io.scalechain.blockchain.chain.{TransactionOutputSet, TransactionBuilder}
import io.scalechain.blockchain.cli.CoinMiner
import io.scalechain.blockchain.proto.codec.TransactionCodec
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.storage.index.{RocksDatabase, KeyValueDatabase}
import io.scalechain.blockchain.transaction._
import io.scalechain.util.HexUtil
import io.scalechain.wallet.Wallet
import org.apache.commons.io.FileUtils
import HashSupported._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Random


object TransactionGenerator {
  def create : TransactionGenerator = {
    Storage.initialize()
    val walletDbPath = new File(s"./target/transaction-generator-${Random.nextLong}")
    FileUtils.deleteDirectory(walletDbPath)
    walletDbPath.mkdir()

    val db : KeyValueDatabase = new RocksDatabase(walletDbPath)
    val wallet = Wallet.create
    new TransactionGenerator(wallet)(db)
  }
}


class TransactionGenerator(wallet : Wallet)(implicit db : KeyValueDatabase) extends AutoCloseable {

  val chainView = new BlockchainView {
    val txMap = new mutable.HashMap[Hash, Transaction]()
    val availableOutputs = new TransactionOutputSet()

    def getIterator(height : Long)(implicit db : KeyValueDatabase) : Iterator[ChainBlock] = {
      assert(false)
      null
    }
    def getBestBlockHeight() : Long = {
      assert(false)
      0
    }

    def getTransaction(transactionHash : Hash)(implicit db : KeyValueDatabase) : Option[Transaction] = {
      txMap.get(transactionHash)
    }

    def getTransactionOutput(outPoint : OutPoint)(implicit db : KeyValueDatabase) : TransactionOutput = {
      availableOutputs.getTransactionOutput(outPoint)
    }

    def addTransaction(transaction : Transaction) = {
      val txHash = transaction.hash
      txMap.put(txHash, transaction)

      for ( i <- 0 until transaction.outputs.length) {
        val outPoint = OutPoint(txHash, i)
        availableOutputs.addTransactionOutput(outPoint, transaction.outputs(i))
      }
    }
  }

  def addTransaction(tx : Transaction) = {
    chainView.addTransaction(tx)
  }

  def close() = {
    db.close
  }

  def newTransaction(inputTxHash : Hash, inputStartIndex : Int, inputEndIndex : Int, newOwnerAddresses : IndexedSeq[CoinAddress]) : Transaction = {
    val builder = TransactionBuilder.newBuilder()

    for (i <- inputStartIndex to inputEndIndex) {
      val outPoint = OutPoint(inputTxHash, i)
      builder.addInput(chainView, outPoint)
    }

    val sumOfInputAmounts = {
      builder.inputs.map { input =>
        chainView.getTransactionOutput(input.getOutPoint())
      }.foldLeft(0L)(_ + _.value)
    }

    val splitOutputCount = newOwnerAddresses.length
    val outputAmount = splitOutputCount

    val eachOutputAmount = sumOfInputAmounts / splitOutputCount
    for (i <- 0 until splitOutputCount) {
      builder.addOutput(CoinAmount.from(eachOutputAmount), newOwnerAddresses(i))
    }

    val transaction = builder.build()

    transaction
  }

  def newAddresses(addressCount : Int) : IndexedSeq[CoinAddress] = {
    (0 until addressCount).map { i =>
      wallet.newAddress("")
    }
  }

  def signTransaction(transaction : Transaction, privateKeysOption : Option[List[PrivateKey]] = None ) : Transaction = {
    val signedTransaction : SignedTransaction =
      Wallet.get.signTransaction(
        transaction,
        chainView,
        List(),
        privateKeysOption,
        SigHash.ALL)

    assert(signedTransaction.complete)
    signedTransaction.transaction
  }
}

/**
  * Created by kangmo on 7/28/16.
  */
object GenerateRawTransactions extends Command {
  def invoke(command : String, args : Array[String]) = {
    val privateKeyString = args(1)
    val outputSplitCount = Integer.parseInt(args(2))
    val transactionGroupCount = Integer.parseInt(args(3))
    val transactionCountPerGroup = Integer.parseInt(args(4))

    val privateKey = PrivateKey.from(privateKeyString)

    // Assumption : The output of the generation transaction of the block height 1 (right above the genesis block)
    //              can be spent by using the given private key.
    //              (1) For a node, scalechain.mining.address in scalechain.conf should have the address generated from the given private key.
    //              (2) The node mines a block with height 1.
    val generationTx = generationTransaction(1, privateKey)

    val txGenerator = TransactionGenerator.create
    txGenerator.addTransaction(generationTx)


    val txGroupAddresses = txGenerator.newAddresses(transactionGroupCount)

    // The transaction that splits the output of the generation tx into N outputs owned by tx group addresses.
    val unsignedInitialSplitTransaction =  txGenerator.newTransaction(generationTx.hash, 0, 0, txGroupAddresses)
    val initialSplitTransaction = txGenerator.signTransaction( unsignedInitialSplitTransaction, Some(List(privateKey)) )
    txGenerator.addTransaction(initialSplitTransaction)

    val initialSplitTxHash = initialSplitTransaction.hash

//    val threads =
      (0 until transactionGroupCount).map { i =>
//        new Thread() {
          var outputIndex = i
          val splitAddresses = txGenerator.newAddresses(outputSplitCount)
          val mergeAddresses = txGenerator.newAddresses(1)
          //override def run(): Unit = {
            val writer = new PrintWriter(new File(s"transaction-group-${i}.txt"))
            var txHash : Hash = initialSplitTxHash
            for (i <- 1 to transactionCountPerGroup/2) {
              if ( ((i >> 10) << 10) == i) { // i % 1024 = 0
                println(s"Processing ${i*2} transactions")
              }
              val splitTx = txGenerator.newTransaction(txHash, outputIndex, outputIndex, splitAddresses )
              val signedSplitTx = txGenerator.signTransaction( splitTx )
              txGenerator.addTransaction(signedSplitTx)

              val mergeTx = txGenerator.newTransaction(signedSplitTx.hash, 0, splitTx.outputs.length-1, mergeAddresses)
              val signedMergeTx = txGenerator.signTransaction( mergeTx )
              txGenerator.addTransaction(signedMergeTx)

              val rawSplitTransaction = HexUtil.hex(TransactionCodec.serialize(signedSplitTx))
              val rawMergeTransaction = HexUtil.hex(TransactionCodec.serialize(signedMergeTx))

              writer.append(rawSplitTransaction)
              writer.append(rawMergeTransaction)

              txHash = signedMergeTx.hash
              outputIndex = 0 // From now on, we will use only the first output in the mergeTx
            }
            writer.close
          //}
  //      }
      }

//    threads foreach { _.start } // Start all threads
//    threads foreach { _.join }  // Join all threads

    // Write the initial split transaction.
    val writer = new PrintWriter(new File(s"initial-split-transaction.txt"))
    val rawInitialSplitTransaction = HexUtil.hex(TransactionCodec.serialize(initialSplitTransaction))
    writer.append(rawInitialSplitTransaction)
    writer.close
  }

  def generationTransaction( height : Long, privateKey: PrivateKey) = {
    val minerAddress = CoinAddress.from(privateKey)
    val coinbaseData = CoinMiner.coinbaseData(1)
    TransactionBuilder.newGenerationTransaction(coinbaseData, minerAddress)
  }
}
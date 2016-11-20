package io.scalechain.blockchain.cli.command.stresstests

import java.io.File

import io.scalechain.blockchain.chain.{TransactionBuilder, TransactionOutputSet}
import io.scalechain.blockchain.cli.CoinMiner
import io.scalechain.blockchain.proto.{TransactionOutput, OutPoint, Transaction, Hash}
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.storage.index.{RocksDatabase, KeyValueDatabase}
import io.scalechain.blockchain.transaction._
import io.scalechain.wallet.Wallet
import org.apache.commons.io.FileUtils
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap
import HashSupported._

import scala.util.Random

object TransactionGenerator {
  Storage.initialize()
  val walletDbPath = File(s"./target/transaction-generator-${Random.nextLong}")
  FileUtils.deleteDirectory(walletDbPath)
  walletDbPath.mkdir()

  val db : KeyValueDatabase = RocksDatabase(walletDbPath)
  val wallet = Wallet.create

  fun create : TransactionGenerator {
    TransactionGenerator(wallet)(db)
  }

  fun generationTransaction( height : Long, privateKey: PrivateKey) {
    val minerAddress = CoinAddress.from(privateKey)
    val coinbaseData = CoinMiner.coinbaseData(1)
    TransactionBuilder.newGenerationTransaction(coinbaseData, minerAddress)
  }
}



class TransactionGenerator(wallet : Wallet)(implicit db : KeyValueDatabase) : AutoCloseable {

  val chainView = BlockchainView {
    val txMap = ConcurrentHashMap<Hash, Transaction>()
    val availableOutputs = TransactionOutputSet()

    fun getIterator(height : Long)(implicit db : KeyValueDatabase) : Iterator<ChainBlock> {
      assert(false)
      null
    }
    fun getBestBlockHeight() : Long {
      assert(false)
      0
    }

    fun getTransaction(transactionHash : Hash)(implicit db : KeyValueDatabase) : Option<Transaction> {
      val tx = txMap.get(transactionHash)
      if (tx == null) None else Some(tx)
    }

    fun getTransactionOutput(outPoint : OutPoint)(implicit db : KeyValueDatabase) : TransactionOutput {
      availableOutputs.getTransactionOutput(outPoint)
    }

    fun addTransaction(transaction : Transaction) {
      val txHash = transaction.hash
      txMap.put(txHash, transaction)

      for ( i <- 0 until transaction.outputs.length) {
        val outPoint = OutPoint(txHash, i)
        availableOutputs.addTransactionOutput(outPoint, transaction.outputs(i))
      }
    }
  }

  fun addTransaction(tx : Transaction) {
    chainView.addTransaction(tx)
  }

  fun close() {
    db.close
  }

  fun newTransaction(inputTxHash : Hash, inputStartIndex : Int, inputEndIndex : Int, newOwnerAddresses : IndexedSeq<CoinAddress>) : Transaction {
    val builder = TransactionBuilder.newBuilder()

    for (i <- inputStartIndex to inputEndIndex) {
      val outPoint = OutPoint(inputTxHash, i)
      builder.addInput(chainView, outPoint)
    }

    val sumOfInputAmounts {
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

  fun newAddresses(addressCount : Int) : IndexedSeq<CoinAddress> {
    (0 until addressCount).map { i =>
      wallet.newAddress("")
    }
  }

  fun signTransaction(transaction : Transaction, privateKeysOption : Option<List<PrivateKey>> = None ) : Transaction {
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

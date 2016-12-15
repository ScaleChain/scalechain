package io.scalechain.blockchain.cli.command.stresstests

import java.io.File

import io.scalechain.blockchain.chain.TransactionBuilder
import io.scalechain.blockchain.chain.TransactionOutputSet
import io.scalechain.blockchain.cli.CoinMiner
import io.scalechain.blockchain.proto.TransactionOutput
import io.scalechain.blockchain.proto.OutPoint
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.storage.index.RocksDatabase
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.*
import io.scalechain.wallet.Wallet
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap

import scala.util.Random
import java.io.Closeable

class TransactionGeneratorBlockchainView : BlockchainView {
  val txMap = ConcurrentHashMap<Hash, Transaction>()
  val availableOutputs = TransactionOutputSet()

  override fun getIterator(db : KeyValueDatabase, height : Long) : Iterator<ChainBlock> {
    throw UnsupportedOperationException()
  }

  override fun getBestBlockHeight() : Long {
    throw UnsupportedOperationException()
  }

  override fun getTransaction(db : KeyValueDatabase, transactionHash : Hash) : Transaction? {
    return txMap.get(transactionHash)
  }

  override fun getTransactionOutput(db : KeyValueDatabase, outPoint : OutPoint) : TransactionOutput {
    return availableOutputs.getTransactionOutput(db, outPoint)
  }

  fun addTransaction(transaction : Transaction) {
    val txHash = transaction.hash()
    txMap.put(txHash, transaction)

    for ( i in 0 until transaction.outputs.size) {
      val outPoint = OutPoint(txHash, i)
      availableOutputs.addTransactionOutput(outPoint, transaction.outputs[i])
    }
  }
}

class TransactionGenerator(private val db : KeyValueDatabase, private val wallet : Wallet) : Closeable {



  val chainView = TransactionGeneratorBlockchainView()

  fun addTransaction(tx : Transaction) {
    chainView.addTransaction(tx)
  }

  override fun close() {
    db.close()
  }

  fun newTransaction(inputTxHash : Hash, inputStartIndex : Int, inputEndIndex : Int, newOwnerAddresses : List<CoinAddress>) : Transaction {
    val builder = TransactionBuilder.newBuilder()

    for (i in inputStartIndex .. inputEndIndex) {
      val outPoint = OutPoint(inputTxHash, i)
      builder.addInput(db, chainView, outPoint)
    }

    val sumOfInputAmounts  =
      builder.inputs.map { input ->
        chainView.getTransactionOutput(db, input.getOutPoint())
      }.fold(0L, {sum, item -> sum + item.value } )

    val splitOutputCount = newOwnerAddresses.size

    val eachOutputAmount = sumOfInputAmounts / splitOutputCount
    for (i in 0 until splitOutputCount) {
      builder.addOutput(CoinAmount.from(eachOutputAmount), newOwnerAddresses[i])
    }

    val transaction = builder.build()

    return transaction
  }

  fun newAddresses(addressCount : Int) : List<CoinAddress> {
    return (0 until addressCount).map { i ->
      wallet.newAddress(db, "")
    }
  }

  fun signTransaction(transaction : Transaction, privateKeysOption : List<PrivateKey>? = null ) : Transaction {
    val signedTransaction : SignedTransaction =
      Wallet.get().signTransaction(
        db,
        transaction,
        chainView,
        listOf(),
        privateKeysOption,
        SigHash.ALL)

    assert(signedTransaction.complete)
    return signedTransaction.transaction
  }

  companion object {
    var db : KeyValueDatabase
    var wallet : Wallet

    init {
      Storage.initialize()
      val walletDbPath = File("./target/transaction-generator-${Random().nextLong()}")
      walletDbPath.deleteRecursively()
      walletDbPath.mkdir()

      db = RocksDatabase(walletDbPath)
      wallet = Wallet.create()
    }

    fun create() : TransactionGenerator {
      return TransactionGenerator(db, wallet)
    }

    fun generationTransaction( height : Long, privateKey: PrivateKey) : Transaction {
      val minerAddress = CoinAddress.from(privateKey)
      val coinbaseData = CoinMiner.coinbaseData(1)
      return TransactionBuilder.newGenerationTransaction(coinbaseData, minerAddress)
    }
  }
}

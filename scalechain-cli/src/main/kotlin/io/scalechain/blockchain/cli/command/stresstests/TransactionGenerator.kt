package io.scalechain.blockchain.cli.command.stresstests

import java.io.File

import io.scalechain.blockchain.chain.TransactionBuilder
import io.scalechain.blockchain.cli.CoinMiner
import io.scalechain.blockchain.proto.OutPoint
import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.storage.Storage
import io.scalechain.blockchain.storage.index.DatabaseFactory
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.*
import io.scalechain.wallet.Wallet
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap

import scala.util.Random
import java.io.Closeable


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
      val walletDbPath = File("./build/transaction-generator-${Random().nextLong()}")
      walletDbPath.deleteRecursively()
      walletDbPath.mkdir()

      db = DatabaseFactory.create(walletDbPath)
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

package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._
import HashSupported._

/**
  * Created by kangmo on 6/16/16.
  */
class TransactionMagnetSpec : BlockchainTestTrait with TransactionTestDataTrait with Matchers {

  this: Suite =>

  val testPath = File("./target/unittests-TransactionMagnetSpec/")

  var tm : TransactionMagnet = null

  implicit var keyValueDB : KeyValueDatabase = null

  override fun beforeEach() {
    // initialize a test.

    super.beforeEach()

    keyValueDB = db

    // put the genesis block
    chain.putBlock(env.GenesisBlockHash, env.GenesisBlock)

    tm = chain.txMagnet
  }

  override fun afterEach() {
    super.afterEach()

    keyValueDB = null
    // finalize a test.
    tm = null
  }

  "markOutputSpent" should "" in {
    val data = TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._
  }

  "markOutputUnspent" should "" in {
    val data = TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._
  }

  "markAllOutputsUnspent" should "" in {
    val data = TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._
  }

  "detachTransactionInput" should "" in {
    val data = TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._
  }

  "detachTransactionInputs" should "" in {
    val data = TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._
  }

  "detachTransaction" should "" in {
    val data = TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._
  }

  "attachTransactionInput" should "" in {
    val data = TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._
  }

  "attachTransactionInputs" should "" in {
    val data = TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._
  }

  "attachTransaction" should "" in {
    val data = TransactionSampleData()
    import data._
    import data.Block._
    import data.Tx._
  }
}

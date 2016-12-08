package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest.*
import HashSupported.*

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

  "markOutputSpent" should "" {
    val data = TransactionSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*
  }

  "markOutputUnspent" should "" {
    val data = TransactionSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*
  }

  "markAllOutputsUnspent" should "" {
    val data = TransactionSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*
  }

  "detachTransactionInput" should "" {
    val data = TransactionSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*
  }

  "detachTransactionInputs" should "" {
    val data = TransactionSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*
  }

  "detachTransaction" should "" {
    val data = TransactionSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*
  }

  "attachTransactionInput" should "" {
    val data = TransactionSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*
  }

  "attachTransactionInputs" should "" {
    val data = TransactionSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*
  }

  "attachTransaction" should "" {
    val data = TransactionSampleData()
    import data.*
    import data.Block.*
    import data.Tx.*
  }
}

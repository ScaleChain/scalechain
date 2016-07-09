package io.scalechain.wallet

import java.io.File

import io.scalechain.blockchain.chain.BlockSampleData
import io.scalechain.blockchain.proto.{Transaction, Hash}
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.{RocksDatabase, KeyValueDatabase}
import io.scalechain.blockchain.storage.{DiskBlockStorage, Storage}
import io.scalechain.blockchain.transaction.{ChainBlock, TransactionTestDataTrait}
import org.apache.commons.io.FileUtils
import org.scalatest.{Suite, Matchers, BeforeAndAfterEach, FlatSpec}
import HashSupported._
/**
  * Test if Wallet returns expected data during block reorganization.
  */
class WalletAndChainSpec extends FlatSpec with WalletTestTrait with BeforeAndAfterEach with TransactionTestDataTrait with Matchers {
  this: Suite =>

  val testPath = new File("./target/unittests-WalletAndChainSpec-storage/")

  implicit var keyValueDB : KeyValueDatabase = null
  override def beforeEach() {

    super.beforeEach()

    keyValueDB = db
  }

  override def afterEach() {

    super.afterEach()

    keyValueDB = null
  }

  /**
    * List transactions in blocks and the transaction pool.
    *
    * @return The list of transaction hashes.
    */
  def listTransactionHashes() : List[Hash] = {
    wallet.listTransactions(chain, Some("test account"), count=100000, skip=0, includeWatchOnly = true ).map{ walletTxDesc : WalletTransactionDescriptor =>
      walletTxDesc.txid.get
    }
  }

  /**
    * List transactions in the transaction pool only.
    *
    * @return The list of transaction hashes.
    */
  def listPoolTransactionHashes() : List[Hash] = {
    chain.txPool.getTransactionsFromPool().map { case(txHash : Hash, transaction : Transaction) =>
      txHash
    }
  }

  "blockchain" should "reorganize blocks" in {
    val data = new BlockSampleData()
    import data._
    import data.Block._
    import data.Tx._

    wallet.importOutputOwnership(chain, "test account", Addr1.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr2.address, rescanBlockchain = false)
    wallet.importOutputOwnership(chain, "test account", Addr3.address, rescanBlockchain = false)

    List(
      GEN01,
      GEN02, TX02,
      GEN03b, TX03, TX03b,
      GEN04a, TX04, TX04a,
      GEN04b, TX04, TX04b, TX04b2,
      GEN05a, TX05a
    ).foreach { t =>
      println (s"all tx hashes : ${t.name} = ${t.transaction.hash}")
    }


    chain.putBlock(BLK01.header.hash, BLK01) // chain work = 4
    listTransactionHashes().toSet shouldBe Set(
      GEN01
    ).map(_.transaction.hash)

    listPoolTransactionHashes().toSet shouldBe Set()

    chain.putBlock(BLK02.header.hash, BLK02) // chain work = 4 + 4
    listTransactionHashes().toSet shouldBe Set(
      GEN01,
      GEN02, TX02
    ).map(_.transaction.hash)
    listPoolTransactionHashes().toSet shouldBe Set()


    chain.putBlock(BLK03a.header.hash, BLK03a) // chain work = 4 + 4 + 4
    listTransactionHashes().toSet shouldBe Set(
      GEN01,
      GEN02, TX02,
      GEN03a, TX03, TX03a
    ).map(_.transaction.hash)
    listPoolTransactionHashes().toSet shouldBe Set()


    // The chain work is equal to the best blockchain. nothing happens.
    chain.putBlock(BLK03b.header.hash, BLK03b) // chain work = 4 + 4 + 4
    listTransactionHashes().toSet shouldBe Set(
      GEN01,
      GEN02, TX02,
      GEN03a, TX03, TX03a
    ).map(_.transaction.hash)
    listPoolTransactionHashes().toSet shouldBe Set()


    chain.putBlock(BLK04a.header.hash, BLK04a) // chain work = 4 + 4 + 4 + 4
    listTransactionHashes().toSet shouldBe Set(
      GEN01,
      GEN02, TX02,
      GEN03a, TX03, TX03a,
      GEN04a, TX04, TX04a
    ).map(_.transaction.hash)
    listPoolTransactionHashes().toSet shouldBe Set()

    
    chain.putBlock(BLK04b.header.hash, BLK04b) // chain work = 4 + 4 + 4 + 8, block reorg should happen.
    listTransactionHashes().toSet shouldBe Set(
      GEN01,
      GEN02, TX02,
      GEN03b, TX03, TX03b,
      GEN04b, TX04, TX04b, TX04b2,
      TX04a // TX04a does not conflict with TX04b
    ).map(_.transaction.hash)
    listPoolTransactionHashes().toSet shouldBe Set(TX04a).map(_.transaction.hash)

    chain.putBlock(BLK05a.header.hash, BLK05a) // chain work = 4 + 4 + 4 + 4 + 8, block reorg should happen again.
    listTransactionHashes().toSet shouldBe Set(
      GEN01,
      GEN02, TX02,
      GEN03a, TX03, TX03a,
      GEN04a, TX04, TX04a,
      GEN05a, TX05a,

      // TX04b can't go into the transaction pool when the BLK04a becomes the best block,
      // as it depends on the output GEN03b created on the branch b.
      TX04b2 // TX04b2 do not conflict with other transactions.
    ).map(_.transaction.hash)
    // TX04b2 goes to the transaction pool, as it depends on the unpent output, (TX02,2)
    listPoolTransactionHashes().toSet shouldBe Set(TX04b2).map(_.transaction.hash)
  }
}
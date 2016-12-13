package io.scalechain.blockchain.chain

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import java.io.File

import io.scalechain.blockchain.chain.mining.BlockMining
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.RocksDatabase
import org.junit.runner.RunWith

// Need to rewrite test case
@RunWith(KTestJUnitRunner::class)
class BlockMiningSpec : BlockchainTestTrait(), Matchers {

  override val testPath = File("./target/unittests-BlockMiningSpec/")

  lateinit var bm : BlockMining
  lateinit var data : TransactionSampleData

  override fun beforeEach() {
    super.beforeEach()

    val data = TransactionSampleData(db)
    val B = data.Block

    // put the genesis block
    chain.putBlock(db, data.env().GenesisBlockHash, data.env().GenesisBlock)
    chain.putBlock(db, B.BLK01.header.hash(), B.BLK01)
    chain.putBlock(db, B.BLK02.header.hash(), B.BLK02)
    chain.putBlock(db, B.BLK03.header.hash(), B.BLK03)

    assert(db is RocksDatabase)
    bm = BlockMining(db as RocksDatabase, chain.txDescIndex(), chain.txPool, chain)

  }

  override fun afterEach() {

    super.afterEach()

    // finalize a test.
  }

  init {
    "getBlockTemplate" should "" {
    }

    "selectTransactions" should "select complete transactions." {
      val d = data
      val T = d.Tx

      val inputTransactions = listOf(
        T.TX04_04,
        T.TX04_03,
        T.TX04_02,
        T.TX04_01
      )

      val expectedTransactions = listOf(
        T.GEN04,
        // T.TX04_01 ~ T.TX04_04 => These are dependent transactions, should be sorted by dependency.
        T.TX04_01, // fee 1
        T.TX04_02, // fee 2
        T.TX04_03, // fee 4
        T.TX04_04 // fee 12
      )

      bm.selectTransactions(T.GEN04.transaction, inputTransactions.map{ it.transaction }, 1024 * 1024).second shouldBe expectedTransactions.map { it.transaction }
    }

    "selectTransactions" should "select complete transactions with higher fees." {
      val d = data
      val T = d.Tx

      val inputTransactions = listOf(
        T.TX04_05_05,
        T.TX04_05_04,
        T.TX04_05_03,
        T.TX04_05_02,
        T.TX04_05_01,
        T.TX04_04,
        T.TX04_03,
        T.TX04_02,
        T.TX04_01
      )

      val expectedTransactions = listOf(
        T.GEN04,
        // T.TX04_01 ~ T.TX04_04 => These are dependent transactions, should be sorted by dependency.
        T.TX04_01, // fee 1
        T.TX04_02, // fee 2
        T.TX04_03, // fee 4
        T.TX04_04, // fee 12
        // T.TX04_05_0X => These are independent transactions, should be sorted by fee in descending order.
        T.TX04_05_01, // fee 8
        T.TX04_05_02, // fee 6
        T.TX04_05_03, // fee 4
        T.TX04_05_04, // fee 2
        T.TX04_05_05  // fee 0
      )

      bm.selectTransactions(T.GEN04.transaction, inputTransactions.map{ it.transaction }, 1024 * 1024).second shouldBe expectedTransactions.map { it.transaction }
    }

    /*
    listOf(Transaction(version=4, inputs=listOf(NormalTransactionInput(outputTransactionHash=Hash(bytes("4b35379e8cc3f7e74f3cdfe3af8c012d9f12cbff6b1ef2bcc6d12051de3d2909")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=listOf(TransactionOutput(value=4900000000L, lockingScript=LockingScript(bytes("76a9147e21c66b58634bac0e4b151faca4720a820c83f588ac")))), lockTime=0L))
    listOf(Transaction(version=4, inputs=listOf(NormalTransactionInput(outputTransactionHash=Hash(bytes("4b35379e8cc3f7e74f3cdfe3af8c012d9f12cbff6b1ef2bcc6d12051de3d2909")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=listOf(TransactionOutput(value=4900000000L, lockingScript=LockingScript(bytes("76a9147e21c66b58634bac0e4b151faca4720a820c83f588ac")))), lockTime=0L), Transaction(version=4, inputs=listOf(NormalTransactionInput(outputTransactionHash=Hash(bytes("7fa38bfa20e703cca80b9436969d30916ca0598c76bf64ff08df7349fc796d45")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=listOf(TransactionOutput(value=2000000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac"))),TransactionOutput(value=2700000000L, lockingScript=LockingScript(bytes("76a914af083a20461a04997f1576d19c61c8b20b537f5288ac")))), lockTime=0L), Transaction(version=4, inputs=listOf(NormalTransactionInput(outputTransactionHash=Hash(bytes("7211ae4a9000306a51799d811b1299c9fcbcc7b4551b5f9e3e2c2335d2c5e0e8")), outputIndex=1L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=listOf(TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a9147e21c66b58634bac0e4b151faca4720a820c83f588ac"))),TransactionOutput(value=1300000000L, lockingScript=LockingScript(bytes("76a914af083a20461a04997f1576d19c61c8b20b537f5288ac")))), lockTime=0L), Transaction(version=4, inputs=listOf(NormalTransactionInput(outputTransactionHash=Hash(bytes("44d8a6acb6c6050c0caab9910fad61cd1fdeb2fb6428d76922d480e4e4c1567b")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L),NormalTransactionInput(outputTransactionHash=Hash(bytes("dee954e1e46293a5a895cb10eb17b72f43f7d1782621638b4a07884f1ec18325")), outputIndex=1L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=listOf(TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac"))),TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a9147e21c66b58634bac0e4b151faca4720a820c83f588ac"))),TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a914af083a20461a04997f1576d19c61c8b20b537f5288ac"))),TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac"))),TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a9147e21c66b58634bac0e4b151faca4720a820c83f588ac")))), lockTime=0L), Transaction(version=4, inputs=listOf(NormalTransactionInput(outputTransactionHash=Hash(bytes("9c900d08f2b7e8e80cd6d57448f59b17c48fac3fe0683171691d3baa709520e2")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=listOf(TransactionOutput(value=200000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac")))), lockTime=0L), Transaction(version=4, inputs=listOf(NormalTransactionInput(outputTransactionHash=Hash(bytes("9c900d08f2b7e8e80cd6d57448f59b17c48fac3fe0683171691d3baa709520e2")), outputIndex=1L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=listOf(TransactionOutput(value=400000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac")))), lockTime=0L), Transaction(version=4, inputs=listOf(NormalTransactionInput(outputTransactionHash=Hash(bytes("9c900d08f2b7e8e80cd6d57448f59b17c48fac3fe0683171691d3baa709520e2")), outputIndex=4L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=listOf(TransactionOutput(value=600000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac")))), lockTime=0L), Transaction(version=4, inputs=listOf(NormalTransactionInput(outputTransactionHash=Hash(bytes("9c900d08f2b7e8e80cd6d57448f59b17c48fac3fe0683171691d3baa709520e2")), outputIndex=3L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=listOf(TransactionOutput(value=800000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac")))), lockTime=0L), Transaction(version=4, inputs=listOf(NormalTransactionInput(outputTransactionHash=Hash(bytes("9c900d08f2b7e8e80cd6d57448f59b17c48fac3fe0683171691d3baa709520e2")), outputIndex=2L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=listOf(TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac")))), lockTime=0L))

     */

    "selectTransactions" should "exclude incomplete transactions. case 1" {
      val d = data
      val T = d.Tx

      val inputTransactions = listOf(
        T.TX04_05_05,
        T.TX04_05_04,
        T.TX04_05_03,
        T.TX04_05_02,
        T.TX04_05_01,
        T.TX04_04,
        //T.TX04_03,
        T.TX04_02,
        T.TX04_01
      )

      val expectedTransactions = listOf(
        T.GEN04,
        // T.TX04_01 ~ T.TX04_04 => These are dependent transactions, should be sorted by dependency.
        T.TX04_01, // fee 1
        T.TX04_02 // fee 2
      )

      bm.selectTransactions(T.GEN04.transaction, inputTransactions.map{ it.transaction }, 1024 * 1024).second shouldBe expectedTransactions.map{ it.transaction }
    }


    "selectTransactions" should "exclude incomplete transactions. case 2" {
      val d = data
      val T = d.Tx

      val inputTransactions = listOf(
        T.TX04_05_05,
        T.TX04_05_04,
        T.TX04_05_03,
        T.TX04_05_02,
        T.TX04_05_01,
        //      T.TX04_04,
        T.TX04_03,
        T.TX04_02,
        T.TX04_01
      )

      val expectedTransactions = listOf(
        T.GEN04,
        // T.TX04_01 ~ T.TX04_04 => These are dependent transactions, should be sorted by dependency.
        T.TX04_01, // fee 1
        T.TX04_02, // fee 2
        T.TX04_03 // fee 4
      )

      bm.selectTransactions(T.GEN04.transaction, inputTransactions.map{ it.transaction }, 1024 * 1024).second shouldBe expectedTransactions.map { it.transaction }
    }
  }
}

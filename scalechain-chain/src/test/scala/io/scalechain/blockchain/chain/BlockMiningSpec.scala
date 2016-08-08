package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.chain.mining.BlockMining
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.{RocksDatabase, KeyValueDatabase}
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._
import HashSupported._

// Need to rewrite test case
@Ignore
class BlockMiningSpec extends BlockchainTestTrait with TransactionTestDataTrait with ShouldMatchers {

  this: Suite =>

  val testPath = new File("./target/unittests-BlockMiningSpec/")

  var bm : BlockMining = null
  implicit var keyValueDB : KeyValueDatabase = null
  var data : TransactionSampleData = null

  override def beforeEach() {
    super.beforeEach()

    keyValueDB = db

    data = new TransactionSampleData()
    val d = data
    import d._
    import d.Tx._
    import d.Block._

    // put the genesis block
    chain.putBlock(d.env.GenesisBlockHash, d.env.GenesisBlock)
    chain.putBlock(BLK01.header.hash, BLK01)
    chain.putBlock(BLK02.header.hash, BLK02)
    chain.putBlock(BLK03.header.hash, BLK03)

    bm = new BlockMining(chain.txDescIndex, chain.txPool, chain)(keyValueDB.asInstanceOf[RocksDatabase])

  }

  override def afterEach() {
    bm = null
    keyValueDB = null

    super.afterEach()

    // finalize a test.
  }

  "getBlockTemplate" should "" in {
  }

  "selectTransactions" should "select complete transactions." in {
    val d = data
    import d._
    import d.Tx._
    import d.Block._

    val inputTransactions = List(
      TX04_04,
      TX04_03,
      TX04_02,
      TX04_01
    )

    val expectedTransactions = List(
      GEN04,
      // TX04_01 ~ TX04_04 => These are dependent transactions, should be sorted by dependency.
      TX04_01, // fee 1
      TX04_02, // fee 2
      TX04_03, // fee 4
      TX04_04 // fee 12
    )

    bm.selectTransactions(GEN04.transaction, inputTransactions.map(_.transaction), 1024 * 1024)._2 shouldBe expectedTransactions.map(_.transaction)
  }

  "selectTransactions" should "select complete transactions with higher fees." in {
    val d = data
    import d._
    import d.Tx._
    import d.Block._

    val inputTransactions = List(
      TX04_05_05,
      TX04_05_04,
      TX04_05_03,
      TX04_05_02,
      TX04_05_01,
      TX04_04,
      TX04_03,
      TX04_02,
      TX04_01
    )

    val expectedTransactions = List(
      GEN04,
      // TX04_01 ~ TX04_04 => These are dependent transactions, should be sorted by dependency.
      TX04_01, // fee 1
      TX04_02, // fee 2
      TX04_03, // fee 4
      TX04_04, // fee 12
      // TX04_05_0X => These are independent transactions, should be sorted by fee in descending order.
      TX04_05_01, // fee 8
      TX04_05_02, // fee 6
      TX04_05_03, // fee 4
      TX04_05_04, // fee 2
      TX04_05_05  // fee 0
    )

    bm.selectTransactions(GEN04.transaction, inputTransactions.map(_.transaction), 1024 * 1024)._2 shouldBe expectedTransactions.map(_.transaction)
  }

  /*
  List(Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("4b35379e8cc3f7e74f3cdfe3af8c012d9f12cbff6b1ef2bcc6d12051de3d2909")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=4900000000L, lockingScript=LockingScript(bytes("76a9147e21c66b58634bac0e4b151faca4720a820c83f588ac")))), lockTime=0L))
  List(Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("4b35379e8cc3f7e74f3cdfe3af8c012d9f12cbff6b1ef2bcc6d12051de3d2909")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=4900000000L, lockingScript=LockingScript(bytes("76a9147e21c66b58634bac0e4b151faca4720a820c83f588ac")))), lockTime=0L), Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("7fa38bfa20e703cca80b9436969d30916ca0598c76bf64ff08df7349fc796d45")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=2000000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac"))),TransactionOutput(value=2700000000L, lockingScript=LockingScript(bytes("76a914af083a20461a04997f1576d19c61c8b20b537f5288ac")))), lockTime=0L), Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("7211ae4a9000306a51799d811b1299c9fcbcc7b4551b5f9e3e2c2335d2c5e0e8")), outputIndex=1L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a9147e21c66b58634bac0e4b151faca4720a820c83f588ac"))),TransactionOutput(value=1300000000L, lockingScript=LockingScript(bytes("76a914af083a20461a04997f1576d19c61c8b20b537f5288ac")))), lockTime=0L), Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("44d8a6acb6c6050c0caab9910fad61cd1fdeb2fb6428d76922d480e4e4c1567b")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L),NormalTransactionInput(outputTransactionHash=Hash(bytes("dee954e1e46293a5a895cb10eb17b72f43f7d1782621638b4a07884f1ec18325")), outputIndex=1L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac"))),TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a9147e21c66b58634bac0e4b151faca4720a820c83f588ac"))),TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a914af083a20461a04997f1576d19c61c8b20b537f5288ac"))),TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac"))),TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a9147e21c66b58634bac0e4b151faca4720a820c83f588ac")))), lockTime=0L), Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("9c900d08f2b7e8e80cd6d57448f59b17c48fac3fe0683171691d3baa709520e2")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=200000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac")))), lockTime=0L), Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("9c900d08f2b7e8e80cd6d57448f59b17c48fac3fe0683171691d3baa709520e2")), outputIndex=1L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=400000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac")))), lockTime=0L), Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("9c900d08f2b7e8e80cd6d57448f59b17c48fac3fe0683171691d3baa709520e2")), outputIndex=4L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=600000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac")))), lockTime=0L), Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("9c900d08f2b7e8e80cd6d57448f59b17c48fac3fe0683171691d3baa709520e2")), outputIndex=3L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=800000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac")))), lockTime=0L), Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("9c900d08f2b7e8e80cd6d57448f59b17c48fac3fe0683171691d3baa709520e2")), outputIndex=2L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a914653fb9a4b83d35d63f0bb1644f286cfc9641b6b388ac")))), lockTime=0L))

   */

  "selectTransactions" should "exclude incomplete transactions. case 1" in {
    val d = data
    import d._
    import d.Tx._
    import d.Block._

    val inputTransactions = List(
      TX04_05_05,
      TX04_05_04,
      TX04_05_03,
      TX04_05_02,
      TX04_05_01,
      TX04_04,
      //TX04_03,
      TX04_02,
      TX04_01
    )

    val expectedTransactions = List(
      GEN04,
      // TX04_01 ~ TX04_04 => These are dependent transactions, should be sorted by dependency.
      TX04_01, // fee 1
      TX04_02 // fee 2
    )

    bm.selectTransactions(GEN04.transaction, inputTransactions.map(_.transaction), 1024 * 1024)._2 shouldBe expectedTransactions.map(_.transaction)
  }


  "selectTransactions" should "exclude incomplete transactions. case 2" in {
    val d = data
    import d._
    import d.Tx._
    import d.Block._

    val inputTransactions = List(
      TX04_05_05,
      TX04_05_04,
      TX04_05_03,
      TX04_05_02,
      TX04_05_01,
//      TX04_04,
      TX04_03,
      TX04_02,
      TX04_01
    )

    val expectedTransactions = List(
      GEN04,
      // TX04_01 ~ TX04_04 => These are dependent transactions, should be sorted by dependency.
      TX04_01, // fee 1
      TX04_02, // fee 2
      TX04_03 // fee 4
    )

    bm.selectTransactions(GEN04.transaction, inputTransactions.map(_.transaction), 1024 * 1024)._2 shouldBe expectedTransactions.map(_.transaction)
  }


}

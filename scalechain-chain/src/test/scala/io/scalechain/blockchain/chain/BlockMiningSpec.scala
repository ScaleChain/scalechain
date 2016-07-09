package io.scalechain.blockchain.chain

import java.io.File

import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.index.{RocksDatabase, KeyValueDatabase}
import io.scalechain.blockchain.transaction.TransactionTestDataTrait
import org.scalatest._
import HashSupported._

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

    bm.selectTransactions(GEN04.transaction, List(), inputTransactions.map(_.transaction), 1024 * 1024) shouldBe expectedTransactions.map(_.transaction)
  }

  /*
List(Transaction(version=4, inputs=List(GenerationTransactionInput(outputTransactionHash=Hash(bytes("0000000000000000000000000000000000000000000000000000000000000000")), outputIndex=16777215L, coinbaseData=CoinbaseData(bytes("ad2157824975331025331ecaabeccecaecabacaa")), sequenceNumber= 0L)), outputs=List(TransactionOutput(value=5000000000L, lockingScript=LockingScript(bytes("76a914522b7c17e6e03ae4bf3e9d1c1d7f68f8dd1c943e88ac")))), lockTime=0L), Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("d1a3719be2e521860af150b05b5977d3168a129e95ac8f7fa36256c3633caec1")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=4900000000L, lockingScript=LockingScript(bytes("76a91407fdda6f816ae750d700ed5c9634f6a9bce2207588ac")))), lockTime=0L))
List(Transaction(version=4, inputs=List(GenerationTransactionInput(outputTransactionHash=Hash(bytes("0000000000000000000000000000000000000000000000000000000000000000")), outputIndex=16777215L, coinbaseData=CoinbaseData(bytes("ad2157824975331025331ecaabeccecaecabacaa")), sequenceNumber= 0L)), outputs=List(TransactionOutput(value=5000000000L, lockingScript=LockingScript(bytes("76a914522b7c17e6e03ae4bf3e9d1c1d7f68f8dd1c943e88ac")))), lockTime=0L), Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("d1a3719be2e521860af150b05b5977d3168a129e95ac8f7fa36256c3633caec1")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=4900000000L, lockingScript=LockingScript(bytes("76a91407fdda6f816ae750d700ed5c9634f6a9bce2207588ac")))), lockTime=0L), Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("70b42aecc60ddcaeda3c3664ff5c64b54bbd977506cdf19f85b6682b552f1a50")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=2000000000L, lockingScript=LockingScript(bytes("76a914522b7c17e6e03ae4bf3e9d1c1d7f68f8dd1c943e88ac"))),TransactionOutput(value=2700000000L, lockingScript=LockingScript(bytes("76a914a364c6f1e9772dec11a9040b7a5d2def4b0f68ff88ac")))), lockTime=0L), Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("a2a8accbf1e3408a64257c406f25f88d77abfd334fe9d2d44b33d710f7807907")), outputIndex=1L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a91407fdda6f816ae750d700ed5c9634f6a9bce2207588ac"))),TransactionOutput(value=1300000000L, lockingScript=LockingScript(bytes("76a914a364c6f1e9772dec11a9040b7a5d2def4b0f68ff88ac")))), lockTime=0L), Transaction(version=4, inputs=List(NormalTransactionInput(outputTransactionHash=Hash(bytes("29955ea45102252d6f9b821b5ce0324e319aab78ff205914cbb40fdb65181ddd")), outputIndex=0L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L),NormalTransactionInput(outputTransactionHash=Hash(bytes("6f6bd1897bfc3f96825bb251a99788449996bcb72877b22877e4b9671ab41536")), outputIndex=1L, unlockingScript=UnlockingScript(bytes("")), sequenceNumber=0L)), outputs=List(TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a914522b7c17e6e03ae4bf3e9d1c1d7f68f8dd1c943e88ac"))),TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a91407fdda6f816ae750d700ed5c9634f6a9bce2207588ac"))),TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a914a364c6f1e9772dec11a9040b7a5d2def4b0f68ff88ac"))),TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a914522b7c17e6e03ae4bf3e9d1c1d7f68f8dd1c943e88ac"))),TransactionOutput(value=1000000000L, lockingScript=LockingScript(bytes("76a91407fdda6f816ae750d700ed5c9634f6a9bce2207588ac")))), lockTime=0L))

   */

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

    bm.selectTransactions(GEN04.transaction, List(), inputTransactions.map(_.transaction), 1024 * 1024) shouldBe expectedTransactions.map(_.transaction)
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

    bm.selectTransactions(GEN04.transaction, List(), inputTransactions.map(_.transaction), 1024 * 1024) shouldBe expectedTransactions.map(_.transaction)
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

    bm.selectTransactions(GEN04.transaction, List(), inputTransactions.map(_.transaction), 1024 * 1024) shouldBe expectedTransactions.map(_.transaction)
  }


  "selectTransactions" should "include signing transactions first" in {
    val d = data
    import d._
    import d.Tx._
    import d.Block._

    // Improve test case : Use signing transactions that are independent, and having low fees.
    val signingTransactions = List(
      TX04_01,
      TX04_02
    )

    val inputTransactions = List(
      TX04_05_05,
      TX04_05_04,
      TX04_05_03,
      TX04_05_02,
      TX04_05_01,
      TX04_04,
      TX04_03
    )

    val expectedTransactions = List(
      GEN04,
      // Signing transactions
      TX04_01, // fee 1
      TX04_02, // fee 2
      // TX04_03 ~ TX04_04 => These are dependent transactions, should be sorted by dependency.
      TX04_03, // fee 4
      TX04_04, // fee 12
      // TX04_05_0X => These are independent transactions, should be sorted by fee in descending order.
      TX04_05_01, // fee 8
      TX04_05_02, // fee 6
      TX04_05_03, // fee 4
      TX04_05_04, // fee 2
      TX04_05_05  // fee 0
    )

    bm.selectTransactions(GEN04.transaction, signingTransactions.map(_.transaction), inputTransactions.map(_.transaction), 1024 * 1024) shouldBe expectedTransactions.map(_.transaction)
  }

}

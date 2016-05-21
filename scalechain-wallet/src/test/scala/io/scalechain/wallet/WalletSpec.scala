package io.scalechain.wallet

import java.io.File

import io.scalechain.blockchain.TransactionVerificationException
import io.scalechain.blockchain.chain.{ChainSampleData, ChainTestDataTrait}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.{BlockStorage, Storage, DiskBlockStorage}
import io.scalechain.blockchain.transaction.{CoinAmount, TransactionVerifier, NormalTransactionVerifier, SigHash}
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 5/12/16.
  */

class WalletSpec extends FlatSpec with BeforeAndAfterEach with ChainTestDataTrait with ShouldMatchers {

  this: Suite =>

  Storage.initialize()

  val TEST_RECORD_FILE_SIZE = 1024 * 1024

  var wallet  : Wallet = null
//  var storage : BlockStorage = null
  val testPath = new File("./target/unittests-WalletSpec/")
  override def beforeEach() {

    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

//    storage = new DiskBlockStorage(testPath, TEST_RECORD_FILE_SIZE)
    wallet = new Wallet(testPath)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

//    storage.close()
    wallet.close()

//    storage = null
    wallet  = null

    FileUtils.deleteDirectory(testPath)

  }

  ////////////////////////////////////////////////////////////////////////////////
  // Methods for signrawtransaction RPC
  ////////////////////////////////////////////////////////////////////////////////
  "signTransaction" should "sign successfully with the private keys argument" in {
    val S = new WalletSampleData(wallet)

    val signedTransaction = Wallet.signTransaction(
      S.S4_AliceToCarryTx,
      List(),
      Some(List( S.Alice.Addr1.privateKey )),
      SigHash.ALL
    )

    signedTransaction.complete shouldBe true

    // Should not throw an exception.
    new TransactionVerifier(signedTransaction.transaction).verify(S.blockIndex)
  }

  "signTransaction" should "fail without the private keys argument if the wallet does not have required private keys" in {
    val S = new WalletSampleData(wallet)

    val signedTransaction = Wallet.signTransaction(
      S.S4_AliceToCarryTx,
      List(),
      None,
      SigHash.ALL
    )

    signedTransaction.complete shouldBe false

    // Should throw an exception.
    a [TransactionVerificationException] should be thrownBy {
      new TransactionVerifier(signedTransaction.transaction).verify(S.blockIndex)
    }
  }

  "signTransaction" should "sign successfully with the private keys argument if the wallet has required private keys" in {
    val S = new WalletSampleData(wallet)

    // TODO : Implement
  }

  "signTransaction" should "sign two inputs from different address in two steps" in {
    val S = new WalletSampleData(wallet)

    //////////////////////////////////////////////////////////////////////////
    // Step 1 : sign for the first input.
    val signedTransaction1 = Wallet.signTransaction(
      S.S5_CarryMergeToAliceTx,
      List(),
      Some(List(S.Carry.Addr1.privateKey)),
      SigHash.ALL
    )

    signedTransaction1.complete shouldBe false

    a [TransactionVerificationException] should be thrownBy {
      new TransactionVerifier(signedTransaction1.transaction).verify(S.blockIndex)
    }

    //////////////////////////////////////////////////////////////////////////
    // Step 2 : sign for the second input.
    val finalTransaction = Wallet.signTransaction(
      signedTransaction1.transaction,
      List(),
      Some(List(S.Carry.Addr2.privateKey)),
      SigHash.ALL
    )

    finalTransaction.complete shouldBe true
    new TransactionVerifier(signedTransaction1.transaction).verify(S.blockIndex)
  }


  ////////////////////////////////////////////////////////////////////////////////
  // Methods for getreceivedbyaddress RPC
  ////////////////////////////////////////////////////////////////////////////////
  "getReceivedByAddress" should "" in {
    val S = new WalletSampleData(wallet)

    // To see the full history of coin receival, see ChainTestDataTrait.History.
    Wallet.getReceivedByAddress(S.TestBlockchainView, S.Alice.Addr1.address, 3) shouldBe CoinAmount(50)
    Wallet.getReceivedByAddress(S.TestBlockchainView, S.Alice.Addr1.address, 2) shouldBe CoinAmount(50)
    Wallet.getReceivedByAddress(S.TestBlockchainView, S.Alice.Addr1.address, 1) shouldBe CoinAmount(50+2)
    Wallet.getReceivedByAddress(S.TestBlockchainView, S.Alice.Addr1.address, 0) shouldBe CoinAmount(50+2+4)
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Methods for listransaction RPC
  ////////////////////////////////////////////////////////////////////////////////
  "getTransactionHashes(None)" should "return all transaction hashes" in {
    val S = new WalletSampleData(wallet) {
      override def onStepFinish(step : Int): Unit = {
        step match {
          case 1 => {
            wallet.getTransactionHashes(None).toSet shouldBe Set(
              S1_AliceGenTxHash
            )
          }
          case 2 => {
            wallet.getTransactionHashes(Some("Alice")).toSet shouldBe Set(
              S1_AliceGenTxHash
            )
            wallet.getTransactionHashes(Some("Bob")).toSet shouldBe Set()
            wallet.getTransactionHashes(Some("Carry")).toSet shouldBe Set()
            wallet.getTransactionHashes(None).toSet shouldBe Set(
              S1_AliceGenTxHash,
              S2_BobGenTxHash, S2_AliceToBobTxHash
            )
          }
          case 3 => {
            wallet.getTransactionHashes(None).toSet shouldBe Set(
              S1_AliceGenTxHash,
              S2_BobGenTxHash, S2_AliceToBobTxHash,
              S3_CarryGenTxHash, S3_BobToAliceAndCarrayTxHash
            )
          }
          case 4 => {
            wallet.getTransactionHashes(None).toSet shouldBe Set(
              S1_AliceGenTxHash,
              S2_BobGenTxHash, S2_AliceToBobTxHash,
              S3_CarryGenTxHash, S3_BobToAliceAndCarrayTxHash,
              S4_AliceToCarryTxHash
            )
          }
          case 5 => {
            wallet.getTransactionHashes(None).toSet shouldBe Set(
              S1_AliceGenTxHash,
              S2_BobGenTxHash, S2_AliceToBobTxHash,
              S3_CarryGenTxHash, S3_BobToAliceAndCarrayTxHash,
              S4_AliceToCarryTxHash,
              S5_CarryMergeToAliceTxHash
            )
          }
        }
      }
    }
  }

  "getTransactionHashes(Some(account))" should "return transaction hashes for an account" in {
    val S = new WalletSampleData(wallet)

    wallet.getTransactionHashes(Some("Alice")).toSet shouldBe Set(
      S.S1_AliceGenTxHash,
      S.S2_AliceToBobTxHash,
      S.S3_BobToAliceAndCarrayTxHash,
      S.S4_AliceToCarryTxHash,
      S.S5_CarryMergeToAliceTxHash
    )
    wallet.getTransactionHashes(Some("Bob")).toSet shouldBe Set(
      S.S2_BobGenTxHash,
      S.S3_BobToAliceAndCarrayTxHash
    )
    wallet.getTransactionHashes(Some("Carry")).toSet shouldBe Set(
      S.S3_CarryGenTxHash,
      S.S3_BobToAliceAndCarrayTxHash,
      S.S4_AliceToCarryTxHash,
      S.S5_CarryMergeToAliceTxHash
    )
  }


  "getWalletTransactions(None)" should "return all wallet transactions for all accounts" in {
    val S = new WalletSampleData(wallet) {
      override def onStepFinish(step : Int): Unit = {
        step match {
          case 1 => {
            wallet.getWalletTransactions(None).toSet shouldBe Set()
          }
          case 2 => {
            wallet.getWalletTransactions(None).toSet shouldBe Set()
          }
          case 3 => {
            wallet.getWalletTransactions(None).toSet shouldBe Set()
          }
          case 4 => {
            wallet.getWalletTransactions(None).toSet shouldBe Set()
          }
          case 5 => {
            wallet.getWalletTransactions(None).toSet shouldBe Set()
          }
        }
      }
    }
  }

  "getWalletTransactions(Some(account))" should "return wallet transactions for an account" in {
    val S = new WalletSampleData(wallet)

    wallet.getWalletTransactions(Some("Alice")).toSet shouldBe Set()
    wallet.getWalletTransactions(Some("Bob")).toSet shouldBe Set()
    wallet.getWalletTransactions(Some("Carry")).toSet shouldBe Set()
  }


  def tx(blockIndexOption : Option[Long], addedTime : Long) = {
    WalletTransaction(
      blockHash        = Some(Hash("00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929")),
      blockIndex       = blockIndexOption,
      blockTime        = Some(1411051649),
      transactionId    = Some(Hash("99845fd840ad2cc4d6f93fafb8b072d188821f55d9298772415175c456f3077d")),
      addedTime        = addedTime,
      transactionIndex = Some(1),
      transaction = transaction1
    )
  }

  "isMoreRecentThan" should "should return true if a transaction is more recent than another" in {
    wallet.isMoreRecentThan( tx(None, 101), tx(None, 100) ) shouldBe true
    wallet.isMoreRecentThan( tx(None, 100), tx(None, 101) ) shouldBe false
    wallet.isMoreRecentThan( tx(Some(1), 101), tx(Some(1), 100) ) shouldBe true
    wallet.isMoreRecentThan( tx(Some(1), 100), tx(Some(1), 101) ) shouldBe false
    wallet.isMoreRecentThan( tx(Some(1), 101), tx(None, 100) ) shouldBe false
    wallet.isMoreRecentThan( tx(Some(1), 100), tx(None, 101) ) shouldBe false
    wallet.isMoreRecentThan( tx(Some(1), 100), tx(None, 100) ) shouldBe false
    wallet.isMoreRecentThan( tx(None, 101), tx(Some(1), 100) ) shouldBe true
    wallet.isMoreRecentThan( tx(None, 100), tx(Some(1), 100) ) shouldBe true
    wallet.isMoreRecentThan( tx(None, 100), tx(Some(1), 101) ) shouldBe true
  }

  "isMoreRecentThan" should "should hit an assertion if two transactions has the same timestamp" in {
    an [AssertionError] should be thrownBy {
      wallet.isMoreRecentThan( tx(Some(1), 100), tx(Some(1), 100) )
    }
  }

  def walletTx(transaction : Transaction, block : Option[Block]) = {
    WalletTransaction(
      blockHash        = block.map( b => Hash( HashCalculator.blockHeaderHash(b.header)) ),
      blockIndex       = Some(11),
      blockTime        = block.map(_.header.timestamp),
      transactionId    = Some(Hash(HashCalculator.transactionHash(transaction))),
      addedTime     = 1418695703,
      transactionIndex = Some(1),
      transaction = transaction
    )
  }

  "getTransactionDescriptor(Left(input), includeWatchOnly=true)" should "return some valid descriptor" in {
    val S = new WalletSampleData(wallet)
    wallet.getTransactionDescriptor(
      S.TestBlockchainView,
      walletTx(S.S2_AliceToBobTx, Some(S.S2_Block)),
      Left(NormalTransactionInput(
        TransactionHash(S.S1_AliceGenCoin_A50.outPoint.transactionHash.value),
        S.S1_AliceGenCoin_A50.outPoint.outputIndex,
        UnlockingScript(Array[Byte]()),
        0L
      )),
      0,
      negativeFeeOption = Some(scala.math.BigDecimal(-1)),
      includeWatchOnly = true
    ).get shouldBe Some(1) // need to update
  }

  "getTransactionDescriptor(Left(input), includeWatchOnly=false)" should "return None" in {
    val S = new WalletSampleData(wallet)
    wallet.getTransactionDescriptor(
      S.TestBlockchainView,
      walletTx(S.S2_AliceToBobTx, Some(S.S2_Block)),
      Left(NormalTransactionInput(
        TransactionHash(S.S1_AliceGenCoin_A50.outPoint.transactionHash.value),
        S.S1_AliceGenCoin_A50.outPoint.outputIndex,
        UnlockingScript(Array[Byte]()),
        0L
      )),
      0,
      negativeFeeOption = Some(scala.math.BigDecimal(-1)),
      includeWatchOnly = false
    ) shouldBe None // need to update
  }

  "getTransactionDescriptor(Right(output))" should "return some valid descriptor" in {
    val S = new WalletSampleData(wallet)
    wallet.getTransactionDescriptor(
      S.TestBlockchainView,
      walletTx(S.S2_AliceToBobTx, Some(S.S2_Block)),
      Right(S.S2_BobCoin1_A10.output),
      0,
      negativeFeeOption = Some(scala.math.BigDecimal(-1)),
      includeWatchOnly = true
    ).get shouldBe Some(1) // need to update
  }

  "getTransactionDescriptor(Right(output), includeWatchOnly=false)" should "return None" in {
    val S = new WalletSampleData(wallet)
    wallet.getTransactionDescriptor(
      S.TestBlockchainView,
      walletTx(S.S2_AliceToBobTx, Some(S.S2_Block)),
      Right(S.S2_BobCoin1_A10.output),
      0,
      negativeFeeOption = Some(scala.math.BigDecimal(-1)),
      includeWatchOnly = false
    ) shouldBe None // need to update
  }

  "listTransactions(None, includeWatchOnly=false)" should "return no transaction" in {
    val S = new WalletSampleData(wallet)

    // Because we did not call wallet.newAddress but wallet.importOutputOwnership,
    // we should not have any transcation with includeWatchOnly = false.
    wallet.listTransactions(
      S.TestBlockchainView,
      None,
      1000, // count
      0,    // skip
      false//includeWatchOnly
    ) shouldBe List() // TODO : Update with actual result.
  }

  "listTransactions(None, includeWatchOnly=true)" should "return all transactions" in {
    val S = new WalletSampleData(wallet) {
      override def onStepFinish(step : Int): Unit = {
        step match {
          case 1 => {
            wallet.listTransactions(
              TestBlockchainView,
              None,
              1000, // count
              0,    // skip
              true//includeWatchOnly
            ) shouldBe List() // TODO : Update with actual result.
          }
          case 2 => {
            wallet.listTransactions(
              TestBlockchainView,
              None,
              1000, // count
              0,    // skip
              true//includeWatchOnly
            ) shouldBe List() // TODO : Update with actual result.
          }
          case 3 => {
            wallet.listTransactions(
              TestBlockchainView,
              None,
              1000, // count
              0,    // skip
              true//includeWatchOnly
            ) shouldBe List() // TODO : Update with actual result.
          }
          case 4 => {
            wallet.listTransactions(
              TestBlockchainView,
              None,
              1000, // count
              0,    // skip
              true//includeWatchOnly
            ) shouldBe List() // TODO : Update with actual result.
          }
          case 5 => {
            wallet.listTransactions(
              TestBlockchainView,
              None,
              1000, // count
              0,    // skip
              true//includeWatchOnly
            ) shouldBe List() // TODO : Update with actual result.
          }
        }
      }
    }
  }

  "listTransactions(Some(account), includeWatchOnly=true)" should "return all transactions for an account" in {
    val S = new WalletSampleData(wallet)

    wallet.listTransactions(
      S.TestBlockchainView,
      Some("Alice"),
      1000, // count
      0,    // skip
      true//includeWatchOnly
    ) shouldBe List() // TODO : Update with actual result.

    wallet.listTransactions(
      S.TestBlockchainView,
      Some("Bob"),
      1000, // count
      0,    // skip
      true//includeWatchOnly
    ) shouldBe List() // TODO : Update with actual result.

    wallet.listTransactions(
      S.TestBlockchainView,
      Some("Carry"),
      1000, // count
      0,    // skip
      true//includeWatchOnly
    ) shouldBe List() // TODO : Update with actual result.

  }


  ////////////////////////////////////////////////////////////////////////////////
  // Methods for listunspent RPC
  ////////////////////////////////////////////////////////////////////////////////
  "getTransactionOutputs(None)" should "return all transaction outputs" in {
    val S = new WalletSampleData(wallet) {
      override def onStepFinish(step : Int): Unit = {
        step match {
          case 1 => {
            wallet.getTransactionOutputs(None).toSet shouldBe Set()
          }
          case 2 => {
            wallet.getTransactionOutputs(None).toSet shouldBe Set()
          }
          case 3 => {
            wallet.getTransactionOutputs(None).toSet shouldBe Set()
          }
          case 4 => {
            wallet.getTransactionOutputs(None).toSet shouldBe Set()
          }
          case 5 => {
            wallet.getTransactionOutputs(None).toSet shouldBe Set()
          }
        }
      }
    }
  }

  "getTransactionOutputs(Some(account))" should "return transaction outputs for an account" in {
    val S = new WalletSampleData(wallet) {
      override def onStepFinish(step : Int): Unit = {
        step match {
          case 1 => {
            wallet.getTransactionOutputs(Some(Alice.Addr1.address)).toSet shouldBe Set()
            wallet.getTransactionOutputs(Some(Bob.Addr1.address)).toSet shouldBe Set()
            wallet.getTransactionOutputs(Some(Carry.Addr1.address)).toSet shouldBe Set()
          }
          case 2 => {
            wallet.getTransactionOutputs(Some(Alice.Addr1.address)).toSet shouldBe Set()
            wallet.getTransactionOutputs(Some(Bob.Addr1.address)).toSet shouldBe Set()
            wallet.getTransactionOutputs(Some(Carry.Addr1.address)).toSet shouldBe Set()
          }
          case 3 => {
            wallet.getTransactionOutputs(Some(Alice.Addr1.address)).toSet shouldBe Set()
            wallet.getTransactionOutputs(Some(Bob.Addr1.address)).toSet shouldBe Set()
            wallet.getTransactionOutputs(Some(Carry.Addr1.address)).toSet shouldBe Set()
          }
          case 4 => {
            wallet.getTransactionOutputs(Some(Alice.Addr1.address)).toSet shouldBe Set()
            wallet.getTransactionOutputs(Some(Bob.Addr1.address)).toSet shouldBe Set()
            wallet.getTransactionOutputs(Some(Carry.Addr1.address)).toSet shouldBe Set()
          }
          case 5 => {
            wallet.getTransactionOutputs(Some(Alice.Addr1.address)).toSet shouldBe Set()
            wallet.getTransactionOutputs(Some(Bob.Addr1.address)).toSet shouldBe Set()
            wallet.getTransactionOutputs(Some(Carry.Addr1.address)).toSet shouldBe Set()
          }
        }
      }
    }
  }

  "getUnspentCoinDescription" should "" in {
    val S = new WalletSampleData(wallet)
    wallet.getUnspentCoinDescription(
      S.TestBlockchainView,
      Some(S.Alice.Addr1.address),
      WalletOutputWithInfo(
        outPoint = OutPoint(
          transactionHash  = Hash("00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929"),
          outputIndex      = 1
        ),
        walletOutput = WalletOutput(
          blockindex    = Some(100L),
          coinbase      = true,
          spent         = false,
          transactionOutput = transaction1.outputs(0)
        )
      )
    ) shouldBe None // Need to update
  }

  "getConfirmations" should "" in {
    val S = new WalletSampleData(wallet) {
      override def onStepFinish(step : Int): Unit = {
        step match {
          case 1 => {
            wallet.getConfirmations(TestBlockchainView, 0) shouldBe 2
            wallet.getConfirmations(TestBlockchainView, 1) shouldBe 1
          }
          case 2 => {
            wallet.getConfirmations(TestBlockchainView, 0) shouldBe 3
            wallet.getConfirmations(TestBlockchainView, 1) shouldBe 2
          }
          case 3 => {
            wallet.getConfirmations(TestBlockchainView, 0) shouldBe 4
            wallet.getConfirmations(TestBlockchainView, 1) shouldBe 3
          }
          case 4 => { // no block is created for step 4
            wallet.getConfirmations(TestBlockchainView, 0) shouldBe 4
            wallet.getConfirmations(TestBlockchainView, 1) shouldBe 3
          }
          case 5 => { // no block is created for step 4
            wallet.getConfirmations(TestBlockchainView, 0) shouldBe 4
            wallet.getConfirmations(TestBlockchainView, 1) shouldBe 3
          }
        }
      }
    }

  }

  "listUnspent(all addresses)" should "" in {
    val S = new WalletSampleData(wallet) {
      override def onStepFinish(step : Int): Unit = {
        step match {
          case 1 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, None).toSet shouldBe Set()
          }
          case 2 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, None).toSet shouldBe Set()
          }
          case 3 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, None).toSet shouldBe Set()
          }
          case 4 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, None).toSet shouldBe Set()
          }
          case 5 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, None).toSet shouldBe Set()
          }
        }
      }
    }
  }

  "listUnspent(some addresses)" should "" in {
    val S = new WalletSampleData(wallet) {
      override def onStepFinish(step : Int): Unit = {
        step match {
          case 1 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Alice.Addr1.address))).toSet shouldBe Set()
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Bob.Addr1.address))).toSet shouldBe Set()
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Carry.Addr1.address))).toSet shouldBe Set()
          }
          case 2 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Alice.Addr1.address))).toSet shouldBe Set()
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Bob.Addr1.address))).toSet shouldBe Set()
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Carry.Addr1.address))).toSet shouldBe Set()
          }
          case 3 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Alice.Addr1.address))).toSet shouldBe Set()
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Bob.Addr1.address))).toSet shouldBe Set()
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Carry.Addr1.address))).toSet shouldBe Set()
          }
          case 4 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Alice.Addr1.address))).toSet shouldBe Set()
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Bob.Addr1.address))).toSet shouldBe Set()
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Carry.Addr1.address))).toSet shouldBe Set()
          }
          case 5 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Alice.Addr1.address))).toSet shouldBe Set()
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Bob.Addr1.address))).toSet shouldBe Set()
            wallet.listUnspent(TestBlockchainView, 0, 100, Some(List(Carry.Addr1.address))).toSet shouldBe Set()
          }
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Methods for importaddress RPC
  ////////////////////////////////////////////////////////////////////////////////

  "importOutputOwnership(rescan=true)" should "rescan current blockchain" ignore {
    val S = new WalletSampleData(wallet)
    // TODO : Implement test case
  }

  "importOutputOwnership(rescan=false)" should "should not rescan the current blockchain" ignore {
    val S = new WalletSampleData(wallet)
    // TODO : Implement test case
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Methods for getaccount RPC
  ////////////////////////////////////////////////////////////////////////////////

  "getAccount" should "" in {
    val test1_Addr1 = wallet.newAddress("test1")
    val test1_Addr2 = wallet.newAddress("test1")
    val test2_Addr1 = wallet.newAddress("test2")
    val test2_Addr2 = wallet.newAddress("test2")
    val test3_Addr1 = wallet.newAddress("test3")
    val test3_Addr2 = wallet.newAddress("test3")
    wallet.getAccount(test1_Addr1) shouldBe Some("test1")
    wallet.getAccount(test1_Addr2) shouldBe Some("test1")
    wallet.getAccount(test2_Addr1) shouldBe Some("test2")
    wallet.getAccount(test2_Addr2) shouldBe Some("test2")
    wallet.getAccount(test3_Addr1) shouldBe Some("test3")
    wallet.getAccount(test3_Addr2) shouldBe Some("test3")

  }

  "getAccount" should "return None if an address not generated by newAddress is privided" in {
    wallet.getAccount(generateAddress().address) shouldBe None
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Methods for newaddress RPC
  ////////////////////////////////////////////////////////////////////////////////

  "newAddress" should "generate a new address and it should become the receiving address" in {
    val address1 = wallet.newAddress("test1")
    address1 shouldBe wallet.getReceivingAddress("test1")

    val address2 = wallet.newAddress("test1")
    address2 shouldBe wallet.getReceivingAddress("test1")
  }


  ////////////////////////////////////////////////////////////////////////////////
  // Methods for getaccountaddress RPC
  ////////////////////////////////////////////////////////////////////////////////
/*
  "getReceivingAddress" should "" ignore {
    Already tested by the following case :
    // "newAddress" should "generate a new address and it should become the receiving address"
  }
*/

  ////////////////////////////////////////////////////////////////////////////////
  // Handlers called by Chain layer.
  ////////////////////////////////////////////////////////////////////////////////

  "registerTransaction" should "ignore transactions that are not related to addresses of an account" ignore {
    val S = new ChainSampleData(None)
    // wallet.registerTransaction(List(), S.S1_AliceGenTx, None, None)
    // TODO : Implement
  }

  "registerTransaction" should "" ignore {
    val S = new ChainSampleData(None)
    // TODO : Implement
  }

  "unregisterTransaction" should "" ignore {
    val S = new WalletSampleData(wallet)
    // TODO : Implement test case
  }

  "onNewTransaction" should "" ignore {
    val S = new WalletSampleData(wallet)
    // TODO : Implement test case
  }

  "onRemoveTransaction" should "" ignore {
    val S = new WalletSampleData(wallet)
    // TODO : Implement test case
  }

  "onNewBlock" should "" ignore {
    val S = new WalletSampleData(wallet)
    // TODO : Implement test case
  }

  "onRemoveBlock" should "" ignore {
    val S = new WalletSampleData(wallet)
    // TODO : Implement test case
  }
}

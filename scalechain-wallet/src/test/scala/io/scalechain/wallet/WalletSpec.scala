package io.scalechain.wallet

import java.io.File

import io.scalechain.blockchain.TransactionVerificationException
import io.scalechain.blockchain.chain.{TransactionWithName, OutputWithOutPoint, ChainSampleData, ChainTestDataTrait}
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.blockchain.storage.{BlockStorage, Storage, DiskBlockStorage}
import io.scalechain.blockchain.transaction._
import io.scalechain.util.HexUtil
import org.apache.commons.io.FileUtils
import org.scalatest._

import HexUtil._
import org.scalatest.matchers.HavePropertyMatcher

import scala.collection.SortedSet

// TODO : BUGBUG : Need to check if coinbase maturity for transaction validation. Need to change test cases as well.

/**
  * Created by kangmo on 5/12/16.
  */
//@Ignore
class WalletSpec extends FlatSpec with BeforeAndAfterEach with ChainTestDataTrait with Matchers {

  this: Suite =>

  Storage.initialize()

  val TEST_RECORD_FILE_SIZE = 1024 * 1024

  var wallet  : Wallet = null
  var storage : DiskBlockStorage = null
  val testPathForWallet = new File("./target/unittests-WalletSpec-wallet/")
  val testPathForStorage = new File("./target/unittests-WalletSpec-storage/")
  override def beforeEach() {
    FileUtils.deleteDirectory(testPathForWallet)
    FileUtils.deleteDirectory(testPathForStorage)
    testPathForWallet.mkdir()
    testPathForStorage.mkdir()

    storage = new DiskBlockStorage(testPathForStorage, TEST_RECORD_FILE_SIZE)
    DiskBlockStorage.theBlockStorage = storage
    wallet = new Wallet(testPathForWallet)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    storage.close()
    wallet.close()

    storage = null
    wallet  = null

    FileUtils.deleteDirectory(testPathForWallet)
    FileUtils.deleteDirectory(testPathForStorage)
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Methods for signrawtransaction RPC
  ////////////////////////////////////////////////////////////////////////////////
  "signTransaction" should "sign successfully with the private keys argument" in {
    val S = new WalletSampleData(wallet)

    val signedTransaction = Wallet.signTransaction(
      S.S4_AliceToCarryTx.transaction,
      S.TestBlockchainView,
      List(),
      Some(List( S.Alice.Addr1.privateKey )),
      SigHash.ALL
    )

    signedTransaction.complete shouldBe true

    // Should not throw an exception.
    new TransactionVerifier(signedTransaction.transaction).verify(S.TestBlockchainView)
  }

  "signTransaction" should "fail without the private keys argument if the wallet does not have required private keys" in {
    val S = new WalletSampleData(wallet)

    val signedTransaction = Wallet.signTransaction(
      S.S4_AliceToCarryTx.transaction,
      S.TestBlockchainView,
      List(),
      None,
      SigHash.ALL
    )

    signedTransaction.complete shouldBe false

    // Should throw an exception.
    a [TransactionVerificationException] should be thrownBy {
      new TransactionVerifier(signedTransaction.transaction).verify(S.TestBlockchainView)
    }
  }

  "signTransaction" should "sign successfully with the private keys argument if the wallet has required private keys" ignore {
    val S = new WalletSampleData(wallet)

    // TODO : Implement
  }

  "signTransaction" should "sign two inputs from different address in two steps" in {
    val S = new WalletSampleData(wallet)

    //////////////////////////////////////////////////////////////////////////
    // Step 1 : sign for the first input.
    val signedTransaction1 = Wallet.signTransaction(
      S.S5_CarryMergeToAliceTx.transaction,
      S.TestBlockchainView,
      List(),
      Some(List(S.Carry.Addr1.privateKey)),
      SigHash.ALL
    )

    signedTransaction1.complete shouldBe false

    a [TransactionVerificationException] should be thrownBy {
      new TransactionVerifier(signedTransaction1.transaction).verify(S.TestBlockchainView)
    }

    //////////////////////////////////////////////////////////////////////////
    // Step 2 : sign for the second input.
    val finalTransaction = Wallet.signTransaction(
      signedTransaction1.transaction,
      S.TestBlockchainView,
      List(),
      Some(List(S.Carry.Addr2.privateKey)),
      SigHash.ALL
    )

    finalTransaction.complete shouldBe true
    new TransactionVerifier(finalTransaction.transaction).verify(S.TestBlockchainView)
  }


  ////////////////////////////////////////////////////////////////////////////////
  // Methods for getreceivedbyaddress RPC
  ////////////////////////////////////////////////////////////////////////////////
  "getReceivedByAddress" should "show the amount of coins received for an address." in {
    val S = new WalletSampleData(wallet)
/*
    println(s"address000(${S.Alice.Addr1.address.base58})")
    println(s"outputs000=${wallet.getTransactionOutputs(Some(S.Alice.Addr1.address))}")
*/
    // To see the full history of coin receival, see ChainTestDataTrait.History.
    wallet.getReceivedByAddress(S.TestBlockchainView, S.Alice.Addr1.address, 3) shouldBe CoinAmount(50)
    wallet.getReceivedByAddress(S.TestBlockchainView, S.Alice.Addr1.address, 2) shouldBe CoinAmount(50)
    wallet.getReceivedByAddress(S.TestBlockchainView, S.Alice.Addr1.address, 1) shouldBe CoinAmount(50+2)
    wallet.getReceivedByAddress(S.TestBlockchainView, S.Alice.Addr1.address, 0) shouldBe CoinAmount(50+2+4)
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

    wallet.getTransactionHashes(Some("Alice")) shouldBe Set(
      S.S1_AliceGenTxHash,
      S.S2_AliceToBobTxHash,
      S.S3_BobToAliceAndCarrayTxHash,
      S.S4_AliceToCarryTxHash,
      S.S5_CarryMergeToAliceTxHash
    )

    wallet.getTransactionHashes(Some("Bob")) shouldBe Set(
      S.S2_BobGenTxHash,
      S.S2_AliceToBobTxHash,
      S.S3_BobToAliceAndCarrayTxHash
    )

    wallet.getTransactionHashes(Some("Carry")) shouldBe Set(
      S.S3_CarryGenTxHash,
      S.S3_BobToAliceAndCarrayTxHash,
      S.S4_AliceToCarryTxHash,
      S.S5_CarryMergeToAliceTxHash
    )
  }


  "getWalletTransactions(None)" should "return all wallet transactions for all accounts" ignore {
    // TODO : Implement
    val S = new WalletSampleData(wallet) {
      override def onStepFinish(step : Int): Unit = {
        step match {
          case 1 => {
            wallet.getWalletTransactions(None) shouldBe Set(1)
          }
          case 2 => {
            wallet.getWalletTransactions(None) shouldBe Set(1)
          }
          case 3 => {
            wallet.getWalletTransactions(None) shouldBe Set(1)
          }
          case 4 => {
            wallet.getWalletTransactions(None) shouldBe Set(1)
          }
          case 5 => {
            wallet.getWalletTransactions(None) shouldBe Set(1)
          }
        }
      }
    }
  }

  "getWalletTransactions(Some(account))" should "return wallet transactions for an account" ignore {

    // TODO : Implement

    val S = new WalletSampleData(wallet)

    wallet.getWalletTransactions(Some("Alice")) shouldBe Set(1)
    wallet.getWalletTransactions(Some("Bob")) shouldBe Set(1)
    wallet.getWalletTransactions(Some("Carry")) shouldBe Set(1)
  }


  def tx(blockIndexOption : Option[Long], txIndexOption : Option[Int], addedTime : Long) = {
    WalletTransaction(
      blockHash        = Some(Hash("00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929")),
      blockIndex       = blockIndexOption,
      blockTime        = Some(1411051649),
      transactionId    = Some(Hash("99845fd840ad2cc4d6f93fafb8b072d188821f55d9298772415175c456f3077d")),
      addedTime        = addedTime,
      transactionIndex = txIndexOption,
      transaction = transaction1
    )
  }

  "isMoreRecentThan" should "should return true if a transaction is more recent than another" in {
    wallet.isMoreRecentThan( tx(None, None, 101), tx(None, None, 100) ) shouldBe true
    wallet.isMoreRecentThan( tx(None, None, 100), tx(None, None, 101) ) shouldBe false
    wallet.isMoreRecentThan( tx(Some(1), Some(101), 1), tx(Some(1), Some(100), 1) ) shouldBe true
    wallet.isMoreRecentThan( tx(Some(1), Some(100), 1), tx(Some(1), Some(101), 1) ) shouldBe false
    wallet.isMoreRecentThan( tx(Some(1), Some(101), 1), tx(None, None, 100) ) shouldBe false
    wallet.isMoreRecentThan( tx(Some(1), Some(100), 1), tx(None, None, 101) ) shouldBe false
    wallet.isMoreRecentThan( tx(Some(1), Some(100), 1), tx(None, None, 100) ) shouldBe false
    wallet.isMoreRecentThan( tx(None, None,101), tx(Some(1), Some(100), 1) ) shouldBe true
    wallet.isMoreRecentThan( tx(None, None,100), tx(Some(1), Some(100), 1) ) shouldBe true
    wallet.isMoreRecentThan( tx(None, None,100), tx(Some(1), Some(101), 1) ) shouldBe true
  }

  "isMoreRecentThan" should "should hit an assertion if two transactions has the same timestamp" in {
    an [AssertionError] should be thrownBy {
      wallet.isMoreRecentThan( tx(Some(1), Some(100), 100), tx(Some(1), Some(100), 101) )
    }
  }

  def walletTx(transactionWithName : TransactionWithName, block : Option[Block], blockHeight : Long) = {
    WalletTransaction(
      blockHash        = block.map( b => Hash( HashCalculator.blockHeaderHash(b.header)) ),
      blockIndex       = block.map( b => blockHeight ),
      blockTime        = block.map(_.header.timestamp),
      transactionId    = Some(Hash(HashCalculator.transactionHash(transactionWithName.transaction))),
      addedTime     = 1418695703,
      transactionIndex = Some(1),
      transaction = transactionWithName.transaction
    )
  }

  "getTransactionDescriptor(Left(input), includeWatchOnly=true)" should "return some valid descriptor" in {
    val S = new WalletSampleData(wallet)
    val txDesc = wallet.getTransactionDescriptor(
      S.TestBlockchainView,
      walletTx(S.S2_AliceToBobTx, Some(S.S2_Block), S.S2_BlockHeight),
      Left(NormalTransactionInput(
        TransactionHash(S.S1_AliceGenCoin_A50.outPoint.transactionHash.value),
        S.S1_AliceGenCoin_A50.outPoint.outputIndex,
        UnlockingScript(Array[Byte]()),
        0L
      )),
      0,
      negativeFeeOption = Some(scala.math.BigDecimal(-1)),
      includeWatchOnly = true
    ).get

    txDesc should have (
      ('involvesWatchonly (true)),
      ('account           ("Alice")),
      ('address           (Some(S.Alice.Addr1.address.base58))),
      ('category          ("send")),
      ('amount   (scala.math.BigDecimal(50))),
      ('vout     (Some(S.S1_AliceGenCoin_A50.outPoint.outputIndex))),
      ('fee      (Some(scala.math.BigDecimal(-1)))),
      ('confirmations  (Some(2))),
      ('generated  (None)),
      ('blockhash  (Some(S.S2_BlockHash))),
      ('blockindex (Some(S.S2_BlockHeight))),
      //('blocktime  ()),
      ('txid       (Some(S.S2_AliceToBobTxHash)))
      //('time ())*/
    )
  }

  "getTransactionDescriptor(Left(input), includeWatchOnly=false)" should "return None" in {
    val S = new WalletSampleData(wallet)
    wallet.getTransactionDescriptor(
      S.TestBlockchainView,
      walletTx(S.S2_AliceToBobTx, Some(S.S2_Block), S.S2_BlockHeight),
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
    val txDesc = wallet.getTransactionDescriptor(
      S.TestBlockchainView,
      walletTx(S.S2_AliceToBobTx, Some(S.S2_Block), S.S2_BlockHeight),
      Right(S.S2_BobCoin1_A10.output),
      0,
      negativeFeeOption = Some(scala.math.BigDecimal(-1)),
      includeWatchOnly = true
    ).get

    txDesc should have (
      ('involvesWatchonly (true)),
      ('account           ("Bob")),
      ('address           (Some(S.Bob.Addr1.address.base58))),
      ('category          ("receive")),
      ('amount   (scala.math.BigDecimal(10))),
      ('vout     (Some(S.S2_BobCoin1_A10.outPoint.outputIndex))),
      ('fee      (Some(scala.math.BigDecimal(-1)))),
      ('confirmations  (Some(2))),
      ('generated  (None)),
      ('blockhash  (Some(S.S2_BlockHash))),
      ('blockindex (Some(S.S2_BlockHeight))),
      //('blocktime  ()),
      ('txid       (Some(S.S2_AliceToBobTxHash)))
      //('time ())*/
    )
  }

  "getTransactionDescriptor(Right(output), includeWatchOnly=false)" should "return None" in {
    val S = new WalletSampleData(wallet)
    wallet.getTransactionDescriptor(
      S.TestBlockchainView,
      walletTx(S.S2_AliceToBobTx, Some(S.S2_Block), S.S2_BlockHeight),
      Right(S.S2_BobCoin1_A10.output),
      0,
      negativeFeeOption = Some(scala.math.BigDecimal(-1)),
      includeWatchOnly = false
    ) shouldBe None // need to update
  }

  "listTransactions(None, includeWatchOnly=false)" should "return no transaction" ignore {
    // TODO : Implement

    val S = new WalletSampleData(wallet)

    // Because we did not call wallet.newAddress but wallet.importOutputOwnership,
    // we should not have any transcation with includeWatchOnly = false.
    wallet.listTransactions(
      S.TestBlockchainView,
      None,
      1000, // count
      0,    // skip
      false//includeWatchOnly
    ) shouldBe List(1) // TODO : Update with actual result.
  }

  "listTransactions(None, includeWatchOnly=true)" should "return all transactions" ignore {
    // TODO : Implement

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
            ) shouldBe List(1) // TODO : Update with actual result.
          }
          case 2 => {
            wallet.listTransactions(
              TestBlockchainView,
              None,
              1000, // count
              0,    // skip
              true//includeWatchOnly
            ) shouldBe List(1) // TODO : Update with actual result.
          }
          case 3 => {
            wallet.listTransactions(
              TestBlockchainView,
              None,
              1000, // count
              0,    // skip
              true//includeWatchOnly
            ) shouldBe List(1) // TODO : Update with actual result.
          }
          case 4 => {
            wallet.listTransactions(
              TestBlockchainView,
              None,
              1000, // count
              0,    // skip
              true//includeWatchOnly
            ) shouldBe List(1) // TODO : Update with actual result.
          }
          case 5 => {
            wallet.listTransactions(
              TestBlockchainView,
              None,
              1000, // count
              0,    // skip
              true//includeWatchOnly
            ) shouldBe List(1) // TODO : Update with actual result.
          }
        }
      }
    }
  }

  "listTransactions(Some(account), includeWatchOnly=true)" should "return all transactions for an account" ignore {
    // TODO : Implement

    val S = new WalletSampleData(wallet)

    wallet.listTransactions(
      S.TestBlockchainView,
      Some("Alice"),
      1000, // count
      0,    // skip
      true//includeWatchOnly
    ) shouldBe List(1) // TODO : Update with actual result.

    wallet.listTransactions(
      S.TestBlockchainView,
      Some("Bob"),
      1000, // count
      0,    // skip
      true//includeWatchOnly
    ) shouldBe List(1) // TODO : Update with actual result.

    wallet.listTransactions(
      S.TestBlockchainView,
      Some("Carry"),
      1000, // count
      0,    // skip
      true//includeWatchOnly
    ) shouldBe List(1) // TODO : Update with actual result.

  }

  def walletOutputWithInfo(output : OutputWithOutPoint, blockHeightOption : Option[Long], coinbase : Boolean, spent : Boolean) = {
    WalletOutputWithInfo(
      output.outPoint,
      WalletOutput(
        blockHeightOption,
        coinbase,
        spent,
        transactionOutput = output.output
      )
    )
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Methods for listunspent RPC
  ////////////////////////////////////////////////////////////////////////////////
  "getTransactionOutputs(None)" should "return all transaction outputs" in {
    val S = new WalletSampleData(wallet) {
      override def onStepFinish(step : Int): Unit = {
        step match {
          case 1 => {
            wallet.getTransactionOutputs(None).toSet shouldBe Set(
              walletOutputWithInfo(S1_AliceGenCoin_A50, Some(S1_BlockHeight), coinbase=true, spent=false)
            )
          }
          case 2 => {
            wallet.getTransactionOutputs(None).toSet should contain theSameElementsAs Set(
              walletOutputWithInfo(S1_AliceGenCoin_A50, Some(S1_BlockHeight), coinbase=true, spent=true), // newly spent
              walletOutputWithInfo(S2_BobGenCoin_A50, Some(S2_BlockHeight), coinbase=true, spent=false),
              walletOutputWithInfo(S2_BobCoin1_A10, Some(S2_BlockHeight), coinbase=false, spent=false),
              walletOutputWithInfo(S2_AliceChangeCoin1_A39, Some(S2_BlockHeight), coinbase=false, spent=false)
            )
            // For debugging purpose.
/*
            (
              wallet.getTransactionOutputs(None).sortBy(_.hashCode()) zip List(
                walletOutputWithInfo(S1_AliceGenCoin_A50, Some(S1_BlockHeight), coinbase=true, spent=true), // newly spent
                walletOutputWithInfo(S2_BobGenCoin_A50, Some(S2_BlockHeight), coinbase=true, spent=false),
                walletOutputWithInfo(S2_BobCoin1_A10, Some(S2_BlockHeight), coinbase=false, spent=false),
                walletOutputWithInfo(S2_AliceChangeCoin1_A39, Some(S2_BlockHeight), coinbase=false, spent=false)
              ).sortBy(_.hashCode)
            ) foreach { case (actual, expected) =>
              actual shouldBe expected
            }
*/
          }
          case 3 => {
            wallet.getTransactionOutputs(None).toSet should contain theSameElementsAs Set(
              walletOutputWithInfo(S1_AliceGenCoin_A50, Some(S1_BlockHeight), coinbase=true, spent=true),
              walletOutputWithInfo(S2_BobGenCoin_A50, Some(S2_BlockHeight), coinbase=true, spent=false),
              walletOutputWithInfo(S2_BobCoin1_A10, Some(S2_BlockHeight), coinbase=false, spent=true),  // newly spent
              walletOutputWithInfo(S2_AliceChangeCoin1_A39, Some(S2_BlockHeight), coinbase=false, spent=false),
              walletOutputWithInfo(S3_CarrayGenCoin_A50, Some(S3_BlockHeight), coinbase=true, spent=false),
              walletOutputWithInfo(S3_AliceCoin1_A2, Some(S3_BlockHeight), coinbase=false, spent=false),
              walletOutputWithInfo(S3_CarrayCoin1_A3, Some(S3_BlockHeight), coinbase=false, spent=false),
              walletOutputWithInfo(S3_BobChangeCoin1_A5, Some(S3_BlockHeight), coinbase=false, spent=false)
            )
          }
          case 4 => {
            wallet.getTransactionOutputs(None).toSet should contain theSameElementsAs Set(
              walletOutputWithInfo(S1_AliceGenCoin_A50, Some(S1_BlockHeight), coinbase=true, spent=true),
              walletOutputWithInfo(S2_BobGenCoin_A50, Some(S2_BlockHeight), coinbase=true, spent=false),
              walletOutputWithInfo(S2_BobCoin1_A10, Some(S2_BlockHeight), coinbase=false, spent=true),
              walletOutputWithInfo(S2_AliceChangeCoin1_A39, Some(S2_BlockHeight), coinbase=false, spent=false),
              walletOutputWithInfo(S3_CarrayGenCoin_A50, Some(S3_BlockHeight), coinbase=true, spent=false),
              walletOutputWithInfo(S3_AliceCoin1_A2, Some(S3_BlockHeight), coinbase=false, spent=true), // newly spent
              walletOutputWithInfo(S3_CarrayCoin1_A3, Some(S3_BlockHeight), coinbase=false, spent=false),
              walletOutputWithInfo(S3_BobChangeCoin1_A5, Some(S3_BlockHeight), coinbase=false, spent=false),
              walletOutputWithInfo(S4_CarryCoin2_A1, None, coinbase=false, spent=false)
            )
          }
          case 5 => {
            wallet.getTransactionOutputs(None).toSet should contain theSameElementsAs Set(
              walletOutputWithInfo(S1_AliceGenCoin_A50, Some(S1_BlockHeight), coinbase=true, spent=true),
              walletOutputWithInfo(S2_BobGenCoin_A50, Some(S2_BlockHeight), coinbase=true, spent=false),
              walletOutputWithInfo(S2_BobCoin1_A10, Some(S2_BlockHeight), coinbase=false, spent=true),
              walletOutputWithInfo(S2_AliceChangeCoin1_A39, Some(S2_BlockHeight), coinbase=false, spent=false),
              walletOutputWithInfo(S3_CarrayGenCoin_A50, Some(S3_BlockHeight), coinbase=true, spent=false),
              walletOutputWithInfo(S3_AliceCoin1_A2, Some(S3_BlockHeight), coinbase=false, spent=true),
              walletOutputWithInfo(S3_CarrayCoin1_A3, Some(S3_BlockHeight), coinbase=false, spent=true), // newly spent
              walletOutputWithInfo(S3_BobChangeCoin1_A5, Some(S3_BlockHeight), coinbase=false, spent=false),
              walletOutputWithInfo(S4_CarryCoin2_A1, None, coinbase=false, spent=true), // newly spent
              walletOutputWithInfo(S5_AliceCoin3_A4, None, coinbase=false, spent=false)
            )
          }
        }
      }
    }
  }

  "getTransactionOutputs(Some(account))" should "return transaction outputs for an account" in {
    val S = new WalletSampleData(wallet) {
      override def onStepFinish(step: Int): Unit = {
        step match {
          case 1 => {
            wallet.getTransactionOutputs(Some(Alice.Addr1.address)).toSet shouldBe Set(
              walletOutputWithInfo(S1_AliceGenCoin_A50, Some(S1_BlockHeight), coinbase=true, spent=false)
            )
            wallet.getTransactionOutputs(Some(Bob.Addr1.address)).toSet shouldBe Set()
            wallet.getTransactionOutputs(Some(Carry.Addr1.address)).toSet shouldBe Set()
          }
          case 2 => {
            wallet.getTransactionOutputs(Some(Alice.Addr1.address)).toSet shouldBe Set(
              walletOutputWithInfo(S1_AliceGenCoin_A50, Some(S1_BlockHeight), coinbase = true, spent = true) // newly spent
            )
            wallet.getTransactionOutputs(Some(Alice.Addr2.address)).toSet shouldBe Set(
              walletOutputWithInfo(S2_AliceChangeCoin1_A39, Some(S2_BlockHeight), coinbase = false, spent = false)
            )
            wallet.getTransactionOutputs(Some(Bob.Addr1.address)).toSet shouldBe Set(
              walletOutputWithInfo(S2_BobGenCoin_A50, Some(S2_BlockHeight), coinbase = true, spent = false),
              walletOutputWithInfo(S2_BobCoin1_A10, Some(S2_BlockHeight), coinbase = false, spent = false)
            )
            wallet.getTransactionOutputs(Some(Carry.Addr1.address)).toSet shouldBe Set()
          }
          case _ => {
            //do nothing
          }
        }
      }
    }
  }


  "getUnspentCoinDescription" should "return the description of the UTXO for generation transaction(spendable=false)" in {
    val S = new WalletSampleData(wallet)
    val utxoDesc = wallet.getUnspentCoinDescription(
      S.TestBlockchainView,
      Some(S.Carry.Addr1.address),
      WalletOutputWithInfo(
        S.S3_CarrayGenCoin_A50.outPoint,
        walletOutput = WalletOutput(
          blockindex    = Some(S.S3_BlockHeight),
          coinbase      = true,
          spent         = false,
          transactionOutput = S.S3_CarrayGenCoin_A50.output
        )
      )
    ).get

    utxoDesc should have (
      ('txid (S.S3_CarrayGenCoin_A50.outPoint.transactionHash)),
      ('vout (S.S3_CarrayGenCoin_A50.outPoint.outputIndex)),
      ('address (Some(S.Carry.Addr1.address.base58()))),
      ('account (Some("Carry"))),
      ('scriptPubKey (hex(S.S3_CarrayGenCoin_A50.output.lockingScript.data))),
      ('redeemScript (None)),
      ('amount (scala.math.BigDecimal(50L))),
      ('confirmations (1)),
      ('spendable (false)) // because of coinbase maturity
    )
  }

  "getUnspentCoinDescription" should "return the description of the UTXO for generation transaction(spendable=true)" ignore {
    // TODO : Implement
  }

  "getUnspentCoinDescription" should "return the description of the UTXO for normal transaction" in {
    val S = new WalletSampleData(wallet)
    val utxoDesc = wallet.getUnspentCoinDescription(
      S.TestBlockchainView,
      Some(S.Alice.Addr2.address),
      WalletOutputWithInfo(
        S.S2_AliceChangeCoin1_A39.outPoint,
        walletOutput = WalletOutput(
          blockindex    = Some(S.S2_BlockHeight),
          coinbase      = false,
          spent         = false,
          transactionOutput = S.S2_AliceChangeCoin1_A39.output
        )
      )
    ).get

    utxoDesc should have (
      ('txid (S.S2_AliceChangeCoin1_A39.outPoint.transactionHash)),
      ('vout (S.S2_AliceChangeCoin1_A39.outPoint.outputIndex)),
      ('address (Some(S.Alice.Addr2.address.base58()))),
      ('account (Some("Alice"))),
      ('scriptPubKey (hex(S.S2_AliceChangeCoin1_A39.output.lockingScript.data))),
      ('redeemScript (None)),
      ('amount (scala.math.BigDecimal(39L))),
      ('confirmations (2)),
      ('spendable (true))
    )
  }

  "getUnspentCoinDescription" should "return nothing if a coin is spent." in {
    val S = new WalletSampleData(wallet)
    val utxoDesc = wallet.getUnspentCoinDescription(
      S.TestBlockchainView,
      Some(S.Alice.Addr1.address),
      WalletOutputWithInfo(
        S.S1_AliceGenCoin_A50.outPoint,
        walletOutput = WalletOutput(
          blockindex    = Some(S.S1_BlockHeight),
          coinbase      = true,
          spent         = true,
          transactionOutput = S.S1_AliceGenCoin_A50.output
        )
      )
    ) shouldBe None
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

  def utxo(account:String, output : OutputWithOutPoint, confirmations : Int, spendable : Boolean) : UnspentCoinDescriptor = {
    UnspentCoinDescriptor(
      txid          = output.outPoint.transactionHash,
      vout          = output.outPoint.outputIndex,
      address       = Some(LockingScriptAnalyzer.extractAddresses(output.output.lockingScript).head.base58()),
      account       = Some(account),
      scriptPubKey  = hex(output.output.lockingScript.data),
      redeemScript  = None,
      amount        = CoinAmount.from(output.output.value).value,
      confirmations = confirmations,
      spendable     = spendable
    )
  }

  "listUnspent(all addresses)" should "" in {
    val S = new WalletSampleData(wallet) {
      override def onStepFinish(step : Int): Unit = {
        step match {
          case 1 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, None).toSet shouldBe Set(
              utxo("Alice", S1_AliceGenCoin_A50, confirmations = 1, spendable = false)
            )
          }
          case 2 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, None).toSet shouldBe Set(
              utxo("Bob", S2_BobGenCoin_A50, confirmations = 1, spendable = false),
              utxo("Bob", S2_BobCoin1_A10, confirmations = 1, spendable = true),
              utxo("Alice", S2_AliceChangeCoin1_A39, confirmations = 1, spendable = true)
            )
          }
          case 3 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, None).toSet shouldBe Set(
              utxo("Bob", S2_BobGenCoin_A50, confirmations = 2, spendable = false),
              utxo("Alice", S2_AliceChangeCoin1_A39, confirmations = 2, spendable = true),
              utxo("Carry", S3_CarrayGenCoin_A50, confirmations = 1, spendable = false),
              utxo("Alice", S3_AliceCoin1_A2, confirmations = 1, spendable = true),
              utxo("Carry", S3_CarrayCoin1_A3, confirmations = 1, spendable = true),
              utxo("Bob", S3_BobChangeCoin1_A5, confirmations = 1, spendable = true)
            )
          }
          case 4 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, None).toSet shouldBe Set(
              utxo("Bob", S2_BobGenCoin_A50, confirmations = 2, spendable = false),
              utxo("Alice", S2_AliceChangeCoin1_A39, confirmations = 2, spendable = true),
              utxo("Carry", S3_CarrayGenCoin_A50, confirmations = 1, spendable = false),
              utxo("Carry", S3_CarrayCoin1_A3, confirmations = 1, spendable = true),
              utxo("Bob", S3_BobChangeCoin1_A5, confirmations = 1, spendable = true),
              utxo("Carry", S4_CarryCoin2_A1, confirmations = 0, spendable = true)
            )
          }

          case 5 => {
            wallet.listUnspent(TestBlockchainView, 0, 100, None).toSet shouldBe Set(
              utxo("Bob", S2_BobGenCoin_A50, confirmations = 2, spendable = false),
              utxo("Alice", S2_AliceChangeCoin1_A39, confirmations = 2, spendable = true),
              utxo("Carry", S3_CarrayGenCoin_A50, confirmations = 1, spendable = false),
              utxo("Bob", S3_BobChangeCoin1_A5, confirmations = 1, spendable = true),
              utxo("Alice", S5_AliceCoin3_A4, confirmations = 0, spendable = true)
            )
          }
        }
      }
    }
  }

  "listUnspent(some addresses)" should "list UTXO for an account." in {
    val S = new WalletSampleData(wallet)

    wallet.listUnspent(S.TestBlockchainView, 0, 100, Some(List(S.Alice.Addr1.address))).toSet shouldBe Set(
      utxo("Alice", S.S5_AliceCoin3_A4, confirmations = 0, spendable = true)
    )

    wallet.listUnspent(S.TestBlockchainView, 0, 100, Some(List(S.Alice.Addr2.address))).toSet shouldBe Set(
      utxo("Alice", S.S2_AliceChangeCoin1_A39, confirmations = 2, spendable = true)
    )

    wallet.listUnspent(S.TestBlockchainView, 0, 100, Some(List(S.Bob.Addr1.address))).toSet shouldBe Set(
      utxo("Bob", S.S2_BobGenCoin_A50, confirmations = 2, spendable = false)
    )

    wallet.listUnspent(S.TestBlockchainView, 0, 100, Some(List(S.Bob.Addr2.address))).toSet shouldBe Set(
      utxo("Bob", S.S3_BobChangeCoin1_A5, confirmations = 1, spendable = true)
    )

    wallet.listUnspent(S.TestBlockchainView, 0, 100, Some(List(S.Carry.Addr1.address))).toSet shouldBe Set(
      utxo("Carry", S.S3_CarrayGenCoin_A50, confirmations = 1, spendable = false)
    )
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

  "importOutputOwnership" should "change the receiving address to the imported address" in {
    val S = new WalletSampleData(wallet)
    wallet.importOutputOwnership(
      S.TestBlockchainView,
      "test1",
      S.Alice.Addr1.address,
      rescanBlockchain = false)
    wallet.getReceivingAddress("test1") shouldBe S.Alice.Addr1.address
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

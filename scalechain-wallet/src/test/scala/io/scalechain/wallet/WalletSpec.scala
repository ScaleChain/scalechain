package io.scalechain.wallet

import java.io.File

import io.scalechain.blockchain.TransactionVerificationException
import io.scalechain.blockchain.chain.ChainTestDataTrait
import io.scalechain.blockchain.storage.{BlockStorage, Storage, DiskBlockStorage}
import io.scalechain.blockchain.transaction.{TransactionVerifier, NormalTransactionVerifier, SigHash}
import org.apache.commons.io.FileUtils
import org.scalatest._

/**
  * Created by kangmo on 5/12/16.
  */

class WalletSpec extends FlatSpec with BeforeAndAfterEach with ChainTestDataTrait with ShouldMatchers {

  this: Suite =>

  Storage.initialize()

  val TEST_RECORD_FILE_SIZE = 1024 * 1024

  var storage : BlockStorage = null
  val testPath = new File("./target/unittests-WalletSpec/")
  override def beforeEach() {

    FileUtils.deleteDirectory(testPath)
    testPath.mkdir()

    storage = new DiskBlockStorage(testPath, TEST_RECORD_FILE_SIZE)

    super.beforeEach()
  }

  override def afterEach() {
    super.afterEach()

    storage.close()

    FileUtils.deleteDirectory(testPath)

  }

  import Account._
  import History._

  ////////////////////////////////////////////////////////////////////////////////
  // Methods for signrawtransaction RPC
  ////////////////////////////////////////////////////////////////////////////////
  "signTransaction" should "sign successfully with the private keys argument" in {
    val signedTransaction = Wallet.signTransaction(
      AliceToCarryTx,
      List(),
      Some(List( Alice.Addr1.privateKey )),
      SigHash.ALL
    )

    signedTransaction.complete shouldBe true

    // Should not throw an exception.
    new TransactionVerifier(signedTransaction.transaction).verify(blockIndex)
  }

  "signTransaction" should "fail without the private keys argument if the wallet does not have required private keys" in {
    val signedTransaction = Wallet.signTransaction(
      AliceToCarryTx,
      List(),
      None,
      SigHash.ALL
    )

    signedTransaction.complete shouldBe false

    // Should throw an exception.
    a [TransactionVerificationException] should be thrownBy {
      new TransactionVerifier(signedTransaction.transaction).verify(blockIndex)
    }
  }

  "signTransaction" should "sign successfully with the private keys argument if the wallet has required private keys" in {
    // TODO : Implement
  }

  "signTransaction" should "sign two inputs from different address in two steps" in {
    //////////////////////////////////////////////////////////////////////////
    // Step 1 : sign for the first input.
    val signedTransaction1 = Wallet.signTransaction(
      CarryMergeToAliceTx,
      List(),
      Some(List(Carry.Addr1.privateKey)),
      SigHash.ALL
    )

    signedTransaction1.complete shouldBe false

    a [TransactionVerificationException] should be thrownBy {
      new TransactionVerifier(signedTransaction1.transaction).verify(blockIndex)
    }

    //////////////////////////////////////////////////////////////////////////
    // Step 2 : sign for the second input.
    val finalTransaction = Wallet.signTransaction(
      signedTransaction1.transaction,
      List(),
      Some(List(Carry.Addr2.privateKey)),
      SigHash.ALL
    )

    finalTransaction.complete shouldBe true
    new TransactionVerifier(signedTransaction1.transaction).verify(blockIndex)
  }


  ////////////////////////////////////////////////////////////////////////////////
  // Methods for getreceivedbyaddress RPC
  ////////////////////////////////////////////////////////////////////////////////
  "getReceivedByAddress" should "" in {
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Methods for listransaction RPC
  ////////////////////////////////////////////////////////////////////////////////
  "getTransactionHashes" should "" in {
  }

  "getWalletTransactions" should "" in {
  }

  "isMoreRecentThan" should "" in {
  }

  "getTransactionDescriptor" should "" in {
  }

  "listTransactions" should "" in {
  }


  ////////////////////////////////////////////////////////////////////////////////
  // Methods for listunspent RPC
  ////////////////////////////////////////////////////////////////////////////////
  "getTransactionOutputs" should "" in {
  }

  "getUnspentCoinDescription" should "" in {
  }

  "getConfirmations" should "" in {
  }

  "listUnspent" should "" in {
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Methods for importaddress RPC
  ////////////////////////////////////////////////////////////////////////////////


  "importOutputOwnership" should "" in {
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Methods for getaccount RPC
  ////////////////////////////////////////////////////////////////////////////////


  "getAccount" should "" in {
  }


  ////////////////////////////////////////////////////////////////////////////////
  // Methods for newaddress RPC
  ////////////////////////////////////////////////////////////////////////////////

  "newAddress" should "" in {
  }


  ////////////////////////////////////////////////////////////////////////////////
  // Methods for getaccountaddress RPC
  ////////////////////////////////////////////////////////////////////////////////

  "getReceivingAddress" should "" in {
  }


  ////////////////////////////////////////////////////////////////////////////////
  // Handlers called by Chain layer.
  ////////////////////////////////////////////////////////////////////////////////

  "registerTransaction" should "" in {
  }

  "unregisterTransaction" should "" in {
  }

  "onNewTransaction" should "" in {
  }

  "onRemoveTransaction" should "" in {
  }

  "onNewBlock" should "" in {
  }

  "onRemoveBlock" should "" in {
  }

}

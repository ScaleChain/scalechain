package io.scalechain.wallet

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.Matchers
import java.io.File

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.WalletException
import io.scalechain.blockchain.TransactionVerificationException
import io.scalechain.blockchain.chain.TransactionWithName
import io.scalechain.blockchain.chain.OutputWithOutPoint
import io.scalechain.blockchain.chain.ChainSampleData
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.transaction.*
import io.scalechain.util.Bytes
import io.scalechain.util.Either
import io.scalechain.util.Either.Left
import io.scalechain.util.Either.Right
import io.scalechain.util.HexUtil.bytes
import io.scalechain.util.HexUtil.hex
import org.junit.runner.RunWith

// TODO : BUGBUG : Need to check if coinbase maturity for transaction validation. Need to change test cases as well.

/**
  * Created by kangmo on 5/12/16.
  */
//@Ignore
@RunWith(KTestJUnitRunner::class)
class WalletSpec : WalletTestTrait(), TransactionTestInterface, Matchers {

  override val testPath = File("./target/unittests-WalletSpec-storage/")

  override fun beforeEach() {

    super.beforeEach()
  }

  override fun afterEach() {

    super.afterEach()
  }

  init {

    ////////////////////////////////////////////////////////////////////////////////
    // Methods for signrawtransaction RPC
    ////////////////////////////////////////////////////////////////////////////////
    "signTransaction" should "sign successfully with the private keys argument" {
      val S = WalletSampleData(db, wallet)

      val signedTransaction = Wallet.get().signTransaction(
        db, 
        S.S4_AliceToCarryTx.transaction,
        S.TestBlockchainView,
        listOf(),
        listOf( S.Alice.Addr1.privateKey ),
        SigHash.ALL
      )

      signedTransaction.complete shouldBe true

      // Should not throw an exception.
      TransactionVerifier(db, signedTransaction.transaction).verify(S.TestBlockchainView)
    }

    "signTransaction" should "fail without the private keys argument if the wallet does not have required private keys" {
      val S = WalletSampleData(db, wallet)

      val signedTransaction = Wallet.get().signTransaction(
        db,
        S.S4_AliceToCarryTx.transaction,
        S.TestBlockchainView,
        listOf(),
        null,
        SigHash.ALL
      )

      signedTransaction.complete shouldBe false

      // Should throw an exception.
      shouldThrow <TransactionVerificationException>  {
        TransactionVerifier(db, signedTransaction.transaction).verify(S.TestBlockchainView)
      }
    }

    /*
  String Request : {"id":1463979961277,"method":"signrawtransaction","params":<"010000000143d4a9d00c80c34a1b3e9955f891e102265068aa2cd4936f23ac0503d7c648da0000000000ffffffff0358020000000000001976a9144dc568a1f934c64402e5674f62e3cf33dcaccd2988ac00000000000000001d6a1b4f410100001568e761d3bce89e8a3f346354d361fb2e3e610aad54c8bc0200000000001976a914f58cfa86c6b7c746de8293701734594adf81180f88ac00000000",<>,<"cMvpcxdeLot2Zn7Ps99RZ4HC2fqst5FZ5mgyzf8TgENmsSd2qtAm","cR8oUtf44zW4gahPews8JtsAPknXX9sdTxjYNqzw462C43pWMYg4">>}
  String Response : {
    "result": {
      "hex": "010000000143d4a9d00c80c34a1b3e9955f891e102265068aa2cd4936f23ac0503d7c648da0000000000ffffffff0358020000000000001976a9144dc568a1f934c64402e5674f62e3cf33dcaccd2988ac00000000000000001d6a1b4f410100001568e761d3bce89e8a3f346354d361fb2e3e610aad54c8bc0200000000001976a914f58cfa86c6b7c746de8293701734594adf81180f88ac00000000",
      "complete": false
    },
    "error": null,
    "id": 1463979961277
  }

     */

    "signTransaction" should "sign successfully with the private keys argument if the wallet has required private keys" {
      // val S = WalletSampleData(db, wallet)

      // TODO : Implement
    }

    "signTransaction" should "sign two inputs from different address in two steps" {
      val S = WalletSampleData(db, wallet)

      //////////////////////////////////////////////////////////////////////////
      // Step 1 : sign for the first input.
      val signedTransaction1 = Wallet.get().signTransaction(
        db,
        S.S5_CarryMergeToAliceTx.transaction,
        S.TestBlockchainView,
        listOf(),
        listOf(S.Carry.Addr1.privateKey),
        SigHash.ALL
      )

      signedTransaction1.complete shouldBe false

      shouldThrow <TransactionVerificationException> {
        TransactionVerifier(db, signedTransaction1.transaction).verify(S.TestBlockchainView)
      }

      //////////////////////////////////////////////////////////////////////////
      // Step 2 : sign for the second input.
      val finalTransaction = Wallet.get().signTransaction(
        db, 
        signedTransaction1.transaction,
        S.TestBlockchainView,
        listOf(),
        listOf(S.Carry.Addr2.privateKey),
        SigHash.ALL
      )

      finalTransaction.complete shouldBe true
      TransactionVerifier(db, finalTransaction.transaction).verify(S.TestBlockchainView)
    }


    ////////////////////////////////////////////////////////////////////////////////
    // Methods for getreceivedbyaddress RPC
    ////////////////////////////////////////////////////////////////////////////////
    "getReceivedByAddress" should "show the amount of coins received for an address." {
      val S = WalletSampleData(db, wallet)
  /*
      println(s"address000(${S.Alice.Addr1.address.base58})")
      println(s"outputs000=${wallet.getTransactionOutputs(Some(S.Alice.Addr1.address))}")
  */
      // To see the full history of coin receival, see ChainTestDataTrait.History.
      wallet.getReceivedByAddress(db, S.TestBlockchainView, S.Alice.Addr1.address, 3) shouldBe CoinAmount(50)
      wallet.getReceivedByAddress(db, S.TestBlockchainView, S.Alice.Addr1.address, 2) shouldBe CoinAmount(50)
      wallet.getReceivedByAddress(db, S.TestBlockchainView, S.Alice.Addr1.address, 1) shouldBe CoinAmount(50+2)
      wallet.getReceivedByAddress(db, S.TestBlockchainView, S.Alice.Addr1.address, 0) shouldBe CoinAmount(50+2+4)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Methods for listransaction RPC
    ////////////////////////////////////////////////////////////////////////////////
    "getTransactionHashes(db, null)" should "return all transaction hashes" {
      object : WalletSampleData(db, wallet) {
        override fun onStepFinish(stepNumber : Int): Unit {
          when(stepNumber) {
            1 -> {
              wallet.getTransactionHashes(db, null).toSet() shouldBe setOf(
                S1_AliceGenTxHash
              )
            }
            2 -> {
              wallet.getTransactionHashes(db, null).toSet() shouldBe setOf(
                S1_AliceGenTxHash,
                S2_BobGenTxHash, S2_AliceToBobTxHash
              )
            }
            3 -> {
              wallet.getTransactionHashes(db, null).toSet() shouldBe setOf(
                S1_AliceGenTxHash,
                S2_BobGenTxHash, S2_AliceToBobTxHash,
                S3_CarryGenTxHash, S3_BobToAliceAndCarrayTxHash
              )
            }
            4 -> {
              wallet.getTransactionHashes(db, null).toSet() shouldBe setOf(
                S1_AliceGenTxHash,
                S2_BobGenTxHash, S2_AliceToBobTxHash,
                S3_CarryGenTxHash, S3_BobToAliceAndCarrayTxHash,
                S4_AliceToCarryTxHash
              )
            }
            5 -> {
              wallet.getTransactionHashes(db, null).toSet() shouldBe setOf(
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


    "getTransactionHashes(Some(account))" should "return transaction hashes for an account" {
      val S = WalletSampleData(db, wallet)

      wallet.getTransactionHashes(db, "Alice").toSet() shouldBe setOf(
        S.S1_AliceGenTxHash,
        S.S2_AliceToBobTxHash,
        S.S3_BobToAliceAndCarrayTxHash,
        S.S4_AliceToCarryTxHash,
        S.S5_CarryMergeToAliceTxHash
      )

      wallet.getTransactionHashes(db, "Bob").toSet() shouldBe setOf(
        S.S2_BobGenTxHash,
        S.S2_AliceToBobTxHash,
        S.S3_BobToAliceAndCarrayTxHash
      )

      wallet.getTransactionHashes(db, "Carry").toSet() shouldBe setOf(
        S.S3_CarryGenTxHash,
        S.S3_BobToAliceAndCarrayTxHash,
        S.S4_AliceToCarryTxHash,
        S.S5_CarryMergeToAliceTxHash
      )
    }

/*
    "getWalletTransactions(null)" should "return all wallet transactions for all accounts" {
      // TODO : Implement
      object : WalletSampleData(db, wallet) {
        override fun onStepFinish(stepNumber : Int): Unit {
          when(stepNumber) {
            1 -> {
              wallet.getWalletTransactions(db, null) shouldBe setOf(1)
            }
            2 -> {
              wallet.getWalletTransactions(db, null) shouldBe setOf(1)
            }
            3 -> {
              wallet.getWalletTransactions(db, null) shouldBe setOf(1)
            }
            4 -> {
              wallet.getWalletTransactions(db, null) shouldBe setOf(1)
            }
            5 -> {
              wallet.getWalletTransactions(db, null) shouldBe setOf(1)
            }
          }
        }
      }
    }
*/

/*
    "getWalletTransactions(Some(account))" should "return wallet transactions for an account" {

      // TODO : Implement

      WalletSampleData(db, wallet)

      wallet.getWalletTransactions(db, "Alice") shouldBe setOf(1)
      wallet.getWalletTransactions(db, "Bob") shouldBe setOf(1)
      wallet.getWalletTransactions(db, "Carry") shouldBe setOf(1)
    }
*/

    fun tx(blockIndexOption : Long?, txIndexOption : Int?, addedTime : Long) : WalletTransaction {
      return WalletTransaction(
        blockHash        = Hash(Bytes.from("00000000bd0ed80435fc9fe3269da69bb0730ebb454d0a29128a870ea1a37929")),
        blockIndex       = blockIndexOption,
        blockTime        = 1411051649,
        transactionId    = Hash(Bytes.from("99845fd840ad2cc4d6f93fafb8b072d188821f55d9298772415175c456f3077d")),
        addedTime        = addedTime,
        transactionIndex = txIndexOption,
        transaction = transaction1()
      )
    }

    "isMoreRecentThan" should "should return true if a transaction is more recent than another" {
      wallet.isMoreRecentThan( tx(null, null, 101), tx(null, null, 100) ) shouldBe true
      wallet.isMoreRecentThan( tx(null, null, 100), tx(null, null, 101) ) shouldBe false
      wallet.isMoreRecentThan( tx(1, 101, 1), tx(1, 100, 1) ) shouldBe true
      wallet.isMoreRecentThan( tx(1, 100, 1), tx(1, 101, 1) ) shouldBe false
      wallet.isMoreRecentThan( tx(1, 101, 1), tx(null, null, 100) ) shouldBe false
      wallet.isMoreRecentThan( tx(1, 100, 1), tx(null, null, 101) ) shouldBe false
      wallet.isMoreRecentThan( tx(1, 100, 1), tx(null, null, 100) ) shouldBe false
      wallet.isMoreRecentThan( tx(null, null,101), tx(1, 100, 1) ) shouldBe true
      wallet.isMoreRecentThan( tx(null, null,100), tx(1, 100, 1) ) shouldBe true
      wallet.isMoreRecentThan( tx(null, null,100), tx(1, 101, 1) ) shouldBe true
    }

    "isMoreRecentThan" should "should hit an assertion if two transactions has the same timestamp" {
      shouldThrow <AssertionError> {
        wallet.isMoreRecentThan( tx(1, 100, 100), tx(1, 100, 101) )
      }
    }

    fun walletTx(transactionWithName : TransactionWithName, block : Block?, blockHeight : Long) : WalletTransaction {
      return WalletTransaction(
        blockHash        = block?.header?.hash(),
        blockIndex       = if (block != null) blockHeight else null,
        blockTime        = block?.header?.timestamp,
        transactionId    = transactionWithName.transaction.hash(),
        addedTime        = 1418695703L,
        transactionIndex = 1,
        transaction      = transactionWithName.transaction
      )
    }

    "getTransactionDescriptor(Left(input), includeWatchOnly=true)" should "return some valid descriptor" {
      val S = WalletSampleData(db, wallet)
      val txDesc = wallet.getTransactionDescriptor(
        db,
        S.TestBlockchainView,
        walletTx(S.S2_AliceToBobTx, S.S2_Block, S.S2_BlockHeight),
        Either.Left(NormalTransactionInput(
          S.S1_AliceGenCoin_A50.outPoint.transactionHash,
          S.S1_AliceGenCoin_A50.outPoint.outputIndex.toLong(),
          UnlockingScript(Bytes(byteArrayOf())),
          0L
        )),
        0,
        java.math.BigDecimal(-1) /*negativeFeeOption*/,
        null, // TODO : BUGBUG Need to provide output ownership filter.
        includeWatchOnly = true
      )!!

      txDesc.involvesWatchonly shouldBe true
      txDesc.account shouldBe "Alice"
      txDesc.address shouldBe S.Alice.Addr1.address.base58()
      txDesc.category shouldBe "send"
      txDesc.amount shouldBe java.math.BigDecimal(50)
      txDesc.vout shouldBe S.S1_AliceGenCoin_A50.outPoint.outputIndex
      txDesc.fee shouldBe java.math.BigDecimal(-1)
      txDesc.confirmations shouldBe 2L
      txDesc.generated shouldBe null
      txDesc.blockhash shouldBe S.S2_BlockHash
      txDesc.txid shouldBe S.S2_AliceToBobTxHash
    }

    "getTransactionDescriptor(Left(input), includeWatchOnly=false)" should "return null" {
      val S = WalletSampleData(db, wallet)
      wallet.getTransactionDescriptor(
        db,
        S.TestBlockchainView,
        walletTx(S.S2_AliceToBobTx, S.S2_Block, S.S2_BlockHeight),
        Left(NormalTransactionInput(
          S.S1_AliceGenCoin_A50.outPoint.transactionHash,
          S.S1_AliceGenCoin_A50.outPoint.outputIndex.toLong(),
          UnlockingScript(Bytes(byteArrayOf())),
          0L
        )),
        0,
        java.math.BigDecimal(-1) /*negativeFeeOption*/,
        null, // TODO : BUGBUG Need to provide output ownership filter.
        false /* includeWatchOnly */
      ) shouldBe null // need to update
    }

    "getTransactionDescriptor(Right(output))" should "return some valid descriptor" {
      val S = WalletSampleData(db, wallet)
      val txDesc = wallet.getTransactionDescriptor(
        db,
        S.TestBlockchainView,
        walletTx(S.S2_AliceToBobTx, S.S2_Block, S.S2_BlockHeight),
        Right(S.S2_BobCoin1_A10.output),
        0,
        java.math.BigDecimal(-1) /*negativeFeeOption*/,
        null, // TODO : BUGBUG Need to provide output ownership filter.
        true /*includeWatchOnly*/
      )!!

      txDesc.involvesWatchonly shouldBe true
      txDesc.account shouldBe "Bob"
      txDesc.address shouldBe S.Bob.Addr1.address.base58()
      txDesc.category shouldBe "receive"
      txDesc.amount shouldBe java.math.BigDecimal(10)
      txDesc.vout shouldBe S.S2_BobCoin1_A10.outPoint.outputIndex
      txDesc.fee shouldBe java.math.BigDecimal(-1)
      txDesc.confirmations shouldBe 2L
      txDesc.generated shouldBe null
      txDesc.blockhash shouldBe S.S2_BlockHash
      txDesc.blockindex shouldBe S.S2_BlockHeight
      txDesc.txid shouldBe S.S2_AliceToBobTxHash
    }

    "getTransactionDescriptor(Right(output), includeWatchOnly=false)" should "return null" {
      val S = WalletSampleData(db, wallet)
      wallet.getTransactionDescriptor(
        db,
        S.TestBlockchainView,
        walletTx(S.S2_AliceToBobTx, S.S2_Block, S.S2_BlockHeight),
        Right(S.S2_BobCoin1_A10.output),
        0,
        java.math.BigDecimal(-1) /* negativeFeeOption */,
        null, // TODO : BUGBUG Need to provide output ownership filter.
        false /* includeWatchOnly */
      ) shouldBe null // need to update
    }

/*
    "listTransactions(null, includeWatchOnly=false)" should "return no transaction" {
      // TODO : Implement

      val S = WalletSampleData(db, wallet)

      // Because we did not call wallet.newAddress but wallet.importOutputOwnership,
      // we should not have any transcation with includeWatchOnly = false.
      wallet.listTransactions(
        db,
        S.TestBlockchainView,
        null,
        1000, // count
        0,    // skip
        false//includeWatchOnly
      ) shouldBe listOf(1) // TODO : Update with actual result.
    }
*/

/*
    "listTransactions(null, includeWatchOnly=true)" should "return all transactions" {
      // TODO : Implement

      object : WalletSampleData(db, wallet) {
        override fun onStepFinish(stepNumber : Int): Unit {
          when(stepNumber) {
            1 -> {
              wallet.listTransactions(
                db,
                TestBlockchainView,
                null,
                1000, // count
                0,    // skip
                true//includeWatchOnly
              ) shouldBe listOf(1) // TODO : Update with actual result.
            }
            2 -> {
              wallet.listTransactions(
                db,
                TestBlockchainView,
                null,
                1000, // count
                0,    // skip
                true//includeWatchOnly
              ) shouldBe listOf(1) // TODO : Update with actual result.
            }
            3 -> {
              wallet.listTransactions(
                db,
                TestBlockchainView,
                null,
                1000, // count
                0,    // skip
                true//includeWatchOnly
              ) shouldBe listOf(1) // TODO : Update with actual result.
            }
            4 -> {
              wallet.listTransactions(
                db,
                TestBlockchainView,
                null,
                1000, // count
                0,    // skip
                true//includeWatchOnly
              ) shouldBe listOf(1) // TODO : Update with actual result.
            }
            5 -> {
              wallet.listTransactions(
                db,
                TestBlockchainView,
                null,
                1000, // count
                0,    // skip
                true//includeWatchOnly
              ) shouldBe listOf(1) // TODO : Update with actual result.
            }
          }
        }
      }
    }
*/

/*
    "listTransactions(Some(account), includeWatchOnly=true)" should "return all transactions for an account" {
      // TODO : Implement

      val S = WalletSampleData(db, wallet)

      wallet.listTransactions(
        db,
        S.TestBlockchainView,
        "Alice",
        1000, // count
        0L,    // skip
        true//includeWatchOnly
      ) shouldBe listOf(1) // TODO : Update with actual result.

      wallet.listTransactions(
        db,
        S.TestBlockchainView,
        "Bob",
        1000, // count
        0L,    // skip
        true//includeWatchOnly
      ) shouldBe listOf(1) // TODO : Update with actual result.

      wallet.listTransactions(
        db,
        S.TestBlockchainView,
        "Carry",
        1000, // count
        0L,    // skip
        true//includeWatchOnly
      ) shouldBe listOf(1) // TODO : Update with actual result.

    }
*/

    fun walletOutputWithInfo(output : OutputWithOutPoint, blockHeightOption : Long?, coinbase : Boolean, spent : Boolean) : WalletOutputWithInfo {
      return WalletOutputWithInfo(
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
    "getTransactionOutputs(null)" should "return all transaction outputs" {
      object : WalletSampleData(db, wallet) {
        override fun onStepFinish(stepNumber : Int): Unit {
          when(stepNumber) {
            1 -> {
              wallet.getTransactionOutputs(db, null).toSet() shouldBe setOf(
                walletOutputWithInfo(S1_AliceGenCoin_A50, S1_BlockHeight, coinbase=true, spent=false)
              )
            }
            2 -> {
              wallet.getTransactionOutputs(db, null).toSet() shouldBe setOf(
                walletOutputWithInfo(S1_AliceGenCoin_A50, S1_BlockHeight, coinbase=true, spent=true), // newly spent
                walletOutputWithInfo(S2_BobGenCoin_A50, S2_BlockHeight, coinbase=true, spent=false),
                walletOutputWithInfo(S2_BobCoin1_A10, S2_BlockHeight, coinbase=false, spent=false),
                walletOutputWithInfo(S2_AliceChangeCoin1_A39, S2_BlockHeight, coinbase=false, spent=false)
              )
              // For debugging purpose.
  /*
              (
                wallet.getTransactionOutputs(null).sortBy(_.hashCode()) zip listOf(
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
            3 -> {
              wallet.getTransactionOutputs(db, null).toSet() shouldBe setOf(
                walletOutputWithInfo(S1_AliceGenCoin_A50, S1_BlockHeight, coinbase=true, spent=true),
                walletOutputWithInfo(S2_BobGenCoin_A50, S2_BlockHeight, coinbase=true, spent=false),
                walletOutputWithInfo(S2_BobCoin1_A10, S2_BlockHeight, coinbase=false, spent=true),  // newly spent
                walletOutputWithInfo(S2_AliceChangeCoin1_A39, S2_BlockHeight, coinbase=false, spent=false),
                walletOutputWithInfo(S3_CarrayGenCoin_A50, S3_BlockHeight, coinbase=true, spent=false),
                walletOutputWithInfo(S3_AliceCoin1_A2, S3_BlockHeight, coinbase=false, spent=false),
                walletOutputWithInfo(S3_CarrayCoin1_A3, S3_BlockHeight, coinbase=false, spent=false),
                walletOutputWithInfo(S3_BobChangeCoin1_A5, S3_BlockHeight, coinbase=false, spent=false)
              )
            }
            4 -> {
              wallet.getTransactionOutputs(db, null).toSet() shouldBe setOf(
                walletOutputWithInfo(S1_AliceGenCoin_A50, S1_BlockHeight, coinbase=true, spent=true),
                walletOutputWithInfo(S2_BobGenCoin_A50, S2_BlockHeight, coinbase=true, spent=false),
                walletOutputWithInfo(S2_BobCoin1_A10, S2_BlockHeight, coinbase=false, spent=true),
                walletOutputWithInfo(S2_AliceChangeCoin1_A39, S2_BlockHeight, coinbase=false, spent=false),
                walletOutputWithInfo(S3_CarrayGenCoin_A50, S3_BlockHeight, coinbase=true, spent=false),
                walletOutputWithInfo(S3_AliceCoin1_A2, S3_BlockHeight, coinbase=false, spent=true), // newly spent
                walletOutputWithInfo(S3_CarrayCoin1_A3, S3_BlockHeight, coinbase=false, spent=false),
                walletOutputWithInfo(S3_BobChangeCoin1_A5, S3_BlockHeight, coinbase=false, spent=false),
                walletOutputWithInfo(S4_CarryCoin2_A1, null, coinbase=false, spent=false)
              )
            }
            5 -> {
              wallet.getTransactionOutputs(db, null).toSet() shouldBe setOf(
                walletOutputWithInfo(S1_AliceGenCoin_A50, S1_BlockHeight, coinbase=true, spent=true),
                walletOutputWithInfo(S2_BobGenCoin_A50, S2_BlockHeight, coinbase=true, spent=false),
                walletOutputWithInfo(S2_BobCoin1_A10, S2_BlockHeight, coinbase=false, spent=true),
                walletOutputWithInfo(S2_AliceChangeCoin1_A39, S2_BlockHeight, coinbase=false, spent=false),
                walletOutputWithInfo(S3_CarrayGenCoin_A50, S3_BlockHeight, coinbase=true, spent=false),
                walletOutputWithInfo(S3_AliceCoin1_A2, S3_BlockHeight, coinbase=false, spent=true),
                walletOutputWithInfo(S3_CarrayCoin1_A3, S3_BlockHeight, coinbase=false, spent=true), // newly spent
                walletOutputWithInfo(S3_BobChangeCoin1_A5, S3_BlockHeight, coinbase=false, spent=false),
                walletOutputWithInfo(S4_CarryCoin2_A1, null, coinbase=false, spent=true), // newly spent
                walletOutputWithInfo(S5_AliceCoin3_A4, null, coinbase=false, spent=false)
              )
            }
          }
        }
      }
    }

    "getTransactionOutputs(Some(account))" should "return transaction outputs for an account" {
      object : WalletSampleData(db, wallet) {
        override fun onStepFinish(stepNumber: Int): Unit {
          when(stepNumber) {
            1 -> {
              wallet.getTransactionOutputs(db, Alice.Addr1.address).toSet() shouldBe setOf(
                walletOutputWithInfo(S1_AliceGenCoin_A50, S1_BlockHeight, coinbase=true, spent=false)
              )
              wallet.getTransactionOutputs(db, Bob.Addr1.address).toSet() shouldBe setOf<WalletOutputWithInfo>()
              wallet.getTransactionOutputs(db, Carry.Addr1.address).toSet() shouldBe setOf<WalletOutputWithInfo>()
            }
            2 -> {
              wallet.getTransactionOutputs(db, Alice.Addr1.address).toSet() shouldBe setOf(
                walletOutputWithInfo(S1_AliceGenCoin_A50, S1_BlockHeight, coinbase = true, spent = true) // newly spent
              )
              wallet.getTransactionOutputs(db, Alice.Addr2.address).toSet() shouldBe setOf(
                walletOutputWithInfo(S2_AliceChangeCoin1_A39, S2_BlockHeight, coinbase = false, spent = false)
              )
              wallet.getTransactionOutputs(db, Bob.Addr1.address).toSet() shouldBe setOf(
                walletOutputWithInfo(S2_BobGenCoin_A50, S2_BlockHeight, coinbase = true, spent = false),
                walletOutputWithInfo(S2_BobCoin1_A10, S2_BlockHeight, coinbase = false, spent = false)
              )
              wallet.getTransactionOutputs(db, Carry.Addr1.address).toSet() shouldBe setOf<WalletOutputWithInfo>()
            }
            else -> {
              //do nothing
            }
          }
        }
      }
    }


    "getUnspentCoinDescription" should "return the description of the UTXO for generation transaction(spendable=false)" {
      val S = WalletSampleData(db, wallet)
      val utxoDesc = wallet.getUnspentCoinDescription(
        db,
        S.TestBlockchainView,
        S.Carry.Addr1.address,
        WalletOutputWithInfo(
          S.S3_CarrayGenCoin_A50.outPoint,
          walletOutput = WalletOutput(
            blockindex    = S.S3_BlockHeight,
            coinbase      = true,
            spent         = false,
            transactionOutput = S.S3_CarrayGenCoin_A50.output
          )
        )
      )!!

      utxoDesc.txid shouldBe S.S3_CarrayGenCoin_A50.outPoint.transactionHash
      utxoDesc.vout shouldBe S.S3_CarrayGenCoin_A50.outPoint.outputIndex
      utxoDesc.address shouldBe S.Carry.Addr1.address.base58()
      utxoDesc.account shouldBe "Carry"
      utxoDesc.scriptPubKey shouldBe hex(S.S3_CarrayGenCoin_A50.output.lockingScript.data.array)
      utxoDesc.redeemScript shouldBe null
      utxoDesc.amount shouldBe java.math.BigDecimal(50L)
      utxoDesc.confirmations shouldBe 1L
      utxoDesc.spendable shouldBe false // because of coinbase maturity
    }

    "getUnspentCoinDescription" should "return the description of the UTXO for generation transaction(spendable=true)" {
      // TODO : Implement
    }

    "getUnspentCoinDescription" should "return the description of the UTXO for normal transaction" {
      val S = WalletSampleData(db, wallet)
      val utxoDesc = wallet.getUnspentCoinDescription(
        db,
        S.TestBlockchainView,
        S.Alice.Addr2.address,
        WalletOutputWithInfo(
          S.S2_AliceChangeCoin1_A39.outPoint,
          walletOutput = WalletOutput(
            blockindex    = S.S2_BlockHeight,
            coinbase      = false,
            spent         = false,
            transactionOutput = S.S2_AliceChangeCoin1_A39.output
          )
        )
      )!!

      utxoDesc.txid shouldBe S.S2_AliceChangeCoin1_A39.outPoint.transactionHash
      utxoDesc.vout shouldBe S.S2_AliceChangeCoin1_A39.outPoint.outputIndex
      utxoDesc.address shouldBe S.Alice.Addr2.address.base58()
      utxoDesc.account shouldBe "Alice"
      utxoDesc.scriptPubKey shouldBe hex(S.S2_AliceChangeCoin1_A39.output.lockingScript.data.array)
      utxoDesc.redeemScript shouldBe null
      utxoDesc.amount shouldBe java.math.BigDecimal(39L)
      utxoDesc.confirmations shouldBe 2L
      utxoDesc.spendable shouldBe true
    }

    "getUnspentCoinDescription" should "return nothing if a coin is spent." {
      val S = WalletSampleData(db, wallet)
      wallet.getUnspentCoinDescription(
        db,
        S.TestBlockchainView,
        S.Alice.Addr1.address,
        WalletOutputWithInfo(
          S.S1_AliceGenCoin_A50.outPoint,
          walletOutput = WalletOutput(
            blockindex    = S.S1_BlockHeight,
            coinbase      = true,
            spent         = true,
            transactionOutput = S.S1_AliceGenCoin_A50.output
          )
        )
      ) shouldBe null
    }

    "getConfirmations" should "" {
      object : WalletSampleData(db, wallet) {
        override fun onStepFinish(stepNumber : Int): Unit {
          when(stepNumber) {
            1 -> {
              wallet.getConfirmations(db, TestBlockchainView, 0) shouldBe 2L
              wallet.getConfirmations(db, TestBlockchainView, 1) shouldBe 1L
            }
            2 -> {
              wallet.getConfirmations(db, TestBlockchainView, 0) shouldBe 3L
              wallet.getConfirmations(db, TestBlockchainView, 1) shouldBe 2L
            }
            3 -> {
              wallet.getConfirmations(db, TestBlockchainView, 0) shouldBe 4L
              wallet.getConfirmations(db, TestBlockchainView, 1) shouldBe 3L
            }
            4 -> { // no block is created for step 4
              wallet.getConfirmations(db, TestBlockchainView, 0) shouldBe 4L
              wallet.getConfirmations(db, TestBlockchainView, 1) shouldBe 3L
            }
            5 -> { // no block is created for step 4
              wallet.getConfirmations(db, TestBlockchainView, 0) shouldBe 4L
              wallet.getConfirmations(db, TestBlockchainView, 1) shouldBe 3L
            }
          }
        }
      }

    }

    fun utxo(account:String, output : OutputWithOutPoint, confirmations : Int, spendable : Boolean) : UnspentCoinDescriptor {
      return UnspentCoinDescriptor(
        txid          = output.outPoint.transactionHash,
        vout          = output.outPoint.outputIndex,
        address       = LockingScriptAnalyzer.extractAddresses(output.output.lockingScript).first().base58(),
        account       = account,
        scriptPubKey  = hex(output.output.lockingScript.data.array),
        redeemScript  = null,
        amount        = CoinAmount.from(output.output.value).value,
        confirmations = confirmations.toLong(),
        spendable     = spendable
      )
    }

    "listUnspent(all addresses)" should "" {
      object : WalletSampleData(db, wallet) {
        override fun onStepFinish(stepNumber : Int): Unit {
          when(stepNumber) {
            1 -> {
              wallet.listUnspent(db, TestBlockchainView, 0, 100, null).toSet() shouldBe setOf(
                utxo("Alice", S1_AliceGenCoin_A50, confirmations = 1, spendable = false)
              )
            }
            2 -> {
              wallet.listUnspent(db, TestBlockchainView, 0, 100, null).toSet() shouldBe setOf(
                utxo("Bob", S2_BobGenCoin_A50, confirmations = 1, spendable = false),
                utxo("Bob", S2_BobCoin1_A10, confirmations = 1, spendable = true),
                utxo("Alice", S2_AliceChangeCoin1_A39, confirmations = 1, spendable = true)
              )
            }
            3 -> {
              wallet.listUnspent(db, TestBlockchainView, 0, 100, null).toSet() shouldBe setOf(
                utxo("Bob", S2_BobGenCoin_A50, confirmations = 2, spendable = true),
                utxo("Alice", S2_AliceChangeCoin1_A39, confirmations = 2, spendable = true),
                utxo("Carry", S3_CarrayGenCoin_A50, confirmations = 1, spendable = false),
                utxo("Alice", S3_AliceCoin1_A2, confirmations = 1, spendable = true),
                utxo("Carry", S3_CarrayCoin1_A3, confirmations = 1, spendable = true),
                utxo("Bob", S3_BobChangeCoin1_A5, confirmations = 1, spendable = true)
              )
            }
            4 -> {
              wallet.listUnspent(db, TestBlockchainView, 0, 100, null).toSet() shouldBe setOf(
                utxo("Bob", S2_BobGenCoin_A50, confirmations = 2, spendable = true),
                utxo("Alice", S2_AliceChangeCoin1_A39, confirmations = 2, spendable = true),
                utxo("Carry", S3_CarrayGenCoin_A50, confirmations = 1, spendable = false),
                utxo("Carry", S3_CarrayCoin1_A3, confirmations = 1, spendable = true),
                utxo("Bob", S3_BobChangeCoin1_A5, confirmations = 1, spendable = true),
                utxo("Carry", S4_CarryCoin2_A1, confirmations = 0, spendable = true)
              )
            }

            5 -> {
              wallet.listUnspent(db, TestBlockchainView, 0, 100, null).toSet() shouldBe setOf(
                utxo("Bob", S2_BobGenCoin_A50, confirmations = 2, spendable = true),
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

    "listUnspent(some addresses)" should "list UTXO for an account." {
      val S = WalletSampleData(db, wallet)

      wallet.listUnspent(db, S.TestBlockchainView, 0, 100, listOf(S.Alice.Addr1.address)).toSet() shouldBe setOf(
        utxo("Alice", S.S5_AliceCoin3_A4, confirmations = 0, spendable = true)
      )

      wallet.listUnspent(db, S.TestBlockchainView, 0, 100, listOf(S.Alice.Addr2.address)).toSet() shouldBe setOf(
        utxo("Alice", S.S2_AliceChangeCoin1_A39, confirmations = 2, spendable = true)
      )

      wallet.listUnspent(db, S.TestBlockchainView, 0, 100, listOf(S.Bob.Addr1.address)).toSet() shouldBe setOf(
        utxo("Bob", S.S2_BobGenCoin_A50, confirmations = 2, spendable = true)
      )

      wallet.listUnspent(db, S.TestBlockchainView, 0, 100, listOf(S.Bob.Addr2.address)).toSet() shouldBe setOf(
        utxo("Bob", S.S3_BobChangeCoin1_A5, confirmations = 1, spendable = true)
      )

      wallet.listUnspent(db, S.TestBlockchainView, 0, 100, listOf(S.Carry.Addr1.address)).toSet() shouldBe setOf(
        utxo("Carry", S.S3_CarrayGenCoin_A50, confirmations = 1, spendable = false)
      )
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Methods for importaddress RPC
    ////////////////////////////////////////////////////////////////////////////////

    "importOutputOwnership(rescan=true)" should "rescan current blockchain"  {
      //val S = WalletSampleData(db, wallet)
      // TODO : Implement test case
    }

    "importOutputOwnership(rescan=false)" should "should not rescan the current blockchain" {
      //val S = WalletSampleData(db, wallet)
      // TODO : Implement test case
    }

    "importOutputOwnership" should "change the receiving address to the imported address" {
      val S = WalletSampleData(db, wallet)
      wallet.importOutputOwnership(
        db,
        S.TestBlockchainView,
        "test1",
        S.Alice.Addr1.address,
        rescanBlockchain = false)
      wallet.getReceivingAddress(db, "test1") shouldBe S.Alice.Addr1.address
    }

    "importOutputOwnership" should "throw an exception if ParsedPubKeyScript was provided" {
      val S = WalletSampleData(db, wallet)
      val e = shouldThrow<WalletException> {
        val parsedPubKeyScript = ParsedPubKeyScript.from(S.Alice.Addr1.address.lockingScript())
        wallet.importOutputOwnership(
          db,
          S.TestBlockchainView,
          "test1",
          parsedPubKeyScript,
          rescanBlockchain = false)
      }
      e.code shouldBe ErrorCode.UnsupportedFeature
    }

      ////////////////////////////////////////////////////////////////////////////////
    // Methods for getaccount RPC
    ////////////////////////////////////////////////////////////////////////////////

    "getAccount" should "" {
      val test1_Addr1 = wallet.newAddress(db, "test1")
      val test1_Addr2 = wallet.newAddress(db, "test1")
      val test2_Addr1 = wallet.newAddress(db, "test2")
      val test2_Addr2 = wallet.newAddress(db, "test2")
      val test3_Addr1 = wallet.newAddress(db, "test3")
      val test3_Addr2 = wallet.newAddress(db, "test3")
      wallet.getAccount(db, test1_Addr1) shouldBe "test1"
      wallet.getAccount(db, test1_Addr2) shouldBe "test1"
      wallet.getAccount(db, test2_Addr1) shouldBe "test2"
      wallet.getAccount(db, test2_Addr2) shouldBe "test2"
      wallet.getAccount(db, test3_Addr1) shouldBe "test3"
      wallet.getAccount(db, test3_Addr2) shouldBe "test3"
    }

    "getAccount" should "return null if an address not generated by newAddress is privided" {
      wallet.getAccount(db, generateAddress().address) shouldBe null
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Methods for newaddress RPC
    ////////////////////////////////////////////////////////////////////////////////

    "newAddress" should "generate a address and it should become the receiving address" {
      val address1 = wallet.newAddress(db, "test1")
      address1 shouldBe wallet.getReceivingAddress(db, "test1")

      val address2 = wallet.newAddress(db, "test1")
      address2 shouldBe wallet.getReceivingAddress(db, "test1")
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

    "registerTransaction" should "ignore transactions that are not related to addresses of an account"  {
      //val S = ChainSampleData(db, null)
      // wallet.registerTransaction(listOf(), S.S1_AliceGenTx, null, null)
      // TODO : Implement
    }

    "registerTransaction" should "" {
      //val S = ChainSampleData(db, null)
      // TODO : Implement
    }

    "unregisterTransaction" should "" {
      //val S = WalletSampleData(db, wallet)
      // TODO : Implement test case
    }

    "onNewTransaction" should "" {
      //val S = WalletSampleData(db, wallet)
      // TODO : Implement test case
    }

    "onRemoveTransaction" should "" {
      //val S = WalletSampleData(db, wallet)
      // TODO : Implement test case
    }

    "onNewBlock" should "" {
      //val S = WalletSampleData(db, wallet)
      // TODO : Implement test case
    }

    "onRemoveBlock" should "" {
      //val S = WalletSampleData(db, wallet)
      // TODO : Implement test case
    }

    "getPrivateKeys" should "return an empty list if the address was imported" {
      val S = WalletSampleData(db, wallet)
      wallet.importOutputOwnership(
        db,
        S.TestBlockchainView,
        "test1",
        S.Alice.Addr1.address,
        rescanBlockchain = false)

      val keys = wallet.getPrivateKeys(db, S.Alice.Addr1.address)
      keys.size shouldBe 0
    }

    "getPrivateKeys" should "return a list that has a private key if the address was generated" {
      val addr1 = wallet.newAddress(db, "test1")

      val addr1_privateKeys = wallet.getPrivateKeys(db, addr1)

      addr1_privateKeys.size shouldBe 1
      CoinAddress.from(addr1_privateKeys.first()) shouldBe addr1
    }
  }
}

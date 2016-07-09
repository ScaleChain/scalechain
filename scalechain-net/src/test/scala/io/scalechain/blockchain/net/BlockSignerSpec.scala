package io.scalechain.blockchain.net

import java.io.File

import io.scalechain.blockchain.chain.{TransactionWithName, NewOutput, BlockSampleData, Blockchain}
import io.scalechain.blockchain.script.HashSupported
import io.scalechain.blockchain.storage.{DiskBlockStorage, Storage}
import io.scalechain.blockchain.transaction._
import io.scalechain.wallet.{WalletTestTrait, WalletBasedBlockSampleData, Wallet}
import org.apache.commons.io.FileUtils
import org.scalatest.{Suite, Matchers, BeforeAndAfterEach, FlatSpec}
import HashSupported._

import scala.collection.mutable.ListBuffer


class BlockSignerSpec extends FlatSpec with WalletTestTrait with BeforeAndAfterEach with TransactionTestDataTrait with Matchers {

  this: Suite =>

  Storage.initialize()

  var data : WalletBasedBlockSampleData = null
  val testPath = new File("./target/unittests-BlockSignerSpec-storage/")

  var signer : BlockSigner = null

  override def beforeEach() {
    super.beforeEach()

    // Set the wallet to the block signer.
    signer = new BlockSigner()(db)
    signer.setWallet(wallet)

    data = new WalletBasedBlockSampleData(wallet)

    // transfer a coin to the signing address.
    {
      chain.putBlock(data.Block.BLK01.header.hash, data.Block.BLK01)
      chain.putBlock(data.Block.BLK02.header.hash, data.Block.BLK02)   // 1 confirm => GEN01 is immature
      chain.putBlock(data.Block.BLK03a.header.hash, data.Block.BLK03a) // 2 confirm => GEN01 is mature.

      val initialTx = data.normalTransaction(
        "sendCoinToSigningAddress",
        spendingOutputs = List( data.getOutput(data.Tx.GEN02,0) ),
        newOutputs = List(
          NewOutput(CoinAmount(50), signer.signingAddress)
          // We have very expensive fee, 4 SC
        )
      )
      chain.putTransaction(initialTx.transaction.hash, initialTx.transaction)
    }

  }

  override def afterEach() {
    super.afterEach()

    data = null

  }


  "signingAddress" should "return the same address" in {
    val signer1 = new BlockSigner()(db)
    signer1.setWallet(wallet)

    val signer2 = new BlockSigner()(db)
    signer2.setWallet(wallet)

    signer.signingAddress shouldBe signer1.signingAddress
    signer.signingAddress shouldBe signer2.signingAddress
  }

  "extractSigningAddress" should "extract the address used for signing" in {
    val transaction = signer.signBlock(chain, env.GenesisBlockHash)
    signer.extractSigningAddress(chain, transaction) shouldBe Some(signer.signingAddress())
  }

  "extractSigningAddress" should "return the address of output pointed by the input 0 even though the transaction was not used for signing." in {
    signer.extractSigningAddress(chain, data.Tx.TX03.transaction) shouldBe Some(data.Addr2.address)
  }


  "extractSignedBlockHash" should "extract the address used for signing" in {
    val transaction = signer.signBlock(chain, env.GenesisBlockHash)
    signer.extractSignedBlockHash(chain, transaction) shouldBe Some(SignedBlock(env.GenesisBlockHash, signer.signingAddress().base58))
  }

  "extractSignedBlockHash" should "return None if the transaction is not a block signing transaction" in {
    signer.extractSignedBlockHash(chain, data.Tx.TX03.transaction) shouldBe None
  }

  "extractSingingAddresses" should "return the list of addresses that signed the transactions with previous block hash" in {
    val prevBlockHash = data.Block.BLK02.header.hash
    val signingTx1 =  signer.signBlock(chain, prevBlockHash, data.Addr1.address)
    val signingTx2 =  signer.signBlock(chain, prevBlockHash, data.Addr2.address)
    val invalidSigningTx = signer.signBlock(chain, env.GenesisBlockHash, data.Addr3.address) // The hash does not match the previous hash. Addr3 should not be included in the signing addresses.

    val transactions = List(data.Tx.GEN03a.transaction, signingTx1, signingTx2, invalidSigningTx).map( TransactionWithName("a", _))

    val block = data.newBlock(prevBlockHash, transactions )
    signer.extractSingingAddresses(chain, block) shouldBe List(data.Addr1.address, data.Addr2.address).map(_.base58)
  }

  "extractSingingAddresses(recursive)" should "return the list of addresses that signed the transactions with previous block hash" in {
    val prevBlockHash = data.Block.BLK02.header.hash
    val signingTx1 =  signer.signBlock(chain, prevBlockHash, data.Addr1.address)
    val signingTx2 =  signer.signBlock(chain, prevBlockHash, data.Addr2.address)
    val invalidSigningTx = signer.signBlock(chain, env.GenesisBlockHash, data.Addr3.address) // The hash does not match the previous hash. Addr3 should not be included in the signing addresses.

    val transactions = List(signingTx1, signingTx2, invalidSigningTx)
    val addresses = new ListBuffer[String]()
    signer.extractSingingAddresses(chain, prevBlockHash, transactions, addresses)
    addresses.toList shouldBe List(data.Addr1.address, data.Addr2.address).map(_.base58)
  }

}

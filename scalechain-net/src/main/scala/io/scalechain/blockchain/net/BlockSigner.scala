package io.scalechain.blockchain.net

import io.scalechain.blockchain.proto.codec.BlockSignatureCodec
import io.scalechain.blockchain.script.{ScriptValue, ScriptParser}
import io.scalechain.blockchain.script.ops.{OpPush, OpReturn}
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.SigHash
import io.scalechain.blockchain.transaction.SigHash._
import io.scalechain.blockchain.{ErrorCode, NetException}
import io.scalechain.wallet.{UnspentCoinDescriptor, Wallet}
import io.scalechain.blockchain.chain.TransactionBuilder
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.transaction._

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

case class SignedBlock(blockHash : Hash, address: String)

/**
  * Created by kangmo on 7/6/16.
  */
class BlockSigner()(implicit db : KeyValueDatabase) {
  protected[net] var wallet : Wallet = null
  protected[net] var blockSigningAddress : CoinAddress = null

  /**
    * Set the wallet for signing a transaction that has a block hash in it.
    *
    * @param wallet The wallet for signing a transaction that has a block hash in it.
    */
  def setWallet(wallet : Wallet) : Unit = {
    assert(wallet != null)
    // Get the block signing address from wallet and set it to the block signer.
    blockSigningAddress = wallet.getReceivingAddress("block-signing")
    this.wallet = wallet
  }

  /**
    * Get the address used for signing blocks.
    *
    * @return The address used for signing blocks. Actually the private key of the address is used for signing blocks.
    */
  def signingAddress() = {
    assert(blockSigningAddress != null)
    blockSigningAddress
  }

  /**
    * Create and sign a transaction that has the block hash in an output with OP_RETURN.
    *
    * @param chainView
    * @param blockHash
    * @return
    */
  def signBlock(chainView : BlockchainView, blockHash : Hash) : Transaction = {
    assert(wallet != null)

    signBlock(chainView, blockHash, blockSigningAddress)
  }


  protected[net] def signBlock(chainView : BlockchainView, blockHash : Hash, signingAdress : CoinAddress) : Transaction = {
    assert(wallet != null)

    val unspentOutputs : List[UnspentCoinDescriptor] = wallet.listUnspent(chainView, 0, Long.MaxValue, Some(List(signingAdress)))
    if (unspentOutputs.isEmpty) {
      throw new NetException(ErrorCode.NoCoinForBlockSigning)
    }

    val outputToSpend = unspentOutputs.head

    val blockSignature = BlockSignatureCodec.serialize( BlockSignature( BlockSignature.MAGIC, blockHash) )

    val unsignedTransaction = TransactionBuilder
      .newBuilder(chainView)
      .addInput(OutPoint(outputToSpend.txid, outputToSpend.vout))
      .addOutput(blockSignature)
      .addOutput(CoinAmount(outputToSpend.amount), blockSigningAddress)
      .build()

    assert( unsignedTransaction.outputs.length == 2)

    val signedTransaction = wallet.signTransaction(unsignedTransaction, chainView, List(), None, SigHash.ALL)
    assert(signedTransaction.complete)

    assert( signedTransaction.transaction.outputs.length == 2)
    signedTransaction.transaction
  }

  /**
    * Extract the signing address from the transaction. The output pointed by the first input has the address.
    *
    * @param chainView
    * @param transaction
    * @return
    */
  def extractSigningAddress(chainView : BlockchainView, transaction : Transaction) : Option[CoinAddress] = {
    transaction.inputs(0) match {
      case input : NormalTransactionInput => {
        val outPoint = OutPoint(input.outputTransactionHash, input.outputIndex.toInt)
        val output = chainView.getTransactionOutput(outPoint)
        val addresses = LockingScriptAnalyzer.extractAddresses( output.lockingScript )
        if (addresses.length == 1) {
          Some( addresses(0) )
        } else {
          None
        }
      }
    }
  }

  /**
    * Extract the block hash from the transaction for signing a block.
    * Assumption : The transaction verification is done before this method is called.
    *
    * @param chainView The view of the blockchain to get the coins.
    * @param transaction The transaction that has the block hash in an output with OP_RETURN.
    * @return Some(signed block) if a block hash was successfully extracted; None otherwise.
    */
  def extractSignedBlockHash(chainView : BlockchainView, transaction : Transaction) : Option[SignedBlock] = {
    val lockingScriptData = transaction.outputs(0).lockingScript.data
    if (lockingScriptData.isEmpty) {
      None
    } else {
      assert( BlockSignature.MAGIC.length == 2)
      // Before parsing the script run a quick check to see if the locking script of the first output contains OpReturn and OpPush with magic values.
      if ( lockingScriptData.length >= ( 1 + 1 + 2 + 32) && // OpReturn, OpPush, Magic(2 bytes), Block Hash(32 bytes)
           lockingScriptData(0).toShort == OpReturn().opCode().code && // The op code for OpReturn is 0x6a, which is less than 127. We can convert it to short, and compare with the op code of OpReturn.
           // lockingScriptData(1) is OpPush(). Do not compare
           lockingScriptData(2) == BlockSignature.MAGIC(0) && // check the first byte of magic.
           lockingScriptData(3) == BlockSignature.MAGIC(1)    // check the second byte of the magic.
      ) {
        val scriptOps = ScriptParser.parse(transaction.outputs(0).lockingScript)
        scriptOps.operations match {
          case List( OpReturn(), OpPush(34, scriptValue : ScriptValue)) => {
            val blockSignature = BlockSignatureCodec.parse(scriptValue.value)
            assert( blockSignature.magic == BlockSignature.MAGIC)
            val signingAddressOption = extractSigningAddress(chainView, transaction)
            signingAddressOption.map{ address : CoinAddress =>
              SignedBlock( blockSignature.blockHash, address.base58 )
            }
          }
          case _ => {
            None
          }
        }
      } else {
        None
      }
    }
  }

  /**
    * Extract signing addresses from transactions that have the previous block hash of a given block.
    *
    * A block has transactions in the following order.
    * 1. The generation transaction
    * 2. Transactions that have the previous block hash in the first output with OpReturn.
    * 3. Normal transactions.
    *
    * @param chainView The view of the blockchain to get the coins.
    * @param block The block to extract signing addresses
    * @return
    */
  def extractSingingAddresses(chainView : BlockchainView, block : Block) : List[String] = {
    assert(block.transactions(0).inputs(0).isCoinBaseInput())
    val addresses = new ListBuffer[String]()

    // Skip the head(generation transaction), and start iteration from the tail of the transactions list.
    extractSingingAddresses(chainView, block.header.hashPrevBlock, block.transactions.tail, addresses)
    addresses.toList
  }


  @tailrec
  protected[net] final def extractSingingAddresses(chainView : BlockchainView, hashPrevBlock : Hash, transactions : List[Transaction], addresses : ListBuffer[String]) : Unit = {
    val signedBlockHash = extractSignedBlockHash(chainView, transactions.head)

    if (signedBlockHash.isEmpty) { // base case
      // do nothing
    } else {
      val signedHash = signedBlockHash.get
      if (signedHash.blockHash == hashPrevBlock) {
        addresses.append(signedHash.address)
        extractSingingAddresses(chainView, hashPrevBlock, transactions.tail, addresses)
      } else {
        // base case : do nothing.
      }
    }
  }
}

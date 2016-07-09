package io.scalechain.blockchain.net

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.mining.BlockTemplate
import io.scalechain.blockchain.chain.{TransactionBuilder, BlockMining, TransactionPool}
import io.scalechain.blockchain.proto.{Hash, Transaction, CoinbaseData}
import io.scalechain.blockchain.storage.index.{RocksDatabase, KeyValueDatabase, TransactionDescriptorIndex}
import io.scalechain.blockchain.transaction.{BlockchainView, CoinAmount, CoinAddress, CoinsView}
import org.slf4j.LoggerFactory

class SignedBlockMining(txDescIndex : TransactionDescriptorIndex, transactionPool : TransactionPool, chainView : BlockchainView)(implicit keyValueDB : RocksDatabase) extends BlockMining(txDescIndex, transactionPool, chainView)(keyValueDB){
  private val logger = Logger(LoggerFactory.getLogger(classOf[SignedBlockMining]))

  /** Get the template for creating a block containing a list of transactions.
    *
    * @return Some(block template), which has a sorted list of transactions to include into a block if we have enough signing transactions with the previous block hash.
    *         None otherwise.
    */
  def getBlockTemplate(hashPrevBlock : Hash, permissionedAddresses : List[String], requiredPermissionedAddressCount : Int, coinbaseData : CoinbaseData, minerAddress : CoinAddress, maxBlockSize : Int) : Option[BlockTemplate] = {
    // TODO : P1 - Use difficulty bits
    //val difficultyBits = getDifficulty()
    val difficultyBits = 10

    val validTransactions : List[Transaction] = transactionPool.getTransactionsFromPool().map {
      case (txHash, transaction) => transaction
    }

    val signingTransactions = validTransactions.filter { tx =>
      val signedBlockHash = BlockSigner.extractSignedBlockHash(chainView, tx)

      signedBlockHash.isDefined && // the signed block hash should exist
      signedBlockHash.get.blockHash == hashPrevBlock && // the hash in the transaction should match the previous block hash
        permissionedAddresses.contains(signedBlockHash.get.address) // the signing address should be one of the given permissioned addresses.
    }

    // Not enough signing transactions
    if (signingTransactions.length < requiredPermissionedAddressCount) {
      None
    } else {
      val nonSigningTransactions = validTransactions.filter { tx =>
        BlockSigner.extractSignedBlockHash(chainView, tx).isEmpty
      }

      val generationTranasction =
        TransactionBuilder.newBuilder(chainView)
          .addGenerationInput(coinbaseData)
          .addOutput(CoinAmount(50), minerAddress)
          .build()

      // Select transactions by priority and fee. Also, sort them.
      val sortedTransactions = selectTransactions(generationTranasction, signingTransactions, nonSigningTransactions, maxBlockSize)

      Some(new BlockTemplate(difficultyBits, sortedTransactions))
    }
  }
}
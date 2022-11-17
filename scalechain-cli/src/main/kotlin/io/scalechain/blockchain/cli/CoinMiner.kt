package io.scalechain.blockchain.cli

import io.scalechain.blockchain.chain.mining.BlockMining
import io.scalechain.blockchain.transaction.CoinAddress
import io.scalechain.util.*
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.net.*
import io.scalechain.blockchain.proto.Block
import io.scalechain.blockchain.proto.CoinbaseData
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.hash
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.crypto.HashEstimation
import io.scalechain.wallet.Wallet
import org.slf4j.LoggerFactory
import java.util.*

data class CoinMinerParams(val P2PPort : Int, val MaxBlockSize : Int )

interface CoinMinerListener {
  // Called when the coin miner thread starts
  fun onStart()
  // Called when the coin is mined.
  fun onCoinMined(block : Block, minerAddress : CoinAddress)
}

class CoinMiner(private val db : KeyValueDatabase, private val minerAccount : String, private val wallet : Wallet, private val chain : Blockchain, private val peerCommunicator: PeerCommunicator, private val params : CoinMinerParams, private val listener : CoinMinerListener?) {

  private val logger = LoggerFactory.getLogger(CoinMiner::class.java)

  /**
    * Check if we can start mining.
    *
    * @return true if we can mine; false otherwise.
    */
  fun canMine() : Boolean {
    val maxPeerCount = Config.get().peerAddresses().size
    val node = Node.get()
    if (maxPeerCount == 1) {
      // regression test mode with only one node.
      return true
    } else if (node.isInitialBlockDownload()) { // During the initial block download, do not do mining.
      return false
    } else {
      val bestPeerOption = node.getBestPeer()
      if (bestPeerOption != null) {
        val bestPeer = bestPeerOption
        val bestBlockHeight = chain.getBestBlockHeight()

        // Did we catch up the best peer, which has the highest block height by the time we connected ?
        return bestBlockHeight >= bestPeer.versionOption?.startHeight ?: 0
      } else {

        return false
      }
    }
  }
  val thread = object : Thread() {
    override fun run() : Unit {
      logger.info("Miner started. Params : ${params}")
      val random = Random(System.currentTimeMillis())

      var nonce : Int = 1

      listener?.onStart()

      while(true) { // This thread loops until the shouldStop flag is set.
        nonce += 1
        // Randomly sleep from 100 to 200 milli seconds. On average, sleep 60 seconds.
        // Because current difficulty(max hash : 00F0.. ) is to find a block at the probability 1/256,
        // We will get a block in (100ms * 256 = 25 seconds) ~ (200 ms * 256 = 52 seconds)

        val bestBlockHeight = chain.getBestBlockHeight()

        //println(s"canMine=${canMine}, isMyTurn=${isMyTurn}")

        if (canMine()) {
          //chain.synchronized {
          // Step 2 : Create the block template
          val minerAddress = wallet.getReceivingAddress(db, minerAccount)
          val bestBlockHash = chain.getBestBlockHash(db)
          if (bestBlockHash != null) {
            val blockHeight = chain.getBlockInfo(db, bestBlockHash)!!.height
            val COINBASE_MESSAGE = coinbaseData(blockHeight + 1)

            val blockMining = BlockMining(db, chain.txDescIndex(), chain.txPool, chain)
            val blockTemplate = blockMining.getBlockTemplate(COINBASE_MESSAGE, minerAddress, params.MaxBlockSize)

            // Step 3 : Get block header
            val blockHeaderTemplate = blockTemplate.getBlockHeader(Hash(bestBlockHash.value))
            val startTime = System.currentTimeMillis()
            var blockFound = false;

            // TODO : BUGBUG : Need to use chain.getDifficulty instead of using a fixed difficulty
            val requiredHashCalulcations = 1024L * 1024L

            // Try mining with the block template for five seconds.
            val MINING_TRYAL_MILLIS=1000
            // Step 3 : Loop until we find a block header hash less than the threshold.
            var nonceValue = 0L
            do {
              val miningBlockHeader = blockHeaderTemplate.copy(nonce = nonceValue)


              if (HashEstimation.getHashCalculations(miningBlockHeader.hash().value.array) == requiredHashCalulcations) {

                // Check the best block hash once more.
                if (bestBlockHash.value == chain.getBestBlockHash(db)!!.value) {
                  // Step 5 : When a block is found, create the block and put it on the blockchain.
                  // Also propate the block to the peer to peer network.
                  val block = blockTemplate.createBlock(miningBlockHeader, miningBlockHeader.nonce)
                  val blockHeaderHash = block.header.hash()

                  BlockPropagator.propagate(blockHeaderHash, block)

                  blockFound = true
                  logger.trace("Block Mined.\n hash : ${blockHeaderHash}\n\n")

                  listener?.onCoinMined(block, minerAddress)
                }
              }

              nonceValue ++

            } while( !blockFound &&
                     System.currentTimeMillis() - startTime < MINING_TRYAL_MILLIS)
          } else {
            logger.error("The best block hash is not defined yet.")
          }
          //}
        } else {

          Thread.sleep(10)
        }
      }
    }
  }

  fun start() : Unit {
    thread.start()
  }

  companion object {
    var theCoinMiner : CoinMiner? = null

    fun create(indexDb : KeyValueDatabase, minerAccount : String, wallet : Wallet, chain : Blockchain, peerCommunicator: PeerCommunicator, params : CoinMinerParams) : CoinMiner {
      theCoinMiner = CoinMiner(indexDb, minerAccount, wallet, chain, peerCommunicator, params, null)
      return theCoinMiner!!
    }

    fun get() : CoinMiner {
      assert(theCoinMiner != null)
      return theCoinMiner!!
    }

    // For every 10 seconds, create a block template for mining a block.
    // This means that transactions received within the time window may not be put into the mined block.
    val MINING_TRIAL_WINDOW_MILLIS = 10000

    fun coinbaseData(height : Long) : CoinbaseData {
      return CoinbaseData(Bytes("height:${height}, ScaleChain by Kwanho, Chanwoo, Kangmo.".toByteArray()))
    }
  }
}

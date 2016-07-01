package io.scalechain.blockchain.cli

import java.util

import io.scalechain.blockchain.chain.{BlockMining, Blockchain}
import io.scalechain.blockchain.net.{PeerInfo, PeerCommunicator}
import io.scalechain.blockchain.proto.{CoinbaseData, Hash, Block}
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.util.Utils
import io.scalechain.wallet.Wallet
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._

import scala.util.Random

case class CoinMinerParams(InitialDelayMS : Int, HashDelayMS : Int, MaxBlockSize : Int )
/**
  * Created by kangmo on 3/15/16.
  */
object CoinMiner {
  var theCoinMiner : CoinMiner = null

  def create(minerAccount : String, wallet : Wallet, chain : Blockchain, peerCommunicator: PeerCommunicator, params : CoinMinerParams) = {
    theCoinMiner = new CoinMiner(minerAccount, wallet, chain, peerCommunicator, params)
    theCoinMiner.start()
    theCoinMiner
  }

  def get = {
    assert(theCoinMiner != null)
    theCoinMiner
  }
}


class CoinMiner(minerAccount : String, wallet : Wallet, chain : Blockchain, peerCommunicator: PeerCommunicator, params : CoinMinerParams) {
  private val logger = LoggerFactory.getLogger(classOf[CoinMiner])

  // For every 10 seconds, create a new block template for mining a block.
  // This means that transactions received within the time window may not be put into the mined block.
  val MINING_TRIAL_WINDOW_MILLIS = 10000
  val PREMINE_BLOCKS = 5;

  val blockMining = chain.createBlockMining()


  /**
    * Check if we can start mining.
    * @return true if we can mine; false otherwise.
    */
  def canMine() : Boolean = {
    val peerCount = io.scalechain.util.Config.scalechain.getConfigList("scalechain.p2p.peers").asScala.toArray.length
    if (peerCount == 1) {
      // regression test mode with only one node.
      true
    } else {
      // exclude myself.
      val peerCountExcudingMe = peerCount - 1
      // We have two-way peer connections. From me to peer, From peer to me.
      val maxPeerConnections = peerCountExcudingMe * 2

      val peerInfos = peerCommunicator.getPeerInfos()
      // Do we have enough number of peers? (At least more than half)
      if ( peerInfos.length >= maxPeerConnections / 2 ) {
        // Did we receive starting height for all peers?
        if ( peerInfos.filter( _.startingheight.isDefined ).length == peerInfos.length ) {
          val bestPeer = peerCommunicator.getBestPeer.get
          val bestBlockHeight = chain.getBestBlockHeight()

          // Did we catch up the best peer, which has the highest block height by the time we connected ?
          bestBlockHeight >= bestPeer.startingheight.get
        } else {
          false
        }
      } else { // Not enough connected peers.
        false
      }
    }
  }
  def start() : Unit = {

    val thread = new Thread {
      override def run {
        logger.info(s"Miner started. Params : ${params}")
        val random = new Random(System.currentTimeMillis())

        // TODO : Need to eliminate this code.
        // Sleep for one minute to wait for each peer to start.
        Thread.sleep(params.InitialDelayMS)

        // Step 1 : Set the minder's coin address to receive block mining reward.
        val minerAddress = wallet.getReceivingAddress(minerAccount)

        var nonce : Int = 1

        while(true) { // This thread loops forever.
          nonce += 1
          // Randomly sleep from 100 to 200 milli seconds. On average, sleep 60 seconds.
          // Because current difficulty(max hash : 00F0.. ) is to find a block at the probability 1/256,
          // We will get a block in (100ms * 256 = 25 seconds) ~ (200 ms * 256 = 52 seconds)

          var sleep = true
          if (params.InitialDelayMS % 100 == 76) {
            if ( chain.getBestBlockHeight() < 10 ) {
              sleep = false
            }
          }
          if (sleep) {
            Thread.sleep(random.nextInt(params.HashDelayMS))
          }

          if (canMine()) {
            chain.synchronized {
              val COINBASE_MESSAGE = CoinbaseData(s"height:${chain.getBestBlockHeight() + 1}, ScaleChain by Kwanho, Chanwoo, Kangmo.")
              // Step 2 : Create the block template
              val bestBlockHash = chain.getBestBlockHash()
              val blockTemplate = blockMining.getBlockTemplate(COINBASE_MESSAGE, minerAddress, params.MaxBlockSize)
              if (bestBlockHash.isDefined) {
                // Step 3 : Get block header
                val blockHeader = blockTemplate.getBlockHeader(Hash(bestBlockHash.get.value))
                val startTime = System.currentTimeMillis()
                var blockFound = false;

                // Step 3 : Loop until we find a block header hash less than the threshold.
                //            do {
                // TODO : BUGBUG : Need to use chain.getDifficulty instead of using a fixed difficulty

                // TODO : BUGBUG : Remove scalechain.mining.header_hash_threshold configuration after the temporary project finishes
                val blockHeaderThreshold =
                  if (io.scalechain.util.Config.scalechain.hasPath("scalechain.mining.header_hash_threshold"))
                    io.scalechain.util.Config.scalechain.getString("scalechain.mining.header_hash_threshold")
                  else "00F0000000000000000000000000000000000000000000000000000000000000"

                val blockHashThreshold = Hash(blockHeaderThreshold)
                if (blockHashThreshold.value.length != 32) {
                  logger.error(s"scalechain.mining.header_hash_threshold should be 32 bytes. The specified value has ${blockHashThreshold.value.length} bytes")
                }

                val newBlockHeader = blockHeader.copy(nonce = nonce)
                val newBlockHash = newBlockHeader.hash

                if (Hash.isLessThan(newBlockHash, blockHashThreshold)) {
                  // Check the best block hash once more.
                  if ( bestBlockHash.get.value == chain.getBestBlockHash().get.value ) {
                    // Step 5 : When a block is found, create the block and put it on the blockchain.
                    // Also propate the block to the peer to peer network.
                    val block = blockTemplate.createBlock(newBlockHeader, nonce)
                    chain.putBlock(Hash(newBlockHash.value), block)
                    peerCommunicator.propagateBlock(block)
                    blockFound = true
                    logger.info(s"Block Mined.\n hash : ${newBlockHash}, block : ${block}\n\n")
                  }
                }
              } else {
                logger.error("The best block hash is not defined yet.")
              }
            }
          } else {
            // If we can't mine, it could take time to catch up other nodes. sleep 10 seconds before checking again.
            Thread.sleep(10000)
          }
        }
      }
    }
    thread.start
  }
}

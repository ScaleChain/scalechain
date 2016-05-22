package io.scalechain.blockchain.cli

import java.util

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.net.PeerCommunicator
import io.scalechain.blockchain.proto.{Hash, BlockHash, Block}
import io.scalechain.blockchain.script.HashCalculator
import io.scalechain.util.Utils
import io.scalechain.wallet.Wallet

/**
  * Created by kangmo on 3/15/16.
  */
object CoinMiner {
  var theCoinMiner : CoinMiner = null

  def create(minerAccount : String, wallet : Wallet, chain : Blockchain, peerCommunicator: PeerCommunicator) = {
    theCoinMiner = new CoinMiner(minerAccount, wallet, chain, peerCommunicator)
    theCoinMiner.start()
    theCoinMiner
  }

  def get = {
    assert(theCoinMiner != null)
    theCoinMiner
  }
}

class CoinMiner(minerAccount : String, wallet : Wallet, chain : Blockchain, peerCommunicator: PeerCommunicator) {
  // For every 10 seconds, create a new block template for mining a block.
  // This means that transactions received within the time window may not be put into the mined block.
  val MINING_TRIAL_WINDOW_MILLIS = 10000

  def isLessThan(hash1 : Hash, hash2 : Hash): Boolean = {
    val value1 = Utils.bytesToBigInteger(hash1.value)
    val value2 = Utils.bytesToBigInteger(hash2.value)

    if ( value1.compareTo( value2 ) < 0 ) {
      true
    } else {
      false
    }
  }

  def start() : Unit = {
    val thread = new Thread {
      override def run {
        // Step 1 : Set the minder's coin address to receive block minging reward.
        val minerAddress = wallet.getReceivingAddress(minerAccount)

        while(true) { // This thread loops forever.
          // Step 2 : Create the block template
          val blockTemplate = chain.getBlockTemplate(minerAddress)
          val bestBlockHash = chain.getBestBlockHash()
          if (bestBlockHash.isDefined) {
            // Step 3 : Get block header
            val blockHeader = blockTemplate.getBlockHeader(BlockHash(bestBlockHash.get.value))
            val startTime = System.currentTimeMillis()
            var blockFound = false;
            var nonce = -1L

            // Step 3 : Loop until we find a block header hash less than the threshold.
            do {
              nonce += 1
              // TODO : BUGBUG : Need to use chain.getDifficulty instead of using a fixed difficulty
              val blockHashThreshold = Hash("0001000000000000000000000000000000000000000000000000000000000000")

              val newBlockHeader = blockHeader.copy(nonce = nonce)
              val newBlockHash = Hash(HashCalculator.blockHeaderHash(newBlockHeader))

              if (isLessThan(newBlockHash, blockHashThreshold)) {
                // Step 5 : When a block is found, create the block and put it on the blockchain.
                // Also propate the block to the peer to peer network.
                val block = blockTemplate.createBlock(newBlockHeader, nonce)
                peerCommunicator.propagateBlock(block)
                chain.putBlock(BlockHash(newBlockHash.value), block)
                blockFound = true
              }

            } while (System.currentTimeMillis() - startTime < MINING_TRIAL_WINDOW_MILLIS && !blockFound)
          } else {
            println("The best block hash is not defined yet.")
          }
        }
      }
    }
    thread.start
  }
}

package io.scalechain.blockchain.net

import java.util.concurrent.TimeUnit

import bftsmart.tom.ServiceProxy
import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.processor.BlockProcessor
import io.scalechain.blockchain.proto.codec.BlockHeaderCodec
import io.scalechain.blockchain.proto.{Hash, Block, BlockHeader}
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.util._
import org.slf4j.LoggerFactory

import scala.annotation.tailrec

object BlockBroadcaster {
  var theBlockBroadcaster : BlockBroadcaster = null
  var theBlockConsensusServer : BlockConsensusServer = null

  fun create(nodeIndex : Int) : BlockBroadcaster {
    if (theBlockBroadcaster == null) {
      theBlockBroadcaster = BlockBroadcaster(nodeIndex)
      assert( theBlockConsensusServer == null )
      theBlockConsensusServer = BlockConsensusServer(nodeIndex)
    }
    theBlockBroadcaster
  }

  fun get() : BlockBroadcaster {
    assert(theBlockBroadcaster != null)
    assert(theBlockConsensusServer != null)
    theBlockBroadcaster
  }
}

class BlockBroadcaster( nodeIndex : Int) {
  val bftProxy : ServiceProxy = ServiceProxy(nodeIndex)

  /**
    * Try to make a header a consensual header that agrees other nodes as well.
    *
    * @param header
    */
  fun broadcastHeader(header : BlockHeader) {
    val rawBlockHeader : ByteArray = BlockHeaderCodec.serialize( header )
    bftProxy.invokeOrdered(rawBlockHeader)
  }
}

object PeerIndexCalculator {
  @tailrec
  final fun getPeerIndexInternal(p2pPort : Int, index : Int, peers : List<PeerAddress>) : Option<Int> {
    if (peers.isEmpty) { // base case
      None
    } else {
      val localAddresses = NetUtil.getLocalAddresses()
      val peerAddress = peers.head
      if ( localAddresses.contains(peerAddress.address) && peerAddress.port == p2pPort) { // Found a match.
        Some(index)
      } else {
        getPeerIndexInternal(p2pPort, index + 1, peers.tail)
      }
    }
  }

  /**
    * Return the peer index of the peers array in the scalechain.conf file.
    * For eaxmple, in case we have the following list of peers,
    * and the node ip address is 127.0.0.1 with p2p port 7645,
    * the peer index is 2
    *
    *   p2p {
    *     port = 7643
    *     peers = [
    *       { address:"127.0.0.1", port:"7643" }, # index 0
    *       { address:"127.0.0.1", port:"7644" }, # index 1
    *       { address:"127.0.0.1", port:"7645" }, # index 2
    *       { address:"127.0.0.1", port:"7646" }, # index 3
    *       { address:"127.0.0.1", port:"7647" }  # index 4
    *     ]
    *   }
    *
    * @return The peer index from <0, peer count-1)
    */
  fun getPeerIndex(p2pPort : Int) : Option<Int> {
    val peerAddresses : List<PeerAddress> = Config.peerAddresses()
    getPeerIndexInternal(p2pPort, 0, peerAddresses)
  }
}

object BlockGateway : BlockGateway

/**
  * Created by kangmo on 7/18/16.
  */
class BlockGateway {
  private val logger = LoggerFactory.getLogger(BlockGateway::class.java)

  val ConsensualBlockHeaderCache = TimeBasedCache<BlockHeader>(5, TimeUnit.MINUTES)

  val ReceivedBlockCache = TimeBasedCache<Block>(5, TimeUnit.MINUTES)

  fun putConsensualHeader(header : BlockHeader) {
    ConsensualBlockHeaderCache.synchronized {
      ReceivedBlockCache.synchronized {
        val chain = Blockchain.get

        // BUGBUG : A fork can happen if the block comes too late. (The consensual header in ConsensualBlockHeaderCache expires)
        val consensualHeader = ConsensualBlockHeaderCache.get(header.hashPrevBlock)
        if (consensualHeader.isDefined) {
          // We already have a consensual header. We only accept the first consensual block header.
          // The all consensual block headers with the same previous block hash of the first one are discarded.
        } else {
          // New consensual header.
          ConsensualBlockHeaderCache.put(header.hashPrevBlock, header)

          // Check if we have a received block that matches the header.
          val blockHash = header.hash
          val receivedBlock = ReceivedBlockCache.get(blockHash)
          if (receivedBlock.isDefined) {
            val currentBestBlockHash = chain.getBestBlockHash()(chain.db)
            if ( currentBestBlockHash.get == receivedBlock.get.header.hashPrevBlock) {
              //            logger.trace("Received block found while putting consensual header.")
              val acceptedAsNewBestBlock = BlockProcessor.get.acceptBlock(blockHash, receivedBlock.get)
              assert(acceptedAsNewBestBlock == true)
              BlockProcessor.get.acceptChildren(blockHash)
              // Recursively process orphan blocks depending on the newly added block
            } else {
              // put the block as an orphan block
              BlockProcessor.get.putOrphan(receivedBlock.get)
            }
          } else {
//            logger.trace("Received block was NOT found while putting consensual header.")
          }
        }
      }
    }
  }

  fun putReceivedBlock(blockHash : Hash, block : Block) : Unit {
    ConsensualBlockHeaderCache.synchronized {
      ReceivedBlockCache.synchronized {
        // Put into the received block cache.
        if (ReceivedBlockCache.get(blockHash).isEmpty) {
          ReceivedBlockCache.put(blockHash, block)
        }

        val chain = Blockchain.get

        val consensualBlockHeader = ConsensualBlockHeaderCache.get(block.header.hashPrevBlock)
        if ( consensualBlockHeader.isDefined ) {
//            logger.trace("Found a consensual header on the previous block.")
          if (consensualBlockHeader.get == block.header) {
            val currentBestBlockHash = chain.getBestBlockHash()(chain.db)
            if ( currentBestBlockHash.get == block.header.hashPrevBlock) {
              // logger.trace(s"The received block is on top of the best block. Hash : ${blockHash}")
              // logger.trace("The received block matches the consensual header.")
              // Already reached consensus.
              val acceptedAsNewBestBlock = BlockProcessor.get.acceptBlock(blockHash, block)
              assert(acceptedAsNewBestBlock == true)
              // TODO : Would be great if we can remove the ReceivedBlockCache
              // Recursively process orphan blocks depending on the newly added block
              BlockProcessor.get.acceptChildren(blockHash)
            } else {
              // put the block as an orphan block
              BlockProcessor.get.putOrphan(block)
            }
          } else {
//              logger.trace(s"The received block does not match the consensual header. consensual : ${consensualBlockHeader.get}, received : ${block.header}")
          }
        } else {
//            logger.trace("No consensual header was found for the received block.")
        }
      }
    }
  }
}

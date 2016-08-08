package io.scalechain.blockchain.net

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.net.message.GetBlocksFactory
import io.scalechain.blockchain.proto.{Hash, ProtocolMessage}
import io.scalechain.util.Config
import org.slf4j.LoggerFactory

object Node {
  var theNode : Node = null
  def create(peerCommunicator: PeerCommunicator, chain : Blockchain) : Node = {
    if (theNode == null) {
      theNode = new Node(peerCommunicator, chain)
    }
    theNode
  }
  def get() : Node = {
    assert( theNode != null )
    theNode
  }
}

/**
  * Created by kangmo on 8/8/16.
  */
class Node(peerCommunicator: PeerCommunicator, chain : Blockchain) {
  private val logger = Logger( LoggerFactory.getLogger(classOf[Node]) )

  // BUGBUG : What if the best peer was a malicious node?
  // The best peer for initial block download
  protected[net] var bestPeerForIBD : Peer = null

  protected[net] var lastBlockHashForIBD : Hash = Hash.ALL_ZERO

  def setLastBlockHashForIBD(blockHash : Hash) : Unit = {
    lastBlockHashForIBD = blockHash
  }
  def getLastBlockHashForIBD() : Hash = {
    lastBlockHashForIBD
  }

  /**
    * Return the best height if we received version message more than two thirds of total nodes.
    *
    * @return Some(best Peer) that has the highest Version.startHeight if we received version message more than two thirds of total nodes. None otherwise.
    */
  def getBestPeer() : Option[Peer] = {
    val maxPeerCount = Config.peerAddresses.length
    val peerInfos = peerCommunicator.getPeerInfos()

    // We have two way communication channels for each peer.
    // PeerInfos list each communication channel.
    val connectedPeers = peerInfos.length / 2

    // Get the total active peers including me.
    val totalActivePeers = connectedPeers + 1

    // Do we have enough number of peers? (At least more than two thirds)
    if ( totalActivePeers > maxPeerCount * 2 / 3 ) {
      // Did we receive starting height for all peers?
      if ( peerInfos.filter( _.startingheight.isDefined ).length == peerInfos.length ) {
        val bestPeer = peerCommunicator.getBestPeer.get
        Some(bestPeer)
      } else {
        None
      }
    } else { // Not enough connected peers.
      None
    }
  }

  def updateStatus(): Unit = {
    synchronized {
      val bestPeerOption = getBestPeer()
      if (bestPeerOption.isDefined) {
        val bestPeer = bestPeerOption.get
        if (bestPeer.versionOption.map(_.startHeight).getOrElse(0) > chain.getBestBlockHeight() ) {
          bestPeerForIBD = bestPeer

          /* Start IBD(Initial block download). Request getblocks message to get inv messages for our missing blocks */
          val getBlocksMessage = GetBlocksFactory.create(getLastBlockHashForIBD)
          bestPeerForIBD.send(getBlocksMessage)

          logger.info("Initial block download started. Requested getblocks message.")
        }
      }
    }
  }

  def bestPeerStartHeight() : Int = {
    synchronized {
      assert(bestPeerForIBD != null)
      bestPeerForIBD.versionOption.map(_.startHeight).getOrElse(0)
    }
  }

  def sendToBestPeer(message : ProtocolMessage) : Unit = {
    synchronized {
      assert(bestPeerForIBD != null)
      bestPeerForIBD.send(message)
    }
  }

  def isInitialBlockDownload() : Boolean = {
    synchronized {
      bestPeerForIBD != null
    }
  }

  def stopInitialBlockDownload(): Unit = {
    synchronized {
      bestPeerForIBD = null
    }
  }
}

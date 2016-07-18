package io.scalechain.blockchain.net

import java.net.{InetAddress, InetSocketAddress}

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.script.HashSupported._
import io.scalechain.util.{HexUtil, StringUtil}
import org.slf4j.LoggerFactory

/**
  * Created by kangmo on 5/22/16.
  */
class PeerCommunicator(peerSet : PeerSet) {
  private val logger = Logger( LoggerFactory.getLogger(classOf[PeerCommunicator]) )

  /*
    protected[net] def sendToAny(message : ProtocolMessage): Unit = {
    }
    protected[net] def sendTo(remoteAddress : InetSocketAddress, message : ProtocolMessage): Unit = {
    }
  */

  protected[net] def sendToAll(message : ProtocolMessage): Unit = {
    peerSet.sendToAll(message)
  }

  /** Propagate a newly mined block to the peers. Called by a miner, whenever a new block was mined.
    *
    * @param block The newly mined block to propagate.
    */
  def propagateBlock(block : Block) : Unit = {
    // Propagating a block is an urgent job to do. Without broadcasting the inventories, send the block itself to the network.
    sendToAll(block)
  }

  /** Propagate a newly received transaction to the peers.
    *
    * @param transaction The transaction to propagate.
    */
  def propagateTransaction(transaction : Transaction) : Unit = {
    sendToAll(transaction)
  }

  /** Get the list of information on each peer.
    *
    * Used by : getpeerinfo RPC.
    *
    * @return The list of peer information.
    */
  def getPeerInfos() : List[PeerInfo] = {

    var peerIndex = 0;

    val peerInfosIter = for (
      (address, peer) <- peerSet.peers()
    ) yield {
      peerIndex += 1
      PeerInfo.create(peerIndex, address, peer)
    }

    peerInfosIter.toList
  }

  /**
    * Get the peer which has highest best block height.
 *
    * @return Some(best PeerInfo) if there is any connected peer; None otherwise.
    */
  def getBestPeer() : Option[PeerInfo] = {
    val peerInfos = getPeerInfos
    if (peerInfos.isEmpty) {
      None
    } else {
      def betterPeer(peer1 : PeerInfo, peer2 : PeerInfo) : PeerInfo = {
        if (peer1.startingheight.getOrElse(0L) > peer2.startingheight.getOrElse(0L))
          peer1
        else
          peer2
      }
      Some( peerInfos.reduceLeft(betterPeer) )
    }
  }
}

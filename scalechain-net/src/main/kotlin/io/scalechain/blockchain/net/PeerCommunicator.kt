package io.scalechain.blockchain.net

import java.net.InetAddress
import java.net.InetSocketAddress

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.script.hash
import io.scalechain.util.HexUtil
import io.scalechain.util.StringUtil
import org.slf4j.LoggerFactory

/**
  * Created by kangmo on 5/22/16.
  */
class PeerCommunicator(peerSet : PeerSet) {
  private val logger = LoggerFactory.getLogger(PeerCommunicator::class.java)

  /*
    protected<net> fun sendToAny(message : ProtocolMessage): Unit {
    }
    protected<net> fun sendTo(remoteAddress : InetSocketAddress, message : ProtocolMessage): Unit {
    }
  */

  protected<net> fun sendToAll(message : ProtocolMessage): Unit {
    peerSet.sendToAll(message)
  }

  /** Propagate a newly mined block to the peers. Called by a miner, whenever a block was mined.
    *
    * @param block The newly mined block to propagate.
    */
  fun propagateBlock(block : Block) : Unit {
    // Propagating a block is an urgent job to do. Without broadcasting the inventories, send the block itself to the network.
    sendToAll(block)
  }

  /** Propagate a newly received transaction to the peers.
    *
    * @param transaction The transaction to propagate.
    */
  fun propagateTransaction(transaction : Transaction) : Unit {
    sendToAll(transaction)
  }

  /** Get the list of information on each peer.
    *
    * Used by : getpeerinfo RPC.
    *
    * @return The list of peer information.
    */
  fun getPeerInfos() : List<PeerInfo> {

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
    * @return Some(best Peer) if there is any connected peer; None otherwise.
    */
  def getBestPeer() : Option<Peer> = {
    val peers = peerSet.peers().map(_._2)
    if (peers.isEmpty) {
      None
    } else {
      def betterPeer(peer1 : Peer, peer2 : Peer) : Peer = {
        if (peer1.versionOption.map(_.startHeight).getOrElse(0) > peer2.versionOption.map(_.startHeight).getOrElse(0))
          peer1
        else
          peer2
      }
      Some( peers.reduceLeft(betterPeer) )
    }
  }
}

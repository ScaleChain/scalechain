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
class PeerCommunicator(private val peerSet : PeerSet) {
  private val logger = LoggerFactory.getLogger(PeerCommunicator::class.java)

  /*
    protected<net> fun sendToAny(message : ProtocolMessage): Unit {
    }
    protected<net> fun sendTo(remoteAddress : InetSocketAddress, message : ProtocolMessage): Unit {
    }
  */

  protected fun sendToAll(message : ProtocolMessage): Unit {
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

    val peerInfosIter = peerSet.peers().map { pair ->
      val address = pair.first
      val peer = pair.second
      peerIndex += 1
      PeerInfo.create(peerIndex, address, peer)
    }

    return peerInfosIter
  }

  /**
    * Get the peer which has highest best block height.
    *
    * @return Some(best Peer) if there is any connected peer; None otherwise.
    */
  fun getBestPeer() : Peer?  {
    val peers = peerSet.peers().map{ it.second }
    if (peers.isEmpty()) {
      return null
    } else {
      fun betterPeer(peer1 : Peer, peer2 : Peer) : Peer  {
        if ( (peer1.versionOption?.startHeight ?: 0) > (peer2.versionOption?.startHeight ?: 0) )
          return peer1
        else
          return peer2
      }
      return peers.reduce(::betterPeer)
    }
  }

  companion object
}

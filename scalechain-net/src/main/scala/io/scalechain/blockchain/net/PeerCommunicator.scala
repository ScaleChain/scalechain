package io.scalechain.blockchain.net

import java.net.InetSocketAddress

import io.scalechain.blockchain.proto._

/**
  * Created by kangmo on 5/22/16.
  */
class PeerCommunicator(peerSet : PeerSet) {
  /*
    protected[net] def sendToAny(message : ProtocolMessage): Unit = {
    }
    protected[net] def sendTo(remoteAddress : InetSocketAddress, message : ProtocolMessage): Unit = {
    }
  */

  protected[net] def sendToAll(message : ProtocolMessage): Unit = {
    peerSet.peers() foreach { case (address: InetSocketAddress, peer : Peer) =>
      println(s"Sending to one of all peers : ${address}, ${message}")
      peer.send(message)
    }
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
}

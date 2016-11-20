package io.scalechain.blockchain.net.handler

import io.scalechain.blockchain.net.{PeerCommunicator, Peer}

/**
  * The context for handling messages for a peer.
  * In case state transitions are required for handling messages for a peer, the states are kept in the message handler context of it.
  *
  * @param peer The peer that this node is handler is communicating.
  * @param communicator The peer communicator that can communicate with any of peers connected to this node.
  */
class MessageHandlerContext(val peer : Peer, val communicator : PeerCommunicator)

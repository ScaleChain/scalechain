package io.scalechain.blockchain.proto

/**
  * All cases classes related to private blockchain are defined here.
  */

data class BlockConsensus(val header : BlockHeader, val height : Long) : ProtocolMessage
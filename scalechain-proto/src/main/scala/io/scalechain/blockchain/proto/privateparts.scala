package io.scalechain.blockchain.proto

import io.scalechain.util.{HexUtil, ByteArray}

/**
  * All cases classes related to private blockchain are defined here.
  */

case class BlockConsensus(header : BlockHeader, height : Long) extends ProtocolMessage
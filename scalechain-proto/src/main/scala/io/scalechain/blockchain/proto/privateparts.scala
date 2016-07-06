package io.scalechain.blockchain.proto

import io.scalechain.util.{HexUtil, ByteArray}

/**
  * All cases classes related to private blockchain are defined here.
  */

/**
  * (private only) The block signature stored in the block signing transaction.
  * @param magic The magic value.
  * @param blockHash The
  */
case class BlockSignature(magic : ByteArray, blockHash : Hash) extends ProtocolMessage {
  override def toString = s"""PrivateVersion($magic, $blockHash)"""
}

object BlockSignature {
  val MAGIC = ByteArray(HexUtil.bytes("B0A1"))
}

/** (private only) PrivateVersion ; An additional version message exchanged in a private blockchain.
  * @param blockSigningAddress The address that this node owns for signing a block.
  */
case class PrivateVersion( blockSigningAddress : String ) extends ProtocolMessage {
  override def toString = s"""PrivateVersion($blockSigningAddress)"""
}
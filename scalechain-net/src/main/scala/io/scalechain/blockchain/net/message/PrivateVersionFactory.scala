package io.scalechain.blockchain.net.message

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.net.BlockSigner
import io.scalechain.blockchain.proto.{PrivateVersion, IPv6Address, NetworkAddress, Version}
import io.scalechain.blockchain.transaction.CoinAddress
import io.scalechain.util.HexUtil._

/**
  * Created by kangmo on 7/6/16.
  */
object PrivateVersionFactory {
  protected[net] var blockSigningAddress : Option[CoinAddress] = None
  def setBlockSigningAddress(address : CoinAddress) : Unit = {
    blockSigningAddress = Some(address)
  }
  def create() : PrivateVersion = {
    assert(blockSigningAddress.isDefined)
    PrivateVersion(new BlockSigner()(Blockchain.get.db).signingAddress.base58())
  }
}


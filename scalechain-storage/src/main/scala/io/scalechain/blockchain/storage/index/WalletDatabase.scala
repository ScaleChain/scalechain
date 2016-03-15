package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.{WalletInfo, Hash}
import io.scalechain.blockchain.proto.codec.{BlockInfoCodec, WalletInfoCodec, HashCodec}

/**
  * Created by mijeong on 2016. 3. 15..
  *
  * TODO : Implement full WalletDatabase
  *
  * WalletDatabase Prototype
  */
class WalletDatabase(db : KeyValueDatabase) {

  def getWalletInfo(hash : Hash) : Option[WalletInfo] = {
    db.getObject(HashCodec.serialize(hash))(WalletInfoCodec)
  }

  def putWalletInfo(hash : Hash, info : WalletInfo) : Unit = {
    val walletInfoOption = getWalletInfo(hash)
    if (walletInfoOption.isDefined) {
      val currentWalletInfo = walletInfoOption.get

      // hit an assertion : change any field on the block header
      assert(currentWalletInfo.walletHeader == info.walletHeader)
    }

    db.putObject(HashCodec.serialize(hash), info)(WalletInfoCodec)
  }

  def close() = db.close()
}

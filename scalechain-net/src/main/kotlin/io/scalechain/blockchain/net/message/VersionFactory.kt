package io.scalechain.blockchain.net.message

import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.util.HexUtil.*

/**
  * Created by kangmo on 6/27/16.
  */
object VersionFactory {
  fun create() : Version {
    implicit val db : KeyValueDatabase = Blockchain.get.db

    // ScaleChain uses Long type for the block height, but the Version.startHeight is encoded in 32bit little endian integer.
    // If we create two blocks a second, it takes about 15 years to fill up
    val bestBlockHeight = Blockchain.get.getBestBlockHeight()
    assert(bestBlockHeight <= Int.MaxValue)

    Version(70002, BigInt("1"), 1454059080L, NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 0), NetworkAddress(BigInt("1"), IPv6Address(bytes("00000000000000000000ffff00000000")), 8333), BigInt("5306546289391447548"), "/Satoshi:0.11.2/", bestBlockHeight.toInt, true)
  }
}

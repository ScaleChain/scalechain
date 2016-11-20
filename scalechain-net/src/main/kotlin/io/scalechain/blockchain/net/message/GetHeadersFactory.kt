package io.scalechain.blockchain.net.message

import io.scalechain.blockchain.proto.{Hash, GetHeaders, GetData, InvVector}
import io.scalechain.blockchain.transaction.ChainEnvironment

/**
  *  The factory that creates GetHeaders messages.
  */
object GetHeadersFactory {
  fun create(blockLocatorHashes:List<Hash>, hashStop : Hash = Hash.ALL_ZERO) : GetHeaders {
    val env = ChainEnvironment.get

    GetHeaders(env.DefaultBlockVersion, blockLocatorHashes, hashStop)
  }
}

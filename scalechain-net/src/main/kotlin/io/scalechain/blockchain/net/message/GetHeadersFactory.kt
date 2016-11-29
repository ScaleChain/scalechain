package io.scalechain.blockchain.net.message

import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.proto.GetHeaders
import io.scalechain.blockchain.proto.GetData
import io.scalechain.blockchain.proto.InvVector
import io.scalechain.blockchain.transaction.ChainEnvironment

/**
  *  The factory that creates GetHeaders messages.
  */
object GetHeadersFactory {
  fun create(blockLocatorHashes:List<Hash>, hashStop : Hash = Hash.ALL_ZERO) : GetHeaders {
    val env = ChainEnvironment.get()

    return GetHeaders(env.DefaultBlockVersion.toLong(), blockLocatorHashes, hashStop)
  }
}

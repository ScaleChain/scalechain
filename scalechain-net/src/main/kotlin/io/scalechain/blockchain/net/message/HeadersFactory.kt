package io.scalechain.blockchain.net.message

import io.scalechain.blockchain.proto.BlockHeader
import io.scalechain.blockchain.proto.Headers
import io.scalechain.blockchain.proto.Inv
import io.scalechain.blockchain.proto.Hash

/**
  *  The factory that creates Headers messages.
  */
object HeadersFactory {
  fun create(blockHeaders : List<BlockHeader>) : Headers {
    return Headers(blockHeaders)
  }
}

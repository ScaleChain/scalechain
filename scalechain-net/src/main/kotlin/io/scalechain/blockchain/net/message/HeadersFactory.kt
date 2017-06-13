package io.scalechain.blockchain.net.message

import io.scalechain.blockchain.proto.BlockHeader
import io.scalechain.blockchain.proto.Headers

/**
  *  The factory that creates Headers messages.
  */
object HeadersFactory {
  fun create(blockHeaders : List<BlockHeader>) : Headers {
    return Headers(blockHeaders)
  }
}

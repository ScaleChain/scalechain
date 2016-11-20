package io.scalechain.blockchain.proto.codec.indexparts

import io.scalechain.blockchain.proto.TransactionCount
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._

class TransactionCountCodecSpec : PayloadTestSuite<TransactionCount>  {

  val codec = TransactionCountCodec.codec

  val payload = bytes(
    """
       01
    """)

  val message = TransactionCount(1)
}

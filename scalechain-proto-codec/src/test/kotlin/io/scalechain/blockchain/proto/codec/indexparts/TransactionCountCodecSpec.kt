package io.scalechain.blockchain.proto.codec.indexparts

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.TransactionCount
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TransactionCountCodecSpec : PayloadTestSuite<TransactionCount>()  {

  override val codec = TransactionCountCodec

  override val payload = bytes(
    """
       01
    """)

  override val message = TransactionCount(1)
}

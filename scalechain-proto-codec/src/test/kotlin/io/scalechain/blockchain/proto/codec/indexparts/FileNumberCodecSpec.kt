package io.scalechain.blockchain.proto.codec.indexparts

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.FileNumber

import io.scalechain.blockchain.proto.codec.FileNumberCodec
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class FileNumberCodecSpec : PayloadTestSuite<FileNumber>()  {

  override val codec = FileNumberCodec

  override val payload = bytes(
    """
      01 00 00 00
    """)

  override val message = FileNumber( 1 )
}

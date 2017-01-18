package io.scalechain.blockchain.proto.codec.envelope

import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.FileNumber
import io.scalechain.blockchain.proto.Magic

import io.scalechain.blockchain.proto.codec.FileNumberCodec
import io.scalechain.blockchain.proto.codec.MagicCodec
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class MagicCodecSpec : PayloadTestSuite<Magic>()  {

  override val codec = MagicCodec

  override val payload = bytes(
    """
      f9 be b4 d9
    """)

  override val message = Magic.MAIN
}

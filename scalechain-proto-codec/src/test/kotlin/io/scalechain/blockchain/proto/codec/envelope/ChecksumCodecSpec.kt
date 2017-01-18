package io.scalechain.blockchain.proto.codec.envelope

/**
 * Created by kangmo on 16/01/2017.
 */
import io.kotlintest.KTestJUnitRunner
import io.scalechain.blockchain.proto.Checksum
import io.scalechain.blockchain.proto.FileNumber
import io.scalechain.blockchain.proto.codec.ChecksumCodec

import io.scalechain.blockchain.proto.codec.FileNumberCodec
import io.scalechain.blockchain.proto.codec.PayloadTestSuite
import io.scalechain.util.HexUtil.bytes
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class ChecksumCodecSpec : PayloadTestSuite<Checksum>()  {

  override val codec = ChecksumCodec

  override val payload = bytes(
    """
      00 01 02 03
    """)

  override val message = Checksum.fromHex("00010203")
}
